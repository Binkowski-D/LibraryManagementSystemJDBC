package service;

import dao.BookDAO;
import dao.BorrowedBookDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.ReaderDAO;
import exception.DatabaseOperationException;
import exception.InvalidDataException;
import model.Book;
import model.Reader;

public class BorrowedBookService {

    private static final Logger logger = Logger.getLogger(BorrowedBookService.class.getName());
    private final Connection connection;
    private final BorrowedBookDAO borrowedBookDAO;
    private final BookDAO bookDAO;
    private final ReaderDAO readerDAO;


    public BorrowedBookService(Connection connection){
        this.connection = connection;
        this.borrowedBookDAO = new BorrowedBookDAO(connection);
        this.bookDAO = new BookDAO(connection);
        this.readerDAO = new ReaderDAO(connection);
    }

    //Adds a new borrow to the database
    public Optional<Integer> addBorrowedBook(Reader reader, Book book) throws DatabaseOperationException, InvalidDataException{
        String firstName = reader.getFirstName();
        String lastName = reader.getLastName();
        LocalDate dateOfBirth = reader.getDateOfBirth();
        String title = book.getTitle();
        String author = book.getAuthor();
        int yearOfPublication = book.getYearOfPublication();

        logger.info("Starting the process of borrowing a book: " + title);

        // Validate reader's data before proceeding
        validateReaderData(firstName, lastName, dateOfBirth);

        // Validate book's data before proceeding
        validateBasicBookData(title, author, yearOfPublication);

        try{
            connection.setAutoCommit(false);

            // Check if the book is available and fetch it
            Book existingBook = checkBookAvailability(book);

            // Check if the reader exists or add a new one
            Reader updatedReader = checkAndAddReaderIfNecessary(reader);

            // Check if the reader has any overdue loans
            if (borrowedBookDAO.hasOverdueLoans(updatedReader)) {
                logger.warning("Reader has overdue loans and cannot borrow a new book.");
                throw new InvalidDataException("Reader has overdue loans and cannot borrow a new book.");
            }

            // Try to add the borrowed book
            Optional<Integer> borrowId = borrowedBookDAO.addBorrowedBook(updatedReader, existingBook);
            if (borrowId.isPresent()) {
                // Decrease the quantity of available books by 1
                bookDAO.decreaseBookQuantity(existingBook, 1);
                connection.commit(); // Commit the transaction
                logger.info("Book borrowed successfully with borrow ID: " + borrowId.get());
                return borrowId;
            } else {
                logger.warning("Failed to borrow the book. The reader may have already borrowed this book.");
                connection.rollback(); // Rollback if the book was already borrowed
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error occurred during borrowing process: " + e.getMessage(), e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to borrow the book.", e);
        }
    }

    // Fetches all books borrowed by a specific reader with borrow and return dates
    public List<String> getBooksBorrowedByReaderWithDates(Reader reader) throws DatabaseOperationException, InvalidDataException{
        logger.info("Fetching books borrowed by reader: " + reader.getFirstName() + " " + reader.getLastName());

        // Validate reader's data before proceeding
        validateReaderData(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        try {
            List<String> allBorrowedBooks = borrowedBookDAO.getBooksBorrowedByReaderWithDates(reader);

            if (allBorrowedBooks.isEmpty()) {
                logger.info("No borrowed books found for reader: " + reader.getFirstName() + " " + reader.getLastName());
            } else {
                logger.info("Fetched " + allBorrowedBooks.size() + " borrowed books for reader: " + reader.getFirstName() + " " + reader.getLastName());
            }
            return allBorrowedBooks;

        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching borrowed books for reader: " + reader.getFirstName() + " " + reader.getLastName() + ". " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch borrowed books", e);
        }

    }

