@echo off
echo Downloading VLCJ library...

REM Create lib directory if it doesn't exist
if not exist "lib" mkdir lib

REM Download VLCJ library (version 4.8.2)
echo Downloading VLCJ 4.8.2...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/caprica/vlcj/releases/download/4.8.2/vlcj-4.8.2.jar' -OutFile 'lib/vlcj-4.8.2.jar'}"

if exist "lib/vlcj-4.8.2.jar" (
    echo VLCJ library downloaded successfully!
    echo.
    echo IMPORTANT: You need to install VLC media player on your system.
    echo Download VLC from: https://www.videolan.org/vlc/
    echo.
    echo After installing VLC, you can compile and run the project with:
    echo javac -cp "lib/*" src/*.java
    echo java -cp "lib/*;src" ImageProcessingApp
) else (
    echo Failed to download VLCJ library.
    echo Please download it manually from: https://github.com/caprica/vlcj/releases
    echo and place the JAR file in the lib directory.
)

pause 