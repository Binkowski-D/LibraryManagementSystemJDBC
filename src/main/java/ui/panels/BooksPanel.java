package ui.panels;

import exception.DatabaseOperationException;
import exception.InvalidDataException;
import model.BookLocation;
import service.BookLocationService;
import service.BookService;
import ui.MainWindow;
import ui.util.BackgroundPanel;
import model.Book;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class BooksPanel extends BackgroundPanel {
    private final BookLocationService bookLocationService;
    private final BookService bookService;

    public BooksPanel(String imagePath, Connection connection){
        super(imagePath);
        this.bookLocationService = new BookLocationService(connection);
        this.bookService = new BookService(connection);
        setLayout(null);

        int buttonWidth = 224;
        int buttonHeight = 50;

        JButton addBookButton = createButton("Add Book");
        addBookButton.setBounds(400, 250, buttonWidth, buttonHeight);
        addBookButton.addActionListener(e -> showAddBookForm());
        add(addBookButton);

        JButton increaseQuantityButton = createButton("Increase Quantity");
        increaseQuantityButton.setBounds(400, 310, buttonWidth, buttonHeight);
        increaseQuantityButton.addActionListener(e -> showIncreaseQuantityForm());
        add(increaseQuantityButton);

        JButton decreaseQuantityButton = createButton("Decrease Quantity");
        decreaseQuantityButton.setBounds(400, 370, buttonWidth, buttonHeight);
        decreaseQuantityButton.addActionListener(e -> showDecreaseQuantitykForm());
        add(decreaseQuantityButton);

        JButton getAllBooksButton = createButton("Get All Books");
        getAllBooksButton.setBounds(400, 430, buttonWidth, buttonHeight);
        getAllBooksButton.addActionListener(e -> showAllBooksForm());
        add(getAllBooksButton);

        JButton getBooksByTitleButton = createButton("Get Books By Title");
        getBooksByTitleButton.setBounds(400, 490, buttonWidth, buttonHeight);
        getBooksByTitleButton.addActionListener(e -> showFindBooksByTitleForm());
        add(getBooksByTitleButton);

        JButton getBooksByAuthorButton = createButton("Get Books By Author");
        getBooksByAuthorButton.setBounds(400, 550, buttonWidth, buttonHeight);
        getBooksByAuthorButton.addActionListener(e -> showFindBooksByAuthorForm());
        add(getBooksByAuthorButton);

        JButton findBookByDetailsButton = createButton("Find Book By Details");
        findBookByDetailsButton.setBounds(400, 610, buttonWidth, buttonHeight);
        findBookByDetailsButton.addActionListener(e -> showFindBookByDetailsForm());
        add(findBookByDetailsButton);

        JButton removeBookByDetailsButton = createButton("Remove Book");
        removeBookByDetailsButton.setBounds(400, 670, buttonWidth, buttonHeight);
        removeBookByDetailsButton.addActionListener(e -> showRemoveBookForm());
        add(removeBookByDetailsButton);

        JButton backButton = createButton("BACK");
        backButton.setBounds(270, 867, buttonWidth, buttonHeight);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(backButton).dispose();
            MainWindow.showMainWindow();
        });
        add(backButton);

        JButton exitButton = createButton("EXIT");
        exitButton.setBounds(550, 867, buttonWidth, buttonHeight);
        exitButton.setBorderPainted(false);
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Exit the program?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        add(exitButton);
    }

    private void showAddBookForm() {
        JFrame frame = new JFrame("Add New Book");
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setBounds(50, 30, 100, 30);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(150, 30, 200, 30);
        frame.add(titleField);

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setBounds(50, 80, 100, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(150, 80, 200, 30);
        frame.add(authorField);

        JLabel yearLabel = new JLabel("Pub. Year:");
        yearLabel.setBounds(50, 130, 100, 30);
        frame.add(yearLabel);

        JTextField yearField = new JTextField();
        yearField.setBounds(150, 130, 200, 30);
        frame.add(yearField);

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(50, 180, 100, 30);
        frame.add(quantityLabel);

        JTextField quantityField = new JTextField();
        quantityField.setBounds(150, 180, 200, 30);
        frame.add(quantityField);

        JLabel sectionLabel = new JLabel("Section:");
        sectionLabel.setBounds(50, 230, 100, 30);
        frame.add(sectionLabel);

        JTextField sectionField = new JTextField();
        sectionField.setBounds(150, 230, 200, 30);
        frame.add(sectionField);

        JLabel shelfLabel = new JLabel("Shelf:");
        shelfLabel.setBounds(50, 280, 100, 30);
        frame.add(shelfLabel);

        JTextField shelfField = new JTextField();
        shelfField.setBounds(150, 280, 200, 30);
        frame.add(shelfField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 330, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e -> {
            String title = titleField.getText();
            String author = authorField.getText();
            String yearString = yearField.getText();
            String quantityString = quantityField.getText();
            String section = sectionField.getText();
            String shelfString = shelfField.getText();

            try {
                int year = Integer.parseInt(yearString);
                int quantity = Integer.parseInt(quantityString);
                int shelf = Integer.parseInt(shelfString);

                Optional<Integer> locationId = bookLocationService.doesBookLocationExist(section, shelf);

                BookLocation location;
                if (locationId.isPresent()) {
                    location = new BookLocation(locationId.get(), section, shelf);
                } else {
                    location = new BookLocation(section, shelf);
                    Optional<Integer> newLocationId = bookLocationService.addLocation(location);
                    if (newLocationId.isEmpty()) {
                        throw new DatabaseOperationException("Failed to add new book location");
                    }
                    location = new BookLocation(newLocationId.get(), section, shelf);
                }

                bookService.addBook(title, author, year, quantity, location);
                JOptionPane.showMessageDialog(frame, "Book added successfully!");
                frame.dispose();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Invalid number format. Check year, quantity, and shelf values.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to add book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showIncreaseQuantityForm(){
        JFrame frame = new JFrame("Increase Quantity");
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setBounds(50, 30, 100, 30);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(150, 30, 200, 30);
        frame.add(titleField);

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setBounds(50, 80, 100, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(150, 80, 200, 30);
        frame.add(authorField);

        JLabel yearLabel = new JLabel("Pub. Year:");
        yearLabel.setBounds(50, 130, 100, 30);
        frame.add(yearLabel);

        JTextField yearField = new JTextField();
        yearField.setBounds(150, 130, 200, 30);
        frame.add(yearField);

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(50, 180, 100, 30);
        frame.add(quantityLabel);

        JTextField quantityField = new JTextField();
        quantityField.setBounds(150, 180, 200, 30);
        frame.add(quantityField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 330, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e -> {
            String title = titleField.getText();
            String author = authorField.getText();
            String yearString = yearField.getText();
            String quantityString = quantityField.getText();

            try{
                int year = Integer.parseInt(yearString);
                int quantity = Integer.parseInt(quantityString);

                Book book = new Book(title, author, year);

                boolean result = bookService.increaseBookQuantity(book, quantity);
                if (result) {
                    JOptionPane.showMessageDialog(frame, "Book quantity increased successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Book does not exist in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                }

                frame.dispose();

            }catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Invalid number format. Check year and quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DatabaseOperationException | InvalidDataException ex) {
                JOptionPane.showMessageDialog(frame, "Failed to increase book quantity: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showDecreaseQuantitykForm(){
        JFrame frame = new JFrame("Decrease Quantity");
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setBounds(50, 30, 100, 30);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(150, 30, 200, 30);
        frame.add(titleField);

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setBounds(50, 80, 100, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(150, 80, 200, 30);
        frame.add(authorField);

        JLabel yearLabel = new JLabel("Pub. Year:");
        yearLabel.setBounds(50, 130, 100, 30);
        frame.add(yearLabel);

        JTextField yearField = new JTextField();
        yearField.setBounds(150, 130, 200, 30);
        frame.add(yearField);

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(50, 180, 100, 30);
        frame.add(quantityLabel);

        JTextField quantityField = new JTextField();
        quantityField.setBounds(150, 180, 200, 30);
        frame.add(quantityField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 330, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e -> {
            String title = titleField.getText();
            String author = authorField.getText();
            String yearString = yearField.getText();
            String quantityString = quantityField.getText();

            try{
                int year = Integer.parseInt(yearString);
                int quantity = Integer.parseInt(quantityString);

                Book book = new Book(title, author, year);

                boolean result = bookService.decreaseBookQuantity(book, quantity);
                if (result) {
                    JOptionPane.showMessageDialog(frame, "Book quantity decreased successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Book does not exist in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                }

                frame.dispose();

            }catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Invalid number format. Check year and quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DatabaseOperationException | InvalidDataException ex) {
                JOptionPane.showMessageDialog(frame, "Failed to decrease book quantity: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showAllBooksForm() {
        JFrame frame = new JFrame("All Books");
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        try {
            List<Book> books = bookService.getAllBooks();

            String[] columnNames = {"ID", "Title", "Author", "Pub. Year", "Quantity", "Loc. ID", "Section", "Shelf"};
            Object[][] data = new Object[books.size()][8];

            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                data[i][0] = book.getId();
                data[i][1] = book.getTitle();
                data[i][2] = book.getAuthor();
                data[i][3] = book.getYearOfPublication();
                data[i][4] = book.getQuantity();
                data[i][5] = book.getLocation().getId();
                data[i][6] = book.getLocation().getSection();
                data[i][7] = book.getLocation().getShelf();
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);
            frame.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to fetch books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        frame.setVisible(true);
    }

    private void showFindBooksByTitleForm() {
        JFrame frame = new JFrame("Find Books by Title");
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setBounds(50, 30, 100, 30);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(150, 30, 200, 30);
        frame.add(titleField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 80, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e-> {
            String title = titleField.getText();

            try {
                List<Book> books = bookService.getBooksByTitle(title);

                if (books.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No books found with the title: " + title, "No Results", JOptionPane.INFORMATION_MESSAGE);
                } else {

                    JFrame resultsFrame = new JFrame("Books with title: " + title);
                    resultsFrame.setSize(800, 400);
                    resultsFrame.setLocationRelativeTo(null);
                    resultsFrame.setLayout(new BorderLayout());

                    resultsFrame.setIconImage(icon.getImage());

                    String[] columnNames = {"ID", "Title", "Author", "Pub. Year", "Quantity", "Loc. ID", "Section", "Shelf"};
                    Object[][] data = new Object[books.size()][8];

                    for (int i = 0; i < books.size(); i++) {
                        Book book = books.get(i);
                        data[i][0] = book.getId();
                        data[i][1] = book.getTitle();
                        data[i][2] = book.getAuthor();
                        data[i][3] = book.getYearOfPublication();
                        data[i][4] = book.getQuantity();
                        data[i][5] = book.getLocation().getId();
                        data[i][6] = book.getLocation().getSection();
                        data[i][7] = book.getLocation().getShelf();
                    }

                    JTable table = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(table);
                    resultsFrame.add(scrollPane, BorderLayout.CENTER);

                    resultsFrame.setVisible(true);

                }
                frame.dispose();
            }catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to find books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showFindBooksByAuthorForm() {
        JFrame frame = new JFrame("Find Books by Author");
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setBounds(50, 30, 100, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(150, 30, 200, 30);
        frame.add(authorField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 80, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e-> {
            String author = authorField.getText();

            try {
                List<Book> books = bookService.getBooksByAuthor(author);

                if (books.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No books found with the author: " + author, "No Results", JOptionPane.INFORMATION_MESSAGE);
                } else {

                    JFrame resultsFrame = new JFrame("Books with author: " + author);
                    resultsFrame.setSize(800, 400);
                    resultsFrame.setLocationRelativeTo(null);
                    resultsFrame.setLayout(new BorderLayout());

                    resultsFrame.setIconImage(icon.getImage());

                    String[] columnNames = {"ID", "Title", "Author", "Pub. Year", "Quantity", "Loc. ID", "Section", "Shelf"};
                    Object[][] data = new Object[books.size()][8];

                    for (int i = 0; i < books.size(); i++) {
                        Book book = books.get(i);
                        data[i][0] = book.getId();
                        data[i][1] = book.getTitle();
                        data[i][2] = book.getAuthor();
                        data[i][3] = book.getYearOfPublication();
                        data[i][4] = book.getQuantity();
                        data[i][5] = book.getLocation().getId();
                        data[i][6] = book.getLocation().getSection();
                        data[i][7] = book.getLocation().getShelf();
                    }

                    JTable table = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(table);
                    resultsFrame.add(scrollPane, BorderLayout.CENTER);

                    resultsFrame.setVisible(true);

                }
                frame.dispose();
            }catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to find books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showFindBookByDetailsForm() {
        JFrame frame = new JFrame("Find Book by Details");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setBounds(50, 30, 100,30);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(150, 30, 200, 30);
        frame.add(titleField);

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setBounds(50, 80, 100, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(150, 80, 200, 30);
        frame.add(authorField);

        JLabel publicationYearLabel = new JLabel("Pub. Year:");
        publicationYearLabel.setBounds(50, 130, 100, 30);
        frame.add(publicationYearLabel);

        JTextField publicationField = new JTextField();
        publicationField.setBounds(150, 130, 200, 30);
        frame.add(publicationField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 180, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e-> {
            String title = titleField.getText();
            String author = authorField.getText();
            String yearOfPublication = publicationField.getText();

            try {
                int yOp = Integer.parseInt(yearOfPublication);

                Optional<Book> foundBook = bookService.findBookByDetails(title, author, yOp);

                if (foundBook.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No book found with the title: " + title + " , author: " + author + " and year of publication " + yOp, "No Results", JOptionPane.INFORMATION_MESSAGE);
                } else {

                    JFrame resultsFrame = new JFrame("Book with details: " + title + " " + author + " " + yOp);
                    resultsFrame.setSize(800, 400);
                    resultsFrame.setLocationRelativeTo(null);
                    resultsFrame.setLayout(new BorderLayout());

                    resultsFrame.setIconImage(icon.getImage());

                    String[] columnNames = {"ID", "Title", "Author", "Pub. Year", "Quantity", "Loc. ID", "Section", "Shelf"};
                    Object[][] data = new Object[1][8];

                        Book book = foundBook.get();
                        data[0][0] = book.getId();
                        data[0][1] = book.getTitle();
                        data[0][2] = book.getAuthor();
                        data[0][3] = book.getYearOfPublication();
                        data[0][4] = book.getQuantity();
                        data[0][5] = book.getLocation().getId();
                        data[0][6] = book.getLocation().getSection();
                        data[0][7] = book.getLocation().getShelf();


                    JTable table = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(table);
                    resultsFrame.add(scrollPane, BorderLayout.CENTER);

                    resultsFrame.setVisible(true);

                }
                frame.dispose();
            }catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Invalid year format. Please enter a valid number for the publication year.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to find book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showRemoveBookForm() {
        JFrame frame = new JFrame("Remove Book");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setBounds(50, 30, 100,30);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(150, 30, 200, 30);
        frame.add(titleField);

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setBounds(50, 80, 100, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(150, 80, 200, 30);
        frame.add(authorField);

        JLabel publicationYearLabel = new JLabel("Pub. Year:");
        publicationYearLabel.setBounds(50, 130, 100, 30);
        frame.add(publicationYearLabel);

        JTextField publicationField = new JTextField();
        publicationField.setBounds(150, 130, 200, 30);
        frame.add(publicationField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 180, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e-> {
            String title = titleField.getText();
            String author = authorField.getText();
            String yearOfPublication = publicationField.getText();

            int confirmation = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to remove this book?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
            );

            if(confirmation == JOptionPane.YES_OPTION) {
                try {
                    int yOp = Integer.parseInt(yearOfPublication);

                    boolean result = bookService.removeBookByDetails(title, author, yOp);

                    if (result) {
                        JOptionPane.showMessageDialog(frame, "Book removed successfully.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Book not found or could not be removed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    frame.dispose();

                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(frame, "Invalid year format. Please enter a valid number for the publication year.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to remove book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setVisible(true);
    }

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }
}
