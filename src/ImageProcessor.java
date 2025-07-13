// ImageProcessor.java
// Contains the core image processing logic for various effects, both sequential and parallel.

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.ForkJoinPool;
import java.io.IOException;
import javax.imageio.ImageIO;

// Import the parallel task classes
import ParallelImageTasks.ParallelImageTasks.GrayscaleTransformTask;
import ParallelImageTasks.ParallelImageTasks.SepiaTransformTask;
import ParallelImageTasks.ParallelImageTasks.CustomFilterTransformTask;

/**
 * Utility class for performing various image processing operations,
 * both sequentially and in parallel using Fork/Join.
 * Now uses direct pixel manipulation for improved performance.
 */
public class ImageProcessor {

    // Helper to clamp color values to 0-255 range
    private static int clamp(int value) {
        return Math.min(255, Math.max(0, value));
    }

    /**
     * Converts an image to grayscale sequentially using direct pixel manipulation.
     *
     * @param originalImage The input BufferedImage to convert.
     * @return A new BufferedImage representing the grayscale version of the input.
     */
    public static BufferedImage toGrayscaleSequential(BufferedImage originalImage) {
        System.out.println("Starting sequential grayscale conversion...");
        System.out.println("Original image type: " + originalImage.getType());
        System.out.println("Original image dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());
        
        // Create a new image with the same dimensions and TYPE_INT_ARGB
        BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Get direct access to pixel data arrays
        int[] originalPixels = ((DataBufferInt) originalImage.getRaster().getDataBuffer()).getData();
        int[] grayPixels = ((DataBufferInt) grayImage.getRaster().getDataBuffer()).getData();

        System.out.println("Processing " + (width * height) + " pixels...");

        for (int i = 0; i < width * height; i++) {
            int pixel = originalPixels[i];
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;

            // Use weighted average for better grayscale conversion
            int avg = (int)(0.299 * red + 0.587 * green + 0.114 * blue);
            grayPixels[i] = (alpha << 24) | (avg << 16) | (avg << 8) | avg;
        }

        System.out.println("Sequential grayscale conversion completed.");
        return grayImage;
    }

    /**
     * Converts an image to sepia tone sequentially using direct pixel manipulation.
     *
     * @param originalImage The input BufferedImage to convert.
     * @return A new BufferedImage representing the sepia-toned version of the input.
     */
    public static BufferedImage toSepiaSequential(BufferedImage originalImage) {
        BufferedImage sepiaImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int[] originalPixels = ((DataBufferInt) originalImage.getRaster().getDataBuffer()).getData();
        int[] sepiaPixels = ((DataBufferInt) sepiaImage.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < width * height; i++) {
            int pixel = originalPixels[i];
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;

            int newRed = (int)(0.393 * red + 0.769 * green + 0.189 * blue);
            int newGreen = (int)(0.349 * red + 0.686 * green + 0.168 * blue);
            int newBlue = (int)(0.272 * red + 0.534 * green + 0.131 * blue);

            newRed = clamp(newRed);
            newGreen = clamp(newGreen);
            newBlue = clamp(newBlue);

            sepiaPixels[i] = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
        }
        return sepiaImage;
    }

    /**
     * Applies a custom convolution filter to an image sequentially.
     *
     * @param originalImage The input BufferedImage.
     * @param kernel The convolution kernel (matrix) to apply.
     * @return A new BufferedImage with the filter applied.
     */
    public static BufferedImage applyCustomFilterSequential(BufferedImage originalImage, double[][] kernel) {
        BufferedImage filteredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int[] originalPixels = ((DataBufferInt) originalImage.getRaster().getDataBuffer()).getData();
        int[] filteredPixels = ((DataBufferInt) filteredImage.getRaster().getDataBuffer()).getData();

        int kernelWidth = kernel[0].length;
        int kernelHeight = kernel.length;
        int halfKernelWidth = kernelWidth / 2;
        int halfKernelHeight = kernelHeight / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double redSum = 0.0;
                double greenSum = 0.0;
                double blueSum = 0.0;
                int alpha = (originalPixels[y * width + x] >> 24) & 0xff; // Preserve alpha

                for (int ky = 0; ky < kernelHeight; ky++) {
                    for (int kx = 0; kx < kernelWidth; kx++) {
                        int pixelX = x + kx - halfKernelWidth;
                        int pixelY = y + ky - halfKernelHeight;

                        // Boundary handling: Replicate pixels at the edges
                        pixelX = clamp(pixelX, 0, width - 1);
                        pixelY = clamp(pixelY, 0, height - 1);

                        int neighborPixel = originalPixels[pixelY * width + pixelX];
                        double kernelValue = kernel[ky][kx];

                        redSum += ((neighborPixel >> 16) & 0xff) * kernelValue;
                        greenSum += ((neighborPixel >> 8) & 0xff) * kernelValue;
                        blueSum += (neighborPixel & 0xff) * kernelValue;
                    }
                }
                int newRed = clamp((int) redSum);
                int newGreen = clamp((int) greenSum);
                int newBlue = clamp((int) blueSum);

                filteredPixels[y * width + x] = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
            }
        }
        return filteredImage;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Converts an image to grayscale using the Fork/Join framework for parallel processing
     * and direct pixel manipulation.
     *
     * @param originalImage The input BufferedImage.
     * @param threshold The minimum number of rows to process sequentially in a task.
     * @return A new BufferedImage representing the grayscale version.
     */
    public static BufferedImage toGrayscaleParallel(BufferedImage originalImage, int threshold, ForkJoinPool pool) {
        System.out.println("Starting parallel grayscale conversion...");
        System.out.println("Original image type: " + originalImage.getType());
        System.out.println("Original image dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());
        
        BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] originalPixels = ((DataBufferInt) originalImage.getRaster().getDataBuffer()).getData();
        int[] grayPixels = ((DataBufferInt) grayImage.getRaster().getDataBuffer()).getData();

        System.out.println("Processing " + (originalImage.getWidth() * originalImage.getHeight()) + " pixels in parallel...");

        System.out.println("Number of threads in ForkJoinPool: " + pool.getParallelism());
        System.out.println("Number of processors available: " + Runtime.getRuntime().availableProcessors());

        GrayscaleTransformTask task = new GrayscaleTransformTask(originalPixels, grayPixels, originalImage.getWidth(), 0, originalImage.getHeight(), threshold);
        pool.invoke(task);

        System.out.println("Parallel grayscale conversion completed.");
        return grayImage;
    }

