package dao;

import exception.DatabaseOperationException;
import model.Book;
import model.BookLocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO {
    private final Connection connection;

    public BookDAO(Connection connection) {
        this.connection = connection;
    }

    // Adds a new book or returns the ID of the newly created book. If the book already exists, returns an empty Optional.
    public Optional<Integer> addBook(Book book) throws DatabaseOperationException {
        String insertNewBookSql = "INSERT INTO books (title, author, year_of_publication, quantity, shelf_location_id) VALUES (?, ?, ?, ?, ?)";

        try {
            Optional<Integer> bookId = findBookIdByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());

            if (bookId.isPresent()) {
                return Optional.empty(); // The book already exists
            } else {
                // Add new book and return the generated ID
                try (PreparedStatement insertStatement = connection.prepareStatement(insertNewBookSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    insertStatement.setString(1, book.getTitle());
                    insertStatement.setString(2, book.getAuthor());
                    insertStatement.setInt(3, book.getYearOfPublication());
                    insertStatement.setInt(4, book.getQuantity());
                    insertStatement.setInt(5, book.getLocation().getId());

                    int result = insertStatement.executeUpdate();
                    if (result > 0) {
                        try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                return Optional.of(generatedKeys.getInt(1)); // Return the generated ID
                            }
                        }
                    }
                    throw new SQLException("Failed to insert new book, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Method to increase the quantity of a book in the database by a specified amount
    public boolean increaseBookQuantity(Book book, int quantityToAdd) throws DatabaseOperationException {
        String query = "UPDATE books SET quantity = quantity + ? WHERE id = ?";

        try {
            Optional<Integer> bookId = findBookIdByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
            if (bookId.isEmpty()) {
                return false; // Book does not exist
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, quantityToAdd);
                statement.setInt(2, bookId.get());

                return statement.executeUpdate() > 0; // Return true if the update was successful
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Method to decrease the quantity of a book in the database by a specified amount
    public boolean decreaseBookQuantity(Book book, int quantityToReduce) throws DatabaseOperationException {
        String query = "UPDATE books SET quantity = quantity - ? WHERE id = ?";

        try {
            Optional<Integer> bookId = findBookIdByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
            if (bookId.isEmpty()) {
                return false; // Book does not exist
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, quantityToReduce);
                statement.setInt(2, bookId.get());

                return statement.executeUpdate() > 0; // Return true if the update was successful
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Fetches all books from the database along with their location data
    public List<Book> getAllBooks() throws DatabaseOperationException {
        String query = "SELECT b.id, b.title, b.author, b.year_of_publication, b.quantity, b.shelf_location_id,  l.section, l.shelf " +
                "FROM books b " +
                "JOIN book_shelf_location l ON b.shelf_location_id = l.id " +
                "ORDER BY b.title, b.author";
        List<Book> allBooks = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                BookLocation location = new BookLocation(resultSet.getInt("shelf_location_id"), resultSet.getString("section"), resultSet.getInt("shelf"));
                Book book = new Book(resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getInt("year_of_publication"),
                        resultSet.getInt("quantity"),
                        location);

                allBooks.add(book);
            }

            return allBooks; // Returns all books with their locations

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Fetches books by title from the database along with location data
    public List<Book> getBooksByTitle(String title) throws DatabaseOperationException {
        String query = "SELECT b.id, b.title, b.author, b.year_of_publication, b.quantity, b.shelf_location_id,  l.section, l.shelf " +
                "FROM books b " +
                "JOIN book_shelf_location l ON b.shelf_location_id = l.id " +
                "WHERE LOWER(b.title) = LOWER(?) " +
                "ORDER BY b.title, b.author";
        List<Book> allBooks = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    BookLocation location = new BookLocation(resultSet.getInt("shelf_location_id"), resultSet.getString("section"), resultSet.getInt("shelf"));
                    Book book = new Book(resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getInt("year_of_publication"),
                            resultSet.getInt("quantity"),
                            location);

                    allBooks.add(book);
                }
            }

            return allBooks; // Returns books with locations if found, or an empty list otherwise

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Fetches books by author from the database along with location data
    public List<Book> getBooksByAuthor(String author) throws DatabaseOperationException {
        String query = "SELECT b.id, b.title, b.author, b.year_of_publication, b.quantity, b.shelf_location_id, l.section, l.shelf " +
                "FROM books b " +
                "JOIN book_shelf_location l ON b.shelf_location_id = l.id " +
                "WHERE LOWER(b.author) = LOWER(?) " +
                "ORDER BY b.title, b.author";
        List<Book> allBooks = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, author);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    BookLocation location = new BookLocation(resultSet.getInt("shelf_location_id"), resultSet.getString("section"), resultSet.getInt("shelf"));
                    Book book = new Book(resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getInt("year_of_publication"),
                            resultSet.getInt("quantity"),
                            location);

                    allBooks.add(book);
                }
            }

            return allBooks; // Returns books with locations if found, or an empty list otherwise

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Fetches book by author, title, and year of publication along with location data
    public Optional<Book> findBookByDetails(String title, String author, int yearOfPublication) throws DatabaseOperationException {
        String query = "SELECT b.id, b.title, b.author, b.year_of_publication, b.quantity, b.shelf_location_id, l.section, l.shelf " +
                "FROM books b " +
                "JOIN book_shelf_location l ON b.shelf_location_id = l.id " +
                "WHERE LOWER(b.title) = LOWER(?) AND LOWER(b.author) = LOWER(?) AND b.year_of_publication = ? " +
                "ORDER BY b.title, b.author";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, yearOfPublication);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BookLocation location = new BookLocation(resultSet.getInt("shelf_location_id"), resultSet.getString("section"), resultSet.getInt("shelf"));
                    Book book = new Book(resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getInt("year_of_publication"),
                            resultSet.getInt("quantity"),
                            location);
                    return Optional.of(book);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Helper method to check if a book exists and return its ID
    private Optional<Integer> findBookIdByDetails(String title, String author, int yearOfPublication) throws SQLException {
        String query = "SELECT id FROM books WHERE LOWER(title) = LOWER(?) AND LOWER(author) = LOWER(?) AND year_of_publication = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, yearOfPublication);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getInt("id"));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    // Helper method to remove a book by its ID
    private boolean removeBookById(int bookId) throws DatabaseOperationException {
        String query = "DELETE FROM books WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bookId);

            return statement.executeUpdate() > 0; // Returns true if the book was successfully removed
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Removes a book by its details
    public boolean removeBookByDetails(String title, String author, int year) throws DatabaseOperationException, SQLException {
        Optional<Integer> bookId = findBookIdByDetails(title, author, year);
        if (bookId.isPresent()) {
            return removeBookById(bookId.get());
        } else {
            return false;
        }
    }

}
