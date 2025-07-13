@echo off
echo Setting up Image Processing Project Libraries...
echo.

REM Create lib directory if it doesn't exist
if not exist "lib" mkdir lib

echo.
echo ========================================
echo STEP 1: OpenCV Setup
echo ========================================
echo.
echo You need to download OpenCV Java library.
echo Please download from: https://opencv.org/releases/
echo.
echo For Windows, download:
echo - opencv-4.x.x-windows-x64.exe
echo.
echo After installation, copy the JAR file from:
echo - C:\opencv\build\java\opencv-xxx.jar
echo.
echo Place it in the lib directory as: lib/opencv-xxx.jar
echo.

echo ========================================
echo STEP 2: VLC Setup (Optional)
echo ========================================
echo.
echo For full video playback features:
echo 1. Install VLC from: https://www.videolan.org/vlc/
echo 2. Download VLCJ library:
echo.

REM Try to download VLCJ
echo Downloading VLCJ library...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://github.com/caprica/vlcj/releases/download/4.8.2/vlcj-4.8.2.jar' -OutFile 'lib/vlcj-4.8.2.jar'; Write-Host 'VLCJ downloaded successfully!' } catch { Write-Host 'Failed to download VLCJ. Please download manually from: https://github.com/caprica/vlcj/releases' }}"

echo.
echo ========================================
echo STEP 3: Compilation Instructions
echo ========================================
echo.
echo After placing the libraries in the lib directory:
echo.
echo 1. Compile the project:
echo    javac -cp "lib/*" src/*.java
echo.
echo 2. Run the application:
echo    java -cp "lib/*;src" ImageProcessingApp
echo.
echo ========================================
echo Current lib directory contents:
echo ========================================
if exist "lib" (
    dir lib
) else (
    echo lib directory is empty
)

echo.
echo ========================================
echo NEXT STEPS:
echo ========================================
echo 1. Download OpenCV and place JAR in lib/
echo 2. (Optional) Install VLC for video playback
echo 3. Compile and run the project
echo.
pause 