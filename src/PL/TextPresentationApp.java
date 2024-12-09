package PL;

//front 
import java.awt.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class TextPresentationApp extends JFrame {

    private static final Logger logger = LogManager.getLogger(TextPresentationApp.class);

    public TextPresentationApp() {
        logger.info("Initializing TextPresentationApp...");

        setTitle("دستور زُبان");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        logger.debug("JFrame initialized with title, size, and layout.");

        // Background Panel with Dark Purple Color
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(90, 0, 140)); // Set background color to dark purple
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        logger.debug("Background panel with dark purple color added.");

        // Urdu Text Label
        JLabel urduTextLabel = new JLabel("دستور زُبان", SwingConstants.CENTER);
        urduTextLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        urduTextLabel.setForeground(Color.WHITE); // White color for contrast
        backgroundPanel.add(urduTextLabel, BorderLayout.CENTER);
        logger.debug("Urdu text label added to background panel.");

        add(backgroundPanel, BorderLayout.CENTER);

        // "Let's Go" Button
        JButton letsGoButton = new JButton("Let's Go");
        letsGoButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        letsGoButton.setBackground(new Color(90, 0, 140)); // Dark purple button
        letsGoButton.setForeground(Color.WHITE);
        letsGoButton.setFocusPainted(false);
        logger.debug("Let's Go button created with properties.");

        // Button Action to Start TextEditorGUI
        letsGoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Let's Go button clicked. Transitioning to TextEditorGUI.");
                dispose(); // Close the presentation screen
                logger.debug("Presentation screen disposed.");
                new TextEditorGUI(null); // Open the TextEditorGUI
                logger.info("TextEditorGUI launched successfully.");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(90, 0, 140)); // Match the background color of the button panel
        buttonPanel.add(letsGoButton);
        add(buttonPanel, BorderLayout.SOUTH);
        logger.debug("Button panel with Let's Go button added to frame.");

        setVisible(true);
        logger.info("TextPresentationApp UI displayed.");
    }

    public static void main(String[] args) {
        logger.info("Application starting...");
        SwingUtilities.invokeLater(() -> {
            new TextPresentationApp();
            logger.info("TextPresentationApp launched on the Event Dispatch Thread.");
        });
    }
}
