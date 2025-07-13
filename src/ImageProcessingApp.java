// ImageProcessingApp.java
// Main application class responsible for setting up the GUI and orchestrating the image processing.

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main class for the Image Processing Project with a Graphical User Interface (GUI).
 * This application demonstrates sequential and parallel image processing (Grayscale, Sepia, Blur, Edge Detection, Custom Filter)
 * using the Java Fork/Join framework, measures the speedup, and displays results in a user-friendly window.
 * It also includes image resizing options and uses direct pixel manipulation for performance.
 */
public class ImageProcessingApp extends JFrame {

    // Define a threshold for parallel tasks. If a segment of the image is smaller
    // than this threshold, it will be processed sequentially rather than forking new tasks.
    private static final int THRESHOLD = 100; // Process at least 100 rows per task

    private BufferedImage originalImage; // Stores the original loaded image
    private BufferedImage currentProcessedImage; // Stores the image after optional resizing

    private JLabel originalImageLabel; // Component to display the original image
    private ImagePanel processedImagePanel; // Custom panel to display processed images
    private JTextArea resultsTextArea; // Area to display processing times and speedup

    private JTextField widthField; // Text field for desired width
    private JTextField heightField; // Text field for desired height
    private JComboBox<String> effectComboBox; // Dropdown for choosing image effect

    private static final int IMAGE_DISPLAY_WIDTH = 300; // Fixed width for displayed images
    private static final int IMAGE_DISPLAY_HEIGHT = 200; // Fixed height for displayed images

    private boolean isVideoMode = false;
    private File currentVideoFile = null;

    private SimpleVideoPlayer originalVideoPlayer;
    private SimpleVideoPlayer processedVideoPlayer;

