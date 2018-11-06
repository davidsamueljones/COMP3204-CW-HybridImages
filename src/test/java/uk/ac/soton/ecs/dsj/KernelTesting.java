package uk.ac.soton.ecs.dsj;

import java.io.File;
import java.net.URI;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import uk.ac.soton.ecs.dsj.lib.MyConvolution;

/**
 * Test main for COMP3204 Coursework 2.
 *
 * @author David Jones (dsj1n15@ecs.soton.ac.uk)
 */
public class KernelTesting {

  public static void main(String[] args) {
    // Load example picture
    MBFImage img1 = null;
    try {
      URI uri1 = new URI(MyConvolution.class.getResource("/examples/submarine.bmp").toString());
      img1 = ImageUtilities.readMBF(new File(uri1.getPath()));
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Unable to load the example images");
      return;
    }

    /* Create test kernel (gaussian blur) */
    float[][] kernel1 = {{0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f,
        0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f,
        0.05f, 0.05f, 0.05f, 0.05f, 0.05f}};
    float[][] kernel2 = {{0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f},
        {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f},
        {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}, {0.05f}};
    float[][] kernel3 = {{0.05f, 0.05f, 0.05f, 0.05f, 0.05f},
        {0.05f, 0.05f, 0.05f, 0.05f, 0.05f}, {0.05f, 0.05f, 0.05f, 0.05f, 0.05f},
        {0.05f, 0.05f, 0.05f, 0.05f, 0.05f}, {0.05f, 0.05f, 0.05f, 0.05f, 0.05f}};
    MyConvolution conv1 = new MyConvolution(kernel1);
    MyConvolution conv2 = new MyConvolution(kernel2);
    MyConvolution conv3 = new MyConvolution(kernel3);

    /* Apply each convolution method */
    MBFImage res1 = img1.process(conv1);
    MBFImage res2 = img1.process(conv2);
    MBFImage res3 = img1.process(conv3);
    try {
      ImageUtilities.write(res1, new File("res1.png"));
      ImageUtilities.write(res2, new File("res2.png"));
      ImageUtilities.write(res3, new File("res3.png"));   
    } catch (Exception e) {
      e.printStackTrace();
    }

    DisplayUtilities.displayLinked("Comparison", 3, img1, res1, res2, res3);
  }

}
