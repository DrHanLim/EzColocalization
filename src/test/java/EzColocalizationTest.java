import java.io.IOException;
import java.net.URISyntaxException;


import ezcol.files.FilesIO;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

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
		/*
		try {
			for(int i = 1; i <= 3; i++)
				FilesIO.getImagePlus("/test_images/Sample image C" + i + ".tif", true);
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			IJ.error("Cannot find test images");
		}*/
		IJ.runPlugIn(EzColocalization_.class.getName(),"");
		//TestFilesIO.test();
	}
}
