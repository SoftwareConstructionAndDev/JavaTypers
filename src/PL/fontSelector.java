package PL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FontSelector extends JDialog {

    private JComboBox<String> fontSelector;
    private JComboBox<String> fontSize;
    private JComboBox<String> fontStyle;
    private JLabel preview;
    private Font selectedFont;

    // Constructor
    public FontSelector() {
        setTitle("Font Selector");
        setSize(300, 200);
        setLayout(null);
        setBackground(Color.blue);

        initializeComponents();
    }

    // Initializes font components
    private void initializeComponents() {
        // Font names
        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontSelector = new JComboBox<>(fontNames);
        fontSelector.setBounds(110, 10, 150, 20);

        // Font sizes
        String[] fontSizes = {"10", "12", "14", "16", "18", "20", "22", "24", "28"};
        fontSize = new JComboBox<>(fontSizes);
        fontSize.setBounds(110, 35, 100, 20);

        // Font styles
        String[] fontStyles = {"Normal", "Bold", "Italic", "Bold Italic"};
        fontStyle = new JComboBox<>(fontStyles);
        fontStyle.setBounds(110, 60, 100, 20);

        // Labels
        JLabel fontLabel = new JLabel("Select Font:");
        fontLabel.setBounds(10, 10, 100, 20);
        JLabel sizeLabel = new JLabel("Select Size:");
        sizeLabel.setBounds(10, 35, 100, 20);
        JLabel styleLabel = new JLabel("Select Style:");
        styleLabel.setBounds(10, 60, 100, 20);

        // Preview area
        JLabel previewLabel = new JLabel("Preview:");
        previewLabel.setBounds(50, 130, 100, 30);
        preview = new JLabel("AaBbCc");
        preview.setBorder(BorderFactory.createLineBorder(Color.black));
        preview.setBounds(120, 130, 70, 30);

        // Buttons
        JButton okButton = new JButton("OK");
        okButton.setBounds(10, 100, 100, 20);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(110, 100, 100, 20);

        // Add components to dialog
        add(fontLabel);
        add(fontSelector);
        add(sizeLabel);
        add(fontSize);
        add(styleLabel);
        add(fontStyle);
        add(okButton);
        add(cancelButton);
        add(previewLabel);
        add(preview);

        // Set action listeners for font preview and selection
        fontSelector.addActionListener(e -> updatePreview());
        fontStyle.addActionListener(e -> updatePreview());
        fontSize.addActionListener(e -> updatePreview());

        // Set OK and Cancel button actions
        okButton.addActionListener(e -> setSelectedFont());
        cancelButton.addActionListener(e -> setVisible(false));
    }

    // Updates the preview label with the current font settings
    private void updatePreview() {
        String fontName = (String) fontSelector.getSelectedItem();
        int style = fontStyle.getSelectedIndex();
        int size = Integer.parseInt((String) fontSize.getSelectedItem());

        Font previewFont = new Font(fontName, style, size);
        preview.setFont(previewFont);
    }

    // Sets the selected font to be used in the main editor
    private void setSelectedFont() {
        String fontName = (String) fontSelector.getSelectedItem();
        int style = fontStyle.getSelectedIndex();
        int size = Integer.parseInt((String) fontSize.getSelectedItem());

        selectedFont = new Font(fontName, style, size);
        setVisible(false);
    }

    // Returns the selected font when OK is clicked
    public Font returnFont() {
        return selectedFont;
    }
}
