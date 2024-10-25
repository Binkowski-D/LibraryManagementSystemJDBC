package ui.panels;

import ui.MainWindow;
import ui.util.BackgroundPanel;

import javax.swing.*;

public class BorrowedBookPanel extends BackgroundPanel {
    public BorrowedBookPanel(String imagePath){
        super(imagePath);
        setLayout(null);

        int buttonWidth = 224;
        int buttonHeight = 50;

        JButton borrowBookButton = createButton("Borrow Book");
        borrowBookButton.setBounds(400, 397, buttonWidth, buttonHeight);
        add(borrowBookButton);

        JButton returnBookButton = createButton("Return Book");
        returnBookButton.setBounds(400, 457, buttonWidth, buttonHeight);
        add(returnBookButton);

        JButton getBorrowedBooksButton = createButton("Get Borrowed Books By Reader");
        getBorrowedBooksButton.setBounds(400, 517, buttonWidth, buttonHeight);
        add(getBorrowedBooksButton);


        JButton getOverdueReadersButton = createButton("Get Overdue Readers");
        getOverdueReadersButton.setBounds(400, 577, buttonWidth, buttonHeight);
        add(getOverdueReadersButton);


        JButton checkOverdueLoansButton = createButton("Check Reader Overdue Loans");
        checkOverdueLoansButton.setBounds(400, 637, buttonWidth, buttonHeight);
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

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }
}
