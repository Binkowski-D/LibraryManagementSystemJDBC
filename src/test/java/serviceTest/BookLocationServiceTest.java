package serviceTest;

import exception.DatabaseOperationException;
import exception.InvalidDataException;
import model.BookLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.BookLocationService;
import service.BookService;
import util.TestDatabaseHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookLocationServiceTest {
    private Connection connection;
    private BookLocationService bookLocationService;
    private BookService bookService;

    @BeforeAll
    public static void setupDatabase() throws SQLException {
        try (Connection conn = TestDatabaseHelper.getTestConnection()) {
            TestDatabaseHelper.createShelfLocationTable(conn);
            TestDatabaseHelper.createBooksTable(conn);
        }
    }

    @BeforeEach
    public void setup() throws SQLException {
        connection = TestDatabaseHelper.getTestConnection();
        clearDatabase();

        bookLocationService = new BookLocationService(connection);
        bookService = new BookService(connection);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void clearDatabase() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            stmt.executeUpdate("DELETE FROM books");
            stmt.executeUpdate("DELETE FROM book_shelf_location");

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    // Test for adding a new book location
    @Test
    public void testAddLocation() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = new BookLocation("A", 1);
        Optional<Integer> locationId = bookLocationService.addLocation(location);

        assertTrue(locationId.isPresent(), "Book location should be added successfully.");
    }

    // Test for adding a book location that already exists
    @Test
    public void testAddDuplicateLocation() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = new BookLocation("A", 1);
        bookLocationService.addLocation(location);

        Optional<Integer> duplicateLocationId = bookLocationService.addLocation(location);
        assertTrue(duplicateLocationId.isEmpty(), "Duplicate location should not be added.");
    }

    // Test for fetching all book locations
    @Test
    public void testGetAllBookLocations() throws DatabaseOperationException, InvalidDataException {
        BookLocation location1 = new BookLocation("A", 1);
        BookLocation location2 = new BookLocation("B", 2);

        bookLocationService.addLocation(location1);
        bookLocationService.addLocation(location2);

        List<BookLocation> allLocations = bookLocationService.getAllBookLocations();
        assertEquals(2, allLocations.size(), "There should be 2 book locations in the database.");
    }

    // Test for removing a book location
    @Test
    public void testRemoveBookLocation() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = new BookLocation("A", 1);
        bookLocationService.addLocation(location);

        boolean result = bookLocationService.removeBookLocation("A", 1);
        assertTrue(result, "Book location should be removed successfully.");
    }

    // Test for attempting to remove a non-existing book location
    @Test
    public void testRemoveNonExistingBookLocation() throws DatabaseOperationException, InvalidDataException {
        boolean result = bookLocationService.removeBookLocation("Non-Existent", 1);
        assertFalse(result, "Removing a non-existing book location should return false.");
    }

    // Test for attempting to remove a book location that has a book assigned
    @Test
    public void testRemoveBookLocationWithBookAssigned() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = new BookLocation("A", 1);
        bookLocationService.addLocation(location);

        bookService.addBook("Effective Java", "Joshua Bloch", 2008, 5, location);

        // Since the location has a book, it should not be removable
        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> bookLocationService.removeBookLocation("A", 1),
                "Expected a DatabaseOperationException when trying to remove a location with a book.");

        assertTrue(exception.getMessage().contains("cannot remove this location"),
                "Exception message should indicate that the location cannot be removed due to an assigned book.");
    }
}

