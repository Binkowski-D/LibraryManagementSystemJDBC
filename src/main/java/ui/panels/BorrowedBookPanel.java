package ui.panels;

import model.Book;
import model.Reader;
import service.BorrowedBookService;
import ui.MainWindow;
import ui.util.BackgroundPanel;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class BorrowedBookPanel extends BackgroundPanel {
    private final BorrowedBookService borrowedBookService;
    public BorrowedBookPanel(String imagePath, Connection connection){
        super(imagePath);
        this.borrowedBookService = new BorrowedBookService(connection);
        setLayout(null);

        int buttonWidth = 224;
        int buttonHeight = 50;

        JButton borrowBookButton = createButton("Borrow Book");
        borrowBookButton.setBounds(400, 397, buttonWidth, buttonHeight);
        borrowBookButton.addActionListener(e -> showBorrowBookForm());
        add(borrowBookButton);

        JButton returnBookButton = createButton("Return Book");
        returnBookButton.setBounds(400, 457, buttonWidth, buttonHeight);
        returnBookButton.addActionListener(e -> showReturnBookForm());
        add(returnBookButton);

        JButton getBorrowedBooksButton = createButton("Get Borrowed Books By Reader");
        getBorrowedBooksButton.setBounds(400, 517, buttonWidth, buttonHeight);
        getBorrowedBooksButton.addActionListener(e -> showBorrowedBooksByReaderForm());
        add(getBorrowedBooksButton);


        JButton getOverdueReadersButton = createButton("Get Overdue Readers");
        getOverdueReadersButton.setBounds(400, 577, buttonWidth, buttonHeight);
        getOverdueReadersButton.addActionListener(e -> showOverdueReadersForm());
        add(getOverdueReadersButton);


        JButton checkOverdueLoansButton = createButton("Check Reader Overdue Loans");
        checkOverdueLoansButton.setBounds(400, 637, buttonWidth, buttonHeight);
        checkOverdueLoansButton.addActionListener(e -> showCheckOverdueLoansForm());
        add(checkOverdueLoansButton);


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

    private void showBorrowBookForm() {
        JFrame frame = new JFrame("Borrow Book");
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel firstNameLabel = new JLabel("Reader First Name:");
        firstNameLabel.setBounds(50, 30, 150, 30);
        frame.add(firstNameLabel);

        JTextField firstNameField = new JTextField();
        firstNameField.setBounds(200, 30, 150, 30);
        frame.add(firstNameField);

        JLabel lastNameLabel = new JLabel("Reader Last Name:");
        lastNameLabel.setBounds(50, 80, 150, 30);
        frame.add(lastNameLabel);

        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(200, 80, 150, 30);
        frame.add(lastNameField);

        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setBounds(50, 130, 150, 30);
        frame.add(dobLabel);

        JTextField dobField = new JTextField("YYYY-MM-DD");
        dobField.setBounds(200, 130, 150, 30);
        frame.add(dobField);

        JLabel titleLabel = new JLabel("Book Title:");
        titleLabel.setBounds(50, 180, 150, 30);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(200, 180, 150, 30);
        frame.add(titleField);

        JLabel authorLabel = new JLabel("Book Author:");
        authorLabel.setBounds(50, 230, 150, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(200, 230, 150, 30);
        frame.add(authorField);

        JLabel yearLabel = new JLabel("Year of Publication:");
        yearLabel.setBounds(50, 280, 150, 30);
        frame.add(yearLabel);

        JTextField yearField = new JTextField();
        yearField.setBounds(200, 280, 150, 30);
        frame.add(yearField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 350, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            LocalDate dob = LocalDate.parse(dobField.getText());
            String title = titleField.getText();
            String author = authorField.getText();
            int yearOfPublication;

            try {
                yearOfPublication = Integer.parseInt(yearField.getText());

                Reader reader = new Reader(firstName, lastName, dob);
                Book book = new Book(title, author, yearOfPublication);

                Optional<Integer> borrowId = borrowedBookService.addBorrowedBook(reader, book);
                if (borrowId.isPresent()) {
                    JOptionPane.showMessageDialog(frame, "Book borrowed successfully with ID: " + borrowId.get());
                } else {
                    JOptionPane.showMessageDialog(frame, "Book is already borrowed by this reader.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                frame.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Year of publication must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to borrow book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame.setVisible(true);
    }

    private void showReturnBookForm() {
        JFrame frame = new JFrame("Return Book");
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel firstNameLabel = new JLabel("Reader First Name:");
        firstNameLabel.setBounds(50, 30, 150, 30);
        frame.add(firstNameLabel);

        JTextField firstNameField = new JTextField();
        firstNameField.setBounds(200, 30, 150, 30);
        frame.add(firstNameField);

        JLabel lastNameLabel = new JLabel("Reader Last Name:");
        lastNameLabel.setBounds(50, 80, 150, 30);
        frame.add(lastNameLabel);

        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(200, 80, 150, 30);
        frame.add(lastNameField);

        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setBounds(50, 130, 150, 30);
        frame.add(dobLabel);

        JTextField dobField = new JTextField("YYYY-MM-DD");
        dobField.setBounds(200, 130, 150, 30);
        frame.add(dobField);

        JLabel titleLabel = new JLabel("Book Title:");
        titleLabel.setBounds(50, 180, 150, 30);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(200, 180, 150, 30);
        frame.add(titleField);

        JLabel authorLabel = new JLabel("Book Author:");
        authorLabel.setBounds(50, 230, 150, 30);
        frame.add(authorLabel);

        JTextField authorField = new JTextField();
        authorField.setBounds(200, 230, 150, 30);
        frame.add(authorField);

        JLabel yearLabel = new JLabel("Year of Publication:");
        yearLabel.setBounds(50, 280, 150, 30);
        frame.add(yearLabel);

        JTextField yearField = new JTextField();
        yearField.setBounds(200, 280, 150, 30);
        frame.add(yearField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 330, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(event -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            LocalDate dob = LocalDate.parse(dobField.getText());
            String title = titleField.getText();
            String author = authorField.getText();
            String yearString = yearField.getText();

            try {
                int yearOfPublication = Integer.parseInt(yearString);

                Reader reader = new Reader(firstName, lastName, dob);
                Book book = new Book(title, author, yearOfPublication);

                boolean result = borrowedBookService.returnBorrowedBook(reader, book);

                if (result) {
                    JOptionPane.showMessageDialog(frame, "Book returned successfully.");
                } else {
                    JOptionPane.showMessageDialog(frame, "No loan found for this book and reader.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                frame.dispose();

            } catch (DateTimeParseException dtpe) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Year of publication must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to return book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showBorrowedBooksByReaderForm() {
        JFrame frame = new JFrame("Borrowed Books by Reader");
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel firstNameLabel = new JLabel("Reader First Name:");
        firstNameLabel.setBounds(50, 30, 150, 30);
        frame.add(firstNameLabel);

        JTextField firstNameField = new JTextField();
        firstNameField.setBounds(200, 30, 150, 30);
        frame.add(firstNameField);

        JLabel lastNameLabel = new JLabel("Reader Last Name:");
        lastNameLabel.setBounds(50, 80, 150, 30);
        frame.add(lastNameLabel);

        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(200, 80, 150, 30);
        frame.add(lastNameField);

        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setBounds(50, 130, 150, 30);
        frame.add(dobLabel);

        JTextField dobField = new JTextField("YYYY-MM-DD");
        dobField.setBounds(200, 130, 150, 30);
        frame.add(dobField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 200, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(event -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String dobString = dobField.getText();

            try {
                LocalDate dob = LocalDate.parse(dobString);
                Reader reader = new Reader(firstName, lastName, dob);

                List<String> borrowedBooks = borrowedBookService.getBooksBorrowedByReaderWithDates(reader);

                if (borrowedBooks.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No borrowed books found for this reader.", "Information", JOptionPane.INFORMATION_MESSAGE);
                } else {

                    JFrame resultFrame = new JFrame("Borrowed Books for " + firstName + " " + lastName);
                    resultFrame.setSize(600, 400);
                    resultFrame.setLocationRelativeTo(null);
                    resultFrame.setIconImage(icon.getImage());

                    String[] columnNames = {"Title", "Author", "Year of publication", "Borrow Date", "Return Date"};
                    Object[][] data = new Object[borrowedBooks.size()][5];

                    for (int i = 0; i < borrowedBooks.size(); i++) {
                        String[] parts = borrowedBooks.get(i).split(", ");
                        data[i][0] = parts[0].split(": ")[1]; // Tittle
                        data[i][1] = parts[1].split(": ")[1]; // Author
                        data[i][2] = parts[2].split(": ")[1]; // Year of publication
                        data[i][3] = parts[3].split(": ")[1]; // Borrow Date
                        data[i][4] = parts[4].split(": ")[1]; // Return Date
                    }

                    JTable table = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(table);
                    resultFrame.add(scrollPane);
                    resultFrame.setVisible(true);
                }

                frame.dispose();

            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to fetch borrowed books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showOverdueReadersForm() {
        JFrame frame = new JFrame("Overdue Readers");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        try {
            List<String> overdueReaders = borrowedBookService.getOverdueReaders();

            if (overdueReaders.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No overdue readers found.", "Information", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                return;
            }

            String[] columnNames = {"First Name", "Last Name", "Date of Birth", "Borrow Date", "Return Due Date"};
            Object[][] data = new Object[overdueReaders.size()][5];

            for (int i = 0; i < overdueReaders.size(); i++) {
                String[] parts = overdueReaders.get(i).split(", ");
                data[i][0] = parts[0].split(": ")[1]; // First Name
                data[i][1] = parts[1].split(": ")[1]; // Last Name
                data[i][2] = parts[2].split(": ")[1]; // Date of Birth
                data[i][3] = parts[3].split(": ")[1]; // Borrow Date
                data[i][4] = parts[4].split(": ")[1]; // Return Due Date
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);
            frame.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to fetch overdue readers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        frame.setVisible(true);
    }

    private void showCheckOverdueLoansForm() {
        JFrame frame = new JFrame("Check Overdue Loans");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel firstNameLabel = new JLabel("Reader First Name:");
        firstNameLabel.setBounds(50, 30, 150, 30);
        frame.add(firstNameLabel);

        JTextField firstNameField = new JTextField();
        firstNameField.setBounds(200, 30, 150, 30);
        frame.add(firstNameField);

        JLabel lastNameLabel = new JLabel("Reader Last Name:");
        lastNameLabel.setBounds(50, 80, 150, 30);
        frame.add(lastNameLabel);

        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(200, 80, 150, 30);
        frame.add(lastNameField);

        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setBounds(50, 130, 150, 30);
        frame.add(dobLabel);

        JTextField dobField = new JTextField("YYYY-MM-DD");
        dobField.setBounds(200, 130, 150, 30);
        frame.add(dobField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 200, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(event -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String dobString = dobField.getText();

            try {
                LocalDate dob = LocalDate.parse(dobString);
                Reader reader = new Reader(firstName, lastName, dob);

                boolean hasOverdueLoans = borrowedBookService.hasOverdueLoans(reader);

                if (hasOverdueLoans) {
                    JOptionPane.showMessageDialog(frame, "Reader " + firstName + " " + lastName + " has overdue loans.", "Overdue Loans", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Reader " + firstName + " " + lastName + " does not have overdue loans.", "Overdue Loans", JOptionPane.INFORMATION_MESSAGE);
                }
                frame.dispose();

            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to check overdue loans: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
