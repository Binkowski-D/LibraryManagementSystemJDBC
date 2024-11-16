package util;

import org.h2.jdbcx.JdbcDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TestDatabaseHelper {

    // Method to get a test database connection
    public static Connection getTestConnection() throws SQLException {

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("user");
        dataSource.setPassword("password");

        return dataSource.getConnection();
    }

    // Method to create the books table in the test database
    public static void createBooksTable(Connection connection) throws SQLException {
        try(PreparedStatement createBooksTable = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS books (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    author VARCHAR(255) NOT NULL,
                    year_of_publication INT NOT NULL,
                    quantity INT NOT NULL,
                    shelf_location_id INT NOT NULL,
                    FOREIGN KEY (shelf_location_id) REFERENCES book_shelf_location(id)
                )
                """)){

            createBooksTable.executeUpdate();
        }
    }

    // Method to create the book_shelf_location table in the test database
    public static void createShelfLocationTable(Connection connection) throws SQLException {
        try (PreparedStatement createShelfLocationTable = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS book_shelf_location (
                    id SERIAL PRIMARY KEY,
                    section VARCHAR(255) NOT NULL,
                    shelf INT NOT NULL
                )
                """)) {

            createShelfLocationTable.executeUpdate();
        }
    }

    // Method to create the readers table in the test database
    public static void createReadersTable(Connection connection) throws SQLException {
        try (PreparedStatement createReadersTable = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS readers (
                    id SERIAL PRIMARY KEY,
                    first_name VARCHAR(255) NOT NULL,
                    last_name VARCHAR(255) NOT NULL,
                    date_of_birth DATE NOT NULL
                )
                """)) {

            createReadersTable.executeUpdate();
        }
    }

    // Method to create the borrowed books table in the test database
    public static void createBorrowedBooksTable(Connection connection) throws SQLException {
        try(PreparedStatement createBorrowedBooksTable = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS borrowed_books (
                    id SERIAL PRIMARY KEY,
                    reader_id INT NOT NULL,
                    book_id INT NOT NULL,
                    borrow_date DATE NOT NULL,
                    return_due_date DATE NOT NULL,
                    FOREIGN KEY(reader_id) REFERENCES readers(id),
                    FOREIGN KEY(book_id) REFERENCES books(id)
                    )
                """)) {
            createBorrowedBooksTable.executeUpdate();
        }
    }

    // Method to drop a table from the test database
    public static void dropTable(Connection connection, String tableName) throws SQLException {
        // Adding basic validation for the table name
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }

        try (PreparedStatement dropTable = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName)) {
            dropTable.executeUpdate();
        }
    }
}
