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
import service.BorrowedBookService;
import service.ReaderService;
import util.TestDatabaseHelper;
import model.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookServiceTest {
    private Connection connection;
    private BookService bookService;
    private BookLocationService bookLocationService;
    private ReaderService readerService;
    private BorrowedBookService borrowedBookService;

    @BeforeEach
    public void setup() throws SQLException {
        connection = TestDatabaseHelper.getTestConnection();
        TestDatabaseHelper.createShelfLocationTable(connection);
        TestDatabaseHelper.createBooksTable(connection);
        TestDatabaseHelper.createReadersTable(connection);
        TestDatabaseHelper.createBorrowedBooksTable(connection);

        bookService = new BookService(connection);
        bookLocationService = new BookLocationService(connection);
        readerService = new ReaderService(connection);
        borrowedBookService = new BorrowedBookService(connection);
    }

    @AfterEach
    public void tearDown() throws SQLException{
        TestDatabaseHelper.dropTable(connection, "borrowed_books");
        TestDatabaseHelper.dropTable(connection, "books");
        TestDatabaseHelper.dropTable(connection, "readers");
        TestDatabaseHelper.dropTable(connection, "book_shelf_location");


        if(connection != null && !connection.isClosed()){
            connection.close();
        }
    }

    private BookLocation insertShelfLocation() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = new BookLocation("A", 1);
        Optional<Integer> locationId = bookLocationService.addLocation(location);
        return new BookLocation(locationId.get(), location.getSection(), location.getShelf());
    }

    // Test for adding a new book
    @Test
    public void testAddBook() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Optional<Integer> bookId = bookService.addBook("Effective Java", "Joshua Bloch", 2008, 5, location);

        assertTrue(bookId.isPresent(), "Book should be added successfully");
    }

    // Test for adding a book with invalid data
    @Test
    public void testAddBookWithInvalidData() throws DatabaseOperationException, InvalidDataException{
        BookLocation location = insertShelfLocation();
        assertThrows(InvalidDataException.class, () -> bookService.addBook("", "Joshua Bloch", 2008, 5, location), "Expected an InvalidDataException for an empty title");
    }

    // Test for increasing the quantity of a book
    @Test
    public void testIncreaseBookQuantity() throws DatabaseOperationException, InvalidDataException{
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        boolean result = bookService.increaseBookQuantity(book, 5);
        assertTrue(result, "Book quantity should be increased.");

        Optional<Book> updatedBook = bookService.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
        assertTrue(updatedBook.isPresent(), "Book should exist in the database after increasing quantity.");
        assertEquals(10, updatedBook.get().getQuantity(), "New book quantity should be 10.");
    }

    @Test
    public void testIncreaseNonExistingBookQuantity() throws DatabaseOperationException, InvalidDataException{
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        boolean result = bookService.increaseBookQuantity(book, 5);
        assertFalse(result, "Book quantity should not be increased, because book does not exist in the database.");
    }

    // Test for decreasing the quantity of a book
    @Test
    public void testDecreaseBookQuantity() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        boolean result = bookService.decreaseBookQuantity(book, 5);
        assertTrue(result, "Book quantity should be decreased.");

        Optional<Book> updatedBook = bookService.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
        assertTrue(updatedBook.isPresent(), "Book should exist in the database after decreasing quantity.");
        assertEquals(0, updatedBook.get().getQuantity(), "New book quantity should be 0.");

    }

    // Test for decreasing the quantity of a book where the available quantity is less than the quantity we want to decrease
    @Test
    public void testDecreaseMoreThanWeHave() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        InvalidDataException exception = assertThrows(InvalidDataException.class,
                () -> bookService.decreaseBookQuantity(book, 10),
                "Expected an InvalidDataException when trying to decrease more books than available.");

        assertTrue(exception.getMessage().contains("Not enough books in stock"),
                "Exception message should indicate there are not enough books available.");
    }

    // Test for fetching all books
    @Test
    public void testGetAllBooks() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();

        bookService.addBook("Effective Java", "Joshua Bloch", 2008, 5, location);
        bookService.addBook("Clean Code", "Robert C. Martin", 2008, 5, location);

        List<Book> books = bookService.getAllBooks();
        assertEquals(2, books.size(), "There should be 2 books in the database.");
    }

    // Test for fetching books by title
    @Test
    public void testGetBooksByTitle() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        List<Book> books = bookService.getBooksByTitle("Effective Java");
        assertFalse(books.isEmpty(), "The list of books should not be empty when the title exists.");
        assertEquals(1, books.size(), "There should be exactly one book with the title 'Effective Java'.");
        assertEquals("Effective Java", books.getFirst().getTitle(), "The title of the fetched book should be 'Effective Java'.");
    }

    // Test for fetching books by title that does not exist
    @Test
    public void testGetBooksByTitleNotFound() throws DatabaseOperationException, InvalidDataException {
        List<Book> books = bookService.getBooksByTitle("Non-Existent Book");
        assertTrue(books.isEmpty(), "The list of books should be empty when the title does not exist.");
    }

    // Test for fetching books by author
    @Test
    public void testGetBooksByAuthor() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        List<Book> books = bookService.getBooksByAuthor("Joshua Bloch");
        assertFalse(books.isEmpty(), "The list of books should not be empty when the author exists.");
        assertEquals(1, books.size(), "There should be exactly one book by the author 'Joshua Bloch'.");
        assertEquals("Joshua Bloch", books.getFirst().getAuthor(), "The author of the fetched book should be 'Joshua Bloch'.");
    }

    // Test for fetching books by author that does not exist
    @Test
    public void testGetBooksByAuthorNotFound() throws DatabaseOperationException, InvalidDataException {
        List<Book> books = bookService.getBooksByAuthor("Non-Existent Author");
        assertTrue(books.isEmpty(), "The list of books should be empty when the author does not exist.");
    }

    // Test for removing a book
    @Test
    public void testRemoveBook() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);

        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        boolean result = bookService.removeBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());

        assertTrue(result, "Book should be removed successfully.");

        Optional<Book> removedBook = bookService.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
        assertTrue(removedBook.isEmpty(), "Book should no longer exist in the database.");
    }

    // Test for attempting to remove a non-existing book
    @Test
    public void testRemoveNonExistingBook() throws DatabaseOperationException, InvalidDataException {
        boolean result = bookService.removeBookByDetails("Non-Existent Book", "Unknown Author", 2000);

        assertFalse(result, "Attempt to remove a non-existing book should return false.");
    }

    // Test for attempting to remove a borrowed book
    @Test
    public void testRemoveBorrowedBook() throws DatabaseOperationException, InvalidDataException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2008, 5, location);
        bookService.addBook(book.getTitle(), book.getAuthor(), book.getYearOfPublication(), book.getQuantity(), book.getLocation());

        Reader reader = new Reader("John", "Doe", LocalDate.of(2000, 1, 1));
        readerService.addReader(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        borrowedBookService.addBorrowedBook(reader, book);

        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> bookService.removeBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication()),
                "Expected a DatabaseOperationException to be thrown when trying to remove a borrowed book.");

        assertTrue(exception.getMessage().contains("currently borrowed"), "Exception message should indicate that the book is currently borrowed.");
    }

}