    // Fetches all readers who are overdue
    public List<String> getOverdueReaders() throws DatabaseOperationException {
        logger.info("Fetching list of overdue readers.");

        try{
            List<String> overdueReaders = borrowedBookDAO.getOverdueReaders();

            if(overdueReaders.isEmpty()){
                logger.info("No overdue readers found.");
            }else{
                logger.info("Fetched " + overdueReaders.size() + " overdue readers.");
            }

            return overdueReaders;

        }catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching overdue readers from the database: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch overdue readers", e);
        }
    }

    // Checks if specific reader has overdue loans
    public boolean hasOverdueLoans(Reader reader) throws DatabaseOperationException, InvalidDataException {
        logger.info("Checking if reader: " + reader.getFirstName() + " " + reader.getLastName() + " has overdue loans.");

        // Validate reader's data before proceeding
        validateReaderData(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        try {
            boolean hasOverdue = borrowedBookDAO.hasOverdueLoans(reader);
            logger.info("Reader " + reader.getFirstName() + " " + reader.getLastName() +
                    (hasOverdue ? " has overdue loans." : " does not have overdue loans."));
            return hasOverdue;

        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while checking if reader has overdue loans: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to check overdue loans", e);
        }
    }

    // Method to remove a borrowed book
    public boolean returnBorrowedBook(Reader reader, Book book) throws DatabaseOperationException, InvalidDataException {
        logger.info("Returning book: " + book.getTitle() + " for reader: " + reader.getFirstName() + " " + reader.getLastName());

        // Validate reader's data before proceeding
        validateReaderData(reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth());

        // Validate book's data before proceeding
        validateBasicBookData(book.getTitle(), book.getAuthor(), book.getYearOfPublication());

        try {
            connection.setAutoCommit(false);

            // Check if the reader exists
            Optional<Integer> existingReader = readerDAO.doesReaderExist(reader);
            if (existingReader.isEmpty()) {
                logger.warning("Reader " + reader.getFirstName() + " " + reader.getLastName() + " does not exist in the database.");
                throw new DatabaseOperationException("Reader does not exist in the database.");
            }

            // Check if the book exists in the database
            Optional<Book> foundBook = bookDAO.findBookByDetails(book.getTitle(), book.getAuthor(), book.getYearOfPublication());
            if (foundBook.isEmpty()) {
                logger.warning("Book does not exist in the database.");
                throw new DatabaseOperationException("Book does not exist in the database.");
            }

            // Check if the reader has borrowed this book
            Optional<Integer> borrowedBook = borrowedBookDAO.findBorrowedBookIdByReaderAndBook(reader, foundBook.get());
            if (borrowedBook.isEmpty()) {
                logger.warning("No loan found in the database.");
                throw new DatabaseOperationException("No loan found in the database.");
            }

            // Remove the borrowed book
            boolean result = borrowedBookDAO.removeBorrowedBook(reader, foundBook.get());
            if (result) {
                logger.info("Successfully returned book " + book.getTitle() + " by " + book.getAuthor());
                bookDAO.increaseBookQuantity(foundBook.get(), 1);
                connection.commit();
                return true; // Return true when the book is successfully returned
            } else {
                logger.warning("Return operation failed.");
                connection.rollback();
                return false; // Return false if the operation failed
            }

        } catch (SQLException e) {
            logger.warning("Return operation failed due to: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
                throw new DatabaseOperationException("Rollback failed during return operation.", ex); // Rethrow exception on rollback failure
            }
            throw new DatabaseOperationException("Failed to return the book.", e);
        }
    }


    // Method to check if a book is available
    private Book checkBookAvailability(Book book) throws DatabaseOperationException, InvalidDataException {
        String title = book.getTitle();
        String author = book.getAuthor();
        int yearOfPublication = book.getYearOfPublication();

        Optional<Book> foundBook = bookDAO.findBookByDetails(title, author, yearOfPublication);
        if (foundBook.isEmpty()) {
            logger.warning("Book does not exist in the database.");
            throw new InvalidDataException("Book does not exist in the database.");
        }

        Book existingBook = foundBook.get();
        if (existingBook.getQuantity() <= 0) {
            logger.warning("Not enough copies of the book are available.");
            throw new InvalidDataException("Not enough copies of the book are available.");
        }

        return existingBook;
    }

    // Method to check if the reader exists or add a new reader if necessary
    private Reader checkAndAddReaderIfNecessary(Reader reader) throws DatabaseOperationException{
        Optional<Integer> existingReaderId = readerDAO.doesReaderExist(reader);

        if (existingReaderId.isEmpty()) {
            logger.info("Reader does not exist. Adding new reader.");
            Optional<Integer> readerID = readerDAO.addReader(reader);
            return new Reader(readerID.get(), reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth()); // Return reader with ID
        } else {
            logger.info("Reader exists with ID: " + existingReaderId.get());
            return new Reader(existingReaderId.get(), reader.getFirstName(), reader.getLastName(), reader.getDateOfBirth()); // Return reader with existing ID
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

    // Validates the reader's data (first name, last name, date of birth)
    private void validateReaderData(String firstName, String lastName, LocalDate dateOfBirth) throws InvalidDataException {
        if (firstName == null || firstName.trim().isEmpty()) {
            logger.severe("Validation failed: First name cannot be empty.");
            throw new InvalidDataException("First name cannot be empty.");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            logger.severe("Validation failed: Last name cannot be empty.");
            throw new InvalidDataException("Last name cannot be empty.");
        }

        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            logger.severe("Validation failed : Date of birth is invalid.");
            throw new InvalidDataException("Invalid date of birth.");
        }
    }
}