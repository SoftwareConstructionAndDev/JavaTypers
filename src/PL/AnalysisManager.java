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

    public AnalysisManager() {
        setTitle("Text Analysis");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        analysisManager = new AnalysisManagerBL();
        databaseManager = new FileDAO();

        resultArea = new JTextArea();
        resultArea.setEditable(false);

        JButton analyzeIndividualButton = new JButton("Analyze Individual Files");
        analyzeIndividualButton.addActionListener(e -> analyzeIndividualFiles());

        JButton analyzeAllButton = new JButton("Analyze Across All Files");
        analyzeAllButton.addActionListener(e -> analyzeAcrossAllFiles());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> navigateBack());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(analyzeIndividualButton);
        buttonPanel.add(analyzeAllButton);
        buttonPanel.add(backButton); 

        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void navigateBack() {
        this.setVisible(false); 
        new TextEditorGUI(); 
    }

    private void analyzeIndividualFiles() {
        try {
            List<String> allFiles = databaseManager.getAllFiles();
            Map<String, Map<String, Double>> results = analysisManager.analyzeIndividualFiles(allFiles);

            resultArea.setText("Individual File Analysis Results:\n\n");
            for (Map.Entry<String, Map<String, Double>> entry : results.entrySet()) {
                resultArea.append(entry.getKey() + ":\n");
                for (Map.Entry<String, Double> metric : entry.getValue().entrySet()) {
                    resultArea.append(metric.getKey() + ": " + metric.getValue() + "\n");
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

    public static void main(String[] args) {
        new AnalysisManager();
    }
}

    