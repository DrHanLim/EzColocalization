package ezcol.align;

import ezcol.debug.ExceptionHandler;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.filter.BackgroundSubtracter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.AutoThresholder.Method;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

public class BackgroundProcessor {

	public static final boolean Default_createBackground = false, DEFAULT_LIGHTBACKGROUND = true,
			Default_useParaboloid = false, Default_doPresmooth = true, Default_correctCorners = true;
	static final double Default_radius = 50.0;
	private ImageProcessor phase;
	// ipThred right now always keep objects as 255 and background as 0
	// when using ParticleAnalyzer, please remember to invert it
	protected ByteProcessor ipThred, ipMask;
	protected static AutoThresholder thresholder = new AutoThresholder();
	// protected ImagePlus imp;
	private boolean isMask, lightBackground;
	private Method algorithm;
	private ImageStatistics impStatistics;
	private ImageProcessor impProcessor;
	private BackgroundSubtracter subBack;
	private Calibration cal;

	public BackgroundProcessor() {
		phase = null;
		algorithm = Method.Default;
	}

	public BackgroundProcessor(ImageProcessor inputIP) {
		phase = inputIP;
		algorithm = Method.Default;
	}

	public BackgroundProcessor(ImagePlus inputImg) {
		phase = inputImg.getProcessor();
		algorithm = Method.Default;
	}

	public BackgroundProcessor(ImageProcessor inputIP, Method inputMethod) {
		phase = inputIP;
		algorithm = inputMethod;
	}

	public BackgroundProcessor(ImagePlus inputImg, Method inputMethod) {
		phase = inputImg.getProcessor();
		algorithm = inputMethod;
	}

	public BackgroundProcessor(ImageProcessor inputIP, String inputString) {
		phase = inputIP;
		algorithm = Method.valueOf(Method.class, inputString);
	}

	public BackgroundProcessor(ImagePlus inputImg, String inputString) {
		phase = inputImg.getProcessor();
		algorithm = Method.valueOf(Method.class, inputString);
	}

	// apply the threshold tothe input phase contrast image
	public void thredImp() {
		if (phase instanceof ColorProcessor) {
			ExceptionHandler.addError(Thread.currentThread(), "Phase Contrast Image cannot be RGB");
			return;
		}
		if (phase.isBinary()) {
			ipThred = (ByteProcessor) phase.duplicate();
			ipThred.invert();
		} else {
			setBackgroundOption(detectBackground());
			ImageProcessor thredPhase = phase.duplicate();
			thredPhase.setAutoThreshold(algorithm, !lightBackground, ImageProcessor.NO_LUT_UPDATE);
			if (lightBackground)
				thredPhase.threshold((int) thredPhase.getMaxThreshold());
			else
				thredPhase.threshold((int) thredPhase.getMinThreshold());
			ipThred = thredPhase.convertToByteProcessor(false);
			if (isMask)
				ipThred.invert();

		}
		ipMask = (ByteProcessor) ipThred.duplicate();
		ipMask.invert();

	}

	public ByteProcessor thredImp(boolean lightBack) {
		isMask = lightBack;
		lightBackground = lightBack;
		thredImp();
		ipMask = (ByteProcessor) ipThred.duplicate();
		ipMask.invert();
		return ipThred;
	}

	public static ImageProcessor thredImp(ImageProcessor imp, String inputalgorithm, boolean lightBack) {

		if (imp instanceof ColorProcessor) {
			ExceptionHandler.addError(Thread.currentThread(), "Phase Contrast Image cannot be RGB");
			return null;
		}
		ImageProcessor rip = imp.duplicate();
		rip.setAutoThreshold(getMethod(inputalgorithm), !lightBack, ImageProcessor.NO_LUT_UPDATE);
		if (lightBack)
			rip.threshold((int) rip.getMaxThreshold());
		else
			rip.threshold((int) rip.getMinThreshold());
		rip = rip.convertToByteProcessor(false);
		if (lightBack)
			rip.invert();
		return rip.duplicate();
	}

	public boolean detectBackground() {

		if (phase != null) {
			ImageStatistics ipStats = ImageStatistics.getStatistics(phase, Measurements.SKEWNESS, cal);
			return ipStats.skewness < 0;
		}
		return DEFAULT_LIGHTBACKGROUND;
	}

