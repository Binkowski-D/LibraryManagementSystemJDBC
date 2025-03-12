package configTest;

import org.junit.jupiter.api.Test;
import util.TestDatabaseHelper;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {


    @Test
    public void testConnection() {
        assertDoesNotThrow(() -> {
        try (Connection connection = TestDatabaseHelper.getTestConnection()) {
            assertNotNull(connection, "Connection should not be null");
        }

        }, "Exception should not be thrown when establishing a database connection");
    }
}

