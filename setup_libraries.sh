#!/bin/bash

echo "Setting up Image Processing Project Libraries..."
echo

# Create lib directory if it doesn't exist
mkdir -p lib

echo "========================================"
echo "STEP 1: OpenCV Setup"
echo "========================================"
echo
echo "Note: OpenCV download requires manual intervention due to large file size."
echo "Please download OpenCV from: https://opencv.org/releases/"
echo
echo "For Linux/Mac:"
echo "1. Download opencv-4.x.x source or pre-built package"
echo "2. Extract opencv-xxx.jar and opencv_javaxxx.so/.dylib"
echo "3. Place both files in the lib/ directory"
echo

echo "========================================"
echo "STEP 2: VLC Setup (Optional)"
echo "========================================"
echo
echo "Downloading VLCJ library..."
if command -v curl &> /dev/null; then
    curl -L -o lib/vlcj-4.8.2.jar https://github.com/caprica/vlcj/releases/download/4.8.2/vlcj-4.8.2.jar
    if [ $? -eq 0 ]; then
        echo "✓ VLCJ downloaded successfully!"
    else
        echo "✗ Failed to download VLCJ. Please download manually from: https://github.com/caprica/vlcj/releases"
    fi
elif command -v wget &> /dev/null; then
    wget -O lib/vlcj-4.8.2.jar https://github.com/caprica/vlcj/releases/download/4.8.2/vlcj-4.8.2.jar
    if [ $? -eq 0 ]; then
        echo "✓ VLCJ downloaded successfully!"
    else
        echo "✗ Failed to download VLCJ. Please download manually from: https://github.com/caprica/vlcj/releases"
    fi
else
    echo "✗ Neither curl nor wget found. Please download VLCJ manually."
fi

echo
echo "========================================"
echo "STEP 3: Verification"
echo "========================================"
echo
echo "Checking for required files..."

if ls lib/opencv-*.jar 1> /dev/null 2>&1; then
    echo "✓ OpenCV JAR found"
else
    echo "✗ OpenCV JAR missing - please download manually"
fi

if ls lib/opencv_java*.so 1> /dev/null 2>&1 || ls lib/opencv_java*.dylib 1> /dev/null 2>&1; then
    echo "✓ OpenCV native library found"
else
    echo "✗ OpenCV native library missing - please download manually"
fi

if ls lib/vlcj-*.jar 1> /dev/null 2>&1; then
    echo "✓ VLCJ JAR found"
else
    echo "✗ VLCJ JAR missing (optional)"
fi

echo
echo "========================================"
echo "STEP 4: Compilation Instructions"
echo "========================================"
echo
echo "After placing all libraries in the lib directory:"
echo
echo "1. Compile the project:"
echo "   javac -cp \"lib/*\" src/*.java src/ParallelImageTasks/*.java"
echo
echo "2. Run the application:"
echo "   java -Djava.library.path=lib -cp \"lib/*:src\" ImageProcessingApp"
echo
echo "========================================"
echo "Current lib directory contents:"
echo "========================================"
if [ -d "lib" ]; then
    ls -la lib/
else
    echo "lib directory is empty"
fi

echo
echo "========================================"
echo "NEXT STEPS:"
echo "========================================"
echo "1. Download OpenCV and place files in lib/"
echo "2. (Optional) Install VLC for video playback"
echo "3. Compile and run the project"
echo 