	public boolean detectBackground(ImageProcessor ip) {

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
	public static boolean detectBackground(ImagePlus imp) {

		if (imp != null) {
			ImageStatistics ipStats = ImageStatistics.getStatistics(imp.getProcessor(), Measurements.SKEWNESS,
					imp.getCalibration());
			return ipStats.skewness < 0;
		}
		return DEFAULT_LIGHTBACKGROUND;
	}

	public void setBackgroundOption(boolean flag) {
		isMask = flag;
		lightBackground = flag;
	}

	public void setBackgroundOption(boolean isMask, boolean lightBackground) {
		this.isMask = isMask;
		this.lightBackground = lightBackground;
	}

	public static Method getMethod(String inputString) {
		return Method.valueOf(Method.class, inputString);
	}

	public void setMethod(Method inputMethod) {
		algorithm = inputMethod;
	}

	public void setMethod(String inputString) {
		algorithm = Method.valueOf(Method.class, inputString);
	}

	public void setPhase(ImageProcessor ip) {
		phase = ip;
	}

	public void reset() {
		phase = null;
		ipThred = null;
		ipMask = null;
		lightBackground = true;
		isMask = true;
		algorithm = Method.Default;
		impStatistics = null;
		impProcessor = null;
		subBack = new BackgroundSubtracter();
	}

	public ImageStatistics calcBackground(ImageProcessor ip, int measurements) {
		return calcBackground(ip, measurements, DEFAULT_LIGHTBACKGROUND);
	}

	public ImageStatistics calcBackground(ImageProcessor ip, int measurements, boolean lightBack) {
		if (ip == null)
			return null;
		isMask = lightBack;
		lightBackground = lightBack;
		thredImp();
		impProcessor = ip;
		impProcessor.resetRoi();
		impProcessor.setMask(ipMask);
		if (impProcessor.getMax() >= 1)
			impProcessor.setThreshold(1, impProcessor.getMax(), ImageProcessor.NO_LUT_UPDATE);
		impStatistics = ImageStatistics.getStatistics(impProcessor, measurements, null);
		return impStatistics;
	}

	/*public double calcBackground(ImageProcessor ip, double radius) {
		return calcBackground(ip, radius, DEFAULT_LIGHTBACKGROUND);
	}

	public double calcBackground(ImageProcessor ip, double radius, boolean lightBack) {
		subBack = new BackgroundSubtracter();
		impProcessor = ip;
		subBack.rollingBallBackground(impProcessor, radius, true, lightBack, Default_useParaboloid, Default_doPresmooth,
				Default_correctCorners);
		if (impProcessor.getMax() >= 1)
			impProcessor.setThreshold(1, impProcessor.getMax(), ImageProcessor.NO_LUT_UPDATE);
		impStatistics = ImageStatistics.getStatistics(impProcessor, Measurements.MEAN, null);
		return impStatistics.mean;
	}*/

	public ByteProcessor getThredImg() {
		if (ipThred == null)
			return null;
		return (ByteProcessor) ipThred.duplicate();
	}

	public ByteProcessor getThredMask() {
		if (ipMask == null)
			return null;
		return (ByteProcessor) ipMask.duplicate();
	}

	public void rollSubBackground(ImageProcessor subip) {
		rollSubBackground(subip, Default_radius, Default_createBackground, DEFAULT_LIGHTBACKGROUND,
				Default_useParaboloid, Default_doPresmooth, Default_correctCorners);
	}

	public void rollSubBackground(ImageProcessor subip, double radius) {
		rollSubBackground(subip, radius, Default_createBackground, DEFAULT_LIGHTBACKGROUND, Default_useParaboloid,
				Default_doPresmooth, Default_correctCorners);
	}

	public void rollSubBackground(ImageProcessor subip, double radius, boolean lightBackground) {
		rollSubBackground(subip, radius, Default_createBackground, lightBackground, Default_useParaboloid,
				Default_doPresmooth, Default_correctCorners);
	}

	public void rollSubBackground(ImageProcessor subip, double radius, boolean createBackground,
			boolean lightBackground, boolean useParaboloid, boolean doPresmooth, boolean correctCorners) {
		subBack = new BackgroundSubtracter();
		subBack.rollingBallBackground(subip, radius, createBackground, lightBackground, useParaboloid, doPresmooth,
				correctCorners);
		// erase progressbar
		IJ.showProgress(1.1);
	}

}
