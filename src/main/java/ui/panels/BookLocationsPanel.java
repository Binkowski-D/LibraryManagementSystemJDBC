package ui.panels;

import ui.MainWindow;
import ui.util.BackgroundPanel;

import javax.swing.*;

public class BookLocationsPanel extends BackgroundPanel {
    public BookLocationsPanel(String imagePath){
        super(imagePath);
        setLayout(null);

        int buttonWidth = 224;
        int buttonHeight = 50;

        JButton addLocationButton = createButton("Add Location");
        addLocationButton.setBounds(400, 397, buttonWidth, buttonHeight);
        add(addLocationButton);

        JButton viewAllLocationsButton = createButton("View All Locations");
        viewAllLocationsButton.setBounds(400, 457, buttonWidth, buttonHeight);
        add(viewAllLocationsButton);

        JButton getLocationForBookButton = createButton("Find Location For Book");
        getLocationForBookButton.setBounds(400, 517, buttonWidth, buttonHeight);
        add(getLocationForBookButton);


        JButton removeLocationButton = createButton("Remove Location");
        removeLocationButton.setBounds(400, 577, buttonWidth, buttonHeight);
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

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }
}
