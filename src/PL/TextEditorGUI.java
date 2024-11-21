
package PL;


import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.lang.reflect.Method;

import BL.FileBL;

public class TextEditorGUI extends JFrame {
    private JTextArea editorArea;
    private JPanel filePanel;
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JLabel pageLabel;
    private FileManager fileManager;
    private PaginationManager paginationManager;
    private JButton transliterateButton;
    private JButton lemmatizeButton;
    private boolean isFilePanelVisible = true;
    private fontSelector fontSelectorDialog;

    // Transliteration table
    private static final HashMap<Character, String> TRANSLITERATION_TABLE = new HashMap<>();

    static {
        // Populate the transliteration table
        TRANSLITERATION_TABLE.put('ا', "a");
        TRANSLITERATION_TABLE.put('ب', "b");
        TRANSLITERATION_TABLE.put('ت', "t");
        TRANSLITERATION_TABLE.put('ث', "th");
        TRANSLITERATION_TABLE.put('ج', "j");
        TRANSLITERATION_TABLE.put('ح', "H");
        TRANSLITERATION_TABLE.put('خ', "kh");
        TRANSLITERATION_TABLE.put('د', "d");
        TRANSLITERATION_TABLE.put('ذ', "dh");
        TRANSLITERATION_TABLE.put('ر', "r");
        TRANSLITERATION_TABLE.put('ز', "z");
        TRANSLITERATION_TABLE.put('س', "s");
        TRANSLITERATION_TABLE.put('ش', "sh");
        TRANSLITERATION_TABLE.put('ص', "S");
        TRANSLITERATION_TABLE.put('ض', "D");
        TRANSLITERATION_TABLE.put('ط', "T");
        TRANSLITERATION_TABLE.put('ظ', "DH");
        TRANSLITERATION_TABLE.put('ع', "3");
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
        TRANSLITERATION_TABLE.put('َ', "a");
        TRANSLITERATION_TABLE.put('ُ', "u");
        TRANSLITERATION_TABLE.put('ِ', "i");
    }

    public TextEditorGUI() {
        initializeFrame();
        initializeComponents();
        setupFilePanel();
        setupEditorPanel();
        setupToolbar();
        setupLemmatization();

        setVisible(true);
    }

    private void initializeFrame() {
        setTitle("Arabic Text Editor with File Management");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void initializeComponents() {
        // Initialize text components
        editorArea = new JTextArea();
        editorArea.setLineWrap(true);
        editorArea.setWrapStyleWord(true);
        pageLabel = new JLabel("Page: 1", JLabel.CENTER);

        // Initialize business logic components
        fontSelectorDialog = new fontSelector();
        fileListModel = new DefaultListModel<>();

        FileBL fileBL = new FileBL();
        paginationManager = new PaginationManager(editorArea, pageLabel, fileBL, -1);
        fileManager = new FileManager(fileBL, editorArea, pageLabel);

        // Load files from database
        fileManager.loadFilesFromDB(fileListModel);
    }

  
    
    private void setupFilePanel() {
        filePanel = new JPanel(new BorderLayout());
        filePanel.setBackground(Color.LIGHT_GRAY);

        fileList = new JList<>(fileListModel);
        fileList.setBackground(new Color(200, 200, 200));
        fileList.setForeground(new Color(80, 80, 80));
        fileList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    fileManager.viewFile(selectedFile);
                }
            }
        });

        JScrollPane fileScrollPane = new JScrollPane(fileList);
        filePanel.add(new JLabel("Files", JLabel.CENTER), BorderLayout.NORTH);
        filePanel.add(fileScrollPane, BorderLayout.CENTER);

        add(filePanel, BorderLayout.WEST);
    }

    private void setupEditorPanel() {
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorArea.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel pageControlPanel = new JPanel(new BorderLayout());
        JButton prevButton = createStyledButton("<");
        prevButton.addActionListener(e -> paginationManager.prevPage());

        JButton nextButton = createStyledButton(">");
        nextButton.addActionListener(e -> paginationManager.nextPage());

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
            createToolbarButton("New", e -> newFile()),
            createToolbarButton("Import", e -> importFiles()),
            createToolbarButton("Save", e -> fileManager.saveFile()),
            createToolbarButton("Delete", e -> deleteSelectedFile()),
            createToolbarButton("View", e -> toggleFilePanel()),
            createToolbarButton("Font", e -> openFontSelector()),
            createToolbarButton("Search", e -> searchContent()), // Add Search button
            createToolbarButton("Transliterate", e -> transliterateContent())
        };

        for (JButton button : buttons) {
            toolbarPanel.add(button);
        }

        add(toolbarPanel, BorderLayout.NORTH);
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

    private JButton createToolbarButton(String text, ActionListener action) {
        JButton button = createStyledButton(text);
        button.addActionListener(action);
        return button;
    }

    private void newFile() {
        editorArea.setText("");
        pageLabel.setText("Page: 1");
        paginationManager = new PaginationManager(editorArea, pageLabel, fileManager.getFileBL(), -1);
        fileManager.setCurrentFileName(null);
    }

    private void importFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path[] selectedFiles = Arrays.stream(fileChooser.getSelectedFiles())
                    .map(File::toPath)
                    .toArray(Path[]::new);

            fileManager.importMultipleFiles(selectedFiles);
            fileManager.loadFilesFromDB(fileListModel);
        }
    }

    private void deleteSelectedFile() {
        String selectedFile = fileList.getSelectedValue();
        if (selectedFile != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Do you really want to delete this file?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                fileManager.deleteFile(fileListModel, selectedFile);
            }
        }
    }

    private void toggleFilePanel() {
        isFilePanelVisible = !isFilePanelVisible;
        filePanel.setVisible(isFilePanelVisible);
    }

    private void openFontSelector() {
        fontSelectorDialog.setVisible(true);
        Font selectedFont = fontSelectorDialog.returnFont();
        if (selectedFont != null) {
            editorArea.setFont(selectedFont);
        }
    }

   


