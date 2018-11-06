package uk.ac.soton.ecs.dsj;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;
import uk.ac.soton.ecs.dsj.lib.ConvFFT;
import uk.ac.soton.ecs.dsj.lib.ConvFFTComplex;
import uk.ac.soton.ecs.dsj.lib.ConvNaive;

/**
 * Test main for COMP3204 Coursework 2.
 *
 * @author David Jones (dsj1n15@ecs.soton.ac.uk)
 */
public class SpeedTest {

  public static void main(String[] args) {
    kernelSizeTest();
    imageSizeTest();
  }

  /**
   * Test across multiple kernel sizes with repeats.
   */
  private static void kernelSizeTest() {
    final int[] kdims = {1, 3, 5, 7, 9, 11, 13, 15, 21, 25, 31, 39};
    // Test image contents does not matter
    final FImage img = new FImage(1024, 1024);
    final int repeats = 25;

    // Create csv writer for results
    Path path = Paths.get(String.format("speed-test-kernel-size-%dx%d.csv", img.getWidth(), img.getHeight()));
    CSVPrinter csvPrinter = null;
    try {
      BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
      csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("kdim", "naive", "fft", "fftcomplex"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException();
    }

    System.out.println("Starting...");
    for (int kdim : kdims) {
      System.out.println("Processing for dimension: " + kdim);
      // Contents of kernel does not matter
      final float[][] kernel = new float[kdim][kdim];
      // Implementations of MyConvultion where the specific method is forced
      final ConvNaive convNaive = new ConvNaive(kernel);
      final ConvFFTComplex convFFTComplex = new ConvFFTComplex(kernel);
      final ConvFFT convFFT = new ConvFFT(kernel);
      try {
        csvPrinter.printRecord(kdim, timeTest(img, convNaive, repeats), timeTest(img, convFFTComplex, repeats),
            timeTest(img, convFFT, repeats));
        csvPrinter.flush();
      } catch (IOException e) {
        System.err.println("Error writing for dimension: " + kdim);
      }
    }
    if (csvPrinter != null) {
      try {
        csvPrinter.close();
      } catch (IOException e) {
        // ignore
      }
    }
    System.out.println("Finished!");
  }

  /**
   * Test across multiple image sizes with repeats.
   */
  private static void imageSizeTest() {
    final int[] imgdims = {64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};
    // Test kernel contents does not matter
    final float[][] kernel = new float[9][9];
    // Implementations of MyConvultion where the specific method is forced
    final ConvNaive convNaive = new ConvNaive(kernel);
    final ConvFFTComplex convFFTComplex = new ConvFFTComplex(kernel);
    final ConvFFT convFFT = new ConvFFT(kernel);
    final int repeats = 25;

    // Create csv writer for results
    Path path = Paths.get(String.format("speed-test-image-size-%dx%d.csv", kernel[0].length, kernel.length));
    CSVPrinter csvPrinter = null;
    try {
      BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
      csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("imgdim", "naive", "fft", "fftcomplex"));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException();
    }

    System.out.println("Starting...");
    for (int imgdim : imgdims) {
      System.out.println("Processing for image size: " + imgdim);
      // Contents of image does not matter
      FImage img = new FImage(imgdim, imgdim);
      try {
        csvPrinter.printRecord(imgdim, timeTest(img, convNaive, repeats), timeTest(img, convFFTComplex, repeats),
            timeTest(img, convFFT, repeats));
        csvPrinter.flush();
      } catch (IOException e) {
        System.err.println("Error writing for image size: " + imgdim);
      }
    }
    if (csvPrinter != null) {
      try {
        csvPrinter.close();
      } catch (IOException e) {
        // ignore
      }
    }
    System.out.println("Finished!");
  }


  /**
   * Time an image processor execution time across a given number of repeats.
   * 
   * @param img Image to process
   * @param processor Image processor to time
   * @param repeats Number of times to execute image processor
   * @return Average time taken for a single execution
   */
  private static long timeTest(FImage img, SinglebandImageProcessor<Float, FImage> processor, int repeats) {
    final long startTime = System.currentTimeMillis();
    for (int r = 0; r < repeats; r++) {
      img.process(processor);
    }
    final long endTime = System.currentTimeMillis();
    long duration = (endTime - startTime);
    return duration / repeats;
  }

}
