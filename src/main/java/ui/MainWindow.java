package ui;

import config.DatabaseConnection;
import ui.panels.BookLocationsPanel;
import ui.panels.BooksPanel;
import ui.panels.BorrowedBookPanel;
import ui.panels.ReadersPanel;
import ui.util.BackgroundPanel;

import javax.swing.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MainWindow {
    private static JFrame mainFrame;
    private static Connection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                connection = DatabaseConnection.getConnection();
                createAndShowGUI();
            } catch (SQLException | IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to connect to the database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void createAndShowGUI() {
        mainFrame = new JFrame("Library Management System");
        mainFrame.setSize(1024, 1024);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        mainFrame.setIconImage(icon.getImage());

        BackgroundPanel backgroundPanel = new BackgroundPanel("/images/background.png");
        backgroundPanel.setLayout(null);
        mainFrame.setContentPane(backgroundPanel);

        JButton booksButton = createButton("BOOKS");
        booksButton.setBounds(400, 397, 224, 50);
        booksButton.addActionListener(e -> openBooksWindow());
        backgroundPanel.add(booksButton);

        JButton readersButton = createButton("READERS");
        readersButton.setBounds(400, 457, 224, 50);
        readersButton.addActionListener(e -> openReadersWindow());
        backgroundPanel.add(readersButton);

        JButton booksLocationsButton = createButton("BOOK LOCATIONS");
        booksLocationsButton.setBounds(400, 517, 224, 50);
        booksLocationsButton.addActionListener(e -> openButtonLocationsWindow());
        backgroundPanel.add(booksLocationsButton);

        JButton borrowsAndReturnsButton = createButton("BORROWS & RETURNS");
        borrowsAndReturnsButton.setBounds(400, 577, 224, 50);
        borrowsAndReturnsButton.addActionListener(e -> openBorrowsAndReturnsWindow());
        backgroundPanel.add(borrowsAndReturnsButton);
    }

    public static void showMainWindow() {
        mainFrame.setVisible(true);
    }

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    private static void openBooksWindow() {
        mainFrame.setVisible(false);

        JFrame booksFrame = new JFrame("Books");
        booksFrame.setSize(1024, 1024);
        booksFrame.setLocationRelativeTo(null);
        booksFrame.setResizable(false);
        booksFrame.setVisible(true);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        booksFrame.setIconImage(icon.getImage());

        BooksPanel booksPanel = new BooksPanel("/images/background.png", connection);
        booksPanel.setLayout(null);
        booksFrame.setContentPane(booksPanel);

        booksFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        booksFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                showMainWindow();
            }
        });
    }

    private static void openReadersWindow() {
        mainFrame.setVisible(false);
        JFrame readersFrame = new JFrame("Readers");
        readersFrame.setSize(1024, 1024);
        readersFrame.setLocationRelativeTo(null);
        readersFrame.setResizable(false);
        readersFrame.setVisible(true);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        readersFrame.setIconImage(icon.getImage());

        ReadersPanel readersPanel = new ReadersPanel("/images/background.png", connection);
        readersPanel.setLayout(null);
        readersFrame.setContentPane(readersPanel);

        readersFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        readersFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                showMainWindow();
            }
        });
    }

    private static void openButtonLocationsWindow() {
        mainFrame.setVisible(false);
        JFrame bookLocationsFrame = new JFrame("Book Locations");
        bookLocationsFrame.setSize(1024, 1024);
        bookLocationsFrame.setLocationRelativeTo(null);
        bookLocationsFrame.setResizable(false);
        bookLocationsFrame.setVisible(true);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        bookLocationsFrame.setIconImage(icon.getImage());

        BookLocationsPanel bookLocationsPanel = new BookLocationsPanel("/images/background.png", connection);
        bookLocationsPanel.setLayout(null);
        bookLocationsFrame.setContentPane(bookLocationsPanel);

        bookLocationsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        bookLocationsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                showMainWindow();
            }
        });
    }

    private static void openBorrowsAndReturnsWindow() {
        mainFrame.setVisible(false);
        JFrame borrowsAndReturnsFrame = new JFrame("Borrows & Returns");
        borrowsAndReturnsFrame.setSize(1024, 1024);
        borrowsAndReturnsFrame.setLocationRelativeTo(null);
        borrowsAndReturnsFrame.setResizable(false);
        borrowsAndReturnsFrame.setVisible(true);

        ImageIcon icon = new ImageIcon(MainWindow.class.getResource("/images/icon.png"));
        borrowsAndReturnsFrame.setIconImage(icon.getImage());

        BorrowedBookPanel borrowsAndReturnsPanel = new BorrowedBookPanel("/images/background.png", connection);
        borrowsAndReturnsPanel.setLayout(null);
        borrowsAndReturnsFrame.setContentPane(borrowsAndReturnsPanel);

        borrowsAndReturnsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        borrowsAndReturnsFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                showMainWindow();
            }
        });
    }
}
