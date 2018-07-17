package ezcol.align;

import ezcol.debug.Debugger;
import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginStatic;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.filter.BackgroundSubtracter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;
import ij.process.AutoThresholder.Method;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;

public class BackgroundProcessor {

	public static final boolean DEFAULT_CREATEBACKGROUND = false, DEFAULT_LIGHTBACKGROUND = true,
			DEFAULT_USEPARABOLOID = false, DEFAULT_DOPRESMOOTH = true, DEFAULT_CORRECTCORNERS = true;
	// The input ImageProcessor
	private ImageProcessor srcIp;
	// lightBackground means the histogram of the image is skewed to high values
	// That is more pixels on the image have higher pixel values
	// Assuming background has more pixels than the image, 
	private Boolean lightBackground;
	// ipThred right now always keep objects as 255 and background as 0 with a regular LUT
	// when using ParticleAnalyzer, please remember to invert it
	private ByteProcessor ipThold;
	
	private Method algorithm;

	// Add in 1.1.0 to take in manualTholds
	private double[] manualTholds = new double[] { ImageProcessor.NO_THRESHOLD, ImageProcessor.NO_THRESHOLD };

	public BackgroundProcessor() {
		srcIp = null;
		algorithm = Method.Default;
	}

	public BackgroundProcessor(ImageProcessor inputIp) {
		this(new ImagePlus("", inputIp));
	}

	public BackgroundProcessor(ImagePlus inputImp) {
		this(inputImp, Method.Default.name());
	}

	public BackgroundProcessor(ImageProcessor inputIp, String inputString) {
		this(new ImagePlus("", inputIp), inputString);
	}

	public BackgroundProcessor(ImagePlus inputImp, String inputString) {
		srcIp = inputImp.getProcessor();
		try{
			algorithm = Method.valueOf(inputString);
		}catch(Exception e){
			algorithm = null;
		}
	}

	// apply the threshold tothe input phase contrast image
	public ByteProcessor thredImp() {
		
		if(srcIp == null)
			return null;
		
		if (srcIp instanceof ColorProcessor) {
			ExceptionHandler.addError(Thread.currentThread(), "Phase Contrast Image cannot be RGB");
			ipThold = (ByteProcessor) srcIp.convertToByteProcessor().createProcessor(srcIp.getWidth(),
					srcIp.getHeight());
		}
		else {
			if(lightBackground == null)
				setBackgroundOption(isLightBackground());
			
			if (srcIp.isBinary()) {
		
				ipThold = (ByteProcessor) srcIp.duplicate();
				if(ipThold.isInvertedLut())
					ipThold.invertLut();
				if(lightBackground)
					ipThold.invert();
			
			} else {
				
				ImageProcessor thredPhase = srcIp.duplicate();
				if(thredPhase.isInvertedLut())
					thredPhase.invertLut();
				// Set the algorithm to default if manual thresholds are missing
				if (algorithm == null && (manualTholds == null || manualTholds[0] == ImageProcessor.NO_THRESHOLD
						|| manualTholds[1] == ImageProcessor.NO_THRESHOLD))
					algorithm = Method.Default;
				if (algorithm == null) {
					thredPhase.setThreshold(manualTholds[0], manualTholds[1], ImageProcessor.NO_LUT_UPDATE);
					applyManualThresholds(thredPhase, manualTholds);
				} else {
					thredPhase.setAutoThreshold(algorithm, !lightBackground, ImageProcessor.NO_LUT_UPDATE);
					if(lightBackground)
						thredPhase.threshold((int) thredPhase.getMaxThreshold());
					else
						thredPhase.threshold((int) thredPhase.getMinThreshold());
					if(lightBackground)
						thredPhase.invert();
				}
				ipThold = thredPhase.convertToByteProcessor(false);
			}
		}
		return ipThold.convertToByteProcessor();
	}

	public ByteProcessor thredImp(Boolean lightBack) {
		lightBackground = lightBack;
		return thredImp();
	}
	
	public static ImageProcessor thredImp(ImageProcessor ip, String inputalgorithm) {
		return BackgroundProcessor.thredImp(ip, inputalgorithm, BackgroundProcessor.isLightBackground(ip));
	}
	
	public static ImageProcessor thredImp(ImageProcessor ip, double[] manualTholds) {
		return BackgroundProcessor.thredImp(ip, manualTholds, BackgroundProcessor.isLightBackground(ip));
	}

