package uk.ac.soton.ecs.dsj.lib;

import org.openimaj.image.FImage;

/**
 * MyConvolution forced to use naive implementation for testing purposes.
 *
 * @author David Jones (dsj1n15@ecs.soton.ac.uk)
 */
public class ConvNaive extends MyConvolution {

  public ConvNaive(float[][] kernel) {
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
    FImage buffer = naiveConvolution(image, this.kernel);

    // Apply new changes all in one go
    image.internalAssign(buffer);
  }

}
