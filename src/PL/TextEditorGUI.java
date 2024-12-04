

package PL;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import BL.FileBL;
import BL.I_File;
import DTO.FileDTO;
import DTO.FilePageDTO;

public class TextEditorGUI extends JFrame {
    private static JTextArea editorArea;
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JLabel pageLabel;
    private JButton lemmatizeButton;
    private fontSelector fontSelectorDialog;
    private String currentFileName = null;
    private List<FilePageDTO> pages = new ArrayList<>();
    private int currentPageIndex = 0;
    private static I_File fileBL;
    private static final HashMap<Character, String> TRANSLITERATION_TABLE = new HashMap<>();
    private JPanel filePanel;
    


    static {// Populate the transliteration table
    	TRANSLITERATION_TABLE.put('ا', "a");
    	TRANSLITERATION_TABLE.put('ب', "b");
    	TRANSLITERATION_TABLE.put('ت', "t");
    	TRANSLITERATION_TABLE.put('ث', "th");
    	TRANSLITERATION_TABLE.put('ج', "j");
    	TRANSLITERATION_TABLE.put('ح', "ḥ");
    	TRANSLITERATION_TABLE.put('خ', "kh");
    	TRANSLITERATION_TABLE.put('د', "d");
    	TRANSLITERATION_TABLE.put('ذ', "dh");
    	TRANSLITERATION_TABLE.put('ر', "r");
    	TRANSLITERATION_TABLE.put('ز', "z");
    	TRANSLITERATION_TABLE.put('س', "s");
    	TRANSLITERATION_TABLE.put('ش', "sh");
    	TRANSLITERATION_TABLE.put('ص', "ṣ");
    	TRANSLITERATION_TABLE.put('ض', "ḍ");
    	TRANSLITERATION_TABLE.put('ط', "ṭ");
    	TRANSLITERATION_TABLE.put('ظ', "ẓ");
    	TRANSLITERATION_TABLE.put('ع', "ʿ");
    	TRANSLITERATION_TABLE.put('غ', "gh");
    	TRANSLITERATION_TABLE.put('ف', "f");
    	TRANSLITERATION_TABLE.put('ق', "q");
    	TRANSLITERATION_TABLE.put('ك', "k");
    	TRANSLITERATION_TABLE.put('ل', "l");
    	TRANSLITERATION_TABLE.put('م', "m");
    	TRANSLITERATION_TABLE.put('ن', "n");
    	TRANSLITERATION_TABLE.put('ه', "h");
    	TRANSLITERATION_TABLE.put('و', "w");
    	TRANSLITERATION_TABLE.put('ي', "y");

    	// Long vowels
    	TRANSLITERATION_TABLE.put('آ', "ā");
    	TRANSLITERATION_TABLE.put('ى', "ā"); // Alif Maqṣūrah
    	TRANSLITERATION_TABLE.put('و', "ū");
    	TRANSLITERATION_TABLE.put('ي', "ī");

    	// Short vowels
    	TRANSLITERATION_TABLE.put('َ', "a");
    	TRANSLITERATION_TABLE.put('ُ', "u");
    	TRANSLITERATION_TABLE.put('ِ', "i");

    	// Sukūn
    	TRANSLITERATION_TABLE.put('ْ', "");

    	// Tanween
    	TRANSLITERATION_TABLE.put('ً', "an");
    	TRANSLITERATION_TABLE.put('ٌ', "un");
    	TRANSLITERATION_TABLE.put('ٍ', "in");

    	// Shadda
    	TRANSLITERATION_TABLE.put('ّ', ""); // Doubles the letter

    	// Diphthongs
//    	TRANSLITERATION_TABLE.put('وَ', "aw");
//    	TRANSLITERATION_TABLE.put('يَ', "ay");

    	// Hamza
    	TRANSLITERATION_TABLE.put('ء', "ʾ");
    	TRANSLITERATION_TABLE.put('أ', "ʾa");
    	TRANSLITERATION_TABLE.put('إ', "ʾi");
    	TRANSLITERATION_TABLE.put('ؤ', "ʾu");
    	TRANSLITERATION_TABLE.put('ئ', "ʾy");

    	// Special Cases
    	TRANSLITERATION_TABLE.put('ة', "h"); // Tāʾ Marbūṭah (at end as 'ah')
    	TRANSLITERATION_TABLE.put('ٱ', "a"); // Alif Wasl
    	TRANSLITERATION_TABLE.put('ﻻ', "lā"); // Lam-Alif Ligature
    	TRANSLITERATION_TABLE.put('ﻹ', "liʾ");
    	TRANSLITERATION_TABLE.put('ﻷ', "laʾ");
    	TRANSLITERATION_TABLE.put('ﻵ', "lāʾ");

    }

