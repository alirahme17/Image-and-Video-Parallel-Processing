import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import java.util.*;
import java.util.concurrent.*;
import java.io.File;

public class VideoProcessor {
    static {
        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // Sequential grayscale for video
    public static void processVideoGrayscaleSequential(File inputFile, File outputFile) {
        try {
            VideoCapture cap = new VideoCapture(inputFile.getAbsolutePath());
            if (!cap.isOpened()) {
                System.err.println("Error: Could not open input video: " + inputFile.getAbsolutePath());
                return;
            }
            
            int fourcc = VideoWriter.fourcc('m','p','4','v'); // Use MP4V codec for MP4
            int width = (int) cap.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int height = (int) cap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double fps = cap.get(Videoio.CAP_PROP_FPS);
            
            VideoWriter writer = new VideoWriter(outputFile.getAbsolutePath(), fourcc, fps, new Size(width, height), false);
            if (!writer.isOpened()) {
                System.err.println("Error: Could not create output video writer: " + outputFile.getAbsolutePath());
                cap.release();
                return;
            }

            Mat frame = new Mat();
            int frameCount = 0;
            while (cap.read(frame)) {
                if (!frame.empty()) {
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    writer.write(grayFrame);
                    frameCount++;
                }
            }
            System.out.println("Sequential processing completed. Processed " + frameCount + " frames.");
            cap.release();
            writer.release();
        } catch (Exception e) {
            System.err.println("Error in sequential video processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Parallel grayscale for video (using thread pool)
    public static void processVideoGrayscaleParallel(File inputFile, File outputFile, int numThreads) {
        try {
            VideoCapture cap = new VideoCapture(inputFile.getAbsolutePath());
            if (!cap.isOpened()) {
                System.err.println("Error: Could not open input video: " + inputFile.getAbsolutePath());
                return;
            }
            
            int fourcc = VideoWriter.fourcc('m','p','4','v'); // Use MP4V codec for MP4
            int width = (int) cap.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int height = (int) cap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double fps = cap.get(Videoio.CAP_PROP_FPS);
            
            VideoWriter writer = new VideoWriter(outputFile.getAbsolutePath(), fourcc, fps, new Size(width, height), false);
            if (!writer.isOpened()) {
                System.err.println("Error: Could not create output video writer: " + outputFile.getAbsolutePath());
                cap.release();
                return;
            }

            List<Mat> frames = new ArrayList<>();
            Mat frame = new Mat();
            while (cap.read(frame)) {
                if (!frame.empty()) {
                    frames.add(frame.clone());
                }
            }
            cap.release();

            // Parallel processing
            ExecutorService pool = Executors.newFixedThreadPool(numThreads);
            List<Future<Mat>> futures = new ArrayList<>();
            for (Mat f : frames) {
                futures.add(pool.submit(() -> {
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(f, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    return grayFrame;
                }));
            }
            pool.shutdown();
            
            for (Future<Mat> fut : futures) {
                try {
                    Mat processedFrame = fut.get();
                    writer.write(processedFrame);
                } catch (Exception e) {
                    System.err.println("Error in parallel processing: " + e.getMessage());
                }
            }
            System.out.println("Parallel processing completed. Processed " + frames.size() + " frames with " + numThreads + " threads.");
            writer.release();
        } catch (Exception e) {
            System.err.println("Error in parallel video processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Timing for 1-12 threads, returns list of times in ms
    public static List<Long> timeParallelGrayscale(File inputFile, File outputFileBase) {
        List<Long> times = new ArrayList<>();
        for (int threads = 1; threads <= 12; threads++) {
            File outFile = new File(outputFileBase.getParent(), 
                outputFileBase.getName().replace(".mp4", "_" + threads + ".mp4"));
            long start = System.nanoTime();
            processVideoGrayscaleParallel(inputFile, outFile, threads);
            long end = System.nanoTime();
            times.add((end - start) / 1_000_000);
        }
        return times;
    }
} 