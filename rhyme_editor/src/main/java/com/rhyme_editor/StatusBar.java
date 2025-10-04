package com.rhyme_editor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class StatusBar extends JPanel {
    
    private JLabel wordCountLabel;
    private JLabel charCountLabel;
    private JLabel rhymeCountLabel;
    
    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            new EmptyBorder(5, 15, 5, 15)
        ));
        
        // Left panel for word/char count
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        wordCountLabel = createLabel("Words: 0");
        charCountLabel = createLabel("Characters: 0");
        
        leftPanel.add(wordCountLabel);
        leftPanel.add(createSeparator());
        leftPanel.add(charCountLabel);
        
        // Right panel for rhyme count
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        rhymeCountLabel = createLabel("Rhymes: 0");
        rhymeCountLabel.setForeground(new Color(37, 99, 235));
        rhymeCountLabel.setFont(rhymeCountLabel.getFont().deriveFont(Font.BOLD));
        
        rightPanel.add(rhymeCountLabel);
        
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(new Color(100, 100, 100));
        return label;
    }
    
    private JLabel createSeparator() {
        JLabel separator = new JLabel("│");
        separator.setForeground(new Color(200, 200, 200));
        return separator;
    }
    
    public void updateStats(int wordCount, int charCount, int rhymeCount) {
        wordCountLabel.setText("Words: " + wordCount);
        charCountLabel.setText("Characters: " + charCount);
        rhymeCountLabel.setText("Rhymes: " + rhymeCount);
    }
}