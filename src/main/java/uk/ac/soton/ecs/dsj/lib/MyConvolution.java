package uk.ac.soton.ecs.dsj.lib;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.image.processing.algorithm.FourierTransformComplex;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Convolution operator implementation for COMP3204 Coursework 2.<br>
 * Both spatial and frequency domain operators are provided.
 *
 * @author David Jones (dsj1n15@ecs.soton.ac.uk)
 */
public class MyConvolution implements SinglebandImageProcessor<Float, FImage> {
  public static final int MAX_NAIVE_SIZE = 49;
  protected final float[][] kernel;
  protected boolean allowFFT;

  /**
   * Calls the {@link #MyConvolution(float[][] kernel, boolean allowFFT)} constructor with allowFFT
   * set to true.
   * 
   * @param kernel Kernel to use for convolution operator
   */
  public MyConvolution(float[][] kernel) {
    this(kernel, true);
  }

  /**
   * Create an instance of the convolution operator with a given kernel. The kernel length in both the
   * x and y axis must be odd. If the max sequential size constant is exceeded and allowFFT is true
   * the FFT will be used to apply the convolution.
   * 
   * @param kernel Kernel to use for convolution operator
   */
  public MyConvolution(float[][] kernel, boolean allowFFT) {
    // Validate inputs
    if (kernel == null) {
      throw new IllegalArgumentException("Kernel cannot be null");
    }
    if ((kernel.length % 2) == 0) {
      throw new IllegalArgumentException(
          "Kernel must have an odd length along the y-axis");
    }
    if ((kernel[0].length % 2) == 0) {
      throw new IllegalArgumentException(
          "Kernel must have an odd length along the x-axis");
    }
    // Assign instance variables
    this.kernel = kernel;
    this.allowFFT = allowFFT;
  }

  @Override
  public void processImage(FImage image) {
    final int rows = image.getRows();
    final int cols = image.getCols();

    // Validate inputs
    if (rows < kernel.length || cols < kernel[0].length) {
      throw new IllegalArgumentException("Image must be bigger than convoluter kernel");
    }

    FImage buffer;
    if (!allowFFT || kernel.length * kernel[0].length < MAX_NAIVE_SIZE) {
      buffer = naiveConvolution(image, this.kernel);
    } else {
      buffer = alternateFourierConvolution(image, this.kernel);
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
    }
    // Apply new changes all in one go
    image.internalAssign(buffer);
  }

  /**
   * Process an image using the provided kernel. Pixels where the full kernel cannot be applied (e.g.
   * pixel 0,0 and a kernel with width or height > 1) are set to 0. Processing is done in the spatial
   * domain and is therefore slow when the kernel size is significant.
   * 
   * @param image Image to process
   * @param kernel Kernel to apply
   * @return Processed image
   */
  public static FImage naiveConvolution(FImage image, float[][] kernel) {
    final int kh = kernel.length;
    final int kw = kernel[0].length;
    final int khh = kh / 2;
    final int khw = kw / 2;
    // Record calculated points in a separate buffer
    final FImage processed = new FImage(image.width, image.height);

    // Process every pixel that can overlay the full kernel
    for (int y = khh; y < image.height - khh; y++) {
      for (int x = khw; x < image.width - khw; x++) {
        // Sum each kernel value to calculate point
        float val = 0;
        for (int ky = 0; ky < kh; ky++) {
          for (int kx = 0; kx < kw; kx++) {
            // Calculate points with kernel flip applied
            final int iy = y - ky + khh;
            final int ix = x - kx + khw;
            val += (image.pixels[iy][ix] * kernel[ky][kx]);
          }
        }
        processed.pixels[y][x] = val;
      }
    }
    return processed;
  }

  /**
   * Process an image using the provided kernel. Processing is done in the frequency domain and is
   * therefore faster for larger kernel sizes. The borders will appear as if the kernel was applied at
   * every point but a cyclic shift was taken into account (using pixel data from the far border).
   * 
   * @param image Image to process
   * @param kernel Kernel to apply
   * @return Processed image
   */
  public static FImage fourierConvolution(FImage image, float[][] kernel) {
    final int rows = image.getRows();
    final int cols = image.getCols();

    // Transform kernel to frequency domain (must be same size as image so pad)
    FImage scaledKernel = getFFTKernel(new FImage(kernel), rows, cols);
    final FourierTransformComplex fftKernel =
        new FourierTransformComplex(scaledKernel, false);
    // Transform image to frequency domain
    final FourierTransformComplex fftImage = new FourierTransformComplex(image, false);

    // Do complex multiply across kernel and image FFTs
    final FImage outReal = new FImage(cols, rows);
    final FImage outImag = new FImage(cols, rows);
    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < cols; x++) {
        final float kr = fftKernel.getReal().pixels[y][x];
        final float ki = fftKernel.getImaginary().pixels[y][x];
        final float ir = fftImage.getReal().pixels[y][x];
        final float ii = fftImage.getImaginary().pixels[y][x];
        // Complex multiply for each point
        outReal.pixels[y][x] = kr * ir - ki * ii;
        outImag.pixels[y][x] = ki * ir + kr * ii;
      }
    }
    // Inverse Fourier to get back in spatial domain
    final FImage processed =
        new FourierTransformComplex(outReal, outImag, false).inverse();
    return processed;
  }

  /**
   * Identical functionality to {@link #fourierConvolution}. However, makes use of frequency domain
   * magnitude and phase, instead of complex numbers. From testing appears to be faster.
   * 
   * @param image Image to process
   * @param kernel Kernel to apply
   * @return Processed image
   */
  public static FImage alternateFourierConvolution(FImage image, float[][] kernel) {
    // Transform kernel to frequency domain (must be same size as image so pad)
    FImage scaledKernel =
        getFFTKernel(new FImage(kernel), image.getRows(), image.getCols());
    final FourierTransform fftKernel = new FourierTransform(scaledKernel, false);

    // Transform image to frequency domain
    final FourierTransform fftImage = new FourierTransform(image, false);

    // Multiply the magnitudes and add the phases
    final FImage outMagnitude =
        fftKernel.getMagnitude().multiply(fftImage.getMagnitude());
    FImage outPhase = fftKernel.getPhase().add(fftImage.getPhase());

    // Inverse Fourier to get back in spatial domain
    final FImage processed =
        new FourierTransform(outMagnitude, outPhase, false).inverse();

    return processed;
  }

  /**
   * Scale a kernel to a given size for frequency convolution. This should be to the size of the image
   * the kernel is being convolved with. The centre pixel of the kernel is placed at the top-leftmost
   * of the output kernel with the rest of the kernel being centred around this, cyclic-shifting where
   * required.
   * 
   * @param kernel Kernel to scale for convolution
   * @param rows Number of rows in output image
   * @param cols Number of columns in output image
   * @return Scaled kernel
   */
  public static FImage getFFTKernel(FImage kernel, int rows, int cols) {
    FImage scaledKernel = new FImage(cols, rows);
    int ky = kernel.getRows();
    int kx = kernel.getCols();
    for (int y = 0; y < ky; y++) {
      for (int x = 0; x < kx; x++) {
        // Calculate wrapping position of kernel pixels
        int iy = (rows + (y - ky / 2)) % rows;
        int ix = (cols + (x - kx / 2)) % cols;
        scaledKernel.pixels[iy][ix] = kernel.pixels[y][x];
      }
    }
    return scaledKernel;
  }

}
