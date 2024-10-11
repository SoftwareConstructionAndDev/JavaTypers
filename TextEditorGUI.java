// PL/TextEditorGUI.java
//package PL;

//import BL.FileBL;
//import DTO.FileDTO;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class TextEditorGUI extends JFrame {
    private FileBL fileManager;
    private JTextField fileNameField;
    private JTextArea contentArea;
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private JLabel statusLabel;

    // Default owner is "imama"
    private static final String DEFAULT_OWNER = "imama";

    public TextEditorGUI() {
        fileManager = new FileBL();
        initializeUI();
        refreshFileList();
    }

    private void initializeUI() {
        setTitle("Text Editor");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the background color of the content pane to light gray
        getContentPane().setBackground(new Color(75, 0, 130));
        setLayout(new BorderLayout());

        // Left panel with file list and View buttons
        JPanel leftPanel = new JPanel(new BorderLayout());

        

//        viewButtonsPanel.add(viewAllFilesButton);
//        viewButtonsPanel.add(viewOneFileButton);
//
//        leftPanel.add(viewButtonsPanel, BorderLayout.NORTH);

        // File list
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(fileList);
        listScrollPane.setPreferredSize(new Dimension(200, 0));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        // Center panel with input fields and text area
        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("File Name:"));
        fileNameField = new JTextField();
        inputPanel.add(fileNameField);

        contentArea = new JTextArea();
        JScrollPane contentScrollPane = new JScrollPane(contentArea);

        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(contentScrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Button panel for CRUD operations and imports
        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 5, 5));
        addButton(buttonPanel, "Create File", new Color(75, 0, 130));  // Dark purple color
        addButton(buttonPanel, "Read File", new Color(75, 0, 130));    // Dark purple color
        addButton(buttonPanel, "Update File", new Color(75, 0, 130));  // Dark purple color
        addButton(buttonPanel, "Delete File", new Color(75, 0, 130));  // Dark purple color
        addButton(buttonPanel, "Import File", new Color(75, 0, 130));  // Dark purple color
        addButton(buttonPanel, "Import Bulk Files", new Color(75, 0, 130));  // Dark purple color

        add(buttonPanel, BorderLayout.SOUTH);

        // Status label
        statusLabel = new JLabel(" ");
        add(statusLabel, BorderLayout.NORTH);

        // Add list selection listener
        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    loadFile(selectedFile);
                }
            }
        });
    }


 // Method to add buttons to the panel with specified colors
 // Method to add buttons to the panel with specified colors and increased height
    private void addButton(JPanel panel, String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);  // Set button background color
        button.setForeground(Color.WHITE);  // Set text color to white
        button.setFocusPainted(false);

        // Set the preferred size for each button (adjust height here)
        button.setPreferredSize(new Dimension(120, 50));  // Increase height to 50px, width is 120px

        button.addActionListener(e -> handleButtonClick(text));
        panel.add(button);
    }


    // Handling button click actions
    private void handleButtonClick(String action) {
        try {
            switch (action) {
                case "Create File":
                    createFile();
                    break;
                case "Read File":
                    readFile();
                    break;
                case "Update File":
                    updateFile();
                    break;
                case "Delete File":
                    deleteFile();
                    break;
                case "Import File":
                
                    importFile();
                  
                    break;
                case "Import Bulk Files":
                    importBulkFiles();
                    break;
            }
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), true);
        }
    }
 
    }

    
    private void importFile() throws IOException, SQLException, NoSuchAlgorithmException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path filePath = fileChooser.getSelectedFile().toPath();
            if (fileManager.importFile(filePath)) {
                setStatus("File imported successfully", false);
                refreshFileList();
            } else {
                setStatus("File already exists", true);
            }
        }
     // Method to read a file
        private void readFile() throws SQLException {
            String fileName = fileNameField.getText();
            loadFile(fileName);
        }
     // Create a file and clear input fields afterward
        private void createFile() throws SQLException, NoSuchAlgorithmException {
            String fileName = fileNameField.getText();
            String content = contentArea.getText();

            if (fileManager.createFile(fileName, content)) {
                setStatus("File created successfully", false);
                clearFields();  // Clear the input fields after file creation
                refreshFileList();
            } else {
                setStatus("Failed to create file", true);
            }
        }

        // Update a file and clear input fields afterward
        private void updateFile() throws SQLException, NoSuchAlgorithmException {
            String fileName = fileNameField.getText();
            String content = contentArea.getText();

            if (fileManager.updateFile(fileName, content)) {
                setStatus("File updated successfully", false);
                clearFields();  // Clear the input fields after file update
                refreshFileList();
            } else {
                setStatus("Failed to update file", true);
            }
        }

     // In TextEditorGUI.java
        private void deleteFile() throws SQLException {
            String fileName = fileNameField.getText();  // Get the file name from the input field

            // Check if the file name is provided
            if (fileName == null || fileName.trim().isEmpty()) {
                setStatus("File name must be provided to delete", true);
                return;  // Do nothing if no file name is provided
            }

            // Call the Business Logic Layer to delete the file
            if (fileManager.deleteFile(fileName)) {
                setStatus("File deleted successfully", false);
                clearFields();  // Clear the input fields after deletion
                refreshFileList();  // Refresh the file list after deletion
            } else {
                setStatus("Failed to delete file", true);
            }
        }
    }
    








    // Method to read a file
    private void readFile() throws SQLException {
        String fileName = fileNameField.getText();
        loadFile(fileName);
    }

    // Method to load a file's content based on its name
    private void loadFile(String fileName) {
        try {
            FileDTO file = fileManager.readFile(fileName);
            if (file != null) {
                fileNameField.setText(file.getFileName());
                contentArea.setText(file.getContent());
                setStatus("File loaded successfully", false);
            } else {
                setStatus("File not found", true);
            }
        } catch (SQLException ex) {
            setStatus("Error loading file: " + ex.getMessage(), true);
        }
    }

 
    // Import a single file and clear input fields afterward
    private void importSingleFile() throws IOException, SQLException, NoSuchAlgorithmException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName();
            String content = new String(Files.readAllBytes(file.toPath()));

            if (fileManager.createFile(fileName, content)) {
                setStatus("File imported successfully", false);
                clearFields();  // Clear the input fields after file import
                refreshFileList();
            } else {
                setStatus("Failed to import file", true);
            }
        }
    }

    // Import multiple files in bulk and clear input fields afterward
    private void importBulkFiles() throws IOException, SQLException, NoSuchAlgorithmException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        String content = new String(Files.readAllBytes(file.toPath()));

                        if (fileManager.createFile(fileName, content)) {
                            setStatus("Bulk files imported successfully", false);
                        } else {
                            setStatus("Failed to import file: " + fileName, true);
                        }
                    }
                }
                clearFields();  // Clear the input fields after bulk file import
                refreshFileList();
            }
        }
    }

    // Method to refresh the file list
    private void refreshFileList() {
        try {
            listModel.clear();
            List<String> fileNames = fileManager.listAllFiles();
            for (String fileName : fileNames) {
                listModel.addElement(fileName);
            }
        } catch (SQLException ex) {
            setStatus("Error refreshing file list: " + ex.getMessage(), true);
        }
    }

    // Method to clear the input fields
    private void clearFields() {
        fileNameField.setText("");
        contentArea.setText("");
    }

    // Method to set the status message in the UI
    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : Color.WHITE);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TextEditorGUI ui = new TextEditorGUI();
            ui.setVisible(true);
        });
    }
}

