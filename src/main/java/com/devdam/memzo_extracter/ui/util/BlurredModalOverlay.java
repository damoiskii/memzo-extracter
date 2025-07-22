package com.devdam.memzo_extracter.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Utility class to create blurred modal overlays for dialogs
 */
public class BlurredModalOverlay {
    
    private static final Color OVERLAY_COLOR = new Color(255, 255, 255, 32); // Very light white overlay
    private static final float BLUR_RADIUS = 3.0f; // Subtle blur effect
    
    /**
     * Shows a dialog with a blurred backdrop
     * @param parent The parent window
     * @param dialog The dialog to show
     */
    public static void showDialogWithBlurredOverlay(Window parent, JDialog dialog) {
        if (parent == null) {
            dialog.setVisible(true);
            return;
        }
        
        // Create overlay window
        JWindow overlay = new JWindow(parent);
        overlay.setContentPane(createBlurredOverlayPanel(parent));
        overlay.setBounds(parent.getBounds());
        overlay.setAlwaysOnTop(false);
        
        // Show overlay first
        overlay.setVisible(true);
        
        // Add window listener to hide overlay when dialog is closed
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                overlay.dispose();
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                overlay.dispose();
            }
        });
        
        // Position dialog relative to parent
        dialog.setLocationRelativeTo(parent);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Shows a JOptionPane with blurred overlay
     * @param parent The parent component
     * @param message The message to display
     * @param title The dialog title
     * @param messageType The message type
     */
    public static void showMessageDialogWithBlurredOverlay(Component parent, Object message, String title, int messageType) {
        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        if (parentWindow == null) {
            JOptionPane.showMessageDialog(parent, message, title, messageType);
            return;
        }
        
        // Create overlay
        JWindow overlay = new JWindow(parentWindow);
        overlay.setContentPane(createBlurredOverlayPanel(parentWindow));
        overlay.setBounds(parentWindow.getBounds());
        overlay.setAlwaysOnTop(false);
        overlay.setVisible(true);
        
        try {
            // Show the message dialog
            JOptionPane.showMessageDialog(parent, message, title, messageType);
        } finally {
            // Always dispose overlay
            overlay.dispose();
        }
    }
    
    /**
     * Shows a JFileChooser with blurred overlay
     * @param parent The parent component
     * @param fileChooser The file chooser to show
     * @param isOpenDialog true for open dialog, false for save dialog
     * @return The result of the file chooser operation
     */
    public static int showFileChooserWithBlurredOverlay(Component parent, JFileChooser fileChooser, boolean isOpenDialog) {
        Window parentWindow = SwingUtilities.getWindowAncestor(parent);
        if (parentWindow == null) {
            return isOpenDialog ? fileChooser.showOpenDialog(parent) : fileChooser.showSaveDialog(parent);
        }
        
        // Create overlay
        JWindow overlay = new JWindow(parentWindow);
        overlay.setContentPane(createBlurredOverlayPanel(parentWindow));
        overlay.setBounds(parentWindow.getBounds());
        overlay.setAlwaysOnTop(false);
        overlay.setVisible(true);
        
        try {
            // Show the file chooser
            return isOpenDialog ? fileChooser.showOpenDialog(parent) : fileChooser.showSaveDialog(parent);
        } finally {
            // Always dispose overlay
            overlay.dispose();
        }
    }
    
    /**
     * Creates a blurred overlay panel
     * @param window The window to capture and blur
     * @return A panel with the blurred background
     */
    private static JPanel createBlurredOverlayPanel(Window window) {
        // Capture the window content
        BufferedImage windowImage = captureWindow(window);
        
        // Apply blur effect
        BufferedImage blurredImage = applyGaussianBlur(windowImage, BLUR_RADIUS);
        
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                if (blurredImage != null) {
                    // Draw the blurred background
                    g.drawImage(blurredImage, 0, 0, getWidth(), getHeight(), null);
                }
                
                // Add subtle overlay
                g.setColor(OVERLAY_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }
    
    /**
     * Captures the content of a window
     * @param window The window to capture
     * @return BufferedImage of the window content
     */
    private static BufferedImage captureWindow(Window window) {
        try {
            Rectangle bounds = window.getBounds();
            BufferedImage image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Paint the window content
            window.paint(g2d);
            g2d.dispose();
            
            return image;
        } catch (Exception e) {
            // If capture fails, return null and fall back to simple overlay
            return null;
        }
    }
    
    /**
     * Applies a simple Gaussian blur effect to an image
     * @param source The source image
     * @param radius The blur radius
     * @return The blurred image
     */
    private static BufferedImage applyGaussianBlur(BufferedImage source, float radius) {
        if (source == null) return null;
        
        try {
            int width = source.getWidth();
            int height = source.getHeight();
            
            BufferedImage blurred = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = blurred.createGraphics();
            
            // Enable rendering hints for better quality
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Simple blur by drawing the image slightly scaled and with transparency
            float alpha = 0.15f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            // Draw multiple offset copies for blur effect
            for (int x = BLUR_OFFSET_MIN; x <= BLUR_OFFSET_MAX; x++) {
                for (int y = BLUR_OFFSET_MIN; y <= BLUR_OFFSET_MAX; y++) {
                    g2d.drawImage(source, x, y, null);
                }
            }
            
            g2d.dispose();
            return blurred;
            
        } catch (Exception e) {
            // If blur fails, return original image
            return source;
        }
    }
}
