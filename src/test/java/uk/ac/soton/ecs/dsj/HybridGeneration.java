package uk.ac.soton.ecs.dsj;

import java.io.File;
import java.net.URI;
import java.net.URL;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import uk.ac.soton.ecs.dsj.lib.HybridTesting;
import uk.ac.soton.ecs.dsj.lib.MyConvolution;

/**
 * Test main for COMP3204 Coursework 2.
 *
 * @author David Jones (dsj1n15@ecs.soton.ac.uk)
 */
public class HybridGeneration {

  public static void main(String[] args) {
    // Get input images and do rough validation
    URL url1 = MyConvolution.class.getResource("/examples/motorcycle.bmp");
    URL url2 = MyConvolution.class.getResource("/examples/bicycle.bmp");
    if (url1 == null || url1.toString().isEmpty()) {
      System.err.println("Error: [Image 1] Path or URL not valid");
      return;
    }
    if (url2 == null || url2.toString().isEmpty()) {
      System.err.println("Error: [Image 2] Path or URL not valid");
      return;
    }

    // Load the images
    MBFImage img1 = null;
    MBFImage img2 = null;
    try {
      // Convert URLs to URIs to compensate for file-path spaces
      URI uri1 = new URI(url1.toString());
      URI uri2 = new URI(url2.toString());
      // Read images into buffers
      img1 = ImageUtilities.readMBF(new File(uri1.getPath()));
      img2 = ImageUtilities.readMBF(new File(uri2.getPath()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    // !!! Comment in for applying initial low-pass to high-frequency image
    // img2 = HybridTesting.applyLowPass(img2, 1f);


    // Generate hybrid image (modify sigmas as appropriate)
    MBFImage imgLowFrequency = null;
    MBFImage imgHighFrequency = null;
    MBFImage imgHybrid = null;

    final float lowSigma = 6f;
    final float highSigma = 1f;

    imgLowFrequency = HybridTesting.applyLowPass(img1, lowSigma);
    imgHighFrequency = img2.subtract(HybridTesting.applyLowPass(img2, highSigma));
    imgHybrid = imgLowFrequency.add(imgHighFrequency);

    // Remove any undefined pixels (set max_sigma to highest sigma used)
    int max_sigma = (int) Math.max(lowSigma, highSigma);
    int border = (8 * max_sigma + 1) / 2;
    imgHybrid = imgHybrid.extractROI(border, border, imgHybrid.getWidth() - border * 2,
        imgHybrid.getHeight() - border * 2);

    try {
      ImageUtilities.write(img1, new File("lowbase.png"));
      ImageUtilities.write(img2, new File("highbase.png"));
      ImageUtilities.write(imgLowFrequency, new File("low.png"));
      ImageUtilities.write(imgHighFrequency.add(0.5f), new File("high.png"));
      ImageUtilities.write(imgHybrid, new File("hybrid.png"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Display hybrid image components
    DisplayUtilities.displayLinked("Flow", 3, imgLowFrequency, imgHighFrequency.add(0.5f),
        imgHybrid);
  }

}
