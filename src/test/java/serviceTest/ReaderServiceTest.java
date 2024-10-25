package serviceTest;

import model.Book;
import model.BookLocation;
import model.Reader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.BookLocationService;
import service.BookService;
import service.BorrowedBookService;
import service.ReaderService;
import util.TestDatabaseHelper;
import exception.DatabaseOperationException;
import exception.InvalidDataException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderServiceTest {

    private Connection connection;
    private ReaderService readerService;

    @BeforeEach
    public void setup() throws SQLException {
        connection = TestDatabaseHelper.getTestConnection();
        TestDatabaseHelper.createReadersTable(connection);
        TestDatabaseHelper.createShelfLocationTable(connection);
        TestDatabaseHelper.createBooksTable(connection);
        TestDatabaseHelper.createBorrowedBooksTable(connection);

        readerService = new ReaderService(connection);

    }

    @AfterEach
    public void tearDown() throws SQLException {
        TestDatabaseHelper.dropTable(connection, "borrowed_books");
        TestDatabaseHelper.dropTable(connection, "books");
        TestDatabaseHelper.dropTable(connection, "shelf_locations");
        TestDatabaseHelper.dropTable(connection, "readers");

        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Test for adding a new reader
    @Test
    public void testAddReader() throws DatabaseOperationException, InvalidDataException {
        Optional<Integer> readerId = readerService.addReader("John", "Doe", LocalDate.of(1990, 1, 1));
        assertTrue(readerId.isPresent(), "Reader should be added successfully.");
    }

    // Test for validation of reader data (invalid first name)
    @Test
    public void testAddReaderInvalidFirstName() {
        Reader reader = new Reader("", "Doe", LocalDate.of(2000,1,1));
        assertThrows(InvalidDataException.class, () -> readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth()),
                "Expected an InvalidDataException to be thrown when first name is empty.");
    }

    // Test for validation of reader data (invalid last name)
    @Test
    public void testAddReaderInvalidLastName() {
        Reader reader = new Reader("John", "", LocalDate.of(2000,1,1));
        assertThrows(InvalidDataException.class, () -> readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth()),
                "Expected an InvalidDataException to be thrown when last name is empty.");
    }

    // Test for validation of reader data (invalid date of birth)
    @Test
    public void testAddReaderInvalidDateOfBirth() {
        Reader reader = new Reader("John", "Doe", LocalDate.of(2200,1,1));
        assertThrows(InvalidDataException.class, () -> readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth()),
                "Expected an InvalidDataException to be thrown when date of birth is invalid.");
    }

    // Test for fetching all readers
    @Test
    public void testGetAllReaders() throws DatabaseOperationException, InvalidDataException {
        readerService.addReader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader("Jan", "Kowalski", LocalDate.of(1992, 2, 2));

        List<Reader> readers = readerService.getAllReaders();
        assertEquals(2, readers.size(), "There should be 2 readers in the database.");
    }

    // Test for fetching readers by last name
    @Test
    public void testGetReadersByLastName() throws DatabaseOperationException, InvalidDataException {
        readerService.addReader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader("Jane", "Doe", LocalDate.of(1992, 2, 2));

        List<Reader> readers = readerService.getReadersByLastName("Doe");
        assertEquals(2, readers.size(), "There should be 2 readers with last name 'Doe'.");
    }

    // Test for removing a reader by ID
    @Test
    public void testRemoveReaderWithBorrowedBook() throws DatabaseOperationException, InvalidDataException {
        BookLocationService bookLocationService = new BookLocationService(connection);
        BookLocation bookLocation = new BookLocation("A", 1);

        // Adding book location
        bookLocationService.addLocation(bookLocation);


        BookService bookService = new BookService(connection);
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, bookLocation);

        // Adding book to the database
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), bookLocation);


        Reader reader = new Reader("John", "Doe", LocalDate.of(2000, 1, 1));

        // Adding reader to the database
        readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        BorrowedBookService borrowedBookService = new BorrowedBookService(connection);

        // Adding new borrow
        borrowedBookService.addBorrowedBook(reader, book);

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class, () -> readerService.removeReaderByDetails(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth()),
                "Expected a DatabaseOperationException to be thrown when trying to remove a reader with borrowed books");

        assertTrue(exception.getMessage().contains("cannot be removed because they have borrowed books."));

    }

}
