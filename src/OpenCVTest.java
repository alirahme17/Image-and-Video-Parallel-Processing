import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import java.io.File;

public class OpenCVTest {
    static {
        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        System.out.println("OpenCV Test Starting...");
        System.out.println("OpenCV Version: " + Core.VERSION);
        
        // Test if we can create a simple image
        Mat testMat = new Mat(100, 100, CvType.CV_8UC3, new Scalar(255, 0, 0));
        System.out.println("Successfully created test Mat: " + testMat.size());
        
        // Test if we can convert to grayscale
        Mat grayMat = new Mat();
        Imgproc.cvtColor(testMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        System.out.println("Successfully converted to grayscale: " + grayMat.size());
        
        // Test video capture (if a video file is provided)
        if (args.length > 0) {
            String videoPath = args[0];
            File videoFile = new File(videoPath);
            
            if (videoFile.exists()) {
                System.out.println("Testing video file: " + videoPath);
                
                VideoCapture cap = new VideoCapture(videoFile.getAbsolutePath());
                if (cap.isOpened()) {
                    System.out.println("Successfully opened video file");
                    System.out.println("Video properties:");
                    System.out.println("  Width: " + cap.get(Videoio.CAP_PROP_FRAME_WIDTH));
                    System.out.println("  Height: " + cap.get(Videoio.CAP_PROP_FRAME_HEIGHT));
                    System.out.println("  FPS: " + cap.get(Videoio.CAP_PROP_FPS));
                    System.out.println("  Frame count: " + cap.get(Videoio.CAP_PROP_FRAME_COUNT));
                    
                    // Read first frame
                    Mat frame = new Mat();
                    if (cap.read(frame)) {
                        System.out.println("Successfully read first frame: " + frame.size());
                        
                        // Test grayscale conversion
                        Mat grayFrame = new Mat();
                        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                        System.out.println("Successfully converted frame to grayscale");
                        
                        // Test video writing
                        String outputPath = "test_output.mp4";
                        int fourcc = VideoWriter.fourcc('m','p','4','v');
                        VideoWriter writer = new VideoWriter(outputPath, fourcc, cap.get(Videoio.CAP_PROP_FPS), 
                                                          new Size(frame.cols(), frame.rows()), false);
                        
                        if (writer.isOpened()) {
                            writer.write(grayFrame);
                            writer.release();
                            System.out.println("Successfully wrote test frame to: " + outputPath);
                        } else {
                            System.out.println("Failed to create video writer");
                        }
                    } else {
                        System.out.println("Failed to read first frame");
                    }
                    cap.release();
                } else {
                    System.out.println("Failed to open video file");
                }
            } else {
                System.out.println("Video file does not exist: " + videoPath);
            }
        } else {
            System.out.println("No video file provided. OpenCV basic functionality test passed.");
        }
        
        System.out.println("OpenCV Test Completed Successfully!");
    }
} 