	/**
	 * This method will convert an image to a binary image 
	 * which has pixel values of 255 for objects
	 * and 0 for background with a regular LUT
	 * Because the threshold might be different for light and dark background due to ties
	 * All light background images are converted to dark background first
	 * @param ip Input ImageProcessor
	 * @param inputalgorithm
	 * @param lightBackground
	 * @return A inverted binary mask with a regular LUT
	 */
	public static ByteProcessor thredImp(ImageProcessor ip, String inputalgorithm, Boolean lightBackground) {

		if (ip instanceof ColorProcessor) {
			ExceptionHandler.addError(Thread.currentThread(), "Phase Contrast Image cannot be RGB");
			return null;
		}
		if(lightBackground == null)
			lightBackground = isLightBackground(ip);
		
		ByteProcessor byteIp;
		if (ip.isBinary()) {
			byteIp = (ByteProcessor) ip.duplicate();
			if(byteIp.isInvertedLut())
				byteIp.invertLut();
			if(lightBackground)
				byteIp.invert();
		
		} else {
			ImageProcessor rip = ip.duplicate();
			if(rip.isInvertedLut())
				rip.invertLut();
			rip.setAutoThreshold(getMethod(inputalgorithm), !lightBackground, ImageProcessor.NO_LUT_UPDATE);
			if(lightBackground)
				rip.threshold((int) rip.getMaxThreshold());
			else
				rip.threshold((int) rip.getMinThreshold());
			byteIp = rip.convertToByteProcessor(false);
			if(lightBackground)
				byteIp.invert();
		}
		return byteIp;
	}
	
	/**
	 * This method will convert an image to a binary image using a given range
	 * which has pixel values of 255 for objects
	 * and 0 for background with a regular LUT
	 * @see BackgroundProcessor#thredImp(ImageProcessor ip, String inputalgorithm, boolean lightBackground)
	 * @param ip
	 * @param manualTholds
	 * @param lightBackground
	 * @return
	 */
	public static ByteProcessor thredImp(ImageProcessor ip, double[] manualTholds, Boolean lightBackground) {

		if (ip instanceof ColorProcessor) {
			ExceptionHandler.addError(Thread.currentThread(), BackgroundProcessor.class.getName() + " cannot apply a threshold to a RGB image");
			return null;
		}
		if(lightBackground == null)
			lightBackground = isLightBackground(ip);
		
		ImageProcessor rip = ip.duplicate();
		if(rip.isInvertedLut())
			rip.invertLut();
		applyManualThresholds(rip, manualTholds);
		return rip.convertToByteProcessor(false);
	}
	
	private static void applyManualThresholds(ImageProcessor ip, double[] thresholds){
		
		int width = ip.getWidth();
		int height = ip.getHeight();
		int fillfront = 255, fillback = 0;
			
		for (int w = 0; w < width; w++){
			for (int h = 0; h < height; h++){
				if (ip.get(w, h) >= thresholds[0] && ip.get(w, h) <= thresholds[1])
					ip.set(w, h, fillfront);
				else
					ip.set(w, h, fillback);
			}
		}
		if (ip instanceof ShortProcessor) {((ShortProcessor)ip).findMinAndMax();}
		else if (ip instanceof FloatProcessor) {((FloatProcessor)ip).findMinAndMax();}
	}

	public boolean isLightBackground() {

		if (srcIp != null) {
			ImageStatistics ipStats = ImageStatistics.getStatistics(srcIp, Measurements.SKEWNESS, null);
			return ipStats.skewness < 0;
		}
		return DEFAULT_LIGHTBACKGROUND;
	}
	
	public static boolean isLightBackground(ImageProcessor ip) {

		if (ip != null) {
			ImageStatistics ipStats = ImageStatistics.getStatistics(ip, Measurements.SKEWNESS, null);
			return ipStats.skewness < 0;
		}
		return DEFAULT_LIGHTBACKGROUND;
	}

