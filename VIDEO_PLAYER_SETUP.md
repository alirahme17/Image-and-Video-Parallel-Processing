# Video Player Setup Guide

This project now supports real video playback with both OpenCV and VLC options.

## Setup Options

### Option 1: OpenCV Only (Current Implementation)
- Uses the `SimpleVideoPlayer` class
- Provides basic video playback with controls
- No audio support
- Works with any video format that OpenCV can read

### Option 2: VLC + OpenCV (Recommended)
- Uses the `VLCVideoPlayer` class for real video playback
- Supports audio, seeking, and proper video controls
- Falls back to OpenCV if VLC is not available

## Setup Instructions

### For VLC Support:

1. **Install VLC Media Player**
   - Download from: https://www.videolan.org/vlc/
   - Install on your system

2. **Download VLCJ Library**
   - Run the provided script: `download_vlcj.bat`
   - Or manually download from: https://github.com/caprica/vlcj/releases
   - Place the JAR file in the `lib` directory

3. **Compile and Run**
   ```bash
   javac -cp "lib/*" src/*.java
   java -cp "lib/*;src" ImageProcessingApp
   ```

### For OpenCV Only:

1. **Compile and Run**
   ```bash
   javac -cp "lib/*" src/*.java
   java -cp "lib/*;src" ImageProcessingApp
   ```

## Features

### Video Processing
- Sequential and parallel grayscale processing
- Saves processed videos as MP4 files
- Timing measurements and speedup graphs

### Video Playback
- **Original Video Player**: Shows the input video
- **Processed Video Player**: Shows the grayscale processed video
- **Controls**: Play, Pause, Stop, Progress slider, Time display
- **Seeking**: Click on progress slider to jump to specific time

## Usage

1. **Start the Application**
   - Choose "Video" mode when prompted
   - The GUI will show two video player panels

2. **Upload Video**
   - Click "Upload Video" to select a video file
   - Supported formats: MP4, AVI, MOV, MKV

3. **Process Video**
   - Click "Process Video" to apply grayscale processing
   - The system will:
     - Process video sequentially and in parallel (1-12 threads)
     - Save the processed video as MP4
     - Display timing results and speedup graph
     - Load both videos into the players

4. **Play Videos**
   - Use the controls on each video player
   - Play, pause, stop, and seek through the videos
   - Compare original vs processed video

## Troubleshooting

### VLC Not Available
- If VLC is not installed, the system will show a warning
- The video player will fall back to OpenCV-based playback
- Install VLC for full video playback features

### Video Not Playing
- Check that the video file is valid and playable
- Ensure OpenCV can read the video format
- Try a different video file

### Compilation Errors
- Make sure all required libraries are in the `lib` directory
- Check that the classpath includes all JAR files
- Ensure OpenCV native libraries are available

## File Structure

```
ImageProcessing/
├── src/
│   ├── ImageProcessingApp.java    # Main GUI application
│   ├── VideoProcessor.java        # Video processing with OpenCV
│   ├── SimpleVideoPlayer.java     # OpenCV-based video player
│   └── VLCVideoPlayer.java       # VLC-based video player
├── lib/
│   ├── opencv-xxx.jar            # OpenCV Java library
│   └── vlcj-4.8.2.jar           # VLC Java library (optional)
└── download_vlcj.bat             # Script to download VLCJ
```

## Performance Notes

- **Video Processing**: Parallel processing shows significant speedup with multiple threads
- **Video Playback**: VLC provides smoother playback than OpenCV frame-by-frame
- **Memory Usage**: Large videos may require more memory for processing
- **File Size**: Processed MP4 files may be larger than original depending on codec 