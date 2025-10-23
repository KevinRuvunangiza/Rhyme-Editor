package com.rhyme_editor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame implements ActionListener {

    private int _width;
    private int _height;

    // Menu components
    private JMenu fileMenu = new JMenu("File");
    private JMenu editMenu = new JMenu("Edit");
    private JMenu viewMenu = new JMenu("View");
    
    private JMenuItem newMenuItem = new JMenuItem("New");
    private JMenuItem openMenuItem = new JMenuItem("Open");
    private JMenuItem saveMenuItem = new JMenuItem("Save");
    private JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
    private JMenuItem exitMenuItem = new JMenuItem("Exit");
    
    private JMenuItem undoMenuItem = new JMenuItem("Undo");
    private JMenuItem redoMenuItem = new JMenuItem("Redo");
    private JMenuItem cutMenuItem = new JMenuItem("Cut");
    private JMenuItem copyMenuItem = new JMenuItem("Copy");
    private JMenuItem pasteMenuItem = new JMenuItem("Paste");
    
    private JCheckBoxMenuItem highlightMenuItem = new JCheckBoxMenuItem("Highlight Rhymes", true);
    
    private JMenuBar menuBar = new JMenuBar();
    private EditorPane editor = new EditorPane();
    private FileManager fileManager = new FileManager();
    private StatusBar statusBar = new StatusBar();

    public MainFrame(int width, int height) {
        this._width = width;
        this._height = height;
    }

    public void createWindow() {
        setTitle("Rhyme Editor - Untitled");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(_width, _height);
        setLocationRelativeTo(null); // Center on screen
        
        // Set custom icon if available
        try {
            setIconImage(Toolkit.getDefaultToolkit().createImage("icon.png"));
        } catch (Exception e) {
            // Icon not found, continue without it
        }

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(250, 250, 250));

        // Setup menu
        setupMenu();

        // Add components
        mainPanel.add(editor.getScrollPane(), BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        add(mainPanel);

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Update status bar on text changes
        editor.addTextChangeListener(() -> updateStatusBar());

        // Show window
        setVisible(true);
        
        // Request focus on editor
        SwingUtilities.invokeLater(() -> editor.requestFocusInWindow());
    }

    private void setupMenu() {
        // File menu
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK));
        
        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        // Edit menu
        editMenu.setMnemonic(KeyEvent.VK_E);
        
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        
        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        editMenu.addSeparator();
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);

        // View menu
        viewMenu.setMnemonic(KeyEvent.VK_V);
        viewMenu.add(highlightMenuItem);

        // Add action listeners
        newMenuItem.addActionListener(this);
        openMenuItem.addActionListener(this);
        saveMenuItem.addActionListener(this);
        saveAsMenuItem.addActionListener(this);
        exitMenuItem.addActionListener(this);
        
        undoMenuItem.addActionListener(this);
        redoMenuItem.addActionListener(this);
        cutMenuItem.addActionListener(this);
        copyMenuItem.addActionListener(this);
        pasteMenuItem.addActionListener(this);
        
        highlightMenuItem.addActionListener(this);

        // Build menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        
        // Style menu bar
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        setJMenuBar(menuBar);
    }

    private void setupKeyboardShortcuts() {
        // Additional shortcuts can be added here
    }

    private void updateStatusBar() {
        int wordCount = editor.getWordCount();
        int charCount = editor.getCharCount();
        int rhymeCount = editor.getRhymeCount();
        
        statusBar.updateStats(wordCount, charCount, rhymeCount);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        // File menu actions
        if (source == newMenuItem) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Do you want to save changes?", 
                "New File", 
                JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                fileManager.save(editor.getPaneText());
            }
            if (result != JOptionPane.CANCEL_OPTION) {
                editor.setPaneText("");
                fileManager.resetFile();
                setTitle("Rhyme Editor - Untitled");
            }
        } 
        else if (source == openMenuItem) {
            String openedText = fileManager.open();
            if (openedText != null) {
                editor.setPaneText(openedText);
                setTitle("Rhyme Editor - " + fileManager.getCurrentFileName());
            }
        } 
        else if (source == saveMenuItem) {
            fileManager.save(editor.getPaneText());
            setTitle("Rhyme Editor - " + fileManager.getCurrentFileName());
        } 
        else if (source == saveAsMenuItem) {
            fileManager.resetFile();
            fileManager.save(editor.getPaneText());
            setTitle("Rhyme Editor - " + fileManager.getCurrentFileName());
        } 
        else if (source == exitMenuItem) {
            System.exit(0);
        }
        
        // Edit menu actions
        else if (source == undoMenuItem) {
            editor.undo();
        } 
        else if (source == redoMenuItem) {
            editor.redo();
        } 
        else if (source == cutMenuItem) {
            editor.cut();
        } 
        else if (source == copyMenuItem) {
            editor.copy();
        } 
        else if (source == pasteMenuItem) {
            editor.paste();
        }
        
        // View menu actions
        else if (source == highlightMenuItem) {
            editor.setHighlightingEnabled(highlightMenuItem.isSelected());
        }
    }
}