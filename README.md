# Parallel Image and Video Processing in Java

## Overview
This project demonstrates high-performance image and video processing in Java using both **sequential** and **parallel** (Fork/Join framework) techniques. It supports effects such as Grayscale, Sepia, Blur, Edge Detection, and Custom Filters, and provides detailed timing, speedup, and efficiency analysis for both images and videos.

## Features
- **Image Processing**: Apply effects sequentially and in parallel, compare performance
- **Video Processing**: Frame-by-frame grayscale conversion, sequential and parallel (1-12 threads)
- **Performance Analysis**: Automatic timing, speedup, and efficiency calculation
- **Parallelization**: Uses Java Fork/Join for images, thread pool for videos
- **OpenCV Integration**: For video and advanced image operations
- **Cross-platform**: Works on Windows, Linux, Mac (with proper OpenCV/VLC setup)

---

## Parallel vs Sequential Processing

### Sequential Processing
- Processes each pixel (or video frame) one at a time, in a single thread
- Simple, but slow for large images/videos
- Baseline for performance comparison

### Parallel Processing
- **Images**: Uses Fork/Join to split the image into row segments, processed by multiple threads
- **Videos**: Processes multiple frames in parallel using a thread pool
- Achieves significant speedup on multi-core CPUs
- Efficiency and speedup are measured and displayed

---

## Setup Instructions

### 1. Prerequisites
- **Java 11+** (JDK)
- **OpenCV Java** (download from https://opencv.org/releases/)
- **VLCJ** (for advanced video playback, download from https://github.com/caprica/vlcj/releases)
- **VLC Media Player** (for VLCJ, https://www.videolan.org/vlc/)

### 2. Directory Structure
```
ImageProcessing/
├── src/                       # Java source files
│   ├── ImageProcessingApp.java
│   ├── ImageProcessor.java
│   ├── VideoProcessor.java
│   ├── SimpleVideoPlayer.java
│   ├── VLCVideoPlayer.java
│   └── ParallelImageTasks/
│       └── ParallelImageTasks.java
├── lib/                       # Place OpenCV and VLCJ JARs and DLLs here
│   ├── opencv-xxx.jar
│   ├── opencv_javaxxx.dll (Windows) or .so/.dylib (Linux/Mac)
│   └── vlcj-4.x.x.jar
├── bin/                       # Compiled classes (optional)
├── README.md
└── ...
```

### 3. Placing Libraries
**Note**: Large library files (>25MB) are not included in this repository due to GitHub's file size limits.

**Download Required Libraries:**
1. **OpenCV Java**: Download from https://opencv.org/releases/
   - Extract `opencv-xxx.jar` and `opencv_javaxxx.dll` (Windows) or `.so` (Linux) or `.dylib` (Mac)
   - Place both files in the `lib/` directory

2. **VLCJ** (optional): Download from https://github.com/caprica/vlcj/releases/
   - Extract `vlcj-4.x.x.jar` and place in `lib/` directory
   - Install VLC Media Player from https://www.videolan.org/vlc/

**File Structure After Download:**
```
lib/
├── opencv-4120.jar          # ~800KB
├── opencv_java4120.dll      # ~50MB (Windows)
└── vlcj-4.8.2.jar          # ~400KB (optional)
```

### 4. Quick Setup (Optional)
Run the setup script to download VLCJ automatically:
- **Windows**: `setup_libraries.bat`
- **Linux/Mac**: `./setup_libraries.sh`

### 5. Compiling
```sh
javac -cp "lib/*" src/*.java src/ParallelImageTasks/*.java
```

### 6. Running
```sh
java -Djava.library.path=lib -cp "lib/*;src" ImageProcessingApp
```

---

## Usage

### Image Processing
1. Start the application and choose **Image** mode
2. Load an image (PNG, JPG, etc.)
3. Select an effect (Grayscale, Sepia, etc.)
4. Click **Process Image**
5. View timing, speedup, and efficiency results

### Video Processing
1. Start the application and choose **Video** mode
2. Upload a video (MP4, AVI, MOV, MKV)
3. Click **Process Video**
4. View timing, speedup, and efficiency results

---

## Performance Analysis
- **Sequential Time**: Time taken for single-threaded processing
- **Parallel Time (N threads)**: Time taken using N threads (1-12)
- **Speedup**: `Speedup = Sequential Time / Parallel Time (12 threads)`
- **Efficiency**: `Efficiency = Speedup / 12 × 100%`
- **Graph**: Visualizes timing and speedup for all thread counts

---

## Technical Details
- **Fork/Join**: Used for parallel image processing (splits image by rows)
- **Thread Pool**: Used for parallel video frame processing
- **OpenCV**: Used for video I/O and advanced image operations
- **No GUI/Playback Details**: This README focuses on processing and performance, not GUI or video playback

---

## Contributing
Pull requests and suggestions are welcome! Please open an issue or submit a PR.


## Contact
For questions or support, open an issue on GitHub.

