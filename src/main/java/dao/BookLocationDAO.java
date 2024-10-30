package dao;

import exception.DatabaseOperationException;
import model.BookLocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookLocationDAO {
    private final Connection connection;

    public BookLocationDAO(Connection connection) {
        this.connection = connection;
    }

    // Adds a new book location or returns an Optional with the ID of the newly created location
    public Optional<Integer> addLocation(BookLocation bookLocation) throws DatabaseOperationException {
        Optional<Integer> locationId = doesBookLocationExist(bookLocation.getSection(), bookLocation.getShelf());
        if (locationId.isPresent()) {
            return Optional.empty(); // Location already exists, return empty Optional
        }
        String insertNewLocationSql = "INSERT INTO book_shelf_location (section, shelf) VALUES(?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertNewLocationSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, bookLocation.getSection());
            insertStatement.setInt(2, bookLocation.getShelf());

            int result = insertStatement.executeUpdate();
            if (result > 0) {
                try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return Optional.of(generatedKeys.getInt(1)); // Return the generated ID
                    }
                }
            }
            throw new SQLException("Failed to insert new location, no ID obtained.");

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    // Method to check if a book location exists in the database based on BookLocation object
    public Optional<Integer> doesBookLocationExist(String section, int shelf) throws DatabaseOperationException {
        String query = "SELECT id FROM book_shelf_location WHERE section = ? AND shelf = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, section);
            statement.setInt(2, shelf);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getInt("id"));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    // Fetches all book locations from the database
    public List<BookLocation> getAllBookLocations() throws DatabaseOperationException{
        String query = "SELECT id, section, shelf FROM book_shelf_location ORDER BY section, shelf";
        List<BookLocation> allBooks = new ArrayList<>();

        try(PreparedStatement statement = connection.prepareStatement(query)){
            try(ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()){
                    BookLocation bookLocation = new BookLocation(
                            resultSet.getInt("id"),
                            resultSet.getString("section"),
                            resultSet.getInt("shelf")
                    );
                    allBooks.add(bookLocation);
                }

                return allBooks;
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    // Method to check if there are any books stored in the specified location
    public boolean isAnyBookInLocation(String section, int shelf) throws DatabaseOperationException {
        String query = "SELECT COUNT(*) FROM books WHERE shelf_location_id = (SELECT id FROM book_shelf_location WHERE section = ? AND shelf = ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, section);
            statement.setInt(2, shelf);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // If count is greater than 0, books are present
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed while checking if any book is in the location", e);
        }
    }


    // Method to remove a book location by its BookLocation object
    public boolean removeBookLocation(String section, int shelf) throws DatabaseOperationException {
        String query = "DELETE FROM book_shelf_location WHERE section = ? AND shelf = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, section);
            statement.setInt(2, shelf);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }
}