//====================================================================================================================
   //..................................LAMITIZATION
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
            // Step 1: Load the JAR file
            File jarFile = new File("C:\\Users\\Mamai Nataki\\Downloads\\AlKhalil-2.1.21.jar");
            if (!jarFile.exists()) {
                return "Error: JAR file not found at " + jarFile.getAbsolutePath();
            }

            // Step 2: Create a class loader for the JAR
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});

            // Step 3: Load the specific class for lemmatization
            Class<?> posTaggerClass = classLoader.loadClass("AlKhalil2.AnalyzedWords");

            // Step 4: Create an instance of the analyzer
            Object posTaggerInstance = posTaggerClass.getDeclaredConstructor().newInstance();

            // Step 5: Load the "analyzedWords" method
            Method tagMethod = posTaggerClass.getMethod("analyzedWords", String.class);

            // Step 6: Invoke the method and get the results
            @SuppressWarnings("unchecked")
            LinkedList<?> analysisResults = (LinkedList<?>) tagMethod.invoke(posTaggerInstance, arabicText);

            // Step 7: Extract unique lemmas from the results
            LinkedHashSet<String> uniqueLemmas = new LinkedHashSet<>();
            for (Object resultObj : analysisResults) {
                // Extract the value of the "lemme" field
                java.lang.reflect.Field lemmeField = resultObj.getClass().getDeclaredField("lemme");
                lemmeField.setAccessible(true); // Allow access to private fields
                String lemma = (String) lemmeField.get(resultObj);
                
                // Add lemma to the set
                uniqueLemmas.add(lemma);
            }

            // Step 8: Close the class loader
            classLoader.close();

            // Step 9: Return lemmatization results
            if (uniqueLemmas.isEmpty()) {
                return "No lemmas found for: " + arabicText;
            }
            
            return "Lemmas for '" + arabicText + "': " + String.join(", ", uniqueLemmas);

        } catch (ClassNotFoundException e) {
            return "Error: AlKhalil class not found. " + e.getMessage();
        } catch (NoSuchMethodException e) {
            return "Error: Method not found in AlKhalil. " + e.getMessage();
        } catch (NoSuchFieldException e) {
            return "Error: Field not found in analysis result. " + e.getMessage();
        } catch (Exception e) {
            return "Unexpected error during lemmatization: " + e.getMessage();
        }
    }

    // Updated lemmatization listener to handle multiple words
    private class LemmatizationCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            String selectedText = editorArea.getSelectedText();
            if (selectedText != null && !selectedText.trim().isEmpty()) {
                showLemmatizationButton(e);
            } else {
                hideLemmatizationButton();
            }
        }
    }

   

    private void showLemmatizationButton(CaretEvent e) {
        try {
            Rectangle viewRect = editorArea.modelToView2D(Math.min(e.getDot(), e.getMark())).getBounds();
            lemmatizeButton.setBounds(viewRect.x, viewRect.y - 30, 100, 30);
            lemmatizeButton.setVisible(true);
        } catch (Exception ex) {
            lemmatizeButton.setBounds(100, 100, 100, 30);
            lemmatizeButton.setVisible(true);
        }
    }

    private void hideLemmatizationButton() {
        if (lemmatizeButton != null) {
            lemmatizeButton.setVisible(false);
        }
    }

//................TRANSLITERATION
//=================================================================================
    // Transliteration Logic
    private String transliterateText(String arabicText) {
        StringBuilder transliteratedText = new StringBuilder();
        for (char c : arabicText.toCharArray()) {
            transliteratedText.append(TRANSLITERATION_TABLE.getOrDefault(c, String.valueOf(c))); // Default to original char if not found
        }
        return transliteratedText.toString();
    }

    private void transliterateContent() {
        String content = editorArea.getText();
        if (content == null || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No content to transliterate.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Transliterate the content
        //=========================================================================
        String transliteratedContent = transliterateText(content);

        // Display the transliteration in a new window
        JFrame transliterationWindow = new JFrame("Transliteration Result");
        transliterationWindow.setSize(800, 600);
        transliterationWindow.setLayout(new BorderLayout());

        JTextArea transliterationArea = new JTextArea(transliteratedContent);
        transliterationArea.setEditable(false);
        transliterationArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(transliterationArea);

        transliterationWindow.add(scrollPane, BorderLayout.CENTER);
        transliterationWindow.setVisible(true);
    }
    
    //====================================================================
 // Search Logic
    private void searchContent() {
        String searchText = JOptionPane.showInputDialog(this, "Enter content to search:", "Search Files", JOptionPane.PLAIN_MESSAGE);

        if (searchText == null || searchText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search text cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.util.List<String> matchingFiles = fileManager.searchFilesContainingText(searchText);

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
                fileManager.viewFile(selectedFile);
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextEditorGUI::new);
    }
    
    
    
}