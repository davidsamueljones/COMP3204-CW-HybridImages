package uk.ac.soton.ecs.dsj.lib;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.image.processing.convolution.Gaussian2D;
import uk.ac.soton.ecs.dsj.lib.MyConvolution;

/**
 * Hybrid testing functions.
 *
 * @author David Jones (dsj1n15@ecs.soton.ac.uk)
 */
public class HybridTesting {

  /**
   * Apply a low-pass to the given image with the given sigma.
   * 
   * @param image Image to process
   * @param sigma Gaussian blur sigma
   * @return New image with low-pass applied 
   */
  public static MBFImage applyLowPass(MBFImage image, float sigma) {
    int size = (int) (8.0f * sigma + 1.0f);
    if (size % 2 == 0) {
      size++;
    }
    float[][] kernel = Gaussian2D.createKernelImage(size, sigma).pixels;
    MyConvolution conv = new MyConvolution(kernel);
    return image.process(conv);
  }

  /**
   * Throwaway test function.
   */
  @Deprecated
  public static void saveFrequencyProfile(Path path, FImage image) {
    if (Files.exists(path)) {
      throw new IllegalArgumentException(
          "Cannot save frequency profile, file already exists");
    }
    // Transform image to frequency domain
    final FourierTransform fftImage = new FourierTransform(image, false);

    CSVPrinter csvPrinter = null;
    try {
      BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
          StandardOpenOption.CREATE_NEW);
      csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("magnitude"));
      final int width = fftImage.getMagnitude().getWidth();
      final int height = fftImage.getMagnitude().getHeight();
      // Save the centre horizontal up to the vertical
      for (int x = 0; x < width / 2; x++) {
        csvPrinter.printRecord(fftImage.getMagnitude().pixels[height / 2 + 1][x]);
      }
      csvPrinter.flush();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException();
    } finally {
      if (csvPrinter != null) {
        try {
          csvPrinter.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }
  
}
