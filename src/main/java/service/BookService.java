package service;

import dao.BookDAO;
import dao.BorrowedBookDAO;
import exception.DatabaseOperationException;
import exception.InvalidDataException;
import model.Book;
import model.BookLocation;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookService {
    private static final Logger logger = Logger.getLogger(BookService.class.getName());
    private final Connection connection;
    private final BookDAO bookDAO;
    private final BorrowedBookDAO borrowedBookDAO;
    private final BookLocationService bookLocationService;

    public BookService(Connection connection){
        this.connection = connection;
        this.bookDAO = new BookDAO(connection);
        this.borrowedBookDAO = new BorrowedBookDAO(connection);
        this.bookLocationService = new BookLocationService(connection);
    }

    // Adds a new book after validation, manages transactions for commit/rollback
    public Optional<Integer> addBook(String title, String author, int yearOfPublication, int quantity, BookLocation bookLocation) throws DatabaseOperationException, InvalidDataException{
        String section = bookLocation.getSection();
        int shelf = bookLocation.getShelf();
        logger.info("Starting to add a new book: " + title + " " + author + " " + yearOfPublication);

        validateAllBookData(title, author, yearOfPublication, quantity, bookLocation.getId());

        try{
            connection.setAutoCommit(false); // Disable auto-commit

            Optional<Integer> locationId = bookLocationService.doesBookLocationExist(section, shelf);

            if (locationId.isEmpty()) {
                logger.info("Location does not exist. Adding new location: Section " + section + ", Shelf " + shelf);
                locationId = bookLocationService.addLocation(new BookLocation(section, shelf));
                if (locationId.isEmpty()) {
                    throw new DatabaseOperationException("Failed to add new book location");
                }
            }

            Book book = new Book(title, author, yearOfPublication, quantity, new BookLocation(locationId.get(), section, shelf));
            Optional<Integer> bookId = bookDAO.addBook(book);

            if(bookId.isPresent()){
                logger.info("New book added with ID: " + bookId.get());
                connection.commit();
            }else{
                logger.warning("Book: " + title + " " + author + " " + yearOfPublication + " already exists.");
                connection.rollback();
            }

            return bookId;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error while adding new book: " + e.getMessage(), e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to add new book", e);
        }
    }

    // Method to increase the quantity of a book in the database
    public boolean increaseBookQuantity(Book book, int quantityToAdd) throws DatabaseOperationException, InvalidDataException {
        logger.info("Starting to increase book quantity: " + book.getTitle() + " by " + quantityToAdd);

        validateBasicBookData(book.getTitle(), book.getAuthor(), book.getYearOfPublication());

        try{
            connection.setAutoCommit(false); // Disable auto-commit
            Optional<Book> existingBook = bookDAO.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());

            if(existingBook.isPresent()){
                boolean result = bookDAO.increaseBookQuantity(existingBook.get(), quantityToAdd);

                if(result){
                    logger.info("Successfully increased book quantity for ID: " + existingBook.get().getId());
                    connection.commit();
                    return true;
                }else{
                    logger.warning("Failed to increase book quantity for ID: " + existingBook.get().getId());
                    return false;
                }

            }else{
                logger.warning("Book does not exist in the database.");
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error while increasing book quantity: " + e.getMessage(), e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to increase book quantity", e);
        }
    }

    // Method to decrease the quantity of a book in the database
    public boolean decreaseBookQuantity(Book book, int quantityToReduce) throws DatabaseOperationException, InvalidDataException{
        logger.info("Starting to decrease book quantity: " + book.getTitle() + " by " + quantityToReduce);

        validateBasicBookData(book.getTitle(), book.getAuthor(), book.getYearOfPublication());

        try{
            connection.setAutoCommit(false); // Disable auto-commit
            Optional<Book> existingBook = bookDAO.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());

            if(existingBook.isPresent()){
                int currentQuantity = existingBook.get().getQuantity();
                if(currentQuantity < quantityToReduce){
                    logger.warning("Failed to decrease book quantity for ID: " + existingBook.get().getId() + ". Not enough books available.");
                    throw new InvalidDataException("Cannot reduce quantity. Not enough books in stock.");
                }
                boolean result = bookDAO.decreaseBookQuantity(existingBook.get(), quantityToReduce);

                if(result){
                    logger.info("Successfully decreased book quantity for ID: " + existingBook.get().getId());
                    connection.commit();
                    return true;
                }else{
                    logger.warning("Failed to decrease book quantity for ID: " + existingBook.get().getId());
                    return false;
                }

            }else{
                logger.warning("Book does not exist in the database.");
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error while decreasing book quantity: " + e.getMessage(), e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to decrease book quantity", e);
        }
    }

    // Fetches all books from the database
    public List<Book> getAllBooks() throws DatabaseOperationException {
        logger.info("Starting to fetch all books from the database.");

        try {
            List<Book> allBooks = bookDAO.getAllBooks();

            if (allBooks.isEmpty()) {
                logger.info("No books found during fetch operation.");
            } else {
                logger.info("Fetched " + allBooks.size() + " books from the database.");
            }
            return allBooks;

        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching all books: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch books", e);
        }
    }

    // Fetches books by title from the database
    public List<Book> getBooksByTitle(String title) throws DatabaseOperationException, InvalidDataException {
        logger.info("Fetching books by title: " + title + " from the database.");

        // Validate title before proceeding
        if(title == null || title.trim().isEmpty()){
            logger.severe("Validation failed : title cannot be empty.");
            throw new InvalidDataException("Title cannot be empty.");
        }
        try{
            List<Book> allBooks = bookDAO.getBooksByTitle(title);

            if(allBooks.isEmpty()){
                logger.info("No books found with title: " + title);
            }else{
                logger.info("Found " + allBooks.size() + " books with title: " + title);
            }
            return allBooks;

        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching books with title: " + title + ". Exception: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch books with title: " + title, e);
        }
    }

    // Fetches books by author from the database
    public List<Book> getBooksByAuthor(String author) throws DatabaseOperationException, InvalidDataException {
        logger.info("Fetching books by author: " + author + " from the database.");

        // Validate author before proceeding
        if(author == null || author.trim().isEmpty()){
            logger.severe("Validation failed : author cannot be empty.");
            throw new InvalidDataException("Author cannot be empty.");
        }
        try{
            List<Book> allBooks = bookDAO.getBooksByAuthor(author);

            if(allBooks.isEmpty()){
                logger.info("No books found by author: " + author);
            }else{
                logger.info("Found " + allBooks.size() + " books by author: " + author);
            }
            return allBooks;

        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching books with author: " + author + ". Exception: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch books with author: " + author, e);
        }
    }

    // Fetches book by author, title, and year of publication
    public Optional<Book> findBookByDetails(String title, String author, int yearOfPublication) throws DatabaseOperationException, InvalidDataException{
        logger.info("Searching for book with title: " + title + ", author: " + author + ", year: " + yearOfPublication);

        // Validate title, author and year of publication before proceeding
        validateBasicBookData(title, author, yearOfPublication);

        try{
            Optional<Book> book = bookDAO.findBookByDetails(title, author, yearOfPublication);

            if(book.isEmpty()){
                logger.info("No book found with title: " + title + ", author: " + author + ", year: " + yearOfPublication);
            }else{
                logger.info("Book found with ID: " + book.get().getId());
            }
            return book;
        }catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching book with title: " + title + ", author: " + author + " and year of publication: " + yearOfPublication + ". Exception: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch books with title: " + title + ", author: " + author + " and year of publication: " + yearOfPublication, e);
        }
    }

    // Removes a book by author, title, and year of publication
    public boolean removeBookByDetails(String title, String author, int yearOfPublication) throws DatabaseOperationException, InvalidDataException{
        logger.info("Remove book with title: " + title + ", author: " + author + ", year of publication: " + yearOfPublication);

        // Validate title, author and year of publication before proceeding
        validateBasicBookData(title, author, yearOfPublication);

        try{
            connection.setAutoCommit(false);

            Optional<Book> b = bookDAO.findBookByDetails(title, author, yearOfPublication);
            if(b.isEmpty()){
                logger.info("No book found with title: " + title + ", author: " + author + ", year: " + yearOfPublication);
                return false;
            }

            Book book = b.get();

            // Check if the book is borrowed
            if (borrowedBookDAO.isBookBorrowed(book)) {
                logger.warning("Book with title: " + title + " is currently borrowed and cannot be removed.");
                throw new DatabaseOperationException("Cannot remove book because it is currently borrowed.");
            }

            // Proceed with removing the book if not borrowed
            boolean result = bookDAO.removeBookByDetails(title, author, yearOfPublication);

            if(result){
                logger.info("Successfully removed book with title: " + title + ", author: " + author + " and year of publication: " + yearOfPublication);
                connection.commit();
            }else{
                logger.warning("Book with title: " + title + ", author: " + author + " and year of publication: " + yearOfPublication + " does not exist in the database.");
            }
            return result;
        }catch (SQLException e){
            logger.log(Level.SEVERE, "Error while removing book: " + e.getMessage(), e);
            try{
                connection.rollback();
            }catch (SQLException ex){
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to remove book", e);
        }
    }


    // Validates the book's data (title, author, year of publication, quantity, shelf location ID)
    private void validateAllBookData(String title, String author, int yearOfPublication, int quantity, int shelfLocationId) throws InvalidDataException {
        validateBasicBookData(title, author, yearOfPublication);

        if(quantity < 0){
            logger.severe("Validation failed : quantity is less than zero.");
            throw new InvalidDataException("Invalid quantity.");
        }

        if(shelfLocationId < 0){
            logger.severe("Validation failed : shelf location id is less than zero.");
            throw new InvalidDataException("Invalid shelf location id.");
        }
    }

    // Validates the book's data (title, author, year of publication)
    void validateBasicBookData(String title, String author, int yearOfPublication) throws InvalidDataException {
        if (title == null || title.trim().isEmpty()) {
            logger.severe("Validation failed: title cannot be empty.");
            throw new InvalidDataException("Title cannot be empty.");
        }

        if (author == null || author.trim().isEmpty()) {
            logger.severe("Validation failed: author cannot be empty.");
            throw new InvalidDataException("Author cannot be empty.");
        }

        int currentYear = LocalDate.now().getYear();

        if (yearOfPublication > currentYear || yearOfPublication < 0) {
            logger.severe("Validation failed: year of publication is invalid.");
            throw new InvalidDataException("Invalid year of publication.");
        }
    }
}
