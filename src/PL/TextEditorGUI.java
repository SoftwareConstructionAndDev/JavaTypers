
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import BL.FileManager;
import BL.FileService;
import DTO.FileDTO;
import DTO.FilePageDTO;
import javax.swing.event.DocumentListener; // to impliment wordcount
import javax.swing.event.DocumentEvent;
import javax.swing.text.Element;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TextEditorGUI extends JFrame {
	private static final Logger LOGGER = Logger.getLogger(TextEditorGUI.class.getName());
    private static JTextArea editorArea;
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JLabel pageLabel;
    private JButton lemmatizeButton;
    private FontSelector fontSelectorDialog;
    private String currentFileName = null;
    private List<FilePageDTO> pages = new ArrayList<>();
    private int currentPageIndex = 0;
    private static FileService fileBL;
    private static final HashMap<Character, String> TRANSLITERATION_TABLE = new HashMap<>();
    private JPanel filePanel;
    private JLabel wordCountLabel;
    


    static {// Populate the transliteration table
    	LOGGER.info("Populating transliteration table");
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

    public TextEditorGUI(FileService fileBL) {
        LOGGER.info("Initializing TextEditorGUI");
        this.fileBL = fileBL;
        try {
            this.fileBL = new FileManager(); // Assuming FileBL handles DB interactions
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize FileManager", e);
            e.printStackTrace();
        }

        // Threads for initializing frame and components
        Thread threadFrame = new Thread(() -> SwingUtilities.invokeLater(this::initializeFrame));
        Thread threadComponents = new Thread(() -> SwingUtilities.invokeLater(this::initializeComponents));

        // Start initialization threads
        threadFrame.start();
        threadComponents.start();

        // Wait for initialization threads to complete
        try {
            threadFrame.join();
            threadComponents.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Threads for setup functions
        Thread threadFilePanel = new Thread(() -> SwingUtilities.invokeLater(this::setupFilePanel));
        Thread threadEditorPanel = new Thread(() -> SwingUtilities.invokeLater(this::setupEditorPanel)); // Update panel
        Thread threadEditorPanel11 = new Thread(() -> SwingUtilities.invokeLater(this::setupEditorPanel11));
        Thread threadToolbar = new Thread(() -> SwingUtilities.invokeLater(this::setupToolbar));

        // Start setup threads
        threadFilePanel.start();
        threadEditorPanel.start();
        threadEditorPanel11.start();
        threadToolbar.start();

        // Wait for setup threads to complete
        try {
            threadFilePanel.join();
            threadEditorPanel.join();
            threadEditorPanel11.join();
            threadToolbar.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Initialize remaining UI elements on the EDT
        SwingUtilities.invokeLater(() -> {
            setupWordCountDisplay();        // Word count
            setupLemmatization();

            // Initialize the font selector dialog
            fontSelectorDialog = new FontSelector();

            loadFilesFromDB();
            setVisible(true);
        });
    }



    private void initializeFrame() {
    	LOGGER.info("Initializing components");
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
     
    //Function to set word counter; 
 // Part of the TextEditorGUI class

 // Initialize the word count label and add it to a panel
    private void setupWordCountDisplay() {
    	LOGGER.info("Setting up word count display");
        wordCountLabel = new JLabel("Words: 0 / 0");
        JPanel wordCountPanel = new JPanel(new BorderLayout());
        wordCountPanel.add(wordCountLabel, BorderLayout.WEST); // Align the label to the left side
        wordCountPanel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Add some padding for aesthetics

        // Ensure this panel is added to the bottom of the layout
        add(wordCountPanel, BorderLayout.SOUTH);
    }

    // Update the word count display
    private void updateWordCount() {
    	LOGGER.fine("Updating word count");
        String text = editorArea.getText();
        int totalWords = text.isEmpty() ? 0 : text.split("\\s+").length; // Calculate total words
        int caretPos = editorArea.getCaretPosition();
        int wordsFromCursor = 0;
        if (caretPos > 0 && caretPos <= text.length()) {
            String textBeforeCursor = text.substring(0, caretPos);
            wordsFromCursor = textBeforeCursor.isEmpty() ? 0 : textBeforeCursor.split("\\s+").length; // Words from start to cursor
        }

        wordCountLabel.setText("Words before cursor: " + wordsFromCursor + " / Total words: " + totalWords);
    }


 // Setup DocumentListener and CaretListener to update word count
 private void setupEditorPanel() {
	 LOGGER.info("Setting up editor panel for word count updates");
     JPanel editorPanel = new JPanel(new BorderLayout());
     editorArea = new JTextArea();
     editorArea.setLineWrap(true);
     editorArea.setWrapStyleWord(true);
     editorArea.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent e) { updateWordCount(); }
         public void removeUpdate(DocumentEvent e) { updateWordCount(); }
         public void changedUpdate(DocumentEvent e) { updateWordCount(); }
     });
     editorArea.addCaretListener(e -> updateWordCount());

     JScrollPane scrollPane = new JScrollPane(editorArea);
     editorPanel.add(scrollPane, BorderLayout.CENTER);
     add(editorPanel, BorderLayout.CENTER);
 }


    private void setupFilePanel() {
    	LOGGER.info("Setting up file panel");
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



    private void setupEditorPanel11() {
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
    	 LOGGER.info("Setting up toolbar");
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
        LOGGER.info("Attempting to toggle file panel visibility");
        if (filePanel != null) {
            boolean isVisible = filePanel.isVisible();
            LOGGER.info("File panel is currently visible: " + isVisible);
            filePanel.setVisible(!isVisible);
            LOGGER.info("File panel visibility should now be set to: " + !isVisible);
        } else {
            LOGGER.severe("File panel is null and cannot be toggled");
        }
    }



    private JButton createStyledButton(String text) {
    	LOGGER.fine("Creating styled button");
        JButton button = new JButton(text);
        button.setBackground(new Color(0x5A008C));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
    private JButton createStyledButton(String text, String iconPath) {
    	LOGGER.fine("Creating styled button with icon");
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
    	LOGGER.info("Creating new file");
        currentFileName = null; // Reset the current file name
        editorArea.setText(""); // Clear the text area
        pages.clear(); // Clear the pages list
        currentPageIndex = 0; // Reset the page index
        pageLabel.setText("Page: 0 / 0"); // Update the page label
    }

   private void saveFile() {
	   LOGGER.info("Saving file");
    try {
        String content = editorArea.getText();

        if (content == null || content.trim().isEmpty()) {
        	LOGGER.warning("Attempted to save an empty file");
            JOptionPane.showMessageDialog(this, "Cannot save an empty file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentFileName == null) {
        	LOGGER.warning("File name input was cancelled or empty");
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
    	LOGGER.info("Deleting selected file");
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
    	 LOGGER.info("Loading files from database");
        try {
            List<FileDTO> files = fileBL.listAllFiles();
            LOGGER.info("Files loaded from database");
            fileListModel.clear();
            for (FileDTO file : files) fileListModel.addElement(file.getFileName());
        } catch (SQLException e) {
        	LOGGER.log(Level.SEVERE, "Failed to load files from database", e);
            JOptionPane.showMessageDialog(this, "Failed to load files from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFile(String fileName) {
    	 LOGGER.info("Loading file: " + fileName);
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
    	LOGGER.info("Paginating content");
        try {
            pages = fileBL.paginateAndSaveContent(fileBL.getFileIdByName(currentFileName), content);
            currentPageIndex = 0;
            updateEditorPage();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error during pagination.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEditorPage() {
    	 LOGGER.fine("Updating editor page");
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
    	LOGGER.info("Navigating to next page");
        if (currentPageIndex < pages.size() - 1) {
            currentPageIndex++;
            updateEditorPage();
        } else {
            JOptionPane.showMessageDialog(this, "You've reached the last page.");
        }
    }

    private void prevPage() {
    	 LOGGER.info("Navigating to previous page");
        if (currentPageIndex > 0) {
            currentPageIndex--;
            updateEditorPage();
        } else {
            JOptionPane.showMessageDialog(this, "You're on the first page.");
        }
    }

    

    private void analyzeFiles() {
    	LOGGER.info("Analyzing files");
    	// Create a new instance of the AnalyzeManager screen and make it visible
        AnalysisManager analyzeManager = new AnalysisManager(null);
        analyzeManager.setVisible(true);
    }

    

    private void setupLemmatization() {
    	LOGGER.info("Setting up lemmatization");
        editorArea.addCaretListener(new LemmatizationCaretListener());

        lemmatizeButton = createStyledButton("Lemmatize");
        lemmatizeButton.setVisible(false);
        lemmatizeButton.addActionListener(e -> lemmatizeSelectedText());

        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(lemmatizeButton, JLayeredPane.POPUP_LAYER);
    }

    private void lemmatizeSelectedText() {
        LOGGER.fine("Attempting to lemmatize selected text");
        String selectedText = editorArea.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            new Thread(() -> {
                LOGGER.info("Starting new thread for lemmatization");
                String lemmatizationResult = performLemmatization(selectedText);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, lemmatizationResult, 
                        "Lemmatization Result", JOptionPane.INFORMATION_MESSAGE);
                    LOGGER.fine("Lemmatization result displayed");
                });
            }).start();
        } else {
            LOGGER.warning("No text selected for lemmatization");
        }
    }


    private String performLemmatization(String arabicText) {
        LOGGER.info("Performing lemmatization on text");
        try {
            File jarFile = new File("C:\\Users\\Admin\\Downloads\\lastphase\\ZainabEman\\src\\lib\\AlKhalil-2.1.21.jar");
            if (!jarFile.exists()) {
                LOGGER.severe("JAR file not found at " + jarFile.getAbsolutePath());
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
                LOGGER.info("No lemmas found for the text");
                return "No lemmas found for: " + arabicText;
            }

            LOGGER.info("Lemmatization successful");
            return "Lemmas: " + String.join(", ", lemmas);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during lemmatization: " + e.getMessage(), e);
            return "Error during lemmatization: " + e.getMessage();
        }
    }

    private class LemmatizationCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            LOGGER.fine("Caret update event received");
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
                    LOGGER.info("Lemmatize button positioned and made visible");
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to position the lemmatize button due to an exception", ex);
                    lemmatizeButton.setVisible(false);
                }
            } else {
                lemmatizeButton.setVisible(false);
                LOGGER.info("Lemmatize button made invisible as no text is selected or text is empty");
            }
        }
    }

    //====================================================================
    //search function
    private void searchContent(String searchText) {
        LOGGER.info("Initiating content search");
        if (searchText == null || searchText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search text cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.warning("Search text was empty");
            return;
        }

        LOGGER.fine("Searching for files containing text: " + searchText);
        java.util.List<String> matchingFiles = searchFilesContainingText(searchText);

        if (matchingFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files found containing the specified text.", "No Matches", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info("No files found containing the specified text");
            return;
        }

        JList<String> fileList = new JList<>(new DefaultListModel<>());
        DefaultListModel<String> listModel = (DefaultListModel<String>) fileList.getModel();
        matchingFiles.forEach(file -> {
            listModel.addElement(file);
            LOGGER.fine("File added to search results: " + file);
        });

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        LOGGER.info("Displaying search results");
        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Search Results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String selectedFile = fileList.getSelectedValue();
            if (selectedFile != null) {
                LOGGER.info("File selected from search results: " + selectedFile);
                // viewFile(selectedFile); // Uncomment or modify this line to handle file viewing.
            } else {
                LOGGER.info("No file selected from search results");
            }
        } else {
            LOGGER.info("Search results dialog was canceled");
        }
    }

    
    public List<String> searchFilesContainingText(String searchText) {
        LOGGER.info("Searching for files containing specified text");
        List<String> matchingFiles = new ArrayList<>();
        try {
            List<FileDTO> allFiles = fileBL.listAllFiles();
            LOGGER.fine("Retrieved list of all files for searching text");

            for (FileDTO file : allFiles) {
                if (file.getContent().contains(searchText)) {
                    matchingFiles.add(file.getFileName());
                    LOGGER.fine("File containing search text found: " + file.getFileName());
                }
            }

            if (matchingFiles.isEmpty()) {
                LOGGER.info("No files found containing the search text");
            } else {
                LOGGER.info("Total files found containing the search text: " + matchingFiles.size());
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL exception occurred while searching files", e);
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
            fontSelectorDialog = new FontSelector(); // Initialize if not already done
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

    private static void processSelectedText() throws Exception {
    	JFrame frame = new JFrame("Arabic POS Tagger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
    	FileService fileBL = new FileManager();
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
