package dao;

import exception.DatabaseOperationException;
import model.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReaderDAO {
    private final Connection connection;

    public ReaderDAO(Connection connection) {
        this.connection = connection;
    }

    // Adds a new reader or returns the ID if the reader already exists
    public Optional<Integer> addReader(Reader reader) throws DatabaseOperationException {
        Optional<Integer> readerId = doesReaderExist(reader);
        if (readerId.isPresent()) {
            return Optional.empty(); // Reader already exists, return empty Optional
        }

        String insertNewReaderSql = "INSERT INTO readers (first_name, last_name, date_of_birth) VALUES (?, ?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertNewReaderSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, reader.getFirstName());
            insertStatement.setString(2, reader.getLastName());
            insertStatement.setDate(3, java.sql.Date.valueOf(reader.getDateOfBirth()));

            int result = insertStatement.executeUpdate();
            if(result > 0){
                try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return Optional.of(generatedKeys.getInt(1)); // Return the generated ID
                    }
                }
            }
            throw new SQLException("Failed to insert new reader, no ID obtained.");

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    // Method to check if a reader exists in the database based on first name, last name, and date of birth
    public Optional<Integer> doesReaderExist(Reader reader) throws DatabaseOperationException {
        String query = "SELECT id FROM readers WHERE first_name = ? AND last_name = ? AND date_of_birth = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, reader.getFirstName());
            statement.setString(2, reader.getLastName());
            statement.setDate(3, java.sql.Date.valueOf(reader.getDateOfBirth()));

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

    // Fetches all readers from the database
    public List<Reader> getAllReaders() throws DatabaseOperationException {
        String query = "SELECT id, first_name, last_name, date_of_birth FROM readers ORDER BY last_name, first_name";
        List<Reader> allReaders = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Reader reader = new Reader(resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getDate("date_of_birth").toLocalDate());
                allReaders.add(reader);
            }
            return allReaders;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    // Fetches reader by last name
    public List<Reader> getReadersByLastName(String lastName) throws DatabaseOperationException {
        String query = "SELECT id, first_name, last_name, date_of_birth FROM readers WHERE last_name = ? ORDER BY last_name, first_name";
        List<Reader> allReaders = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, lastName);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Reader reader = new Reader(resultSet.getInt("id"),
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name"),
                            resultSet.getDate("date_of_birth").toLocalDate());
                    allReaders.add(reader);
                }
                return allReaders;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    // Method to remove a reader by its ID
    public boolean removeReaderById(Reader reader) throws DatabaseOperationException {
        Optional<Integer> readerId = doesReaderExist(reader);
        if (readerId.isEmpty()) {
            return false; // Reader does not exist
        }

        String query = "DELETE FROM readers WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, readerId.get());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }
}
