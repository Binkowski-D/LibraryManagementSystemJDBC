package service;

import dao.BookLocationDAO;
import exception.DatabaseOperationException;
import exception.InvalidDataException;
import model.Book;
import model.BookLocation;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookLocationService {
    private static final Logger logger = Logger.getLogger(BookLocationService.class.getName());
    private final Connection connection;
    private final BookLocationDAO bookLocationDao;

    public BookLocationService(Connection connection){
        this.connection = connection;
        this.bookLocationDao = new BookLocationDAO(connection);
    }

    // Checks if a book location exists in the database based on BookLocation object
    public Optional<Integer> doesBookLocationExist(String section, int shelf) throws DatabaseOperationException, InvalidDataException{
        logger.info("Starting to check if book location with specific section: " + section + " and shelf: " + shelf + " exists.");

        // Validate section and shelf before proceeding
        validateBookLocationData(section, shelf);

        try{
            Optional<Integer> bookLocationId = bookLocationDao.doesBookLocationExist(section, shelf);

            if(bookLocationId.isEmpty()){
                logger.warning("Book location with specific section: " + section + " and shelf: " + shelf + " does not exist.");
            }else{
                logger.info("Book location with specific section: " + section + " and shelf: " + shelf + " exists and has ID: " + bookLocationId.get());
            }
            return bookLocationId;
        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while checking book location: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to check book location", e);
        }
    }

    // Adds a new book location after validation, manages transactions for commit/rollback
    public Optional<Integer> addLocation(BookLocation bookLocation) throws DatabaseOperationException, InvalidDataException{
        String section = bookLocation.getSection();
        int shelf = bookLocation.getShelf();
        logger.info("Adding new book location: Section " + section + ", Shelf " + shelf);

        // Validate section and shelf before proceeding
        validateBookLocationData(section, shelf);

        try{
            connection.setAutoCommit(false);
            Optional<Integer> bookLocationId = bookLocationDao.addLocation(bookLocation);

            if(bookLocationId.isPresent()){
                logger.info("New book location added with ID: " + bookLocationId.get());
                connection.commit();
            }else{
                logger.warning("Location already exists: Section " + section + ", Shelf " + shelf);
            }

            return bookLocationId;

        }catch (SQLException e){
            logger.log(Level.SEVERE, "Error while adding new book location: " + e.getMessage(), e);
            try {
                connection.rollback();
            }catch (SQLException ex){
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to add new book location", e);
        }
    }

    // Fetches all book locations from the database
    public List<BookLocation> getAllBookLocations() throws DatabaseOperationException {
        logger.info("Starting to fetch all book locations from the database.");

        try {
            List<BookLocation> allBookLocations = bookLocationDao.getAllBookLocations();

            if (allBookLocations.isEmpty()) {
                logger.info("No book locations found during fetch operation.");
            } else {
                logger.info("Found " + allBookLocations.size() + " book locations.");
            }
            return allBookLocations;

        } catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching all book locations: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch book locations", e);
        }
    }

    // Fetches the book location of a book by its shelfLocationID using a Book object
    public Optional<BookLocation> getLocationByBookShelfLocationID(Book book) throws DatabaseOperationException, InvalidDataException {
        int shelfLocationId = book.getLocation().getId();
        logger.info("Searching for book location with shelf location ID: " + shelfLocationId);

        // Validate book's shelf location before proceeding
        validateShelfLocationId(shelfLocationId);

        try{
            Optional<BookLocation> bookLocation = bookLocationDao.getLocationByBookShelfLocationID(book);
            if(bookLocation.isEmpty()){
                logger.info("No location found with shelf location ID: " + shelfLocationId);
            }else{
                logger.info("Book location found with section: " + bookLocation.get().getSection() + " and shelf: " + bookLocation.get().getShelf());
            }
            return bookLocation;
        }catch (DatabaseOperationException e) {
            logger.log(Level.SEVERE, "Error while fetching book location with book's shelf location ID: " + shelfLocationId, e);
            throw new DatabaseOperationException("Failed to fetch book location with book's shelf locatio ID: " + shelfLocationId, e);
        }
    }

    // Removes a book location by section and shelf
    public boolean removeBookLocation(String section, int shelf) throws DatabaseOperationException, InvalidDataException {
        logger.info("Removing book location: Section " + section + ", Shelf " + shelf);

        // Validate section and shelf before proceeding
        validateBookLocationData(section, shelf);
        try{
            connection.setAutoCommit(false);

            boolean isBookConnectedWithLocation = bookLocationDao.isAnyBookInLocation(section, shelf);
            if(isBookConnectedWithLocation){
                logger.warning("There is a book in this location: " + section + " " + shelf + ". We cannot remove this location.");
                throw new DatabaseOperationException("There is a book in this location: " + section + " " + shelf + ". We cannot remove this location.");
            }

            boolean result = bookLocationDao.removeBookLocation(section, shelf);

            if(result){
                logger.info("Successfully removed location: Section " + section + ", Shelf " + shelf);
                connection.commit();
            }else{
                logger.warning("Book location with section: " + section + " and shelf: " + shelf + " does not exist in the database.");
            }
            return result;
        }catch (SQLException e){
            logger.log(Level.SEVERE, "Error while removing book location: " + e.getMessage(), e);
            try{
                connection.rollback();
            }catch (SQLException ex){
                logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
            }
            throw new DatabaseOperationException("Failed to remove book location", e);
        }
    }

    // Validates the reader's data (first name, last name, date of birth)
    private void validateBookLocationData(String section, int shelf) throws InvalidDataException {
        if(section == null || section.trim().isEmpty()){
            logger.severe("Validation failed: Section cannot be empty.");
            throw new InvalidDataException("Section cannot be empty.");
        }

        if(shelf <= 0){
            logger.severe("Validation failed : Shelf number is less than zero.");
            throw new InvalidDataException("Shelf number is less than zero.");
        }

    }

    // Validates the book's shelf location id
    private void validateShelfLocationId(int shelfLocationId) throws InvalidDataException {
        if (shelfLocationId <= 0) {
            logger.severe("Validation failed: Shelf location ID must be greater than zero.");
            throw new InvalidDataException("Invalid shelf location ID.");
        }
    }

}
