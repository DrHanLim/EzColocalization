package testclasses;

import ezcol.align.BackgroundProcessor;
import ezcol.main.PluginStatic;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class TestBackgroundProcessor {
	public static void test(){
		ImageProcessor ip = WindowManager.getCurrentImage().getProcessor();
		
		BackgroundProcessor impBackground = new BackgroundProcessor(ip,"Default");

		impBackground.setManualThresholds(new double[]{ip.getMinThreshold(), ip.getMaxThreshold()});

		// Change in 1.1.0
		// Do NOT subtract background is threshold is manually selected
		//if (!ip.isBinary())
		//	impBackground.rollSubBackground(ip, 50, impBackground.detectBackground(ip));

		// IDcell module
		// because we have calculated the background so ipThred should have
		// been
		// generated, in case that ipThred is null, do it again.
		ByteProcessor ipMask = impBackground.thredImp(BackgroundProcessor.DEFAULT_LIGHTBACKGROUND);
		ImageProcessor ipStaticMask = BackgroundProcessor.thredImp(ip, "Default", BackgroundProcessor.DEFAULT_LIGHTBACKGROUND);
		
		new ImagePlus("ipMask", ipMask).show();
	}
}
