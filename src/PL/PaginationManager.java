package PL;
import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import BL.FileBL;
import DTO.FilePageDTO;

public class PaginationManager {
    private static final int WORDS_PER_PAGE = 500;

    private final JTextArea editorArea;
    private final JLabel pageLabel;
    private final FileBL fileBL;
    private List<FilePageDTO> pages;
    private int currentPageIndex;
    private int documentId;

    public PaginationManager(JTextArea editorArea, JLabel pageLabel, FileBL fileBL, int documentId) {
        this.editorArea = editorArea;
        this.pageLabel = pageLabel;
        this.fileBL = fileBL;
        this.documentId = documentId;
        this.pages = new ArrayList<>();
        this.currentPageIndex = 0;

        System.out.println("PaginationManager initialized with documentId: " + documentId);
    }
 // Paginate content and save pages to the database
    public void paginateAndSaveContent(String content) {
        try {
            System.out.println("Starting pagination for content...");
            pages.clear(); // Clear existing pages

            String[] words = content.split("\\s+");
            StringBuilder pageContent = new StringBuilder();
            int pageNumber = 1;

            for (int i = 0; i < words.length; i++) {
                pageContent.append(words[i]).append(" ");
                if ((i + 1) % WORDS_PER_PAGE == 0 || i == words.length - 1) {
                    // Save the current page
                    String pageText = pageContent.toString().trim();
                    System.out.println("Page " + pageNumber + " content: " + pageText);

                    FilePageDTO page = new FilePageDTO(0, documentId, pageNumber++, pageText);
                    fileBL.saveFilePage(page); // Save the page to DB
                    pages.add(page); // Add to the list

                    pageContent.setLength(0); // Reset for the next page
                }
            }

            System.out.println("Total pages created: " + pages.size());
            currentPageIndex = 0; // Start at the first page
            updateEditorPage();
        } catch (SQLException e) {
            showError("Error during pagination and saving content", e);
        }
    }

    private void showError(String message, Exception e) {
        // Log the error to the console for debugging
        System.err.println("Error: " + message);
        e.printStackTrace();

        // Display the error in a dialog box for the user
        JOptionPane.showMessageDialog(
            null, 
            message + ": " + e.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }

    // Method to save file and paginate content
    public void saveAndPaginateContent(String fileName, String content) {
        try {
            // Use FileBL method to save file and paginate
            fileBL.saveFileAndPaginate(fileName, content);
            
            // Load the pages after saving
            int fileId = fileBL.getFileIdByName(fileName);
            loadPages(fileId);
        } catch (SQLException e) {
            showError("Error saving and paginating content", e);
        }
    }

    public void loadPages(int documentId) {
        try {
            System.out.println("Loading pages for document ID: " + documentId);
            this.documentId = documentId;
            this.pages = fileBL.getAllPagesForDocument(documentId);

            System.out.println("Total pages loaded: " + pages.size());
            for (int i = 0; i < pages.size(); i++) {
                System.out.println("Page " + (i + 1) + " content: " + pages.get(i).getContent());
            }

            if (pages.isEmpty()) {
                System.err.println("No pages found for document ID: " + documentId);
                editorArea.setText("No content available.");
                pageLabel.setText("Page: 0 / 0");
                return;
            }

            currentPageIndex = 0; // Reset to the first page
            updateEditorPage();
        } catch (SQLException e) {
            showError("Error loading pages", e);
        }
    }



    public void nextPage() {
        System.out.println("Current page index before next: " + currentPageIndex);
        if (pages == null || pages.isEmpty()) {
            System.err.println("No pages available.");
            return;
        }

        if (currentPageIndex < pages.size() - 1) {
            currentPageIndex++;
            System.out.println("Navigating to next page, new index: " + currentPageIndex);
            updateEditorPage();
        } else {
            System.out.println("Already on the last page.");
            JOptionPane.showMessageDialog(null, "You've reached the last page.", "Navigation", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void prevPage() {
        System.out.println("Current page index before previous: " + currentPageIndex);
        if (pages == null || pages.isEmpty()) {
            System.err.println("No pages available.");
            return;
        }

        if (currentPageIndex > 0) {
            currentPageIndex--;
            System.out.println("Navigating to previous page, new index: " + currentPageIndex);
            updateEditorPage();
        } else {
            System.out.println("Already on the first page.");
            JOptionPane.showMessageDialog(null, "You're on the first page.", "Navigation", JOptionPane.INFORMATION_MESSAGE);
        }
    }

//    // Update editor with current page content
//    private void updateEditorPage() {
//        if (pages == null || pages.isEmpty()) {
//            editorArea.setText("No content available.");
//            pageLabel.setText("Page: 0 / 0");
//            return;
//        }
//
//        try {
//            FilePageDTO currentPage = pages.get(currentPageIndex);
//            
//            // Display page content
//            editorArea.setText(currentPage.getContent());
//            
//            // Update page label
//            pageLabel.setText("Page: " + (currentPageIndex + 1) + " / " + pages.size());
//            
//            // Debug logging
//            System.out.println("Displaying Page " + (currentPageIndex + 1) + 
//                               " of " + pages.size());
//        } catch (IndexOutOfBoundsException e) {
//            System.err.println("Invalid page index: " + currentPageIndex);
//            editorArea.setText("Error displaying page.");
//            pageLabel.setText("Page: 0 / 0");
//        }
//    }
    private void updateEditorPage() {
        System.out.println("Updating editor page...");
        if (pages == null || pages.isEmpty()) {
            System.err.println("No pages available to display.");
            editorArea.setText("No content available.");
            pageLabel.setText("Page: 0 / 0");
            return;
        }

        try {
            FilePageDTO currentPage = pages.get(currentPageIndex);
            System.out.println("Displaying page index: " + currentPageIndex + ", Content: " + currentPage.getContent());

            editorArea.setText(currentPage.getContent());
            pageLabel.setText("Page: " + (currentPageIndex + 1) + " / " + pages.size());
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Invalid page index: " + currentPageIndex);
            editorArea.setText("Error displaying page.");
            pageLabel.setText("Page: 0 / 0");
        }
    }

    // Getter for current page number
    public int getCurrentPageNumber() {
        return currentPageIndex + 1;
    }

    // Getter for total pages
    public int getTotalPages() {
        return pages != null ? pages.size() : 0;
    }
}