    public TextEditorGUI(I_File fileBL) {
        this.fileBL = fileBL;
		try {
			this.fileBL = new FileBL();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Assuming FileBL handles DB interactions
        initializeFrame();
        initializeComponents();
        setupFilePanel();
        setupEditorPanel();
        setupToolbar();
        setupLemmatization();

        // Initialize the font selector dialog
        fontSelectorDialog = new fontSelector();

        loadFilesFromDB();
        setVisible(true);
    }


    private void initializeFrame() {
        setTitle("Arabic Text Editor with File Management");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void initializeComponents() {
        editorArea = new JTextArea();
        editorArea.setLineWrap(true);
        editorArea.setWrapStyleWord(true);
        pageLabel = new JLabel("Page: 0 / 0", JLabel.CENTER);

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);

        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    loadFile(selectedFile);
                }
            }
        });

        filePanel = new JPanel(new BorderLayout());
        filePanel.add(new JLabel("Files", JLabel.CENTER), BorderLayout.NORTH);
        filePanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        // Main Layout
        setLayout(new BorderLayout());
        add(filePanel, BorderLayout.WEST); // Add the sidebar (initially visible)
        add(new JScrollPane(editorArea), BorderLayout.CENTER); // Add the editor in the center

        loadFilesFromDB(); // Populate the sidebar with files
    }


    private void setupFilePanel() {
        JPanel filePanel = new JPanel(new BorderLayout());
        
        // Set a preferred size for the sidebar
        filePanel.setPreferredSize(new Dimension(200, getHeight())); // Adjust width as needed

        fileList = new JList<>(fileListModel);

        // Set a larger font for the file list items
        fileList.setFont(new Font("SansSerif", Font.PLAIN, 16)); // Adjust font size (16) as needed

        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    loadFile(selectedFile);
                }
            }
        });

        filePanel.add(new JLabel("Files", JLabel.CENTER), BorderLayout.NORTH);
        filePanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        add(filePanel, BorderLayout.WEST);
    }



    private void setupEditorPanel() {
        JPanel editorPanel = new JPanel(new BorderLayout());
        JPanel pageControlPanel = new JPanel(new BorderLayout());
        JButton prevButton = createStyledButton("<");
        prevButton.addActionListener(e -> prevPage());
        JButton nextButton = createStyledButton(">");
        nextButton.addActionListener(e -> nextPage());
        pageControlPanel.add(prevButton, BorderLayout.WEST);
        pageControlPanel.add(pageLabel, BorderLayout.CENTER);
        pageControlPanel.add(nextButton, BorderLayout.EAST);
        editorPanel.add(new JScrollPane(editorArea), BorderLayout.CENTER);
        editorPanel.add(pageControlPanel, BorderLayout.SOUTH);
        add(editorPanel, BorderLayout.CENTER);
    }

    private void setupToolbar() {
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton[] buttons = {
            createToolbarButton("Toggle File Panel", "/resources/eye.png", e -> toggleFilePanel()),
            createToolbarButton("New", "/resources/add.png", e -> newFile()),
            createToolbarButton("Import", "/resources/import.png", e -> importFiles()),
            createToolbarButton("Save", "/resources/save.png", e -> saveFile()),
            createToolbarButton("Delete", "/resources/delete.png", e -> deleteSelectedFile()),
            createToolbarButton("Font", "/resources/font.png", e -> openFontSelector()),
            createToolbarButton("Transliterate", "/resources/tranliterate.png", e -> transliterateContent()),
            createToolbarButton("POS Tags", "/resources/tag.png", e -> {
                try {
                    processSelectedText();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }),
            createToolbarButton("Analyze Files", "/resources/analysis.png", e -> {
                try {
                    analyzeFiles();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            })
        };

        // Add buttons to the toolbar panel
        for (JButton button : buttons) {
            toolbarPanel.add(button);
        }

        // Create search field and add search button with icon
        JTextField searchField = new JTextField(17);  // Adjusted field width
        JButton searchButton = createToolbarButton("Search", "/resources/search.png", e -> searchContent(searchField.getText()));
        toolbarPanel.add(searchField);
        toolbarPanel.add(searchButton);

        // Add the toolbar panel to the frame
        add(toolbarPanel, BorderLayout.NORTH);
    }


private void toggleFilePanel() {
        if (filePanel != null) {
            boolean isVisible = filePanel.isVisible();
            filePanel.setVisible(!isVisible); // Toggle visibility
        }
    }


    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0x5A008C));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
    private JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton();
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                button.setIcon(icon);
                button.setToolTipText(text); // Set tooltip to the button text
            } catch (Exception ex) {
                button.setText(text); // Fallback to text if icon fails to load
                System.err.println("Failed to load icon: " + iconPath);
            }
        } else {
            button.setText(text); // Set text if no icon path is provided
        }

        // Set styling
        button.setBackground(new Color(0x5A008C));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        // Increase the button size
        button.setPreferredSize(new Dimension(60, 40)); // Adjust width and height as needed

        return button;
    }

    private JButton createToolbarButton(String text, String iconPath, ActionListener action) {
        JButton button = createStyledButton(text, iconPath);
        button.addActionListener(action);
        return button;
    }

    private void newFile() {
        currentFileName = null; // Reset the current file name
        editorArea.setText(""); // Clear the text area
        pages.clear(); // Clear the pages list
        currentPageIndex = 0; // Reset the page index
        pageLabel.setText("Page: 0 / 0"); // Update the page label
    }

   private void saveFile() {
    try {
        String content = editorArea.getText();

        if (content == null || content.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot save an empty file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentFileName == null) {
            currentFileName = JOptionPane.showInputDialog(this, "Enter file name:", "Save File", JOptionPane.PLAIN_MESSAGE);
            if (currentFileName == null || currentFileName.trim().isEmpty()) {
                return;
            }
        }

        // Save the file
        boolean success = fileBL.createFile(currentFileName, content);

        if (!success) {
            JOptionPane.showMessageDialog(this, "File content is duplicate and cannot be saved.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Refresh the side bar (file list)
        loadFilesFromDB();
        
        // Paginate content
        paginateContent(content);

        JOptionPane.showMessageDialog(this, "File saved successfully.");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (NoSuchAlgorithmException e) {
        JOptionPane.showMessageDialog(this, "Algorithm Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private void deleteSelectedFile() {
        String selectedFile = fileList.getSelectedValue();
        if (selectedFile != null) {
            try {
                fileBL.deleteFile(selectedFile);
                fileListModel.removeElement(selectedFile);
                editorArea.setText("");
                pages.clear();
                updateEditorPage();
                JOptionPane.showMessageDialog(this, "File deleted successfully.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    

    private void loadFilesFromDB() {
        try {
            List<FileDTO> files = fileBL.listAllFiles();
            fileListModel.clear();
            for (FileDTO file : files) fileListModel.addElement(file.getFileName());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load files from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFile(String fileName) {
        try {
            FileDTO file = fileBL.readFile(fileName);
            if (file == null) {
                JOptionPane.showMessageDialog(this, "File not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentFileName = fileName;
            paginateContent(file.getContent());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading file content.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void paginateContent(String content) {
        try {
            pages = fileBL.paginateAndSaveContent(fileBL.getFileIdByName(currentFileName), content);
            currentPageIndex = 0;
            updateEditorPage();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error during pagination.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEditorPage() {
        if (pages.isEmpty()) {
            editorArea.setText("No content available.");
            pageLabel.setText("Page: 0 / 0");
            return;
        }
        FilePageDTO currentPage = pages.get(currentPageIndex);
        editorArea.setText(currentPage.getContent());
        pageLabel.setText("Page: " + (currentPageIndex + 1) + " / " + pages.size());
    }

    private void nextPage() {
        if (currentPageIndex < pages.size() - 1) {
            currentPageIndex++;
            updateEditorPage();
        } else {
            JOptionPane.showMessageDialog(this, "You've reached the last page.");
        }
    }

    private void prevPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            updateEditorPage();
        } else {
            JOptionPane.showMessageDialog(this, "You're on the first page.");
        }
    }

    

    private void analyzeFiles() {
    	// Create a new instance of the AnalyzeManager screen and make it visible
        //AnalysisManager analyzeManager = new AnalysisManager();
        //analyzeManager.setVisible(true);
    }

    

    private void setupLemmatization() {
        editorArea.addCaretListener(new LemmatizationCaretListener());

        lemmatizeButton = createStyledButton("Lemmatize");
        lemmatizeButton.setVisible(false);
        lemmatizeButton.addActionListener(e -> lemmatizeSelectedText());

        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(lemmatizeButton, JLayeredPane.POPUP_LAYER);
    }

    private void lemmatizeSelectedText() {
        String selectedText = editorArea.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            new Thread(() -> {
                String lemmatizationResult = performLemmatization(selectedText);
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, lemmatizationResult, 
                        "Lemmatization Result", JOptionPane.INFORMATION_MESSAGE)
                );
            }).start();
        }
    }

    private String performLemmatization(String arabicText) {
        try {
            File jarFile = new File("C:\\Users\\Mamai Nataki\\Desktop\\jars\\AlKhalil-2.1.21.jar");
            if (!jarFile.exists()) {
                return "Error: JAR file not found at " + jarFile.getAbsolutePath();
            }

            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
            Class<?> analyzerClass = classLoader.loadClass("AlKhalil2.AnalyzedWords");
            Object analyzerInstance = analyzerClass.getDeclaredConstructor().newInstance();
            Method analyzeMethod = analyzerClass.getMethod("analyzedWords", String.class);

            LinkedList<?> results = (LinkedList<?>) analyzeMethod.invoke(analyzerInstance, arabicText);
            LinkedHashSet<String> lemmas = new LinkedHashSet<>();

            for (Object result : results) {
                java.lang.reflect.Field lemmeField = result.getClass().getDeclaredField("lemme");
                lemmeField.setAccessible(true);
                String lemma = (String) lemmeField.get(result);
                lemmas.add(lemma);
            }

            classLoader.close();

            if (lemmas.isEmpty()) {
                return "No lemmas found for: " + arabicText;
            }

            return "Lemmas: " + String.join(", ", lemmas);

        } catch (Exception e) {
            return "Error during lemmatization: " + e.getMessage();
        }
    }
    private class LemmatizationCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            String selectedText = editorArea.getSelectedText();
            if (selectedText != null && !selectedText.trim().isEmpty()) {
                try {
                    Rectangle viewRect = editorArea.modelToView2D(editorArea.getSelectionStart()).getBounds();
                    lemmatizeButton.setBounds(
                        (int) viewRect.getX() + editorArea.getLocationOnScreen().x - getLocationOnScreen().x,
                        (int) viewRect.getY() + editorArea.getLocationOnScreen().y - getLocationOnScreen().y,
                        100, 30
                    );
                    lemmatizeButton.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lemmatizeButton.setVisible(false);
                }
            } else {
                lemmatizeButton.setVisible(false);
            }
        }
    }
    //====================================================================
    //search function
    private void searchContent(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search text cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.util.List<String> matchingFiles = searchFilesContainingText(searchText);

        if (matchingFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files found containing the specified text.", "No Matches", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JList<String> fileList = new JList<>(new DefaultListModel<>());
        DefaultListModel<String> listModel = (DefaultListModel<String>) fileList.getModel();
        matchingFiles.forEach(listModel::addElement);

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Search Results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String selectedFile = fileList.getSelectedValue();
            if (selectedFile != null) {
                //viewFile(selectedFile);
            }
        }
    }
    
    public List<String> searchFilesContainingText(String searchText) {
        List<String> matchingFiles = new ArrayList<>();
        try {
            List<FileDTO> allFiles = fileBL.listAllFiles();
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
    /*public void viewFile(String fileName) {
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
    }*/

    //=======================T R N S L I T E R A T I O N
    private String transliterateText(String arabicText) {
        if (arabicText == null || arabicText.isEmpty()) {
            return "No content to transliterate.";
        }

        StringBuilder transliteratedText = new StringBuilder();
        for (char c : arabicText.toCharArray()) {
            // Use getOrDefault to handle missing characters in the table
            transliteratedText.append(TRANSLITERATION_TABLE.getOrDefault(c, String.valueOf(c)));
        }
        return transliteratedText.toString();
    }

    private void transliterateContent() {
        String content = editorArea.getText();

        if (content == null || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No content to transliterate.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Transliterate the content
            String transliteratedContent = transliterateText(content);

            // Create a vertical box to display the transliteration page by page
            JFrame transliterationWindow = new JFrame("Transliteration Result");
            transliterationWindow.setSize(400, 600);
            transliterationWindow.setLayout(new BorderLayout());

            JTextArea transliterationArea = new JTextArea(transliteratedContent);
            transliterationArea.setLineWrap(true);
            transliterationArea.setWrapStyleWord(true);
            transliterationArea.setEditable(false);
            transliterationArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

            // Add a scrollable pane for vertical navigation
            JScrollPane scrollPane = new JScrollPane(transliterationArea, 
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            transliterationWindow.add(scrollPane, BorderLayout.CENTER);

            // Display the transliteration window
            transliterationWindow.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error during transliteration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    //===================VIEW 1 FILE===================
    private void viewOneFile() {
        String fileName = JOptionPane.showInputDialog(this, "Enter File Name to View:", "View File", JOptionPane.PLAIN_MESSAGE);

        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                FileDTO file = fileBL.readFile(fileName.trim()); // Fetch file details from the business logic
                if (file != null) {
                    // Populate the editor area with file content and metadata
                    editorArea.setText(
                        "----- File Details -----\n" +
                        "File Name: " + file.getFileName() + "\n" +
                        "Created At: " + file.getCreatedAt() + "\n" +
                        "Updated At: " + file.getUpdatedAt() + "\n\n" +
                        "----- Content -----\n" +
                        file.getContent()
                    );

                    // Hide or move the sidebar (file panel)
                    if (filePanel != null && filePanel.isVisible()) {
                        filePanel.setVisible(false); // Hide the sidebar
                    }

                    JOptionPane.showMessageDialog(this, "File \"" + fileName + "\" loaded successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "File \"" + fileName + "\" not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void importFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true); // Allow selecting multiple files

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles(); // Get the selected files
            StringBuilder successFiles = new StringBuilder(); // Track successfully imported files
            StringBuilder duplicateFiles = new StringBuilder(); // Track duplicate content files
            StringBuilder renamedFiles = new StringBuilder(); // Track renamed files due to duplicate names

            for (File file : selectedFiles) {
                try {
                    String content = Files.readString(file.toPath()); // Read file content as a string

                    // Check for duplicate content
                    if (fileBL.fileHashExists(content)) {
                        duplicateFiles.append(file.getName()).append("\n"); // Track duplicate files
                        continue;
                    }

                    // Use the BL layer to handle duplicate file names
                    String uniqueFileName = fileBL.generateUniqueFileName(file.getName());

                    // Import the file with a unique name
                    boolean success = fileBL.createPaginatedFile(uniqueFileName, content);
                    if (success) {
                        successFiles.append(uniqueFileName).append("\n");
                        if (!uniqueFileName.equals(file.getName())) {
                            renamedFiles.append(file.getName()).append(" -> ").append(uniqueFileName).append("\n");
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Failed to import file: " + file.getName() + "\nError: " + e.getMessage(),
                            "Import Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Show a summary message of the import process
            StringBuilder summary = new StringBuilder("Import Summary:\n");
            if (successFiles.length() > 0) {
                summary.append("Successfully Imported Files:\n").append(successFiles).append("\n");
            }
            if (renamedFiles.length() > 0) {
                summary.append("Renamed Files (Due to Duplicate Names):\n").append(renamedFiles).append("\n");
            }
            if (duplicateFiles.length() > 0) {
                summary.append("Duplicate Content Files (Not Imported):\n").append(duplicateFiles).append("\n");
            }

            JOptionPane.showMessageDialog(this, summary.toString(), "Import Results", JOptionPane.INFORMATION_MESSAGE);

            // Refresh the file list in the UI
            loadFilesFromDB();
        }
    }
    private void openFontSelector() {
        if (fontSelectorDialog == null) {
            fontSelectorDialog = new fontSelector(); // Initialize if not already done
        }
        fontSelectorDialog.setVisible(true); // Show the font selector dialog
        Font selectedFont = fontSelectorDialog.returnFont();
        if (selectedFont != null) {
            editorArea.setFont(selectedFont); // Apply the selected font to the editor
        }
    }
   
    private void updateFile() {
        try {
            String fileName = JOptionPane.showInputDialog(this, "Enter the name of the file to update:", "Update File", JOptionPane.PLAIN_MESSAGE);
            if (fileName == null || fileName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "File name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String content = editorArea.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Content cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (fileBL.updateFile(fileName, content)) {
                JOptionPane.showMessageDialog(this, "File updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();  // Clear the input fields after update
                loadFilesFromDB(); // Refresh the file list in the sidebar
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update file. Check if the file exists or content is duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void clearFields() {
        editorArea.setText("");
        pageLabel.setText("Page: 1");
    }
    
    
  //POS Tagging
    private static void setupContextMenu() {
        // Create a popup menu for POS tags
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem posTagsItem = new JMenuItem("POS Tags");
        popupMenu.add(posTagsItem);

        // Add MouseListener to JTextArea for right-click
        editorArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(editorArea, e.getX(), e.getY());
                }
            }
        });

        // Add action listener for the POS Tags menu item
        posTagsItem.addActionListener(e -> {
			try {
				processSelectedText();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
    }

    private static void processSelectedText() throws Exception {
    	JFrame frame = new JFrame("Arabic POS Tagger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
    	I_File fileBL = new FileBL();
        String selectedText = editorArea.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            try {
                String posTag = fileBL.processTextForPOSTags(selectedText);
                JOptionPane.showMessageDialog(frame, "POS Tag for selected text: " + posTag,
                                              "POS Tag", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error fetching POS tag: " + ex.getMessage(),
                                              "POS Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "No text selected.",
                                          "Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextEditorGUI(fileBL));
    }

}
