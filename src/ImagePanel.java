// ImagePanel.java
// Custom JPanel component for displaying processed images in the GUI.

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Custom JPanel to display the sequential and parallel processed BufferedImages side-by-side.
 */
public class ImagePanel extends JPanel {
    private JLabel seqLabel;
    private JLabel parLabel;

    // Preferred size for each individual image display label
    private static final int LABEL_IMG_WIDTH = 300; // Adjusted for better viewing
    private static final int LABEL_IMG_HEIGHT = 250; // Adjusted for better viewing

    public ImagePanel() {
        setBackground(new Color(240, 240, 240));
        setLayout(new GridLayout(1, 2, 10, 10)); // One row, two columns
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        seqLabel = createStyledLabel("Sequential Result");
        parLabel = createStyledLabel("Parallel Result");

        add(seqLabel);
        add(parLabel);
    }

    /**
     * Helper method to create a JLabel with common styling.
     * @param title The title for the label's border.
     * @return A styled JLabel.
     */
    private JLabel createStyledLabel(String title) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(LABEL_IMG_WIDTH, LABEL_IMG_HEIGHT));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));
        return label;
    }

    /**
     * Sets the images to be displayed in this panel by updating the ImageIcons of the JLabels.
     * Each image is scaled to fit its respective JLabel.
     * @param seqImage Sequential processed image.
     * @param parImage Parallel processed image.
     * @param seqTitle Title for sequential label.
     * @param parTitle Title for parallel label.
     */
    public void setImages(BufferedImage seqImage, BufferedImage parImage, String seqTitle, String parTitle) {
        updateLabelImage(seqLabel, seqImage, seqTitle);
        updateLabelImage(parLabel, parImage, parTitle);
    }

    /**
     * Helper method to update a JLabel's icon, scaling the image and updating its title.
     * @param label The JLabel to update.
     * @param image The BufferedImage to set as the icon.
     * @param title The new title for the label's border.
     */
    private void updateLabelImage(JLabel label, BufferedImage image, String title) {
        if (image != null) {
            Image scaledImage = image.getScaledInstance(
                    label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImage));
        } else {
            label.setIcon(null); // Clear icon if image is null
        }
        // Update the titled border's title
        if (label.getBorder() instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.CompoundBorder compoundBorder = (javax.swing.border.CompoundBorder) label.getBorder();
            if (compoundBorder.getOutsideBorder() instanceof javax.swing.border.TitledBorder) {
                ((javax.swing.border.TitledBorder) compoundBorder.getOutsideBorder()).setTitle(title);
            }
        }
        label.repaint(); // Repaint to ensure title change is visible
    }

    /**
     * Clears all displayed images from the panel.
     */
    public void clearImages() {
        seqLabel.setIcon(null);
        parLabel.setIcon(null);
        // Reset titles to default
        if (seqLabel.getBorder() instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.CompoundBorder compoundBorder = (javax.swing.border.CompoundBorder) seqLabel.getBorder();
            if (compoundBorder.getOutsideBorder() instanceof javax.swing.border.TitledBorder) {
                ((javax.swing.border.TitledBorder) compoundBorder.getOutsideBorder()).setTitle("Sequential Result");
            }
        }
         if (parLabel.getBorder() instanceof javax.swing.border.CompoundBorder) {
            javax.swing.border.CompoundBorder compoundBorder = (javax.swing.border.CompoundBorder) parLabel.getBorder();
            if (compoundBorder.getOutsideBorder() instanceof javax.swing.border.TitledBorder) {
                ((javax.swing.border.TitledBorder) compoundBorder.getOutsideBorder()).setTitle("Parallel Result");
            }
        }
        repaint();
    }
}
