package daoTest;

import dao.BookDAO;
import dao.BookLocationDAO;
import exception.DatabaseOperationException;
import model.Book;
import model.BookLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TestDatabaseHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookLocationDAOTest {

    private BookLocationDAO bookLocationDAO;
    private BookDAO bookDAO;
    private Connection connection;

    @BeforeAll
    public static void setupDatabase() throws SQLException {
        try (Connection conn = TestDatabaseHelper.getTestConnection()) {
            TestDatabaseHelper.createShelfLocationTable(conn);
            TestDatabaseHelper.createBooksTable(conn);
        }
    }

    // Setting up the database connection before each test
    @BeforeEach
    public void setup() throws SQLException {
        connection = TestDatabaseHelper.getTestConnection(); // Connect to the H2 test database
        clearDatabase();

        bookLocationDAO = new BookLocationDAO(connection); // Initialize the DAO class
        bookDAO = new BookDAO(connection);
    }

    // Cleaning up after each test by dropping the table and closing the connection
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
    public void testAddLocation() throws DatabaseOperationException {

        BookLocation bookLocation = new BookLocation("A", 1);
        Optional<Integer> result = bookLocationDAO.addLocation(bookLocation);

        assertTrue(result.isPresent(), "Book location should be added successfully when data is valid.");

    }

    // Test for adding a duplicate book location
    @Test
    public void testAddDuplicateBookLocationReturnsFalse() throws DatabaseOperationException{
        BookLocation bookLocation = new BookLocation("A", 1);
        bookLocationDAO.addLocation(bookLocation);

        BookLocation bookLocation2 = new BookLocation("A", 1); // Create a duplicate book location
        Optional<Integer> result = bookLocationDAO.addLocation(bookLocation2); // Try adding the duplicate book location

        assertTrue(result.isEmpty(), "Adding a duplicate book location should return an empty Optional.");
    }

    // Test for checking if a book location already exists
    @Test
    public void testDoesBookLocationExist() throws DatabaseOperationException{
        BookLocation bookLocation = new BookLocation("A", 1);

        Optional<Integer> result = bookLocationDAO.doesBookLocationExist(bookLocation.getSection(), bookLocation.getShelf());
        assertTrue(result.isEmpty(), "This book location should not exist in the database.");

        bookLocationDAO.addLocation(bookLocation);

        result = bookLocationDAO.doesBookLocationExist(bookLocation.getSection(), bookLocation.getShelf());
        assertTrue(result.isPresent(), "This book location should exist in the database.");
    }


    // Test for retrieving all book locations from the database
    @Test
    public void testGetAllLocations() throws DatabaseOperationException{
        BookLocation bookLocation = new BookLocation("A", 1);
        BookLocation bookLocation2 = new BookLocation("A", 2);

        bookLocationDAO.addLocation(bookLocation);
        bookLocationDAO.addLocation(bookLocation2);

        List<BookLocation> allBookLocations = bookLocationDAO.getAllBookLocations();

        assertEquals(2, allBookLocations.size(), "There should be 2 book locations in the database.");

        assertEquals(1, allBookLocations.get(0).getShelf(), "The shelf number of the first location should be 1.");
        assertEquals(2, allBookLocations.get(1).getShelf(), "The shelf number of the second location should be 2.");
    }

    // Test for checking if there are any books stored in the specific location
    @Test
    public void testIsAnyBookInLocation() throws DatabaseOperationException {
        // Add a new book location
        BookLocation bookLocation = new BookLocation("A", 1);
        Optional<Integer> locationId = bookLocationDAO.addLocation(bookLocation);

        boolean isBookPresent = bookLocationDAO.isAnyBookInLocation(bookLocation.getSection(), bookLocation.getShelf());
        assertFalse(isBookPresent, "There should be no books in this location initially.");

        // Add a book associated with this location
        BookLocation locationWithId = new BookLocation(locationId.get(), bookLocation.getSection(), bookLocation.getShelf());
        Book book = new Book("Effective Java", "Joshua Bloch", 2018, 10, locationWithId);
        bookDAO.addBook(book);

        boolean isNewBookPresent = bookLocationDAO.isAnyBookInLocation(locationWithId.getSection(), locationWithId.getShelf());
        assertTrue(isNewBookPresent, "There should be a book in this location.");

    }


    // Test for removing a book location by its section and shelf
    @Test
    public void testRemoveBookLocation() throws DatabaseOperationException{
        BookLocation bookLocation = new BookLocation("A", 1);
        bookLocationDAO.addLocation(bookLocation);

        boolean result = bookLocationDAO.removeBookLocation(bookLocation.getSection(), bookLocation.getShelf());
        assertTrue(result, "Book location should be removed successfully.");

    }

    // Test for attempting to remove a non-existing book location
    @Test
    public void testRemoveNonExistingBookLocation() throws DatabaseOperationException {
        BookLocation bookLocation = new BookLocation("A", 1);
        boolean result = bookLocationDAO.removeBookLocation(bookLocation.getSection(), bookLocation.getShelf());
        assertFalse(result, "Attempting to remove a non-existing location should return false.");
    }

}
