package configTest;

import config.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class DatabaseConnectionTest {

    // Test for verifying that the database connection is successfully established
    @Test
    public void testConnection() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Assert that the connection is not null
            assertNotNull(connection, "Connection should not be null");
            System.out.println("Connection to the database was successful!");
        } catch (SQLException | IOException e) {
            // Fail the test if an exception occurs while establishing the connection
            fail("An error occurred while trying to connect to the database: " + e.getMessage());
        }
    }

}

