import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple Video Player Component using OpenCV
 * Provides basic video playback with controls
 */
public class SimpleVideoPlayer extends JPanel {
    
    private JLabel videoLabel;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JSlider progressSlider;
    private JLabel timeLabel;
    private JLabel durationLabel;
    private Timer frameTimer;
    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private AtomicBoolean isPaused = new AtomicBoolean(false);
    private File videoFile;
    private Thread videoThread;
    private int currentFrame = 0;
    private int totalFrames = 0;
    private double fps = 30.0;
    private int frameDelay = 33; // ~30 FPS
    
    public SimpleVideoPlayer() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Video display area
        videoLabel = new JLabel();
        videoLabel.setPreferredSize(new Dimension(400, 300));
        videoLabel.setHorizontalAlignment(JLabel.CENTER);
        videoLabel.setVerticalAlignment(JLabel.CENTER);
        videoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        videoLabel.setBackground(Color.BLACK);
        videoLabel.setOpaque(true);
        
        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        
        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        
        playButton.addActionListener(e -> play());
        pauseButton.addActionListener(e -> pause());
        stopButton.addActionListener(e -> stop());
        
        progressSlider = new JSlider(0, 100, 0);
        progressSlider.addChangeListener(e -> {
            if (!progressSlider.getValueIsAdjusting() && videoFile != null) {
                seekToPosition(progressSlider.getValue() / 100.0);
            }
        });
        
        timeLabel = new JLabel("00:00");
        durationLabel = new JLabel("00:00");
        
        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(new JLabel("Time:"));
        controlPanel.add(timeLabel);
        controlPanel.add(progressSlider);
        controlPanel.add(new JLabel("Duration:"));
        controlPanel.add(durationLabel);
        
        add(videoLabel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        updateButtonStates();
    }
    
    public void loadVideo(File videoFile) {
        this.videoFile = videoFile;
        stop();
        
        // Load first frame to get video info
        loadFirstFrame();
        
        updateButtonStates();
        updateLabels();
    }
    
    private void loadFirstFrame() {
        if (videoFile == null) return;
        
        try {
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
            org.opencv.videoio.VideoCapture cap = new org.opencv.videoio.VideoCapture(videoFile.getAbsolutePath());
            
            if (!cap.isOpened()) {
                System.err.println("Error: Could not open video file: " + videoFile.getAbsolutePath());
                return;
            }
            
            // Get video properties
            totalFrames = (int) cap.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_COUNT);
            fps = cap.get(org.opencv.videoio.Videoio.CAP_PROP_FPS);
            if (fps <= 0) fps = 30.0; // Default FPS
            frameDelay = (int) (1000.0 / fps);
            
            // Read first frame
            org.opencv.core.Mat frame = new org.opencv.core.Mat();
            if (cap.read(frame) && !frame.empty()) {
                displayFrame(frame);
            }
            
            cap.release();
            
        } catch (Exception e) {
            System.err.println("Error loading video: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void play() {
        if (videoFile == null) return;
        
        if (isPaused.get()) {
            isPaused.set(false);
            updateButtonStates();
            return;
        }
        
        stop(); // Stop any existing playback
        
        isPlaying.set(true);
        updateButtonStates();
        
        videoThread = new Thread(() -> {
            try {
                System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
                org.opencv.videoio.VideoCapture cap = new org.opencv.videoio.VideoCapture(videoFile.getAbsolutePath());
                
                if (!cap.isOpened()) {
                    System.err.println("Error: Could not open video file: " + videoFile.getAbsolutePath());
                    return;
                }
                
                org.opencv.core.Mat frame = new org.opencv.core.Mat();
                currentFrame = 0;
                
                while (isPlaying.get() && cap.read(frame)) {
                    if (!frame.empty()) {
                        final org.opencv.core.Mat frameCopy = frame.clone();
                        SwingUtilities.invokeLater(() -> {
                            displayFrame(frameCopy);
                            updateProgress();
                        });
                        
                        currentFrame++;
                        
                        // Check for pause
                        while (isPaused.get() && isPlaying.get()) {
                            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
                        }
                        
                        try { Thread.sleep(frameDelay); } catch (InterruptedException e) { break; }
                    }
                }
                
                cap.release();
                
                SwingUtilities.invokeLater(() -> {
                    isPlaying.set(false);
                    updateButtonStates();
                });
                
            } catch (Exception e) {
                System.err.println("Error playing video: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    isPlaying.set(false);
                    updateButtonStates();
                });
            }
        });
        
        videoThread.start();
    }
    
    public void pause() {
        isPaused.set(true);
        updateButtonStates();
    }
    
    public void stop() {
        isPlaying.set(false);
        isPaused.set(false);
        currentFrame = 0;
        
        if (videoThread != null && videoThread.isAlive()) {
            videoThread.interrupt();
        }
        
        updateButtonStates();
        updateProgress();
    }
    
    private void seekToPosition(double position) {
        if (videoFile == null) return;
        
        stop();
        currentFrame = (int) (position * totalFrames);
        
        // Load frame at position
        try {
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
            org.opencv.videoio.VideoCapture cap = new org.opencv.videoio.VideoCapture(videoFile.getAbsolutePath());
            
            if (cap.isOpened()) {
                cap.set(org.opencv.videoio.Videoio.CAP_PROP_POS_FRAMES, currentFrame);
                org.opencv.core.Mat frame = new org.opencv.core.Mat();
                if (cap.read(frame) && !frame.empty()) {
                    displayFrame(frame);
                }
                cap.release();
            }
            
        } catch (Exception e) {
            System.err.println("Error seeking: " + e.getMessage());
        }
    }
    
    private void displayFrame(org.opencv.core.Mat frame) {
        try {
            int type = frame.channels() > 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
            int bufferSize = frame.channels() * frame.cols() * frame.rows();
            byte[] b = new byte[bufferSize];
            frame.get(0, 0, b);
            BufferedImage img = new BufferedImage(frame.cols(), frame.rows(), type);
            img.getRaster().setDataElements(0, 0, frame.cols(), frame.rows(), b);
            
            // Scale image to fit the label
            Image scaledImage = img.getScaledInstance(videoLabel.getWidth(), videoLabel.getHeight(), Image.SCALE_SMOOTH);
            videoLabel.setIcon(new ImageIcon(scaledImage));
            
        } catch (Exception e) {
            System.err.println("Error displaying frame: " + e.getMessage());
        }
    }
    
    private void updateProgress() {
        if (totalFrames > 0) {
            double position = (double) currentFrame / totalFrames;
            progressSlider.setValue((int) (position * 100));
            
            long currentTime = (long) (currentFrame / fps * 1000);
            long totalTime = (long) (totalFrames / fps * 1000);
            
            timeLabel.setText(formatTime(currentTime));
            durationLabel.setText(formatTime(totalTime));
        }
    }
    
    private String formatTime(long timeMs) {
        if (timeMs < 0) return "00:00";
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private void updateButtonStates() {
        boolean hasVideo = videoFile != null;
        boolean playing = isPlaying.get();
        boolean paused = isPaused.get();
        
        playButton.setEnabled(hasVideo && (!playing || paused));
        pauseButton.setEnabled(hasVideo && playing && !paused);
        stopButton.setEnabled(hasVideo && (playing || paused));
        progressSlider.setEnabled(hasVideo);
    }
    
    private void updateLabels() {
        if (videoFile != null) {
            timeLabel.setText("00:00");
            durationLabel.setText(formatTime((long) (totalFrames / fps * 1000)));
        } else {
            timeLabel.setText("00:00");
            durationLabel.setText("00:00");
        }
    }
    
    public void release() {
        stop();
    }
} 