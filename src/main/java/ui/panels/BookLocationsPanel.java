package ui.panels;

import model.BookLocation;
import service.BookLocationService;
import ui.MainWindow;
import ui.util.BackgroundPanel;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class BookLocationsPanel extends BackgroundPanel {
    private final BookLocationService bookLocationService;
    public BookLocationsPanel(String imagePath, Connection connection){
        super(imagePath);
        this.bookLocationService = new BookLocationService(connection);
        setLayout(null);

        int buttonWidth = 224;
        int buttonHeight = 50;

        JButton addLocationButton = createButton("Add Location");
        addLocationButton.setBounds(400, 397, buttonWidth, buttonHeight);
        addLocationButton.addActionListener(e -> showAddLocationForm());
        add(addLocationButton);

        JButton viewAllLocationsButton = createButton("View All Locations");
        viewAllLocationsButton.setBounds(400, 457, buttonWidth, buttonHeight);
        viewAllLocationsButton.addActionListener(e -> showAllLocationsForm());
        add(viewAllLocationsButton);

        JButton removeLocationButton = createButton("Remove Location");
        removeLocationButton.setBounds(400, 517, buttonWidth, buttonHeight);
        removeLocationButton.addActionListener(e -> showRemoveLocationForm());
        add(removeLocationButton);


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

    private void showAddLocationForm() {
        JFrame frame = new JFrame("Add New Location");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel sectionLabel = new JLabel("Section:");
        sectionLabel.setBounds(50, 30, 100, 30);
        frame.add(sectionLabel);

        JTextField sectionField = new JTextField();
        sectionField.setBounds(150, 30, 200, 30);
        frame.add(sectionField);

        JLabel shelfLabel = new JLabel("Shelf:");
        shelfLabel.setBounds(50, 80, 100, 30);
        frame.add(shelfLabel);

        JTextField shelfField = new JTextField();
        shelfField.setBounds(150, 80, 200, 30);
        frame.add(shelfField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 150, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(event -> {
            String section = sectionField.getText();
            String shelfString = shelfField.getText();

            try {
                int shelf = Integer.parseInt(shelfString);

                BookLocation newLocation = new BookLocation(section, shelf);
                Optional<Integer> locationId = bookLocationService.addLocation(newLocation);

                if (locationId.isPresent()) {
                    JOptionPane.showMessageDialog(frame, "Location added successfully with ID: " + locationId.get());
                } else {
                    JOptionPane.showMessageDialog(frame, "Location already exists.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                frame.dispose();

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Shelf must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to add location: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    private void showAllLocationsForm() {
        JFrame frame = new JFrame("All Book Locations");
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        try {
            List<BookLocation> locations = bookLocationService.getAllBookLocations();

            String[] columnNames = {"ID", "Section", "Shelf"};
            Object[][] data = new Object[locations.size()][3];

            for (int i = 0; i < locations.size(); i++) {
                BookLocation location = locations.get(i);
                data[i][0] = location.getId();
                data[i][1] = location.getSection();
                data[i][2] = location.getShelf();
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);
            frame.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to fetch locations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        frame.setVisible(true);
    }

    private void showRemoveLocationForm() {
        JFrame frame = new JFrame("Remove Book Location");
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        frame.setIconImage(icon.getImage());

        JLabel sectionLabel = new JLabel("Section:");
        sectionLabel.setBounds(50, 30, 100, 30);
        frame.add(sectionLabel);

        JTextField sectionField = new JTextField();
        sectionField.setBounds(150, 30, 200, 30);
        frame.add(sectionField);

        JLabel shelfLabel = new JLabel("Shelf:");
        shelfLabel.setBounds(50, 80, 100, 30);
        frame.add(shelfLabel);

        JTextField shelfField = new JTextField();
        shelfField.setBounds(150, 80, 200, 30);
        frame.add(shelfField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 150, 100, 30);
        frame.add(submitButton);

        submitButton.addActionListener(event -> {
            String section = sectionField.getText();
            String shelfString = shelfField.getText();

            int confirmation = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to remove this location?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    int shelf = Integer.parseInt(shelfString);
                    boolean result = bookLocationService.removeBookLocation(section, shelf);

                    if (result) {
                        JOptionPane.showMessageDialog(frame, "Location removed successfully.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Location not found or could not be removed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    frame.dispose();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(frame, "Shelf must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to remove location: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
