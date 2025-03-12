package daoTest;

import dao.BookDAO;
import dao.BookLocationDAO;
import dao.BorrowedBookDAO;
import dao.ReaderDAO;
import exception.DatabaseOperationException;
import model.Book;
import model.BookLocation;
import model.BorrowedBook;
import model.Reader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.TestDatabaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class BorrowedBookDAOTest {
    private BorrowedBookDAO borrowedBookDAO;
    private BookDAO bookDAO;
    private ReaderDAO readerDAO;
    private Connection connection;
    private BookLocationDAO bookLocationDAO;

    @BeforeAll
    public static void setupDatabase() throws SQLException {
        try (Connection conn = TestDatabaseHelper.getTestConnection()) {
            TestDatabaseHelper.createShelfLocationTable(conn);
            TestDatabaseHelper.createBooksTable(conn);
            TestDatabaseHelper.createReadersTable(conn);
            TestDatabaseHelper.createBorrowedBooksTable(conn);
        }
    }

    // Setting up the database connection before each test
    @BeforeEach
    public void setup() throws SQLException {
        connection = TestDatabaseHelper.getTestConnection(); // Connect to the H2 test database
        clearDatabase();

        borrowedBookDAO = new BorrowedBookDAO(connection); // Initialize the DAO class
        bookDAO = new BookDAO(connection); // Initialize the DAO class
        readerDAO = new ReaderDAO(connection); // Initialize the DAO class
        bookLocationDAO = new BookLocationDAO(connection); // Initialize the DAO class

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

            stmt.executeUpdate("DELETE FROM borrowed_books");
            stmt.executeUpdate("DELETE FROM books");
            stmt.executeUpdate("DELETE FROM book_shelf_location");
            stmt.executeUpdate("DELETE FROM readers");

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    private BookLocation insertShelfLocation() throws DatabaseOperationException {
        BookLocation location = new BookLocation("A", 1);
        Optional<Integer> locationId = bookLocationDAO.addLocation(location);
        return new BookLocation(locationId.get(), location.getSection(), location.getShelf());
    }

    // Helper method to insert a reader and return the Reader object with the ID fetched from the database
    private Reader insertReader() throws DatabaseOperationException {
        Reader reader = new Reader("John", "Doe", LocalDate.of(2000, 1, 1));
        Optional<Integer> readerId = readerDAO.addReader(reader);
        return new Reader(readerId.get(), reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());
    }

    // Helper method to insert a book and return the Book object with the ID fetched from the database
    private Book insertBook() throws DatabaseOperationException {
        BookLocation bookLocation = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2018, 10, bookLocation);
        Optional<Integer> bookId = bookDAO.addBook(book);
        return new Book(bookId.get(), book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());
    }

    // Test for adding a new borrowed book
    @Test
    public void testAddBorrowedBook() throws DatabaseOperationException {
        Reader reader = insertReader();
        Book book = insertBook();

        Optional<Integer> result = borrowedBookDAO.addBorrowedBook(reader, book);
        assertTrue(result.isPresent(), "Borrowed book should be added successfully.");
    }

    // Test for trying to add an existing borrowed book (it should return an empty Optional)
    @Test
    public void testAddExistingBorrowedBook() throws DatabaseOperationException {
        Reader reader = insertReader();
        Book book = insertBook();

        borrowedBookDAO.addBorrowedBook(reader,book);
        Optional<Integer> result = borrowedBookDAO.addBorrowedBook(reader, book);
        assertTrue(result.isEmpty(), "Should not add the same borrowed book again.");
    }

    // Test for fetching all books borrowed by a specific reader with borrow and return dates
    @Test
    public void testGetBooksBorrowedByReaderWithDates() throws DatabaseOperationException {
        Reader reader = insertReader();
        Book book = insertBook();
        borrowedBookDAO.addBorrowedBook(reader, book);

        List<String> borrowedBooks = borrowedBookDAO.getBooksBorrowedByReaderWithDates(reader);
        assertEquals(1, borrowedBooks.size(), "Reader should have borrowed one book.");
        assertTrue(borrowedBooks.getFirst().contains("Effective Java"), "Borrowed book title should be 'Effective Java'.");
    }

    // Retrieves a list of readers who have overdue borrowed books based on the current date.
    @Test
    public void testGetOverdueReaders() throws DatabaseOperationException, SQLException {
        Reader reader = insertReader();
        Book book = insertBook();

        // Add a borrowed book
        borrowedBookDAO.addBorrowedBook(reader, book);

        // Update borrow and return due dates to simulate an overdue book
        try (PreparedStatement updateStmt = connection.prepareStatement("UPDATE borrowed_books SET borrow_date = ?, return_due_date = ? WHERE reader_id = ? AND book_id = ?")) {
            updateStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(BorrowedBook.BORROW_PERIOD_DAYS + 5))); // Borrow date in the past
            updateStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(5))); // Return due date in the past
            updateStmt.setInt(3, reader.getId());
            updateStmt.setInt(4, book.getId());
            updateStmt.executeUpdate();
        }

        // Verify that the overdue reader is correctly returned
        List<String> overdueReaders = borrowedBookDAO.getOverdueReaders();
        assertEquals(1, overdueReaders.size(), "There should be 1 overdue reader.");
        assertTrue(overdueReaders.getFirst().contains("John"), "The overdue reader should be " + reader.getFirstName());
    }

    // Test for checking if a reader has overdue loans
    @Test
    public void testHasOverdueLoans() throws DatabaseOperationException, SQLException{
        Reader reader = insertReader();
        Book book = insertBook();

        //Add a borrowed book
        borrowedBookDAO.addBorrowedBook(reader, book);

        // Update borrow and return due dates to simulate an overdue book
        try (PreparedStatement updateStmt = connection.prepareStatement("UPDATE borrowed_books SET borrow_date = ?, return_due_date = ? WHERE reader_id = ? AND book_id = ?")) {
            updateStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(BorrowedBook.BORROW_PERIOD_DAYS + 5))); // Borrow date in the past
            updateStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(5))); // Return due date in the past
            updateStmt.setInt(3, reader.getId());
            updateStmt.setInt(4, book.getId());
            updateStmt.executeUpdate();
        }

        assertTrue(borrowedBookDAO.hasOverdueLoans(reader), "We expect the reader to be in arrears with the return of borrowed books.");
    }

    // Test for checking if a reader does not have overdue loans
    @Test
    public void testHasNoOverdueLoans() throws DatabaseOperationException {
        Reader reader = insertReader();
        Book book = insertBook();

        // Add a borrowed book (but don't update the dates to simulate overdue)
        borrowedBookDAO.addBorrowedBook(reader, book);

        assertFalse(borrowedBookDAO.hasOverdueLoans(reader), "We expect the reader to not have overdue loans.");
    }

    // Test for checking if a book is borrowed
    @Test
    public void testIsBookBorrowed() throws DatabaseOperationException {
        Reader reader = insertReader();
        Book book = insertBook();

        // Initially, the book should not be borrowed
        assertFalse(borrowedBookDAO.isBookBorrowed(book), "The book should not be marked as borrowed.");

        // Add a borrowed book
        borrowedBookDAO.addBorrowedBook(reader, book);

        // Now, the book should be marked as borrowed
        assertTrue(borrowedBookDAO.isBookBorrowed(book), "The book should be marked as borrowed after adding it to borrowed_books.");
    }


    // Test for removing a borrowed book
    @Test
    public void testRemoveBorrowedBook() throws DatabaseOperationException {
        Reader reader = insertReader();
        Book book = insertBook();
        borrowedBookDAO.addBorrowedBook(reader, book);

        boolean result = borrowedBookDAO.removeBorrowedBook(reader, book);
        assertTrue(result, "Borrowed book should be removed successfully.");

        // Ensure the book is no longer in the borrowed_books table
        List<String> borrowedBooks = borrowedBookDAO.getBooksBorrowedByReaderWithDates(reader);
        assertTrue(borrowedBooks.isEmpty(), "Reader should have no borrowed books after removal.");
    }

    // Test for trying to remove a non-existing borrowed book (it should return false)
    @Test
    public void testRemoveNonExistingBorrowedBook() throws DatabaseOperationException {
        Reader reader = insertReader();
        Book book = insertBook();

        // Attempt to remove a borrowed book that hasn't been added
        boolean result = borrowedBookDAO.removeBorrowedBook(reader, book);
        assertFalse(result, "Removing a non-existing borrowed book should return false.");

    }
}