    /**
     * Constructor for the ImageProcessingApp GUI.
     * Sets up the main window and all its components.
     */
    public ImageProcessingApp() {
        setTitle("Image Processing with Parallel Computing");
        setSize(1200, 800); // Set initial window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation
        setLocationRelativeTo(null); // Center the window on the screen

        // User chooses image or video
        String[] options = {"Image", "Video"};
        int choice = JOptionPane.showOptionDialog(
            null, "What do you want to process?", "Choose Media Type",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        isVideoMode = (choice == 1);

        initComponents();
    }

    /**
     * Initializes and arranges all Swing components for the GUI.
     */
    private void initComponents() {
        // Main content panel with a BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around the main panel
        mainPanel.setBackground(new Color(240, 240, 240)); // Light gray background

        // --- Top Panel for Controls ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS)); // Vertical layout for control sections
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Padding below controls
        controlPanel.setBackground(new Color(240, 240, 240));

        // Button Panel (Load/Process)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); // Horizontal layout for buttons
        buttonPanel.setBackground(new Color(240, 240, 240));
        JButton loadImageButton = new JButton("Load Image");
        styleButton(loadImageButton);
        loadImageButton.addActionListener(e -> loadImage());

        JButton processImageButton = new JButton("Process Image");
        styleButton(processImageButton);
        processImageButton.addActionListener(e -> {
            if (originalImage != null) {
                processImages();
            } else {
                JOptionPane.showMessageDialog(ImageProcessingApp.this,
                        "Please load an image first!", "No Image Loaded",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(loadImageButton);
        buttonPanel.add(processImageButton);
        // Only add image controls if not in video mode
        if (!isVideoMode) {
            controlPanel.add(buttonPanel);
        }

        // Resizing Panel
        JPanel resizePanel = new JPanel();
        resizePanel.setBackground(new Color(240, 240, 240));
        resizePanel.add(new JLabel("Resize To (W x H):"));
        widthField = new JTextField(5);
        heightField = new JTextField(5);
        resizePanel.add(widthField);
        resizePanel.add(new JLabel("x"));
        resizePanel.add(heightField);
        JButton applyResizeButton = new JButton("Apply Resize");
        styleButton(applyResizeButton);
        applyResizeButton.addActionListener(e -> applyImageResize());
        resizePanel.add(applyResizeButton);
        if (!isVideoMode) {
            controlPanel.add(resizePanel);
        }

        // Effect Selection Panel
        JPanel effectPanel = new JPanel();
        effectPanel.setBackground(new Color(240, 240, 240));
        effectPanel.add(new JLabel("Select Effect:"));
        String[] effects = {"Grayscale", "Sepia", "Blur", "Edge Detection", "Custom Filter"};
        effectComboBox = new JComboBox<>(effects);
        effectComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        effectPanel.add(effectComboBox);
        if (!isVideoMode) {
            controlPanel.add(effectPanel);
        }

        // Add video controls if in video mode
        if (isVideoMode) {
            JButton uploadVideoButton = new JButton("Upload Video");
            styleButton(uploadVideoButton);
            uploadVideoButton.addActionListener(e -> uploadVideo());
            controlPanel.add(uploadVideoButton);

            JButton processVideoButton = new JButton("Process Video");
            styleButton(processVideoButton);
            processVideoButton.addActionListener(e -> processVideoGrayscale());
            controlPanel.add(processVideoButton);
        }
        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // --- Center Panel for Image/Video Displays and Results ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(240, 240, 240));

        if (isVideoMode) {
            // Video display: two video players side by side
            JPanel videoDisplayPanel = new JPanel();
            videoDisplayPanel.setLayout(new BoxLayout(videoDisplayPanel, BoxLayout.X_AXIS));
            videoDisplayPanel.setBackground(new Color(240, 240, 240));
            
            // Original video player
            JPanel originalPanel = new JPanel(new BorderLayout());
            originalPanel.setBorder(BorderFactory.createTitledBorder("Original Video"));
            originalVideoPlayer = new SimpleVideoPlayer();
            originalPanel.add(originalVideoPlayer, BorderLayout.CENTER);
            
            // Processed video player
            JPanel processedPanel = new JPanel(new BorderLayout());
            processedPanel.setBorder(BorderFactory.createTitledBorder("Processed Video (Parallel)"));
            processedVideoPlayer = new SimpleVideoPlayer();
            processedPanel.add(processedVideoPlayer, BorderLayout.CENTER);
            
            videoDisplayPanel.add(originalPanel);
            videoDisplayPanel.add(processedPanel);
            centerPanel.add(videoDisplayPanel, BorderLayout.CENTER);
        } else {
            // Image display as before
            JPanel imageDisplayPanel = new JPanel();
            imageDisplayPanel.setLayout(new BoxLayout(imageDisplayPanel, BoxLayout.X_AXIS));
            imageDisplayPanel.setBackground(new Color(240, 240, 240));
            JPanel originalPanelWrapper = new JPanel(new BorderLayout());
            originalPanelWrapper.setBorder(BorderFactory.createTitledBorder("Original Image (or Resized)"));
            originalPanelWrapper.setBackground(new Color(240, 240, 240));
            originalImageLabel = new JLabel();
            originalImageLabel.setPreferredSize(new Dimension(IMAGE_DISPLAY_WIDTH, IMAGE_DISPLAY_HEIGHT));
            originalImageLabel.setHorizontalAlignment(JLabel.CENTER);
            originalImageLabel.setVerticalAlignment(JLabel.CENTER);
            originalImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            originalPanelWrapper.add(originalImageLabel, BorderLayout.CENTER);
            imageDisplayPanel.add(originalPanelWrapper);
            processedImagePanel = new ImagePanel();
            processedImagePanel.setBorder(BorderFactory.createTitledBorder("Processed Images"));
            imageDisplayPanel.add(processedImagePanel);
            centerPanel.add(imageDisplayPanel, BorderLayout.CENTER);
        }

        // Results text area
        resultsTextArea = new JTextArea(10, 50);
        resultsTextArea.setEditable(false);
        resultsTextArea.setBackground(new Color(255, 255, 240)); // Light yellow background for results
        resultsTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Processing Results"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollPane = new JScrollPane(resultsTextArea); // Add scroll capability
        centerPanel.add(scrollPane, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel); // Add the main panel to the frame
    }

    /**
     * Applies a standard style to JButtons for a consistent look.
     * @param button The JButton to style.
     */
    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 130, 180)); // SteelBlue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setPreferredSize(new Dimension(150, 40));
        button.setFont(button.getFont().deriveFont(14f));
    }

    /**
     * Opens a file chooser dialog to allow the user to select an image file.
     * Updates the GUI with the loaded image.
     */
    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        // Filter for common image file extensions
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this); // Show open dialog
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Read the image
                BufferedImage loadedImage = ImageIO.read(selectedFile);
                if (loadedImage != null) {
                    // Convert to TYPE_INT_ARGB if it's not already
                    originalImage = new BufferedImage(
                        loadedImage.getWidth(),
                        loadedImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                    );
                    // Draw the loaded image onto our ARGB image
                    Graphics2D g = originalImage.createGraphics();
                    g.drawImage(loadedImage, 0, 0, null);
                    g.dispose();

                    currentProcessedImage = originalImage; // Initially, current processed is the original
                    updateOriginalImageDisplay(currentProcessedImage); // Display the loaded image
                    resultsTextArea.setText("Image loaded successfully: " + selectedFile.getName() +
                            "\nDimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight() + "\n");
                    processedImagePanel.clearImages(); // Clear previous processed images
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not read image from " + selectedFile.getName(),
                            "Image Read Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading image: " + e.getMessage(),
                        "Loading Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Resizes the current image if valid dimensions are provided.
     * Updates the 'original' image display with the resized image.
     */
    private void applyImageResize() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first!", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int newWidth = Integer.parseInt(widthField.getText());
            int newHeight = Integer.parseInt(heightField.getText());

            if (newWidth <= 0 || newHeight <= 0) {
                JOptionPane.showMessageDialog(this, "Width and Height must be positive integers.", "Invalid Dimensions", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create a new BufferedImage for the scaled image
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, newWidth, newHeight, null);
            g.dispose();

            currentProcessedImage = resizedImage; // Set the resized image as the current one for processing
            updateOriginalImageDisplay(currentProcessedImage);
            resultsTextArea.append(String.format("Image resized to %dx%d.\n", newWidth, newHeight));
            processedImagePanel.clearImages();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid integer values for Width and Height.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Updates the JLabel displaying the original/current image.
     * @param imageToDisplay The BufferedImage to display.
     */
    private void updateOriginalImageDisplay(BufferedImage imageToDisplay) {
        if (imageToDisplay != null) {
            int width = originalImageLabel.getWidth();
            int height = originalImageLabel.getHeight();
            // Fallback to default size if not yet laid out
            if (width <= 0 || height <= 0) {
                width = 300;  // Default width
                height = 200; // Default height
            }
            Image scaledImage = imageToDisplay.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            originalImageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            originalImageLabel.setIcon(null);
        }
    }

    /**
     * Initiates the image processing tasks (selected effect, sequential and parallel)
     * in a background thread using SwingWorker to keep the GUI responsive.
     */
    private void processImages() {
        if (currentProcessedImage == null) {
            JOptionPane.showMessageDialog(this, "No image to process. Load or resize an image first.", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedEffect = (String) effectComboBox.getSelectedItem();
        resultsTextArea.append(String.format("\nStarting image processing: %s Effect...\n", selectedEffect));
        setButtonsEnabled(false);

        new SwingWorker<Void, Void>() {
            BufferedImage sequentialResult;
            BufferedImage parallelResult;
            long sequentialTime;
            long parallelTime;
            java.util.List<Long> parallelTimes = new java.util.ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                long startTime, endTime;

                // Sequential processing (only once)
                switch (selectedEffect) {
                    case "Grayscale":
                        startTime = System.nanoTime();
                        sequentialResult = ImageProcessor.toGrayscaleSequential(currentProcessedImage);
                        endTime = System.nanoTime();
                        sequentialTime = (endTime - startTime) / 1_000_000;
                        break;
                    case "Sepia":
                        startTime = System.nanoTime();
                        sequentialResult = ImageProcessor.toSepiaSequential(currentProcessedImage);
                        endTime = System.nanoTime();
                        sequentialTime = (endTime - startTime) / 1_000_000;
                        break;
                    case "Blur":
                        double[][] blurKernel = {
                                {1.0/16, 2.0/16, 1.0/16},
                                {2.0/16, 4.0/16, 2.0/16},
                                {1.0/16, 2.0/16, 1.0/16}
                        };
                        startTime = System.nanoTime();
                        sequentialResult = ImageProcessor.applyCustomFilterSequential(currentProcessedImage, blurKernel);
                        endTime = System.nanoTime();
                        sequentialTime = (endTime - startTime) / 1_000_000;
                        break;
                    case "Edge Detection":
                        double[][] edgeKernel = {
                                {-1, -1, -1},
                                {-1,  8, -1},
                                {-1, -1, -1}
                        };
                        startTime = System.nanoTime();
                        sequentialResult = ImageProcessor.applyCustomFilterSequential(currentProcessedImage, edgeKernel);
                        endTime = System.nanoTime();
                        sequentialTime = (endTime - startTime) / 1_000_000;
                        break;
                    case "Custom Filter":
                        double[][] customKernel = {
                                { 0, -1,  0},
                                {-1,  5, -1},
                                { 0, -1,  0}
                        };
                        startTime = System.nanoTime();
                        sequentialResult = ImageProcessor.applyCustomFilterSequential(currentProcessedImage, customKernel);
                        endTime = System.nanoTime();
                        sequentialTime = (endTime - startTime) / 1_000_000;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown image effect selected: " + selectedEffect);
                }

                // Parallel processing: 1 to 12 threads
                for (int threads = 1; threads <= 12; threads++) {
                    java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool(threads);
                    long pStart = System.nanoTime();
                    switch (selectedEffect) {
                        case "Grayscale":
                            parallelResult = ImageProcessor.toGrayscaleParallel(currentProcessedImage, THRESHOLD, pool);
                            break;
                        case "Sepia":
                            parallelResult = ImageProcessor.toSepiaParallel(currentProcessedImage, THRESHOLD, pool);
                            break;
                        case "Blur":
                            double[][] blurKernel = {
                                    {1.0/16, 2.0/16, 1.0/16},
                                    {2.0/16, 4.0/16, 2.0/16},
                                    {1.0/16, 2.0/16, 1.0/16}
                            };
                            parallelResult = ImageProcessor.applyCustomFilterParallel(currentProcessedImage, blurKernel, THRESHOLD, pool);
                            break;
                        case "Edge Detection":
                            double[][] edgeKernel = {
                                    {-1, -1, -1},
                                    {-1,  8, -1},
                                    {-1, -1, -1}
                            };
                            parallelResult = ImageProcessor.applyCustomFilterParallel(currentProcessedImage, edgeKernel, THRESHOLD, pool);
                            break;
                        case "Custom Filter":
                            double[][] customKernel = {
                                    { 0, -1,  0},
                                    {-1,  5, -1},
                                    { 0, -1,  0}
                            };
                            parallelResult = ImageProcessor.applyCustomFilterParallel(currentProcessedImage, customKernel, THRESHOLD, pool);
                            break;
                    }
                    long pEnd = System.nanoTime();
                    parallelTimes.add((pEnd - pStart) / 1_000_000L); // ms
                    pool.shutdown();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    processedImagePanel.setImages(
                            sequentialResult, parallelResult,
                            selectedEffect + " Sequential", selectedEffect + " Parallel");

                    StringBuilder results = new StringBuilder();
                    results.append(String.format("--- %s Results ---\n", selectedEffect));
                    results.append("Sequential Time: ").append(sequentialTime).append(" ms\n");
                    
                    // Calculate speedup for 12-thread parallel processing
                    long parallel12ThreadTime = parallelTimes.get(11); // 12 threads (index 11)
                    double speedup = (double) sequentialTime / parallel12ThreadTime;
                    
                    results.append("Parallel Time (12 threads): ").append(parallel12ThreadTime).append(" ms\n");
                    results.append("Speedup (12 threads): ").append(String.format("%.2f", speedup)).append("x\n");
                    results.append("Efficiency: ").append(String.format("%.2f", speedup / 12.0 * 100)).append("%\n\n");
                    
                    // Show all parallel times
                    for (int i = 0; i < parallelTimes.size(); i++) {
                        results.append("Parallel Time (" + (i+1) + " threads): ").append(parallelTimes.get(i)).append(" ms");
                        if (i == 11) { // Highlight 12-thread result
                            results.append(" ← Best Performance");
                        }
                        results.append("\n");
                    }
                    resultsTextArea.append(results.toString());

                    // Show timing graph
                    JFrame graphFrame = new JFrame("Parallel Execution Time vs Threads");
                    graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    graphFrame.setSize(600, 400);
                    graphFrame.add(new TimingGraphPanel(parallelTimes, sequentialTime));
                    graphFrame.setLocationRelativeTo(ImageProcessingApp.this);
                    graphFrame.setVisible(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ImageProcessingApp.this,
                            "Error during image processing: " + ex.getMessage(),
                            "Processing Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void showFirstVideoFrame(File videoFile) {
        try {
            // Load the video into the original video player
            if (originalVideoPlayer != null) {
                originalVideoPlayer.loadVideo(videoFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Video Files", "mp4", "avi", "mov", "mkv"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentVideoFile = fileChooser.getSelectedFile();
            resultsTextArea.setText("Video loaded: " + currentVideoFile.getName() + "\n");
            showFirstVideoFrame(currentVideoFile);
        }
    }

    private void loadVideoInPlayer(File videoFile, SimpleVideoPlayer player) {
        if (player != null) {
            player.loadVideo(videoFile);
        }
    }

    private void processVideoGrayscale() {
        if (currentVideoFile == null) {
            JOptionPane.showMessageDialog(this, "Please upload a video first!", "No Video", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Disable buttons during processing
        setButtonsEnabled(false);
        
        new SwingWorker<Void, Void>() {
            private long seqTime;
            private java.util.List<Long> parTimes;
            private File processedVideoFile;
            
            @Override
            protected Void doInBackground() throws Exception {
                // Sequential processing
                File outSeq = new File(currentVideoFile.getParent(), "grayscale_seq.mp4");
                long start = System.nanoTime();
                VideoProcessor.processVideoGrayscaleSequential(currentVideoFile, outSeq);
                long end = System.nanoTime();
                seqTime = (end - start) / 1_000_000;
                
                // Parallel 1-12 threads
                File outParBase = new File(currentVideoFile.getParent(), "grayscale_par.mp4");
                parTimes = VideoProcessor.timeParallelGrayscale(currentVideoFile, outParBase);
                
                // The last file is the 12-thread processed video
                processedVideoFile = new File(currentVideoFile.getParent(), "grayscale_par_12.mp4");
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    // Check if processed video file exists
                    if (!processedVideoFile.exists()) {
                        JOptionPane.showMessageDialog(ImageProcessingApp.this,
                                "Error: Processed video file not found: " + processedVideoFile.getName(),
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Load videos into players
                    loadVideoInPlayer(currentVideoFile, originalVideoPlayer);
                    loadVideoInPlayer(processedVideoFile, processedVideoPlayer);
                    
                    // Show results
                    StringBuilder sb = new StringBuilder();
                    sb.append("Video processing completed!\n");
                    sb.append("Processed video saved as: ").append(processedVideoFile.getName()).append("\n");
                    sb.append("Sequential Time: ").append(seqTime).append(" ms\n");
                    sb.append("Parallel Time (12 threads): ").append(parTimes.get(11)).append(" ms\n");
                    sb.append("Speedup: ").append(String.format("%.2f", (double)seqTime / parTimes.get(11))).append("x\n\n");
                    for (int i = 0; i < parTimes.size(); i++) {
                        sb.append("Parallel Time (" + (i+1) + " threads): ").append(parTimes.get(i)).append(" ms\n");
                    }
                    resultsTextArea.append(sb.toString());
                    
                    // Show timing graph
                    JFrame graphFrame = new JFrame("Parallel Execution Time vs Threads (Video)");
                    graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    graphFrame.setSize(600, 400);
                    graphFrame.add(new TimingGraphPanel(parTimes, seqTime));
                    graphFrame.setLocationRelativeTo(ImageProcessingApp.this);
                    graphFrame.setVisible(true);
                    
                    // Show success message
                    JOptionPane.showMessageDialog(ImageProcessingApp.this,
                            "Video processing completed! Processed video saved as: " + processedVideoFile.getName(),
                            "Processing Complete", JOptionPane.INFORMATION_MESSAGE);
                            
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ImageProcessingApp.this,
                            "Error during video processing: " + ex.getMessage(),
                            "Processing Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * Enables or disables the Load and Process buttons, and other controls.
     * @param enabled True to enable, false to disable.
     */
    private void setButtonsEnabled(boolean enabled) {
        Container contentPane = getContentPane();
        JPanel mainPanel = (JPanel) contentPane.getComponent(0);
        JPanel controlPanel = (JPanel) mainPanel.getComponent(0); // This is the controlPanel

        // Iterate through all components in the controlPanel
        for (java.awt.Component comp : controlPanel.getComponents()) {
            if (comp instanceof JPanel) { // Check if it's one of the sub-panels (buttonPanel, resizePanel, effectPanel)
                for (java.awt.Component subComp : ((JPanel) comp).getComponents()) {
                    subComp.setEnabled(enabled);
                }
            } else {
                comp.setEnabled(enabled); // For direct components, if any
            }
        }
        // Special case for JComboBox as it has internal components
        effectComboBox.setEnabled(enabled);
    }

    // Enhanced timing graph panel with speedup information
    class TimingGraphPanel extends JPanel {
        private java.util.List<Long> times;
        private long sequentialTime; // For speedup calculation
        
        public TimingGraphPanel(java.util.List<Long> times) { 
            this.times = times; 
            this.sequentialTime = 0; // Will be set separately
        }
        
        public TimingGraphPanel(java.util.List<Long> times, long sequentialTime) { 
            this.times = times; 
            this.sequentialTime = sequentialTime;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth() - 80;
            int h = getHeight() - 80;
            int x0 = 60, y0 = getHeight() - 60;
            
            // Draw axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(x0, y0, x0 + w, y0); // X axis
            g2d.drawLine(x0, y0, x0, y0 - h); // Y axis
            g2d.drawString("Threads", x0 + w/2, y0 + 30);
            g2d.drawString("Time (ms)", 5, y0 - h/2);
            
            // Find max time
            long max = 1;
            for (Long t : times) if (t > max) max = t;
            
            // Draw sequential time line if available
            if (sequentialTime > 0) {
                g2d.setColor(Color.RED);
                int seqY = y0 - (int)((h) * sequentialTime / (double)max);
                g2d.drawLine(x0, seqY, x0 + w, seqY);
                g2d.drawString("Sequential: " + sequentialTime + " ms", x0 + w + 5, seqY + 5);
            }
            
            // Draw parallel times
            g2d.setColor(Color.BLUE);
            int prevX = -1, prevY = -1;
            for (int i = 0; i < times.size(); i++) {
                int x = x0 + (int)((w) * i / (times.size() - 1));
                int y = y0 - (int)((h) * times.get(i) / (double)max);
                
                // Highlight 12-thread result
                if (i == 11) {
                    g2d.setColor(Color.GREEN);
                    g2d.fillOval(x-5, y-5, 10, 10);
                    g2d.setColor(Color.BLUE);
                } else {
                    g2d.fillOval(x-3, y-3, 6, 6);
                }
                
                g2d.drawString("" + (i+1), x-5, y0 + 15);
                g2d.drawString(times.get(i) + " ms", x-10, y - 10);
                
                if (prevX != -1) g2d.drawLine(prevX, prevY, x, y);
                prevX = x; prevY = y;
            }
            
            // Draw speedup information if sequential time is available
            if (sequentialTime > 0 && times.size() > 11) {
                long parallel12ThreadTime = times.get(11);
                double speedup = (double) sequentialTime / parallel12ThreadTime;
                double efficiency = speedup / 12.0 * 100;
                
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString("Speedup (12 threads): " + String.format("%.2f", speedup) + "x", 10, 20);
                g2d.drawString("Efficiency: " + String.format("%.1f", efficiency) + "%", 10, 35);
            }
        }
    }

    /**
     * Main method to run the application.
     * Creates and displays the GUI on the Event Dispatch Thread (EDT).
     */
    public static void main(String[] args) {
        ImageProcessingApp app = new ImageProcessingApp();
        
        // Add window listener to release video players when closing
        app.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (app.originalVideoPlayer != null) {
                    app.originalVideoPlayer.release();
                }
                if (app.processedVideoPlayer != null) {
                    app.processedVideoPlayer.release();
                }
            }
        });
        
        javax.swing.SwingUtilities.invokeLater(() -> app.setVisible(true));
    }
}
