package PL;

import BL.AnalysisManagerBL;
import DAL.FileDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AnalysisManager extends JFrame {
    private AnalysisManagerBL analysisManager;
    private FileDAO databaseManager;
    private JTextArea resultArea;
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JPanel resultPanel;

    public AnalysisManager() throws Exception {
        setTitle("Text Analysis");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        analysisManager = new AnalysisManagerBL();
        databaseManager = new FileDAO();

        // Main Panel to hold all sub-panels
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.LIGHT_GRAY); // Set background color for main panel

        // Panel to display results
        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(new Color(240, 240, 255)); // Light blue background for result panel

        // Text area to display results
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(245, 245, 245)); // Light gray background for the text area
        resultArea.setForeground(Color.BLACK); // Text color for the result area
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14)); // Set a readable font
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Panel for buttons
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.DARK_GRAY); // Dark background for the button panel

        // Create buttons
        JButton analyzeIndividualButton = new JButton("Analyze Individual Files");
        analyzeIndividualButton.setBackground(new Color(60, 179, 113)); // Green background
        analyzeIndividualButton.setForeground(Color.WHITE); // White text
        analyzeIndividualButton.setFocusPainted(false); // Remove focus highlight
        analyzeIndividualButton.addActionListener(e -> analyzeIndividualFiles());

        JButton analyzeAllButton = new JButton("Analyze Across All Files");
        analyzeAllButton.setBackground(new Color(70, 130, 180)); // Steel blue background
        analyzeAllButton.setForeground(Color.WHITE); // White text
        analyzeAllButton.setFocusPainted(false);
        analyzeAllButton.addActionListener(e -> analyzeAcrossAllFiles());

        JButton backButton = new JButton("Back");
        backButton.setBackground(new Color(255, 99, 71)); // Tomato red background
        backButton.setForeground(Color.WHITE); // White text
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
			try {
				navigateBack();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

        // Add buttons to the panel
        buttonPanel.add(analyzeIndividualButton);
        buttonPanel.add(analyzeAllButton);
        buttonPanel.add(backButton);

        // Add the button panel and result panel to the main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(resultPanel, BorderLayout.CENTER);

        // Set the main panel to the frame
        add(mainPanel);
        setVisible(true);
    }

    private void navigateBack() throws Exception {
        // Close current window and show the previous screen (TextEditorGUI)
        this.setVisible(false); 
        new TextEditorGUI(); 
    }

    private void analyzeIndividualFiles() {
        try {
            List<String> allFiles = databaseManager.getAllFiles();
            Map<String, Map<String, Map<String, Double>>> results = analysisManager.analyzeIndividualFiles(allFiles);

            resultArea.setText("Individual File Analysis Results:\n\n");
            for (Map.Entry<String, Map<String, Map<String, Double>>> entry : results.entrySet()) {
                resultArea.append("File: " + entry.getKey() + "\n");

                Map<String, Map<String, Double>> fileResults = entry.getValue();
                for (Map.Entry<String, Map<String, Double>> metricEntry : fileResults.entrySet()) {
                    resultArea.append("  " + metricEntry.getKey() + ":\n");
                    for (Map.Entry<String, Double> metric : metricEntry.getValue().entrySet()) {
                        resultArea.append("    " + metric.getKey() + ": " + metric.getValue() + "\n");
                    }
                }
                resultArea.append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error analyzing individual files: " + ex.getMessage());
        }
    }

    private void analyzeAcrossAllFiles() {
        try {
            List<String> allFiles = databaseManager.getAllFiles();
            Map<String, Map<String, Double>> results = analysisManager.analyzeAcrossAllFiles(allFiles);

            resultArea.setText("Overall Analysis Results Across All Files:\n\n");
            for (Map.Entry<String, Map<String, Double>> entry : results.entrySet()) {
                resultArea.append(entry.getKey() + ":\n");
                for (Map.Entry<String, Double> metric : entry.getValue().entrySet()) {
                    resultArea.append(metric.getKey() + ": " + metric.getValue() + "\n");
                }
                resultArea.append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error analyzing across all files: " + ex.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        new AnalysisManager();
    }
}
