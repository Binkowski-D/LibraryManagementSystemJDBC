package service;

import dao.BorrowedBookDAO;
import dao.ReaderDAO;
import exception.DatabaseOperationException;
import exception.InvalidDataException;
import model.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReaderService {
    private static final Logger logger = Logger.getLogger(ReaderService.class.getName());
    private final Connection connection;
    private final ReaderDAO readerDAO;
    private final BorrowedBookDAO borrowedBookDAO;

    public ReaderService(Connection connection){
        this.connection = connection;
        this.readerDAO = new ReaderDAO(connection);
        this.borrowedBookDAO = new BorrowedBookDAO(connection);
    }

    // Adds a new reader after validation, manages transactions for commit/rollback
    public Optional<Integer> addReader(String firstName, String lastName, LocalDate dateOfBirth) throws DatabaseOperationException, InvalidDataException{
        logger.info("Starting to add a new reader: " + firstName + " " + lastName + " " + dateOfBirth);

        validateReaderData(firstName, lastName, dateOfBirth);
        Reader reader = new Reader(firstName, lastName, dateOfBirth);

        try{
            connection.setAutoCommit(false); // Disable auto-commit
            Optional<Integer> readerId = readerDAO.addReader(reader);

            if(readerId.isPresent()){
                logger.info("New reader added with ID: " + readerId.get());
                connection.commit();
            }else{
                logger.warning("Reader " + firstName + " " + lastName + " already exists.");
            }

            return readerId;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error while adding new reader: " + e.getMessage(), e);
            try{
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to add new reader", e);
        }
    }

    // Fetches all readers from the database
    public List<Reader> getAllReaders() throws DatabaseOperationException {
        logger.info("Starting to fetch all readers from the database.");

        try {
            List<Reader> allReaders = readerDAO.getAllReaders();

            if (allReaders.isEmpty()) {
                logger.info("No readers found in the database.");
            } else {
                logger.info("Successfully fetched " + allReaders.size() + " readers.");
            }
            return allReaders;

        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching all readers: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch readers", e);
        }
    }

    // Fetches readers by last name
    public List<Reader> getReadersByLastName(String lastName) throws DatabaseOperationException, InvalidDataException {
        logger.info("Starting to fetch all readers with last name: " + lastName + " from the database.");

        // Validate last name before proceeding
        validateLastName(lastName);

        try{
            List<Reader> allReaders = readerDAO.getReadersByLastName(lastName);

            if(allReaders.isEmpty()){
                logger.info("No readers found with last name: " + lastName + " in the database.");
            }else{
                logger.info("Successfully fetched " + allReaders.size() + " readers with last name: " + lastName);
            }
            return allReaders;

        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching readers with last name: " + lastName + ". Exception: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch readers with last name: " + lastName, e);
        }
    }

    // Removes a reader by first name, last name and date of birth, handles commit/rollback for transactions
    public boolean removeReaderByDetails(String firstName, String lastName, LocalDate dateOfBirth) throws DatabaseOperationException, InvalidDataException {
        logger.info("Remove reader by details: " + firstName + " " + lastName + " " + dateOfBirth);

        validateReaderData(firstName, lastName, dateOfBirth);

        try{
            connection.setAutoCommit(false);

            Reader reader = new Reader(firstName, lastName, dateOfBirth);

            Optional<Integer> readerId = readerDAO.doesReaderExist(reader);
            if (readerId.isEmpty()) {
                logger.warning("Reader does not exist in the database: " + firstName + " " + lastName + " " + dateOfBirth);
                throw new DatabaseOperationException("Reader does not exist in the database: " + firstName + " " + lastName + " " + dateOfBirth);
            }

            Reader readerWithId = new Reader(readerId.get(), firstName, lastName, dateOfBirth);

            // Check if the reader has any borrowed books
            List<String> borrowedBooks = borrowedBookDAO.getBooksBorrowedByReaderWithDates(readerWithId);
            if(!borrowedBooks.isEmpty()){
                logger.warning("Reader " + firstName + " " + lastName + " cannot be removed because they have borrowed books.");
                throw new DatabaseOperationException("Reader " + firstName + " " + lastName + " cannot be removed because they have borrowed books.");
            }

            boolean result = readerDAO.removeReaderById(readerWithId);

            if(result){
                logger.info("Successfully removed reader: " + firstName + " " + lastName + " " + dateOfBirth);
                connection.commit();

            }else{
                logger.warning("Failed to remove reader: " + firstName + " " + lastName + " " + dateOfBirth);
            }

            return result;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error while removing reader: " + e.getMessage(), e);
            try{
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to remove reader", e);
        }
    }

    // Validates the reader's data (first name, last name, date of birth)
    void validateReaderData(String firstName, String lastName, LocalDate dateOfBirth) throws InvalidDataException{
        if(firstName == null || firstName.trim().isEmpty()){
            logger.severe("Validation failed: First name cannot be empty.");
            throw new InvalidDataException("First name cannot be empty.");
        }

        validateLastName(lastName);

        if(dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())){
            logger.severe("Validation failed : Date of birth is invalid.");
            throw new InvalidDataException("Invalid date of birth.");
        }

    }

    // Validates last name for fetching readers by last name
    private void validateLastName(String lastName) throws InvalidDataException{
        if(lastName == null || lastName.trim().isEmpty()){
            logger.severe("Validation failed : last name cannot be empty.");
            throw new InvalidDataException("Last name cannot be empty.");
        }
    }
}
