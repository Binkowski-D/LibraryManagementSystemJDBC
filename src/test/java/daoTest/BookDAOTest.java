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
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookDAOTest {

    private BookDAO bookDAO;
    private BookLocationDAO bookLocationDAO;
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

        bookDAO = new BookDAO(connection); // Initialize the DAO class
        bookLocationDAO = new BookLocationDAO(connection); // Initialize the BookLocation DAO class
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close(); // Close the database connection
        }
    }

    private void clearDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            stmt.executeUpdate("DELETE FROM books");
            stmt.executeUpdate("DELETE FROM book_shelf_location");

            stmt.executeUpdate("ALTER TABLE books ALTER COLUMN id RESTART WITH 1");

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    private BookLocation insertShelfLocation() throws DatabaseOperationException {
        BookLocation location = new BookLocation("A", 1);
        Optional<Integer> locationId = bookLocationDAO.addLocation(location);
        return new BookLocation(locationId.get(), location.getSection(), location.getShelf());
    }

    // Test for adding a new book
    @Test
    public void testAddBook() throws DatabaseOperationException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        Optional<Integer> bookID = bookDAO.addBook(book);

        assertTrue(bookID.isPresent(), "Book should be added successfully when data is valid.");
        assertEquals(1, bookID.get(), "Book ID should be generated.");
    }

    // Test for adding an existing book (it should return an empty Optional)
    @Test
    public void testAddExistingBook() throws DatabaseOperationException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        bookDAO.addBook(book); // Add once

        // Attempt to add the same book again
        Optional<Integer> bookID = bookDAO.addBook(book);
        assertTrue(bookID.isEmpty(), "Should not add the same book again.");
    }

    // Test for increasing the quantity of a book
    @Test
    public void testIncreaseBookQuantity() throws DatabaseOperationException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        bookDAO.addBook(book);

        bookDAO.increaseBookQuantity(book, 2);
        List<Book> books = bookDAO.getAllBooks();

        int currentQuantity = books.getFirst().getQuantity();

        assertEquals(12, currentQuantity, "The quantity should be increased to 12 after increasing by 2.");
    }

    // Test for decreasing the quantity of a book
    @Test
    public void testDecreaseBookQuantity() throws DatabaseOperationException {
        BookLocation location = insertShelfLocation();
        Book book = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        bookDAO.addBook(book);

        bookDAO.decreaseBookQuantity(book, 2);
        List<Book> books = bookDAO.getAllBooks();

        int currentQuantity = books.getFirst().getQuantity();

        assertEquals(8, currentQuantity, "The quantity should be reduced to 8 after decreasing by 2.");
    }

    // Test for retrieving all books from the database
    @Test
    public void testGetAllBooks() throws DatabaseOperationException {
        BookLocation location = insertShelfLocation();

        Book book1 = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        Book book2 = new Book("Clean Code", "Robert C. Martin", 2008, 5, location);

        bookDAO.addBook(book1);
        bookDAO.addBook(book2);

        List<Book> allBooks = bookDAO.getAllBooks();

        assertEquals(2, allBooks.size(), "There should be 2 books in the database.");
        assertEquals("Clean Code", allBooks.get(0).getTitle(), "First book's title should be 'Clean Code'.");
        assertEquals("Effective Java", allBooks.get(1).getTitle(), "Second book's title should be 'Effective Java'.");
    }

    // Test for retrieving books by title
    @Test
    public void testGetBooksByTitle() throws DatabaseOperationException {
        BookLocation location = insertShelfLocation();

        Book book1 = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        Book book2 = new Book("Clean Code", "Robert C. Martin", 2008, 5, location);
        bookDAO.addBook(book1);
        bookDAO.addBook(book2);

        List<Book> books = bookDAO.getBooksByTitle("Effective Java");
        assertEquals(1, books.size(), "There should be exactly 1 book with title 'Effective Java'.");
        assertEquals("Joshua Bloch", books.getFirst().getAuthor(), "The author should be 'Joshua Bloch'.");
    }

    // Test for retrieving books by a non-existing title
    @Test
    public void testGetBooksByNonExistingTitle() throws DatabaseOperationException {
        List<Book> books = bookDAO.getBooksByTitle("Non-existing title");
        assertTrue(books.isEmpty(), "No books should be returned for a non-existing title.");
    }

    // Test for retrieving books by author
    @Test
    public void testGetBooksByAuthor() throws DatabaseOperationException {
        BookLocation location = insertShelfLocation();

        Book book1 = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        Book book2 = new Book("Clean Code", "Robert C. Martin", 2008, 5, location);
        bookDAO.addBook(book1);
        bookDAO.addBook(book2);

        List<Book> books = bookDAO.getBooksByAuthor("Joshua Bloch");
        assertEquals(1, books.size(), "There should be exactly 1 book by 'Joshua Bloch'.");
        assertEquals("Effective Java", books.getFirst().getTitle(), "The title should be 'Effective Java'.");
    }

    // Test for finding a book by its details (title, author, year of publication)
    @Test
    public void testFindBookByDetails() throws DatabaseOperationException {
        BookLocation location = insertShelfLocation();

        Book book1 = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        Book book2 = new Book("Clean Code", "Robert C. Martin", 2008, 5, location);
        bookDAO.addBook(book1);
        bookDAO.addBook(book2);

        // Try to find the first book by its details
        Optional<Book> result = bookDAO.findBookByDetails("Effective Java", "Joshua Bloch", 2018);

        // Assert that the book is present
        assertTrue(result.isPresent(), "Expected to find 'Effective Java' by Joshua Bloch.");

        // Assert that the details of the found book are correct
        Book foundBook = result.get();
        assertEquals("Effective Java", foundBook.getTitle(), "Expected the title to be 'Effective Java'.");
        assertEquals("Joshua Bloch", foundBook.getAuthor(), "Expected the author to be 'Joshua Bloch'.");
        assertEquals(2018, foundBook.getYearOfPublication(), "Expected the year of publication to be 2018.");
        assertEquals(10, foundBook.getQuantity(), "Expected the quantity to be 10.");
    }

    // Test for removing a book by its details
    @Test
    public void testRemoveBookByDetails() throws SQLException, DatabaseOperationException {
        BookLocation location = insertShelfLocation();

        Book book = new Book("Effective Java", "Joshua Bloch", 2018, 10, location);
        bookDAO.addBook(book);

        boolean result = bookDAO.removeBookByDetails("Effective Java", "Joshua Bloch", 2018);
        assertTrue(result, "The book should be removed successfully.");

        List<Book> booksAfterRemoval = bookDAO.getAllBooks();
        assertTrue(booksAfterRemoval.isEmpty(), "There should be no books after removal.");
    }
}
