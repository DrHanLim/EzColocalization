package testclasses;

import java.io.InputStream;

import ezcol.files.FilesIO;
import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;

public class TestFilesIO {
	public static void test(){
		
		ImagePlus imp = null;
		InputStream is = FilesIO.class.getResourceAsStream("/test.tif");
		if (is != null) {
			Opener opener = new Opener();
			imp = opener.openTiff(is, "test.tif");
			imp.show();
		}else
			IJ.error("load failed");
		
	}
}
