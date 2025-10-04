package com.rhyme_editor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

/**
 * This class displays line numbers for a JTextComponent.
 */
public class TextLineNumber extends JPanel implements PropertyChangeListener {
    
    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;
    private JTextComponent component;
    private int lastDigits;
    private int lastHeight;
    private int lastLine;

    public TextLineNumber(JTextComponent component) {
        this.component = component;
        
        setFont(component.getFont().deriveFont(12f));
        setForeground(new Color(150, 150, 150));
        setBackground(new Color(248, 248, 248));
        setBorder(new EmptyBorder(0, 10, 0, 10));
        
        component.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                documentChanged();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                documentChanged();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                documentChanged();
            }
        });
        
        component.addPropertyChangeListener("font", this);
    }

    private void documentChanged() {
        SwingUtilities.invokeLater(() -> {
            try {
                int endPos = component.getDocument().getLength();
                Rectangle rect = component.modelToView(endPos);
                if (rect != null && rect.y != lastHeight) {
                    setPreferredWidth();
                    repaint();
                    lastHeight = rect.y;
                }
            } catch (BadLocationException ex) {
                // Ignore
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("font")) {
            setFont(component.getFont().deriveFont(12f));
            lastDigits = 0;
            setPreferredWidth();
        }
    }

    private void setPreferredWidth() {
        Element root = component.getDocument().getDefaultRootElement();
        int lines = root.getElementCount();
        int digits = Math.max(String.valueOf(lines).length(), 2);

        if (lastDigits != digits) {
            lastDigits = digits;
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int width = fontMetrics.charWidth('0') * digits;
            Insets insets = getInsets();
            int preferredWidth = insets.left + insets.right + width;

            Dimension d = getPreferredSize();
            d.setSize(preferredWidth, HEIGHT);
            setPreferredSize(d);
            setSize(d);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        Insets insets = getInsets();
        int availableWidth = getSize().width - insets.left - insets.right;

        Rectangle clip = g.getClipBounds();
        int rowStartOffset = component.viewToModel(new Point(0, clip.y));
        int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));

        Element root = component.getDocument().getDefaultRootElement();
        
        while (rowStartOffset <= endOffset) {
            try {
                Rectangle r = component.modelToView(rowStartOffset);
                if (r == null) break;
                
                int lineNumber = root.getElementIndex(rowStartOffset) + 1;
                String lineStr = String.valueOf(lineNumber);
                int stringWidth = fontMetrics.stringWidth(lineStr);
                int x = availableWidth - stringWidth + insets.left;
                int y = r.y + r.height - fontMetrics.getDescent();
                g.drawString(lineStr, x, y);

                rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
            } catch (BadLocationException e) {
                break;
            }
        }
    }
}