package com.rhyme_editor;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class EditorPane extends JTextPane implements DocumentListener {

    private final JScrollPane scrollPane;
    private final RhymeDetector rhymeDetector;
    private boolean highlightingEnabled = true;
    private UndoManager undoManager;
    private Runnable textChangeListener;
    private Map<String, List<String>> currentRhymeGroups;
    private Timer updateTimer;
    private volatile boolean isUpdating = false;

    public EditorPane() {
        super();
        
        // Load custom font
        loadCustomFont();
        
        // Initialize components
        scrollPane = new JScrollPane(this);
        rhymeDetector = new RhymeDetector();
        undoManager = new UndoManager();
        currentRhymeGroups = new HashMap<>();

        // Style the editor
        setupEditorStyle();
        
        // Setup undo/redo
        getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        // Setup debounced timer for highlighting (300ms delay)
        updateTimer = new Timer(300, e -> performHighlighting());
        updateTimer.setRepeats(false);

        // Listen to text changes
        getDocument().addDocumentListener(this);
    }

    private void loadCustomFont() {
        try {
            // Try to load Satoshi font from system
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fonts = ge.getAvailableFontFamilyNames();
            
            boolean satoshiFound = false;
            for (String font : fonts) {
                if (font.equalsIgnoreCase("Satoshi")) {
                    setFont(new Font("Satoshi", Font.PLAIN, 16));
                    satoshiFound = true;
                    break;
                }
            }
            
            // If Satoshi not found, try to load from file
            if (!satoshiFound) {
                File fontFile = new File("Satoshi-Regular.ttf");
                if (fontFile.exists()) {
                    Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(16f);
                    ge.registerFont(customFont);
                    setFont(customFont);
                } else {
                    // Fallback to modern system fonts
                    setFont(new Font("Inter", Font.PLAIN, 16));
                }
            }
        } catch (Exception e) {
            // Fallback to safe default
            setFont(new Font("SansSerif", Font.PLAIN, 16));
        }
    }

    private void setupEditorStyle() {
        setMargin(new Insets(20, 20, 20, 20));
        setBackground(new Color(252, 252, 252));
        setForeground(new Color(30, 30, 30));
        setCaretColor(new Color(70, 130, 255));
        setSelectionColor(new Color(180, 210, 255));
        
        // Style scroll pane
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(252, 252, 252));
        
        // Add line numbers (optional enhancement)
        TextLineNumber lineNumber = new TextLineNumber(this);
        scrollPane.setRowHeaderView(lineNumber);
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public String getPaneText() {
        return getText();
    }

    public void setPaneText(String text) {
        setText(text);
    }

    public void setHighlightingEnabled(boolean enabled) {
        highlightingEnabled = enabled;
        if (enabled) {
            updateHighlighting();
        } else {
            clearHighlighting();
        }
    }

    public void addTextChangeListener(Runnable listener) {
        this.textChangeListener = listener;
    }

    public int getWordCount() {
        String text = getText().trim();
        if (text.isEmpty()) return 0;
        return text.split("\\s+").length;
    }

    public int getCharCount() {
        return getText().length();
    }

    public int getRhymeCount() {
        if (currentRhymeGroups == null) return 0;
        int count = 0;
        for (List<String> group : currentRhymeGroups.values()) {
            if (group.size() >= 2) {
                count += group.size();
            }
        }
        return count;
    }

    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    // DocumentListener methods
    @Override
    public void insertUpdate(DocumentEvent e) {
        scheduleUpdate();
        notifyTextChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        scheduleUpdate();
        notifyTextChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        scheduleUpdate();
    }

    private void scheduleUpdate() {
        // Restart timer - this debounces rapid typing
        updateTimer.restart();
    }

    private void notifyTextChange() {
        if (textChangeListener != null) {
            SwingUtilities.invokeLater(textChangeListener);
        }
    }

    private void updateHighlighting() {
        if (updateTimer.isRunning()) {
            updateTimer.restart();
        } else {
            performHighlighting();
        }
    }

    private void clearHighlighting() {
        try {
            StyledDocument doc = getStyledDocument();
            Style defaultStyle = addStyle("default", null);
            StyleConstants.setForeground(defaultStyle, new Color(30, 30, 30));
            doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
            currentRhymeGroups = new HashMap<>();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Main highlighting logic: detects rhymes and applies colors.
     */
    private void performHighlighting() {
        if (isUpdating || !highlightingEnabled) return;
        
        isUpdating = true;

        // Run rhyme detection in background thread
        new SwingWorker<Map<String, List<String>>, Void>() {
            private String textSnapshot;
            
            @Override
            protected Map<String, List<String>> doInBackground() {
                try {
                    StyledDocument doc = getStyledDocument();
                    textSnapshot = doc.getText(0, doc.getLength());
                    return rhymeDetector.findRhymes(textSnapshot);
                } catch (BadLocationException ex) {
                    return new HashMap<>();
                }
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<String>> rhymeGroups = get();
                    applyHighlighting(textSnapshot, rhymeGroups);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    isUpdating = false;
                }
            }
        }.execute();
    }

    private void applyHighlighting(String text, Map<String, List<String>> rhymeGroups) {
        try {
            StyledDocument doc = getStyledDocument();
            
            // Reset to default style
            Style defaultStyle = addStyle("default", null);
            StyleConstants.setForeground(defaultStyle, new Color(30, 30, 30));
            StyleConstants.setFontSize(defaultStyle, 16);
            doc.setCharacterAttributes(0, text.length(), defaultStyle, true);

            currentRhymeGroups = rhymeGroups;
            
            if (rhymeGroups.isEmpty()) return;

            // Enhanced color palette with better contrast
            Color[] colors = {
                new Color(220, 38, 38),   // Red
                new Color(37, 99, 235),   // Blue
                new Color(22, 163, 74),   // Green
                new Color(234, 88, 12),   // Orange
                new Color(147, 51, 234),  // Purple
                new Color(14, 165, 233),  // Cyan
                new Color(219, 39, 119),  // Pink
                new Color(202, 138, 4)    // Yellow-gold
            };

            int colorIndex = 0;

            // Apply highlighting for each rhyme group
            for (List<String> group : rhymeGroups.values()) {
                if (group.size() < 2) continue;

                Color color = colors[colorIndex % colors.length];
                colorIndex++;

                Style style = addStyle("rhymeStyle" + colorIndex, null);
                StyleConstants.setForeground(style, color);
                StyleConstants.setBold(style, true);

                // Highlight each word in the group
                for (String word : group) {
                    highlightWord(doc, text, word, style);
                }
            }

            // Notify listener after highlighting is complete
            notifyTextChange();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void highlightWord(StyledDocument doc, String text, String word, Style style) {
        String lowerText = text.toLowerCase();
        String lowerWord = word.toLowerCase();
        
        int index = 0;
        while ((index = lowerText.indexOf(lowerWord, index)) != -1) {
            // Check for full word match
            boolean validStart = (index == 0 || !Character.isLetterOrDigit(lowerText.charAt(index - 1)));
            boolean validEnd = (index + word.length() >= lowerText.length() ||
                                !Character.isLetterOrDigit(lowerText.charAt(index + word.length())));

            if (validStart && validEnd) {
                doc.setCharacterAttributes(index, word.length(), style, false);
            }
            index++;
        }
    }
}