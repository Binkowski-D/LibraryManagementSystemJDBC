package serviceTest;

import exception.DatabaseOperationException;
import exception.InvalidDataException;
import model.BookLocation;
import model.BorrowedBook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.BookLocationService;
import service.BookService;
import service.BorrowedBookService;
import service.ReaderService;
import util.TestDatabaseHelper;
import model.Book;
import model.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BorrowedBookServiceTest {

    private Connection connection;
    private BorrowedBookService borrowedBookService;
    private BookService bookService;
    private BookLocationService bookLocationService;
    private ReaderService readerService;

    @BeforeEach
    public void setup() throws SQLException {
        connection = TestDatabaseHelper.getTestConnection();
        TestDatabaseHelper.createShelfLocationTable(connection);
        TestDatabaseHelper.createBooksTable(connection);
        TestDatabaseHelper.createReadersTable(connection);
        TestDatabaseHelper.createBorrowedBooksTable(connection);

        borrowedBookService = new BorrowedBookService(connection);
        bookLocationService = new BookLocationService(connection);
        bookService = new BookService(connection);
        readerService = new ReaderService(connection);

    }

    @AfterEach
    public void tearDown() throws SQLException {
        TestDatabaseHelper.dropTable(connection, "borrowed_books");
        TestDatabaseHelper.dropTable(connection, "books");
        TestDatabaseHelper.dropTable(connection, "readers");
        TestDatabaseHelper.dropTable(connection, "book_shelf_location");

        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Helper method to add a shelf location
    private BookLocation insertShelfLocation() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = new BookLocation("A", 1);
        Optional<Integer> locationId = bookLocationService.addLocation(location);
        return new BookLocation(locationId.get(), location.getSection(), location.getShelf());
    }

    // Test for adding a new borrowed book
    @Test
    public void testAddBorrowedBook() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        Optional<Integer> borrowId = borrowedBookService.addBorrowedBook(reader, book);
        assertTrue(borrowId.isPresent(), "Book should be borrowed successfully.");
    }

    // Test for adding a borrowed book when there are no available copies
    @Test
    public void testAddBorrowedBookNoAvailableCopies() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 1, location); // Only 1 copy available
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), location);

        Reader reader1 = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader(reader1.getFirstName(), reader1.getLastName(), reader1.getDateOfBirth());

        borrowedBookService.addBorrowedBook(reader1, book); // Borrow the only copy

        Reader reader2 = new Reader("Jane", "Doe", LocalDate.of(1992, 2, 2));
        readerService.addReader(reader2.getFirstName(), reader2.getLastName(), reader2.getDateOfBirth());

        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                        borrowedBookService.addBorrowedBook(reader2, book),
                "Expected an InvalidDataException when no copies are available."
        );
        assertTrue(exception.getMessage().contains("Not enough copies of the book are available"),
                "Exception message should indicate no available copies.");
    }

    // Test for checking if a reader has overdue loans
    @Test
    public void testHasOverdueLoans() throws DatabaseOperationException, InvalidDataException, SQLException {
        BookLocation location = insertShelfLocation();

        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);
        Optional<Integer> bookId = bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), location);

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        Optional<Integer> readerId = readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        borrowedBookService.addBorrowedBook(reader, book);

        // Simulate overdue by manually updating borrow_date and return_due_date in the database
        try (PreparedStatement updateStmt = connection.prepareStatement(
                "UPDATE borrowed_books SET borrow_date = ?, return_due_date = ? WHERE reader_id = ? AND book_id = ?")) {
            // Set borrow date to 35 days ago and return due date to 5 days ago to simulate overdue
            updateStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(BorrowedBook.BORROW_PERIOD_DAYS + 5)));
            updateStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(5)));
            updateStmt.setInt(3, bookId.get());
            updateStmt.setInt(4, readerId.get());
            updateStmt.executeUpdate();
        }

        // Check if the reader has overdue loans
        boolean hasOverdueLoans = borrowedBookService.hasOverdueLoans(reader);
        assertTrue(hasOverdueLoans, "Reader should have overdue loans.");
    }


    // Test for adding a borrowed book when reader has overdue loans
    @Test
    public void testAddBorrowedBookWithOverdueLoans() throws DatabaseOperationException, InvalidDataException, SQLException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);
        Optional<Integer> bookId = bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), location);

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        Optional<Integer> readerId = readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        // First borrow
        borrowedBookService.addBorrowedBook(reader, book);

        // Simulate overdue by manually updating the borrow_date and return_due_date in the database
        try (PreparedStatement updateStmt = connection.prepareStatement(
                "UPDATE borrowed_books SET borrow_date = ?, return_due_date = ? WHERE reader_id = ? AND book_id = ?")) {
            updateStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(BorrowedBook.BORROW_PERIOD_DAYS + 5))); // Borrow date in the past
            updateStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(5))); // Return due date in the past
            updateStmt.setInt(3, readerId.get());
            updateStmt.setInt(4, bookId.get());
            updateStmt.executeUpdate();
        }

        Book book2 = new Book("Robinson Crusoe", "Daniel Defoe", 2008, 5,location);
        bookService.addBook(book2.getTitle(), book2.getAuthor(), book2.getYearOfPublication(), book2.getQuantity(), book2.getLocation());

        // Now the reader should have overdue loans
        InvalidDataException exception = assertThrows(InvalidDataException.class, () ->
                        borrowedBookService.addBorrowedBook(reader, book2),
                "Expected an InvalidDataException for overdue loans."
        );
        assertTrue(exception.getMessage().contains("overdue loans"), "Exception message should indicate overdue loans.");
    }

    // Test to verify that the quantity of available books decreases after a book is borrowed
    @Test
    public void testBookQuantityDecreasesAfterBorrowing() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        Optional<Integer> bookId = bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), location);
        assertTrue(bookId.isPresent(), "Book should be added successfully.");

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        Optional<Integer> readerId = readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());
        assertTrue(readerId.isPresent(), "Reader should be added successfully.");

        borrowedBookService.addBorrowedBook(reader, book);

        Optional<Book> bookAfterBorrowing = bookService.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
        int quantityAfterBorrow = bookAfterBorrowing.get().getQuantity();

        assertEquals(4, quantityAfterBorrow, "Book quantity should decrease by 1 after borrowing.");
    }

    // Test for fetching books borrowed by a specific reader
    @Test
    public void testGetBooksBorrowedByReaderWithDates() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        borrowedBookService.addBorrowedBook(reader, book);

        List<String> borrowedBooks = borrowedBookService.getBooksBorrowedByReaderWithDates(reader);

        assertFalse(borrowedBooks.isEmpty(), "Borrowed books list should not be empty for the reader.");
        assertEquals(1, borrowedBooks.size(), "There should be one book borrowed by the reader.");

        String expectedBorrowDate = "Date of hire: " + LocalDate.now(); // Adjust if needed to match actual date
        boolean containsBorrowDate = borrowedBooks.getFirst().contains(expectedBorrowDate);

        assertTrue(containsBorrowDate, "Borrowed books list should contain the correct borrow date.");

    }

    // Test for fetching books borrowed by a reader with no loans
    @Test
    public void testGetBooksBorrowedByReaderWithNoLoans() throws DatabaseOperationException, InvalidDataException {
        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());
        List<String> borrowedBooks = borrowedBookService.getBooksBorrowedByReaderWithDates(reader);

        assertTrue(borrowedBooks.isEmpty(), "Borrowed books list should be empty for a reader with no loans.");
    }

    // Test for fetching overdue readers
    @Test
    public void testGetOverdueReaders() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), location);

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        borrowedBookService.addBorrowedBook(reader, book); // Borrow book

        List<String> overdueReaders = borrowedBookService.getOverdueReaders();
        assertTrue(overdueReaders.isEmpty(), "No overdue readers should be present initially.");
    }

    // Test for returning borrowed book which should end successfully
    @Test
    public void testReturnBorrowedBook() throws DatabaseOperationException, InvalidDataException{
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), location);

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        borrowedBookService.addBorrowedBook(reader, book); // Borrow book

        boolean result = borrowedBookService.returnBorrowedBook(reader, book); // Return book

        assertTrue(result, "The return of a borrowed book should be successful");
    }

    // Test for returning an unloaned book
    @Test
    public void testReturnUnborrowedBook() throws DatabaseOperationException, InvalidDataException{
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), location);

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class, () -> borrowedBookService.returnBorrowedBook(reader, book),
                "Attempting to return an unlent book should result in a DatabaseOperationException.");

        assertTrue(exception.getMessage().contains("No loan found in the database"));
    }

    // Test to verify that the quantity of available books increases after a book is returned
    @Test
    public void testBookQuantityIncreasesAfterReturning() throws DatabaseOperationException, InvalidDataException{
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), location);

        Reader reader = new Reader("John", "Doe", LocalDate.of(1990, 1, 1));
        readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        borrowedBookService.addBorrowedBook(reader, book);
        Optional<Book> bookAfterBorrowing = bookService.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
        int quantityAfterBorrow = bookAfterBorrowing.get().getQuantity();
        assertEquals(4, quantityAfterBorrow, "Book quantity should decrease by 1 after borrowing.");

        borrowedBookService.returnBorrowedBook(reader, book);

        Optional<Book> bookAfterReturning = bookService.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
        int quantityAfterReturn = bookAfterReturning.get().getQuantity();
        assertEquals(5, quantityAfterReturn, "Book quantity should increase by 1 after returning.");

    }

}
