package serviceTest;

import exception.DatabaseOperationException;
import exception.InvalidDataException;
import model.Book;
import model.BookLocation;
import org.junit.jupiter.api.AfterEach;
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

    @BeforeEach
    public void setup() throws SQLException {
        connection = TestDatabaseHelper.getTestConnection();
        TestDatabaseHelper.createShelfLocationTable(connection);
        TestDatabaseHelper.createBooksTable(connection);

        bookLocationService = new BookLocationService(connection);
        bookService = new BookService(connection);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        TestDatabaseHelper.dropTable(connection, "books");
        TestDatabaseHelper.dropTable(connection, "book_shelf_location");

        if (connection != null && !connection.isClosed()) {
            connection.close();
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

    // Test for checking if a book location exists
    @Test
    public void testDoesBookLocationExist() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = new BookLocation("A", 1);
        bookLocationService.addLocation(location);

        Optional<Integer> locationId = bookLocationService.doesBookLocationExist("A", 1);
        assertTrue(locationId.isPresent(), "Book location should exist in the database.");
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

    // Test for fetching a book location by its shelf location ID
    @Test
    public void testGetLocationByBookShelfLocationID() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = new BookLocation("A", 1);
        Optional<Integer> locationId = bookLocationService.addLocation(location);

        assertTrue(locationId.isPresent(), "Location ID should be generated.");

        BookLocation locationWithId = new BookLocation(locationId.get(), location.getSection(), location.getShelf());
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, locationWithId);

        Optional<BookLocation> fetchedLocation = bookLocationService.getLocationByBookShelfLocationID(book);
        assertTrue(fetchedLocation.isPresent(), "Book location should be found.");
        assertEquals("A", fetchedLocation.get().getSection(), "Section should be 'A'.");
        assertEquals(1, fetchedLocation.get().getShelf(), "Shelf should be 1.");
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

