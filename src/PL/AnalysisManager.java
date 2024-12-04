package PL;

import BL.AnalysisManagerBL;
import BL.FileBL;
import BL.I_File;
import DTO.FileDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisManager extends JFrame {
    private final AnalysisManagerBL analysisManager;
    private final I_File fileBL;
    private final JTextArea resultArea;
    private final JRadioButton tfidfButton;
    private final JRadioButton pmiButton;
    private final JRadioButton pklButton;
    private final JList<String> fileList;

    public AnalysisManager(I_File fileBL) {
        setTitle("Text Analysis");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Dependency injection with fallback
        this.fileBL = (fileBL != null) ? fileBL : initializeFileBL();
        analysisManager = new AnalysisManagerBL();

        // Result area setup
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(240, 240, 240));
        resultArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Metric selection buttons
        tfidfButton = new JRadioButton("TF-IDF");
        pmiButton = new JRadioButton("PMI");
        pklButton = new JRadioButton("PKL");
        ButtonGroup metricGroup = new ButtonGroup();
        metricGroup.add(tfidfButton);
        metricGroup.add(pmiButton);
        metricGroup.add(pklButton);
        tfidfButton.setSelected(true);

        // Action buttons
        JButton analyzeButton = createPurpleButton("Analyze Metric");
        analyzeButton.addActionListener(e -> analyzeFiles());

        JButton analyzeAllButton = createPurpleButton("Analyze All Files");
        analyzeAllButton.addActionListener(e -> analyzeAllFiles());

        JButton backButton = createPurpleButton("Back");
        backButton.addActionListener(e -> navigateBack());

        // File list setup
        DefaultListModel<String> fileListModel = new DefaultListModel<>();
        loadFilesFromDB(fileListModel);

        fileList = new JList<>(fileListModel);
        fileList.setBackground(new Color(240, 240, 240));
        fileList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane fileListScrollPane = new JScrollPane(fileList);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0x5A008C));
        buttonPanel.add(tfidfButton);
        buttonPanel.add(pmiButton);
        buttonPanel.add(pklButton);
        buttonPanel.add(analyzeButton);
        buttonPanel.add(analyzeAllButton);
        buttonPanel.add(backButton);

        // Layout setup
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileListScrollPane, new JScrollPane(resultArea));
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private I_File initializeFileBL() {
        try {
            return new FileBL();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error initializing FileBL: " + e.getMessage());
            return null;
        }
    }

    private JButton createPurpleButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0x5A008C));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    private void navigateBack() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(false);
            try {
                new TextEditorGUI(fileBL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void analyzeFiles() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<String> selectedFiles = fileList.getSelectedValuesList();
                if (selectedFiles.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select at least one file.");
                    return;
                }

                List<String> fileContents = selectedFiles.stream()
                        .map(fileName -> {
                            FileDTO fileDTO =null ;
                            try {
                                fileDTO = fileBL.getFileIdByName(fileName);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            return fileDTO != null ? fileDTO.getContent() : null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (fileContents.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No valid file contents found.");
                    return;
                }

                Map<String, Map<String, Double>> results = null;
                if (tfidfButton.isSelected()) {
                    results = analysisManager.analyzeIndividualTFIDF(fileContents);
                } else if (pmiButton.isSelected()) {
                    results = analysisManager.analyzeIndividualPMI(fileContents);
                } else if (pklButton.isSelected()) {
                    results = analysisManager.analyzeIndividualPKL(fileContents);
                }

                if (results != null) {
                    displayResultsInTable(results);
                } else {
                    JOptionPane.showMessageDialog(this, "No analysis selected.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error analyzing files: " + ex.getMessage());
            }
        });
    }

    private void analyzeAllFiles() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<FileDTO> allFiles = fileBL.listAllFiles();
                if (allFiles.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No files available for analysis.");
                    return;
                }

                List<String> allFileContents = allFiles.stream()
                        .map(FileDTO::getContent)
                        .collect(Collectors.toList());

                Map<String, Double> tfidfResults = analysisManager.analyzeCombinedTFIDF(allFileContents);
                Map<String, Double> pmiResults = analysisManager.analyzeCombinedPMI(allFileContents);
                Map<String, Double> pklResults = analysisManager.analyzeCombinedPKL(allFileContents);

                DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Term", "TF-IDF", "PMI", "PKL"}, 0);
                for (FileDTO file : allFiles) {
                    Set<String> terms = new HashSet<>(tfidfResults.keySet());
                    terms.addAll(pmiResults.keySet());
                    terms.addAll(pklResults.keySet());
                    for (String term : terms) {
                        tableModel.addRow(new Object[]{term, tfidfResults.get(term), pmiResults.get(term), pklResults.get(term)});
                    }
                }

                displayResultsInTable(tableModel);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error analyzing files: " + ex.getMessage());
            }
        });
    }

    private void displayResultsInTable(Map<String, Map<String, Double>> results) {
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Term", "Value"}, 0);
        results.forEach((file, metrics) -> metrics.forEach((term, value) -> tableModel.addRow(new Object[]{term, value})));
        JTable resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultArea.removeAll();
        resultArea.setLayout(new BorderLayout());
        resultArea.add(scrollPane, BorderLayout.CENTER);
        resultArea.revalidate();
        resultArea.repaint();
    }

    private void displayResultsInTable(DefaultTableModel tableModel) {
        JTable resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultArea.removeAll();
        resultArea.setLayout(new BorderLayout());
        resultArea.add(scrollPane, BorderLayout.CENTER);
        resultArea.revalidate();
        resultArea.repaint();
    }

    private void loadFilesFromDB(DefaultListModel<String> fileListModel) {
        try {
            List<FileDTO> files = fileBL.listAllFiles();
            if (files.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No files available.");
            }
            fileListModel.clear();
            files.forEach(file -> fileListModel.addElement(file.getFileName()));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load files from the database.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalysisManager(null));
    }
}
