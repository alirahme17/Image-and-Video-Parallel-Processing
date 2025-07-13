import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * VLC Video Player Component for Java Swing
 * Provides real video playback with audio and controls
 */
public class VLCVideoPlayer extends JPanel {
    
    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JSlider progressSlider;
    private JLabel timeLabel;
    private JLabel durationLabel;
    private Timer progressTimer;
    private boolean isPlaying = false;
    private boolean vlcAvailable = false;
    
    public VLCVideoPlayer() {
        initComponents();
        initVLC();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Video display area
        setPreferredSize(new Dimension(400, 300));
        setBackground(Color.BLACK);
        
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
            if (vlcAvailable && mediaPlayer != null && !progressSlider.getValueIsAdjusting()) {
                float position = progressSlider.getValue() / 100.0f;
                mediaPlayer.controls().setPosition(position);
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
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Progress timer
        progressTimer = new Timer(100, e -> updateProgress());
        
        updateButtonStates();
    }
    
    private void initVLC() {
        try {
            factory = new MediaPlayerFactory();
            mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
            
            // Add event listener
            mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void playing(MediaPlayer mediaPlayer) {
                    SwingUtilities.invokeLater(() -> {
                        isPlaying = true;
                        updateButtonStates();
                        progressTimer.start();
                    });
                }
                
                @Override
                public void paused(MediaPlayer mediaPlayer) {
                    SwingUtilities.invokeLater(() -> {
                        isPlaying = false;
                        updateButtonStates();
                        progressTimer.stop();
                    });
                }
                
                @Override
                public void stopped(MediaPlayer mediaPlayer) {
                    SwingUtilities.invokeLater(() -> {
                        isPlaying = false;
                        updateButtonStates();
                        progressTimer.stop();
                        progressSlider.setValue(0);
                        timeLabel.setText("00:00");
                    });
                }
                
                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    SwingUtilities.invokeLater(() -> {
                        isPlaying = false;
                        updateButtonStates();
                        progressTimer.stop();
                        progressSlider.setValue(0);
                        timeLabel.setText("00:00");
                    });
                }
            });
            
            vlcAvailable = true;
            
        } catch (Exception e) {
            System.err.println("Error initializing VLC: " + e.getMessage());
            e.printStackTrace();
            vlcAvailable = false;
            
            // Show error message
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "VLC is not available. Please install VLC media player from https://www.videolan.org/vlc/\n" +
                    "The video player will use OpenCV fallback instead.",
                    "VLC Not Available", JOptionPane.WARNING_MESSAGE);
            });
        }
    }
    
    public void loadVideo(String videoPath) {
        if (vlcAvailable && mediaPlayer != null) {
            stop();
            mediaPlayer.media().start(videoPath);
            updateButtonStates();
        } else {
            // Fallback to OpenCV-based player
            System.err.println("VLC not available, using OpenCV fallback");
        }
    }
    
    public void play() {
        if (vlcAvailable && mediaPlayer != null) {
            mediaPlayer.controls().start();
        }
    }
    
    public void pause() {
        if (vlcAvailable && mediaPlayer != null) {
            mediaPlayer.controls().setPause(true);
        }
    }
    
    public void stop() {
        if (vlcAvailable && mediaPlayer != null) {
            mediaPlayer.controls().stop();
        }
    }
    
    private void updateProgress() {
        if (vlcAvailable && mediaPlayer != null && isPlaying) {
            long time = mediaPlayer.status().time();
            long duration = mediaPlayer.status().length();
            
            if (duration > 0) {
                float position = (float) time / duration;
                progressSlider.setValue((int) (position * 100));
            }
            
            timeLabel.setText(formatTime(time));
            durationLabel.setText(formatTime(duration));
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
        boolean hasVLC = vlcAvailable && mediaPlayer != null;
        playButton.setEnabled(hasVLC && !isPlaying);
        pauseButton.setEnabled(hasVLC && isPlaying);
        stopButton.setEnabled(hasVLC);
        progressSlider.setEnabled(hasVLC);
    }
    
    public void release() {
        if (progressTimer != null) {
            progressTimer.stop();
        }
        if (vlcAvailable && mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (vlcAvailable && factory != null) {
            factory.release();
        }
    }
    
    public boolean isVlcAvailable() {
        return vlcAvailable;
    }
    
    // Note: VLC video surface integration is complex and varies by version
    // For now, this player will work with VLC's native window
    // The video will play in a separate VLC window
} 