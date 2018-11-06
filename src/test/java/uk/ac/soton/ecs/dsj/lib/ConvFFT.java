package uk.ac.soton.ecs.dsj.lib;

import org.openimaj.image.FImage;

/**
 * MyConvolution forced to use FFT for testing purposes.
 *
 * @author David Jones (dsj1n15@ecs.soton.ac.uk)
 */
public class ConvFFT extends MyConvolution {

  public ConvFFT(float[][] kernel) {
    super(kernel, true);
  }

  @Override
  public void processImage(FImage image) {
    final int rows = image.getRows();
    final int cols = image.getCols();

    // Validate inputs
    if (rows < kernel.length || cols < kernel[0].length) {
      throw new IllegalArgumentException("Image must be bigger than convoluter kernel");
    }

    FImage buffer = fourierConvolution(image, this.kernel);
    // Apply border to conform with coursework specification
    final int miny = kernel.length / 2;
    final int minx = kernel[0].length / 2;
    final int maxy = rows - miny;
    final int maxx = cols - minx;
    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {
        if (x < minx || y < miny || x > maxx || y > maxy) {
          buffer.pixels[y][x] = 0f;
        }
      }
    }
    // Apply new changes all in one go
    image.internalAssign(buffer);
  }
  
}
