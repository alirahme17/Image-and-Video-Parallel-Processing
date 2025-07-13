// ParallelImageTasks.java
// Contains the RecursiveAction classes for parallel image processing using Fork/Join.

package ParallelImageTasks;

import java.util.concurrent.RecursiveAction;
import org.opencv.videoio.Videoio;

/**
 * Utility class to hold all RecursiveAction implementations for parallel image processing.
 * This helps in organizing the Fork/Join specific tasks separately.
 */
public class ParallelImageTasks {

    // Helper to clamp color values to 0-255 range for all tasks
    private static int clampColor(int value) {
        return Math.min(255, Math.max(0, value));
    }

    // Helper to clamp coordinates for boundary replication in convolution tasks
    private static int clampCoord(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * RecursiveAction for applying grayscale transformation to a segment of an image's pixel array.
     */
    public static class GrayscaleTransformTask extends RecursiveAction {
        private final int[] originalPixels;
        private final int[] grayPixels;
        private final int width;
        private final int startY;
        private final int endY;
        private final int threshold;
        private static long totalOperations = 0; // Count operations

        public GrayscaleTransformTask(int[] originalPixels, int[] grayPixels, int width, int startY, int endY, int threshold) {
            this.originalPixels = originalPixels;
            this.grayPixels = grayPixels;
            this.width = width;
            this.startY = startY;
            this.endY = endY;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int rowsToProcess = endY - startY;

            if (rowsToProcess <= threshold) {
                // Process pixels directly in the assigned row range
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * width + x;
                        int pixel = originalPixels[index];

                        int alpha = (pixel >> 24) & 0xff;
                        int red = (pixel >> 16) & 0xff;
                        int green = (pixel >> 8) & 0xff;
                        int blue = pixel & 0xff;

                        // Use weighted average for better grayscale conversion
                        int avg = (int)(0.299 * red + 0.587 * green + 0.114 * blue);
                        grayPixels[index] = (alpha << 24) | (avg << 16) | (avg << 8) | avg;
                        
                        // Count operations: 3 bit shifts, 3 ANDs, 3 multiplications, 2 additions
                        totalOperations += 11;
                    }
                }
                System.out.println("Grayscale operations per pixel: 11");
                System.out.println("Total grayscale operations: " + totalOperations);
            } else {
                // Split the task into two subtasks
                int midY = startY + (rowsToProcess / 2);
                invokeAll(new GrayscaleTransformTask(originalPixels, grayPixels, width, startY, midY, threshold),
                          new GrayscaleTransformTask(originalPixels, grayPixels, width, midY, endY, threshold));
            }
        }
    }

    /**
     * RecursiveAction for applying sepia transformation to a segment of an image's pixel array.
     */
    public static class SepiaTransformTask extends RecursiveAction {
        private final int[] originalPixels;
        private final int[] sepiaPixels;
        private final int width;
        private final int startY;
        private final int endY;
        private final int threshold;

        public SepiaTransformTask(int[] originalPixels, int[] sepiaPixels, int width, int startY, int endY, int threshold) {
            this.originalPixels = originalPixels;
            this.sepiaPixels = sepiaPixels;
            this.width = width;
            this.startY = startY;
            this.endY = endY;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int rowsToProcess = endY - startY;

            if (rowsToProcess <= threshold) {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * width + x;
                        int pixel = originalPixels[index];

                        int alpha = (pixel >> 24) & 0xff;
                        int red = (pixel >> 16) & 0xff;
                        int green = (pixel >> 8) & 0xff;
                        int blue = pixel & 0xff;

                        int newRed = (int)(0.393 * red + 0.769 * green + 0.189 * blue);
                        int newGreen = (int)(0.349 * red + 0.686 * green + 0.168 * blue);
                        int newBlue = (int)(0.272 * red + 0.534 * green + 0.131 * blue);

                        newRed = clampColor(newRed);
                        newGreen = clampColor(newGreen);
                        newBlue = clampColor(newBlue);

                        sepiaPixels[index] = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
                    }
                }
            } else {
                int midY = startY + (rowsToProcess / 2);
                invokeAll(new SepiaTransformTask(originalPixels, sepiaPixels, width, startY, midY, threshold),
                          new SepiaTransformTask(originalPixels, sepiaPixels, width, midY, endY, threshold));
            }
        }
    }

    /**
     * RecursiveAction for applying a custom convolution filter to a segment of an image's pixel array.
     */
    public static class CustomFilterTransformTask extends RecursiveAction {
        private final int[] originalPixels;
        private final int[] filteredPixels;
        private final int width;
        private final int height;
        private final int startY;
        private final int endY;
        private final double[][] kernel;
        private final int threshold;
        private static long totalOperations = 0; // Count operations

        public CustomFilterTransformTask(int[] originalPixels, int[] filteredPixels, int width, int height, int startY, int endY, double[][] kernel, int threshold) {
            this.originalPixels = originalPixels;
            this.filteredPixels = filteredPixels;
            this.width = width;
            this.height = height;
            this.startY = startY;
            this.endY = endY;
            this.kernel = kernel;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int rowsToProcess = endY - startY;

            if (rowsToProcess <= threshold) {
                int kernelWidth = kernel[0].length;
                int kernelHeight = kernel.length;
                int halfKernelWidth = kernelWidth / 2;
                int halfKernelHeight = kernelHeight / 2;

                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        double redSum = 0.0;
                        double greenSum = 0.0;
                        double blueSum = 0.0;
                        int alpha = (originalPixels[y * width + x] >> 24) & 0xff;

                        for (int ky = 0; ky < kernelHeight; ky++) {
                            for (int kx = 0; kx < kernelWidth; kx++) {
                                int pixelX = x + kx - halfKernelWidth;
                                int pixelY = y + ky - halfKernelHeight;

                                // Boundary handling: Replicate pixels at the edges
                                pixelX = clampCoord(pixelX, 0, width - 1);
                                pixelY = clampCoord(pixelY, 0, height - 1);

                                int neighborPixel = originalPixels[pixelY * width + pixelX];
                                double kernelValue = kernel[ky][kx];

                                redSum += ((neighborPixel >> 16) & 0xff) * kernelValue;
                                greenSum += ((neighborPixel >> 8) & 0xff) * kernelValue;
                                blueSum += (neighborPixel & 0xff) * kernelValue;
                                
                                // Count operations per kernel element:
                                // 2 bit shifts, 2 ANDs, 1 multiplication, 1 addition
                                totalOperations += 6;
                            }
                        }
                        int newRed = clampColor((int) redSum);
                        int newGreen = clampColor((int) greenSum);
                        int newBlue = clampColor((int) blueSum);

                        filteredPixels[y * width + x] = (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
                        
                        // Additional operations for final pixel:
                        // 3 bit shifts, 3 ORs, 3 type casts
                        totalOperations += 9;
                    }
                }
                System.out.println("Blur operations per pixel: " + (6 * kernelWidth * kernelHeight + 9));
                System.out.println("Total blur operations: " + totalOperations);
            } else {
                int midY = startY + (rowsToProcess / 2);
                invokeAll(new CustomFilterTransformTask(originalPixels, filteredPixels, width, height, startY, midY, kernel, threshold),
                          new CustomFilterTransformTask(originalPixels, filteredPixels, width, height, midY, endY, kernel, threshold));
            }
        }
    }
}
