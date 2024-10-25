package ui.util;

import ui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackgroundPanel extends JPanel {
    private final static Logger logger = Logger.getLogger(BackgroundPanel.class.getName());
    private final Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        backgroundImage = new ImageIcon(MainWindow.class.getResource(imagePath)).getImage();
        if (backgroundImage == null) {
            logger.log(Level.SEVERE, "Error: Background image not found at " + imagePath);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Ensure the parent class paints its components first

        if(backgroundImage != null) {
            // Draw the image at its original size, without scaling
            g.drawImage(backgroundImage, 0, 0, this);
        }else {
            logger.log(Level.WARNING, "Background image is null. Painting a white background.");
            g.setColor(Color.WHITE);
            g.fillRect(0,0, getWidth(), getHeight());
        }
    }
}
