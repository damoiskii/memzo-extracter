package com.devdam.memzo_extracter.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Utility class to create modal backdrops for dialogs
 */
public class ModalBackdrop {
    
    private static final Color BACKDROP_COLOR = new Color(0, 0, 0, 32); // Very light semi-transparent overlay
    
    /**
     * Shows a dialog with a modal backdrop
     * @param parent The parent window
     * @param dialog The dialog to show
     */
    public static void showDialogWithBackdrop(Window parent, JDialog dialog) {
        if (parent == null) {
            dialog.setVisible(true);
            return;
        }
        
        // Create backdrop panel
        JWindow backdrop = new JWindow(parent);
        JPanel backdropPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BACKDROP_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        backdrop.setContentPane(backdropPanel);
        backdrop.setBounds(parent.getBounds());
        backdrop.setAlwaysOnTop(false);
        
        // Show backdrop first
        backdrop.setVisible(true);
        
        // Add window listener to hide backdrop when dialog is closed
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                backdrop.dispose();
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                backdrop.dispose();
            }
        });
        
        // Position dialog relative to parent
        dialog.setLocationRelativeTo(parent);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Shows a JOptionPane with backdrop
     * @param parent The parent component
     * @param message The message to display
     * @param title The dialog title
     * @param messageType The message type
     */
    public static void showMessageDialogWithBackdrop(Component parent, Object message, String title, int messageType) {
        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        if (parentWindow == null) {
            JOptionPane.showMessageDialog(parent, message, title, messageType);
            return;
        }
        
        // Create backdrop
        JWindow backdrop = new JWindow(parentWindow);
        JPanel backdropPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BACKDROP_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        backdrop.setContentPane(backdropPanel);
        backdrop.setBounds(parentWindow.getBounds());
        backdrop.setAlwaysOnTop(false);
        backdrop.setVisible(true);
        
        try {
            // Show the message dialog
            JOptionPane.showMessageDialog(parent, message, title, messageType);
        } finally {
            // Always dispose backdrop
            backdrop.dispose();
        }
    }
    
    /**
     * Shows a JFileChooser with backdrop
     * @param parent The parent component
     * @param fileChooser The file chooser to show
     * @param isOpenDialog true for open dialog, false for save dialog
     * @return The result of the file chooser operation
     */
    public static int showFileChooserWithBackdrop(Component parent, JFileChooser fileChooser, boolean isOpenDialog) {
        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        if (parentWindow == null) {
            return isOpenDialog ? fileChooser.showOpenDialog(parent) : fileChooser.showSaveDialog(parent);
        }
        
        // Create backdrop
        JWindow backdrop = new JWindow(parentWindow);
        JPanel backdropPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BACKDROP_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        backdrop.setContentPane(backdropPanel);
        backdrop.setBounds(parentWindow.getBounds());
        backdrop.setAlwaysOnTop(false);
        backdrop.setVisible(true);
        
        try {
            // Show the file chooser
            return isOpenDialog ? fileChooser.showOpenDialog(parent) : fileChooser.showSaveDialog(parent);
        } finally {
            // Always dispose backdrop
            backdrop.dispose();
        }
    }
}
