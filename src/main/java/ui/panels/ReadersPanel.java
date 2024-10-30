package ui.panels;

import model.Reader;
import service.ReaderService;
import ui.MainWindow;
import ui.util.BackgroundPanel;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReadersPanel extends BackgroundPanel {
    private final ReaderService readerService;
    public ReadersPanel(String imagePath, Connection connection){
        super(imagePath);
        this.readerService = new ReaderService(connection);
        setLayout(null);

        int buttonWidth = 224;
        int buttonHeight = 50;

        JButton addReaderButton = createButton("Add Reader");
        addReaderButton.setBounds(400, 397, buttonWidth, buttonHeight);
        addReaderButton.addActionListener(e -> showAddReaderForm());
        add(addReaderButton);

        JButton getAllReadersButton = createButton("Get All Readers");
        getAllReadersButton.setBounds(400, 457, buttonWidth, buttonHeight);
        getAllReadersButton.addActionListener(e -> showAllReaders());
        add(getAllReadersButton);

        JButton getReadersByLastNameButton = createButton("Get Readers By Last Name");
        getReadersByLastNameButton.setBounds(400, 517, buttonWidth, buttonHeight);
        getReadersByLastNameButton.addActionListener(e -> showFindReadersByLastNameForm());
        add(getReadersByLastNameButton);

        JButton removeReaderButton = createButton("Remove Reader");
        removeReaderButton.setBounds(400, 577, buttonWidth, buttonHeight);
        removeReaderButton.addActionListener(e -> showRemoveReaderForm());
        add(removeReaderButton);

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

    private void showAddReaderForm() {
        JFrame frame = new JFrame("Add New Reader");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameLabel.setBounds(50, 30, 100, 30);
        frame.add(firstNameLabel);

        JTextField firstNameField = new JTextField();
        firstNameField.setBounds(150, 30, 200, 30);
        frame.add(firstNameField);

        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setBounds(50, 80, 100, 30);
        frame.add(lastNameLabel);

        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(150, 80, 200, 30);
        frame.add(lastNameField);

        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setBounds(50, 130, 100, 30);
        frame.add(dobLabel);

        JTextField dobField = new JTextField("YYYY-MM-DD");
        dobField.setBounds(150, 130, 200, 30);
        frame.add(dobField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 180, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String dobString = dobField.getText();
            try {
                LocalDate dob = LocalDate.parse(dobString);
                readerService.addReader(firstName, lastName, dob);
                JOptionPane.showMessageDialog(frame, "Reader added successfully!");
                frame.dispose();
            } catch (DateTimeParseException dtpe) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to add reader: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showAllReaders() {
        JFrame frame = new JFrame("All Readers");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        try {
            List<Reader> readers = readerService.getAllReaders();

            String[] columnNames = {"ID", "First Name", "Last Name", "Date of Birth"};
            Object[][] data = new Object[readers.size()][4];

            for (int i = 0; i < readers.size(); i++) {
                Reader reader = readers.get(i);
                data[i][0] = reader.getId();
                data[i][1] = reader.getFirstName();
                data[i][2] = reader.getLastName();
                data[i][3] = reader.getDateOfBirth();
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);
            frame.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to fetch readers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        frame.setVisible(true);
    }

    private void showFindReadersByLastNameForm() {
        JFrame frame = new JFrame("Find Readers by Last Name");
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setBounds(50, 30, 100, 30);
        frame.add(lastNameLabel);

        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(150, 30, 200, 30);
        frame.add(lastNameField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 80, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e -> {
            String lastName = lastNameField.getText();
            try {

                List<Reader> readers = readerService.getReadersByLastName(lastName);

                if (readers.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No readers found with the last name: " + lastName, "No Results", JOptionPane.INFORMATION_MESSAGE);
                } else {

                    JFrame resultsFrame = new JFrame("Readers with Last Name: " + lastName);
                    resultsFrame.setSize(600, 400);
                    resultsFrame.setLocationRelativeTo(null);
                    resultsFrame.setLayout(new BorderLayout());

                    resultsFrame.setIconImage(icon.getImage());

                    String[] columnNames = {"ID", "First Name", "Last Name", "Date of Birth"};
                    Object[][] data = new Object[readers.size()][4];

                    for (int i = 0; i < readers.size(); i++) {
                        Reader reader = readers.get(i);
                        data[i][0] = reader.getId();
                        data[i][1] = reader.getFirstName();
                        data[i][2] = reader.getLastName();
                        data[i][3] = reader.getDateOfBirth();
                    }

                    JTable table = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(table);
                    resultsFrame.add(scrollPane, BorderLayout.CENTER);

                    resultsFrame.setVisible(true);
                }
                frame.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to find readers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showRemoveReaderForm() {
        JFrame frame = new JFrame("Remove Reader");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameLabel.setBounds(50, 30, 100, 30);
        frame.add(firstNameLabel);

        JTextField firstNameField = new JTextField();
        firstNameField.setBounds(150, 30, 200, 30);
        frame.add(firstNameField);

        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setBounds(50, 80, 100, 30);
        frame.add(lastNameLabel);

        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(150, 80, 200, 30);
        frame.add(lastNameField);

        JLabel dobLabel = new JLabel("Date of Birth: ");
        dobLabel.setBounds(50, 130, 100, 30);
        frame.add(dobLabel);

        JTextField dobField = new JTextField("YYYY-MM-DD");
        dobField.setBounds(150, 130, 200, 30);
        frame.add(dobField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 180, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String dobString = dobField.getText();

            int confirmation = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to remove this reader?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
            );

            if(confirmation == JOptionPane.YES_OPTION) {
                try {
                    LocalDate dob = LocalDate.parse(dobString);
                    boolean result = readerService.removeReaderByDetails(firstName, lastName, dob);

                    if (result) {
                        JOptionPane.showMessageDialog(frame, "Reader removed successfully.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Reader not found or could not be removed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    frame.dispose();

                } catch (DateTimeParseException dtpe) {
                    JOptionPane.showMessageDialog(frame, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to remove reader: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
