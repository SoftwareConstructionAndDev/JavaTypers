package PL;
//front 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class TextPresentationApp extends JFrame {

    public TextPresentationApp() {
        setTitle("3D Urdu Language Presentation");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Background Panel with Dark Purple Color
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Set background color to dark purple
                g.setColor(new Color(90, 0, 140)); 
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // Urdu Text Label
        JLabel urduTextLabel = new JLabel("دستور زُبان", SwingConstants.CENTER);
        urduTextLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        urduTextLabel.setForeground(Color.WHITE); // White color for contrast
        backgroundPanel.add(urduTextLabel, BorderLayout.CENTER);

        add(backgroundPanel, BorderLayout.CENTER);

        // "Let's Go" Button
        JButton letsGoButton = new JButton("Let's Go");
        letsGoButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        letsGoButton.setBackground(new Color(90, 0, 140)); // Dark purple button
        letsGoButton.setForeground(Color.WHITE);
        letsGoButton.setFocusPainted(false);

        // Button Action to Start TextEditorGUI
        letsGoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the presentation screen
                try {
					new TextEditorGUI();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} // Open the TextEditorGUI
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(90, 0, 140)); // Match the background color of the button panel
        buttonPanel.add(letsGoButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextPresentationApp::new);
    }
}