	/**
	 * Automatically detect if an image has dark or light background using
	 * skewness
	 * <p>
	 * skewness>=0: dark background
	 * <p>
	 * skewness< 0: light background
	 * <p>
	 * This might not be very accurate but sufficient for most flourescent
	 * images
	 * 
	 * @param imp
	 * @return true if it is light background, false if it is dark background
	 */
	public static boolean isLightBackground(ImagePlus imp) {

		if (imp != null) {
			//Add in 1.1.0 avoid roi override
			ImageProcessor ip = imp.getProcessor().duplicate();
			ip.resetRoi();
			ImageStatistics ipStats = ImageStatistics.getStatistics(ip, Measurements.SKEWNESS, null);
			return ipStats.skewness < 0;
		}
		return DEFAULT_LIGHTBACKGROUND;
	}
	
	/**
	 * Return true if the image appears to have light background
	 * which takes inverted LUT into account
	 * @param imp
	 * @return
	 */
	public static boolean looksLightBackground(ImagePlus imp){
		if(imp.getProcessor().isInvertedLut())
			return !isLightBackground(imp);
		else
			return isLightBackground(imp);
	}

	public void setBackgroundOption(boolean lightBackground) {
		this.lightBackground = lightBackground;
		ipThold = null;
	}

	public static Method getMethod(String inputString) {
		return Method.valueOf(Method.class, inputString);
	}

	public void setMethod(String inputString) {
		algorithm = Method.valueOf(Method.class, inputString);
		ipThold = null;
	}

	public void setInput(ImageProcessor ip) {
		srcIp = ip;
		ipThold = null;
	}
	
	/**
	 * Add in 2.0.0 here to set the manual thresholds (min and max)
	 * if *Manual* is selected
	 * Note: no need to reset ipThold here unless algorithm is null (*Manual*)
	 * @param thresholds
	 */
	public void setManualThresholds(double[] thresholds) {
		this.manualTholds = thresholds;
		if(algorithm == null)
			ipThold = null;
	}

	public void reset() {
		srcIp = null;
		ipThold = null;
		lightBackground = null;
		algorithm = Method.Default;
	}

	public ByteProcessor getThredImg() {
		if (ipThold == null)
			return thredImp();
		return (ByteProcessor) ipThold.duplicate();
	}

	public ByteProcessor getThredMask() {
		if (ipThold == null)
			thredImp();
		ByteProcessor ipMask = (ByteProcessor) ipThold.duplicate();
		ipMask.invert();
		return ipMask;
	}
	
	public ImageStatistics calcBackground(ImageProcessor ip) {
		return calcBackground(ip, Measurements.MEAN);
	}

	public ImageStatistics calcBackground(ImageProcessor ip, int measurements) {
		return calcBackground(ip, measurements, isLightBackground(ip), null);
	}

	public ImageStatistics calcBackground(ImageProcessor ip, int measurements, boolean lightBack, Calibration cal) {
		if (ip == null)
			return null;
		setBackgroundOption(lightBack);
		thredImp();
		ip.resetRoi();
		ip.setMask(getThredMask());
		//if (ip.getMax() >= ip.getMin())
		//	ip.setThreshold(ip.getMin() + Double.MIN_VALUE, ip.getMax(), ImageProcessor.NO_LUT_UPDATE);
		ImageStatistics impStatistics = ImageStatistics.getStatistics(ip, measurements, cal);
		return impStatistics;
	}

	public static void rollSubBackground(ImageProcessor subip, double radius) {
		rollSubBackground(subip, radius, DEFAULT_CREATEBACKGROUND, isLightBackground(subip), DEFAULT_USEPARABOLOID,
				DEFAULT_DOPRESMOOTH, DEFAULT_CORRECTCORNERS);
	}

	public static void rollSubBackground(ImageProcessor subip, double radius, Boolean lightBackground) {
		if (lightBackground == null)
			lightBackground = isLightBackground(subip);
		rollSubBackground(subip, radius, DEFAULT_CREATEBACKGROUND, lightBackground, DEFAULT_USEPARABOLOID,
				DEFAULT_DOPRESMOOTH, DEFAULT_CORRECTCORNERS);
	}

	public static void rollSubBackground(ImageProcessor subip, double radius, boolean createBackground,
			boolean lightBackground, boolean useParaboloid, boolean doPresmooth, boolean correctCorners) {
		BackgroundSubtracter subBack = new BackgroundSubtracter();
		subBack.rollingBallBackground(subip, radius, createBackground, lightBackground, useParaboloid, doPresmooth,
				correctCorners);
		// erase progressbar
		IJ.showProgress(1.1);
	}

}

