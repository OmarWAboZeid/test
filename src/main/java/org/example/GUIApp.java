package org.example;

import org.example.versioningTools.Git;
import org.example.versioningTools.VersionControl;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class GUIApp extends JFrame {
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private JTextArea fileContentArea;
    private final VersionControl versionControl;
    private final String selectedFileName;
    private JButton restoreVersion; // New button for refreshing the file list

    public GUIApp() {
        super("File Viewer");

        this.versionControl = new Git();
        this.selectedFileName = "hamada.txt";

        // Initialize components
        fileList = new JList<>();
        listModel = new DefaultListModel<>();
        restoreVersion = new JButton("Refresh"); // Create a new button with text "Refresh"

        fileList.setModel(listModel);
        fileContentArea = new JTextArea();
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFileName))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            fileContentArea.setText(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
            fileContentArea.setText("Error reading file: " + e.getMessage());
        }


        // Set layout
        setLayout(new BorderLayout());

        // Add panels to the frame
        add(new JScrollPane(fileList), BorderLayout.WEST);
        add(new JScrollPane(fileContentArea), BorderLayout.CENTER);
        add(restoreVersion, BorderLayout.NORTH); // Add the button to the top of the frame

        // Set initial size and close operation
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Listens for selection changes in the file list
        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    displaySelectedFile();
                }
            }
        });

        restoreVersion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restoreVersionProvided();
                populateVersions();
            }
        });

        // Populate the file list with files from the current directory
        populateVersions();
    }

    private void restoreVersionProvided() {
        String selectedVersion = fileList.getSelectedValue();
        String fileContent = this.versionControl.getVersion(selectedVersion);

        this.versionControl.commitVersion(fileContent, selectedVersion);
    }

    // Method to populate the Version History
    private void populateVersions() {
        listModel.clear();
        for (String commitMsg : this.versionControl.getCommitMessages()) {
            listModel.addElement(commitMsg);
        }
    }

    // Method to display the content of the selected file
    private void displaySelectedFile() {
        String selectedVersion = fileList.getSelectedValue();
        String fileContent = this.versionControl.getVersion(selectedVersion);
        fileContentArea.setText(fileContent);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUIApp fileViewer = new GUIApp();
            fileViewer.setVisible(true);
        });
    }
}