    /**
     * Converts an image to sepia tone using the Fork/Join framework for parallel processing
     * and direct pixel manipulation.
     *
     * @param originalImage The input BufferedImage.
     * @param threshold The minimum number of rows to process sequentially in a task.
     * @return A new BufferedImage representing the sepia-toned version.
     */
    public static BufferedImage toSepiaParallel(BufferedImage originalImage, int threshold, ForkJoinPool pool) {
        System.out.println("Starting parallel sepia conversion...");
        System.out.println("Original image type: " + originalImage.getType());
        System.out.println("Original image dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());
        
        BufferedImage sepiaImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] originalPixels = ((DataBufferInt) originalImage.getRaster().getDataBuffer()).getData();
        int[] sepiaPixels = ((DataBufferInt) sepiaImage.getRaster().getDataBuffer()).getData();

        System.out.println("Processing " + (originalImage.getWidth() * originalImage.getHeight()) + " pixels in parallel...");

        System.out.println("Number of threads in ForkJoinPool: " + pool.getParallelism());
        System.out.println("Number of processors available: " + Runtime.getRuntime().availableProcessors());

        SepiaTransformTask task = new SepiaTransformTask(originalPixels, sepiaPixels, originalImage.getWidth(), 0, originalImage.getHeight(), threshold);
        pool.invoke(task);

        System.out.println("Parallel sepia conversion completed.");
        return sepiaImage;
    }

    /**
     * Applies a custom convolution filter to an image using the Fork/Join framework for parallel processing
     * and direct pixel manipulation.
     *
     * @param originalImage The input BufferedImage.
     * @param kernel The convolution kernel (matrix) to apply.
     * @param threshold The minimum number of rows to process sequentially in a task.
     * @return A new BufferedImage with the filter applied.
     */
    public static BufferedImage applyCustomFilterParallel(BufferedImage originalImage, double[][] kernel, int threshold, ForkJoinPool pool) {
        System.out.println("Starting parallel custom filter application...");
        System.out.println("Original image type: " + originalImage.getType());
        System.out.println("Original image dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());
        
        BufferedImage filteredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] originalPixels = ((DataBufferInt) originalImage.getRaster().getDataBuffer()).getData();
        int[] filteredPixels = ((DataBufferInt) filteredImage.getRaster().getDataBuffer()).getData();

        System.out.println("Processing " + (originalImage.getWidth() * originalImage.getHeight()) + " pixels in parallel...");

        System.out.println("Number of threads in ForkJoinPool: " + pool.getParallelism());
        System.out.println("Number of processors available: " + Runtime.getRuntime().availableProcessors());

        CustomFilterTransformTask task = new CustomFilterTransformTask(originalPixels, filteredPixels, originalImage.getWidth(), originalImage.getHeight(), 0, originalImage.getHeight(), kernel, threshold);
        pool.invoke(task);

        System.out.println("Parallel custom filter application completed.");
        return filteredImage;
    }

    private BufferedImage loadDefaultImage() {
        try {
            // If you put it in src/resources, use "/resources/default.png"
            return ImageIO.read(getClass().getResource("/lebanon.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load default image: " + e.getMessage());
            return null;
        }
    }
}
