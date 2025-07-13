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
echo Downloading OpenCV Java library...
echo This may take a few minutes depending on your internet connection.
echo.

REM Try to download OpenCV (this is a simplified version - users may need to download manually)
echo Note: OpenCV download requires manual intervention due to large file size.
echo Please download OpenCV from: https://opencv.org/releases/
echo.
echo For Windows:
echo 1. Download opencv-4.x.x-windows-x64.exe
echo 2. Install it
echo 3. Copy from C:\opencv\build\java\:
echo    - opencv-xxx.jar to lib/
echo    - opencv_javaxxx.dll to lib/
echo.

echo ========================================
echo STEP 2: VLC Setup (Optional)
echo ========================================
echo.
echo Downloading VLCJ library...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri 'https://github.com/caprica/vlcj/releases/download/4.8.2/vlcj-4.8.2.jar' -OutFile 'lib/vlcj-4.8.2.jar'; Write-Host 'VLCJ downloaded successfully!' } catch { Write-Host 'Failed to download VLCJ. Please download manually from: https://github.com/caprica/vlcj/releases' }}"

echo.
echo ========================================
echo STEP 3: Verification
echo ========================================
echo.
echo Checking for required files...
if exist "lib\opencv-*.jar" (
    echo ✓ OpenCV JAR found
) else (
    echo ✗ OpenCV JAR missing - please download manually
)

if exist "lib\opencv_java*.dll" (
    echo ✓ OpenCV DLL found
) else (
    echo ✗ OpenCV DLL missing - please download manually
)

if exist "lib\vlcj-*.jar" (
    echo ✓ VLCJ JAR found
) else (
    echo ✗ VLCJ JAR missing (optional)
)

echo.
echo ========================================
echo STEP 4: Compilation Instructions
echo ========================================
echo.
echo After placing all libraries in the lib directory:
echo.
echo 1. Compile the project:
echo    javac -cp "lib/*" src/*.java src/ParallelImageTasks/*.java
echo.
echo 2. Run the application:
echo    java -Djava.library.path=lib -cp "lib/*;src" ImageProcessingApp
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
echo 1. Download OpenCV and place files in lib/
echo 2. (Optional) Install VLC for video playback
echo 3. Compile and run the project
echo.
pause 