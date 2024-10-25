package daoTest;

import dao.ReaderDAO;
import exception.DatabaseOperationException;
import model.Reader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TestDatabaseHelper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReaderDAOTest {

    private ReaderDAO readerDAO;
    private Connection connection;

    // Setting up the database connection before each test
    @BeforeEach
    public void setup() throws SQLException {
        connection = TestDatabaseHelper.getTestConnection(); // Connect to the H2 test database
        TestDatabaseHelper.createReadersTable(connection);
        readerDAO = new ReaderDAO(connection); // Initialize the DAO class
    }

    // Cleaning up after each test by dropping the table and closing the connection
    @AfterEach
    public void tearDown() throws SQLException {
        TestDatabaseHelper.dropTable(connection, "readers");

        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Test for adding a new reader
    @Test
    public void testAddReader() throws DatabaseOperationException {
        Reader reader = new Reader("John", "Doe", LocalDate.of(2000, 1, 1));
        Optional<Integer> result = readerDAO.addReader(reader);

        assertTrue(result.isPresent(), "Reader should be added successfully when data is valid.");
        assertEquals(1, result.get(), "Reader ID should be generated.");
    }

    // Test for adding a duplicate reader
    @Test
    public void testAddDuplicateReaderReturnsFalse() throws DatabaseOperationException {
        Reader reader = new Reader("John", "Doe", LocalDate.of(2000, 1, 1));
        readerDAO.addReader(reader);

        Reader reader2 = new Reader("John", "Doe", LocalDate.of(2000, 1, 1)); // Create a duplicate reader
        Optional<Integer> result = readerDAO.addReader(reader2); // Try adding the duplicate reader

        assertTrue(result.isEmpty(), "Adding a duplicate reader should return an empty Optional.");
    }

    // Test for retrieving all readers from the database
    @Test
    public void testGetAllReaders() throws DatabaseOperationException {
        Reader reader = new Reader("John", "Doe", LocalDate.of(2000, 1, 1));
        Reader reader2 = new Reader("John", "Oed", LocalDate.of(2000, 1, 1));

        readerDAO.addReader(reader);
        readerDAO.addReader(reader2);

        // Retrieve all readers
        List<Reader> allReaders = readerDAO.getAllReaders();

        assertEquals(2, allReaders.size(), "There should be 2 readers in the database.");
        // Check the first reader
        assertEquals("Doe", allReaders.get(0).getLastName(), "First reader's last name should be 'Doe'.");
        assertEquals("John", allReaders.get(0).getFirstName(), "First reader's first name should be 'John'.");
        // Check the second reader
        assertEquals("Oed", allReaders.get(1).getLastName(), "Second reader's last name should be 'Oed'.");
        assertEquals("John", allReaders.get(1).getFirstName(), "Second reader's first name should be 'John'.");
    }

    // Test for retrieving readers by last name
    @Test
    public void testGetReaderByLastName() throws DatabaseOperationException {
        Reader reader = new Reader("John", "Doe", LocalDate.of(2000, 1, 1));
        readerDAO.addReader(reader);

        // Retrieve readers by last name
        List<Reader> readers = readerDAO.getReadersByLastName("Doe");
        assertEquals(1, readers.size(), "There should be exactly 1 reader with last name 'Doe'.");
        assertEquals("John", readers.getFirst().getFirstName(), "Reader's first name should be 'John'.");
    }

    // Test for removing a reader
    @Test
    public void testRemoveReader() throws DatabaseOperationException {
        Reader reader = new Reader("John", "Doe", LocalDate.of(2000, 1, 1));
        Optional<Integer> resultOfAddition = readerDAO.addReader(reader);
        assertTrue(resultOfAddition.isPresent(), "Reader should be added successfully.");

        // Retrieve readers before removal
        List<Reader> readersBeforeRemoval = readerDAO.getAllReaders();
        assertEquals(1, readersBeforeRemoval.size(), "There should be 1 reader before removal.");

        // Remove the reader
        boolean resultOfRemoval = readerDAO.removeReaderById(reader);
        assertTrue(resultOfRemoval, "Reader should be removed successfully.");

        // Retrieve readers after removal
        List<Reader> readersAfterRemoval = readerDAO.getAllReaders();
        assertEquals(0, readersAfterRemoval.size(), "There should be no readers after removal.");
    }

    // Test for removing a non-existing reader
    @Test
    public void testRemoveNotExistingReader() throws DatabaseOperationException {
        // Try removing a non-existing reader
        Reader nonExistentReader = new Reader(999, "Non", "Existent", LocalDate.of(1900, 1, 1));

        boolean result = readerDAO.removeReaderById(nonExistentReader);
        assertFalse(result, "An attempt to remove a non-existing reader should return false.");
    }
}
