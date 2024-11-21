package PL;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import BL.FileBL;
import DTO.FileDTO;

public class FileManager {
    private final FileBL fileBL;
    private final JTextArea editorArea;
    private PaginationManager paginationManager;
    private JLabel pageLabel;
    private String currentFileName;

    // Constructor
    public FileManager(FileBL fileBL, JTextArea editorArea, JLabel pageLabel) {
        this.fileBL = fileBL;
        this.editorArea = editorArea;
        this.pageLabel = pageLabel;
        this.paginationManager = new PaginationManager(editorArea, pageLabel, fileBL, -1); // Initialize with placeholder document ID
    }

    // Load files from the database and display in a list
    public void loadFilesFromDB(DefaultListModel<String> fileListModel) {
        try {
            List<FileDTO> files = fileBL.listAllFiles();
            fileListModel.clear();
            for (FileDTO file : files) {
                fileListModel.addElement(file.getFileName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to load files from the database.");
        }
    }
    public List<String> searchFilesContainingText(String searchText) {
        List<String> matchingFiles = new ArrayList<>();
        try {
            // Get all files from the database
            List<FileDTO> allFiles = fileBL.listAllFiles();

            // Filter files containing the search text
            for (FileDTO file : allFiles) {
                if (file.getContent().contains(searchText)) {
                    matchingFiles.add(file.getFileName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error searching files: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return matchingFiles;
    }
    


    // Save a new file and paginate its content
    public void saveFile() {
        String content = editorArea.getText();

        if (currentFileName == null) {
            currentFileName = JOptionPane.showInputDialog("Enter file name to save:");
            if (currentFileName == null || currentFileName.trim().isEmpty()) return;
        }

        try {
            fileBL.createFile(currentFileName, content);
            JOptionPane.showMessageDialog(null, "File saved successfully.");

            // Retrieve the document ID after saving and initialize pagination
            int documentId = fileBL.getFileIdByName(currentFileName);
            paginationManager = new PaginationManager(editorArea, pageLabel, fileBL, documentId);
            paginationManager.paginateAndSaveContent(content); // Paginate and save pages after saving

        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to save file.");
        }
    }

    // Update an existing document with pagination
    public void updateFile(String fileName, String content) {
        try {
            if (fileBL.updateFile(fileName, content)) {
                JOptionPane.showMessageDialog(null, "File updated successfully.");

                // Retrieve document ID from DB for pagination association
                int documentId = fileBL.getFileIdByName(fileName);

                // Re-initialize PaginationManager with the updated document ID
                paginationManager = new PaginationManager(editorArea, pageLabel, fileBL, documentId);
                paginationManager.paginateAndSaveContent(content); // Paginate and save updated pages

            } else {
                JOptionPane.showMessageDialog(null, "File could not be updated.");
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to update file.");
        }
    }

    // Import multiple files from the filesystem
    public void importMultipleFiles(Path[] filePaths) {
        for (Path filePath : filePaths) {
            try {
                String content = Files.readString(filePath);
                fileBL.createFile(filePath.getFileName().toString(), content);
            } catch (IOException | SQLException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to import file: " + filePath.getFileName());
            }
        }
        JOptionPane.showMessageDialog(null, "Selected files imported successfully.");
    }

    // Delete a file from the database
    public void deleteFile(DefaultListModel<String> fileListModel, String fileName) {
        try {
            fileBL.deleteFile(fileName);
            fileListModel.removeElement(fileName);
            JOptionPane.showMessageDialog(null, "File deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to delete file.");
        }
    }

    // View and paginate an existing file's content
    public void viewFile(String fileName) {
        try {
            // Retrieve the document by its name
            FileDTO file = fileBL.readFile(fileName);
            if (file == null) {
                JOptionPane.showMessageDialog(null, "File not found.");
                return;
            }

            currentFileName = fileName;

            // Retrieve document ID for pagination association
            int documentId = fileBL.getFileIdByName(fileName);

            // Initialize PaginationManager with the document ID
            paginationManager = new PaginationManager(editorArea, pageLabel, fileBL, documentId);

            // Paginate and display the content
            paginationManager.paginateAndSaveContent(file.getContent());
            System.out.println("Pages after pagination: " + paginationManager.getTotalPages());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load file content.");
        }
    }

    // Set the current file name
    public void setCurrentFileName(String fileName) {
        currentFileName = fileName;
    }

    // Get the current file name
    public String getCurrentFileName() {
        return currentFileName;
    }

    // Getter for fileBL to enable access to business logic in other classes, if needed
    public FileBL getFileBL() {
        return fileBL;
    }
}
