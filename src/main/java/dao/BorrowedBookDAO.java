package dao;

import exception.DatabaseOperationException;
import model.Book;
import model.BorrowedBook;
import model.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BorrowedBookDAO {
    private final Connection connection;

    public BorrowedBookDAO(Connection connection){
        this.connection = connection;
    }

    // Adds a new Borrowed Book or returns the ID if it was added. If the book is already borrowed, returns an empty Optional.
    public Optional<Integer> addBorrowedBook(Reader reader, Book book) throws DatabaseOperationException {
        Optional<Integer> borrowedBookId = findBorrowedBookIdByReaderAndBook(reader,book);

        if(borrowedBookId.isPresent()){
            return Optional.empty(); // Borrowed book already exists
        }

        String insertNewBorrowedBookSql = "INSERT INTO borrowed_books (reader_id, book_id, borrow_date, return_due_date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertNewBorrowedBookSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStatement.setInt(1, reader.getId());
            insertStatement.setInt(2, book.getId());
            insertStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            insertStatement.setDate(4, java.sql.Date.valueOf(LocalDate.now().plusDays(BorrowedBook.BORROW_PERIOD_DAYS)));

            int result = insertStatement.executeUpdate();
            if (result > 0) {
                try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return Optional.of(generatedKeys.getInt(1)); // Return the generated ID
                    }
                }
            }
            throw new SQLException("Failed to insert new borrowed book, no ID obtained.");

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    // Method to check if a book is currently borrowed
    public boolean isBookBorrowed(Book book) throws DatabaseOperationException {
        String query = "SELECT COUNT(*) FROM borrowed_books WHERE book_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, book.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // If count is greater than 0, book is borrowed
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed while checking if book is borrowed", e);
        }
    }


    // Method to find the borrowed book ID by reader and book details
    public Optional<Integer> findBorrowedBookIdByReaderAndBook(Reader reader, Book book) throws DatabaseOperationException {
        String query = "SELECT bb.id FROM borrowed_books bb "+
                "JOIN readers r ON bb.reader_id = r.id "+
                "JOIN books b ON bb.book_id = b.id "+
                "WHERE LOWER(r.first_name) = LOWER(?) AND LOWER(r.last_name) = LOWER(?) AND r.date_of_birth = ? "+
                "AND LOWER(b.title) = LOWER(?) AND LOWER(b.author) = LOWER(?) AND b.year_of_publication = ? ";

        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, reader.getFirstName());
            statement.setString(2, reader.getLastName());
            statement.setDate(3, java.sql.Date.valueOf(reader.getDateOfBirth()));
            statement.setString(4, book.getTitle());
            statement.setString(5, book.getAuthor());
            statement.setInt(6, book.getYearOfPublication());

            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(resultSet.getInt("id"));
                }else{
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    // Method to fetch all books borrowed by a specific reader with borrow and return dates
    public List<String> getBooksBorrowedByReaderWithDates(Reader reader) throws DatabaseOperationException {
        String query = "SELECT b.title, b.author, b.year_of_publication, bb.borrow_date, bb.return_due_date "+
                "FROM borrowed_books bb "+
                "JOIN books b ON bb.book_id = b.id "+
                "JOIN readers r ON bb.reader_id = r.id "+
                "WHERE LOWER(r.first_name) = LOWER(?) AND LOWER(r.last_name) = LOWER(?) AND r.date_of_birth = ?";

        List<String> borrowedBooksInfo = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, reader.getFirstName());
            statement.setString(2, reader.getLastName());
            statement.setDate(3, java.sql.Date.valueOf(reader.getDateOfBirth()));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String bookInfo = String.format(
                            "Title: %s, Author: %s, Year of publication: %d, Date of hire: %s, Date of return: %s",
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getInt("year_of_publication"),
                            resultSet.getDate("borrow_date").toLocalDate(),
                            resultSet.getDate("return_due_date").toLocalDate()
                    );
                    borrowedBooksInfo.add(bookInfo);
                }

                return borrowedBooksInfo;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Method to fetch all readers who have overdue loans
    public List<String> getOverdueReaders() throws DatabaseOperationException {
        String query = "SELECT r.first_name, r.last_name, r.date_of_birth, bb.borrow_date, bb.return_due_date " +
                "FROM borrowed_books bb " +
                "JOIN readers r ON bb.reader_id = r.id " +
                "WHERE bb.return_due_date < CURRENT_DATE";

        List<String> overdueReaders = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String readerInfo = String.format(
                            "First name: %s, Last name: %s, Date of birth: %s, Date of hire: %s, Date of return: %s",
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name"),
                            resultSet.getDate("date_of_birth").toLocalDate(),
                            resultSet.getDate("borrow_date").toLocalDate(),
                            resultSet.getDate("return_due_date").toLocalDate()
                    );
                    overdueReaders.add(readerInfo);
                }
            }
            return overdueReaders;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Method to check if a specific reader has overdue loans
    public boolean hasOverdueLoans(Reader reader) throws DatabaseOperationException {
        String query = "SELECT bb.id FROM borrowed_books bb " +
                "JOIN readers r ON bb.reader_id = r.id " +
                "WHERE LOWER(r.first_name) = LOWER(?) AND LOWER(r.last_name) = LOWER(?) AND r.date_of_birth = ? " +
                "AND bb.return_due_date < CURRENT_DATE";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, reader.getFirstName());
            statement.setString(2, reader.getLastName());
            statement.setDate(3, java.sql.Date.valueOf(reader.getDateOfBirth()));

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Return true if the reader has any overdue loans
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed.", e);
        }
    }

    // Method to remove a borrowed book
    public boolean removeBorrowedBook(Reader reader, Book book) throws DatabaseOperationException {
        Optional<Integer> borrowedBookId = findBorrowedBookIdByReaderAndBook(reader, book);
        if (borrowedBookId.isEmpty()) {
            return false; // Borrowed book does not exist
        }

        String query = "DELETE FROM borrowed_books WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, borrowedBookId.get());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }
}
