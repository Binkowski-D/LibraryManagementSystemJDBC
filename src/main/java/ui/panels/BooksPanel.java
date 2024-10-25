package ui.panels;

import ui.MainWindow;
import ui.util.BackgroundPanel;

import javax.swing.*;

public class BooksPanel extends BackgroundPanel {
    public BooksPanel(String imagePath){
        super(imagePath);
        setLayout(null);

        int buttonWidth = 224;
        int buttonHeight = 50;

        JButton addBookButton = createButton("Add Book");
        addBookButton.setBounds(400, 250, buttonWidth, buttonHeight);
        add(addBookButton);

        JButton increaseQuantityButton = createButton("Increase Quantity");
        increaseQuantityButton.setBounds(400, 310, buttonWidth, buttonHeight);
        add(increaseQuantityButton);

        JButton decreaseQuantityButton = createButton("Decrease Quantity");
        decreaseQuantityButton.setBounds(400, 370, buttonWidth, buttonHeight);
        add(decreaseQuantityButton);

        JButton getAllBooksButton = createButton("Get All Books");
        getAllBooksButton.setBounds(400, 430, buttonWidth, buttonHeight);
        add(getAllBooksButton);

        JButton getBooksByTitleButton = createButton("Get Books By Title");
        getBooksByTitleButton.setBounds(400, 490, buttonWidth, buttonHeight);
        add(getBooksByTitleButton);

        JButton getBooksByAuthorButton = createButton("Get Books By Author");
        getBooksByAuthorButton.setBounds(400, 550, buttonWidth, buttonHeight);
        add(getBooksByAuthorButton);

        JButton findBookByDetailsButton = createButton("Find Book By Details");
        findBookByDetailsButton.setBounds(400, 610, buttonWidth, buttonHeight);
        add(findBookByDetailsButton);

        JButton removeBookByDetailsButton = createButton("Remove Book");
        removeBookByDetailsButton.setBounds(400, 670, buttonWidth, buttonHeight);
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

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }
}
