import java.io.IOException;
import java.net.URISyntaxException;


import ezcol.files.FilesIO;
import ij.IJ;
import ij.ImageJ;

/**
 * Test class for Coloc 2 functionality.
 * 
 * @author Ellen T Arena
 */
public class EzColocalizationTest {
	

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads an
	 * image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// start ImageJ
		ImageJ ij = new ImageJ();
		try {
			FilesIO.getImagePlus("/test_images/Sample image C1.tif", true);
			FilesIO.getImagePlus("/test_images/Sample image C2.tif", true);
			FilesIO.getImagePlus("/test_images/Sample image C3.tif", true);
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			IJ.error("Cannot find test images");
		}
		IJ.runPlugIn(EzColocalization_.class.getName(),"");
		//TestFilesIO.test();
	}
}
