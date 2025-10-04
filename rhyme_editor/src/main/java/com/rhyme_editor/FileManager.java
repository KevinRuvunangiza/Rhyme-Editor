package com.rhyme_editor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileManager {

    private File currentFile = null;

    public void save(String content) {
        if (currentFile == null) {
            JFileChooser fileChooser = createFileChooser();
            fileChooser.setDialogTitle("Save File");
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                
                // Add .txt extension if no extension provided
                if (!currentFile.getName().contains(".")) {
                    currentFile = new File(currentFile.getAbsolutePath() + ".txt");
                }
            } else {
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            writer.write(content);
            JOptionPane.showMessageDialog(null, 
                "File saved successfully!", 
                "Save", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Error saving file: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public String open() {
        JFileChooser fileChooser = createFileChooser();
        fileChooser.setDialogTitle("Open File");
        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, 
                    "Error opening file: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public void resetFile() {
        currentFile = null;
    }

    public String getCurrentFileName() {
        if (currentFile != null) {
            return currentFile.getName();
        }
        return "Untitled";
    }

    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        
        // Add file filters
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter(
            "Text Files (*.txt)", "txt");
        FileNameExtensionFilter allFilter = new FileNameExtensionFilter(
            "All Files", "*");
        
        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.addChoosableFileFilter(allFilter);
        fileChooser.setFileFilter(txtFilter);
        
        return fileChooser;
    }
}