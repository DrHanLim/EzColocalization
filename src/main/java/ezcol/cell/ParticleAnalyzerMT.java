package ezcol.cell;

import java.awt.Frame;

import ezcol.debug.Debugger;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.Roi;
import ij.macro.Interpreter;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

//This is a thread safe particleanalyzer in terms of roiManager and resultswindow
public class ParticleAnalyzerMT extends ParticleAnalyzer {

	private RoiManager roiManager;
	private int lineWidth = 1;
	private boolean hyperstack;
	private boolean showResultsWindow = true;
	/** Saves statistics for one particle in a results table. This is
	a method subclasses may want to override. */

	public ParticleAnalyzerMT(int options, int measurements, ResultsTable rt, double minSize, double maxSize, double minCirc, double maxCirc) 
	{super(options,measurements,rt,minSize,maxSize,minCirc,maxCirc);}
	
	/** Constructs a ParticleAnalyzer using the default min and max circularity values (0 and 1). */
	public ParticleAnalyzerMT(int options, int measurements, ResultsTable rt, double minSize, double maxSize) 
	{this(options, measurements, rt, minSize, maxSize, 0.0, 1.0);}

	/** Default constructor */
	public ParticleAnalyzerMT() 
	{super();}
	
	
	public void setHyperstack(ImagePlus imp)
	{hyperstack=imp.isHyperStack();}
	
	public void setThreadLineWidth(int lineWidth)
	{this.lineWidth=lineWidth;}
	
	public void setThreadRoiManager(RoiManager roiManager){
		this.roiManager=roiManager;
		setRoiManager(roiManager);
	}
	
	public void setThreadResultsTable(ResultsTable rt)
	{this.rt=rt;}
		
	//override to make it thread safe
	@Override
	protected void saveResults(ImageStatistics stats, Roi roi) {
		analyzer.saveResults(stats, roi);
		if (recordStarts) {
			rt.addValue("XStart", stats.xstart);
			rt.addValue("YStart", stats.ystart);
		}
		if (addToManager) {
			if (roiManager==null) {
				if (Macro.getOptions()!=null && Interpreter.isBatchMode())
					roiManager = Interpreter.getBatchModeRoiManager();
				if (roiManager==null) {
					Frame frame = WindowManager.getFrame("ROI Manager");
					if (frame==null)
						IJ.run("ROI Manager...");
					frame = WindowManager.getFrame("ROI Manager");
					if (frame==null || !(frame instanceof RoiManager))
						{addToManager=false; return;}
					roiManager = (RoiManager)frame;
				}
				if (resetCounter)
					roiManager.runCommand("reset");
			}
			if (imp.getStackSize()>1) {
				int n = imp.getCurrentSlice();
				if (hyperstack) {
					int[] pos = imp.convertIndexToPosition(n);
					roi.setPosition(pos[0],pos[1],pos[2]);
				} else
					roi.setPosition(n);
			}
			if (lineWidth!=1)
				roi.setStrokeWidth(lineWidth);
			roiManager.add(imp, roi, rt.getCounter());
		}
		if (showResultsWindow && showResults)
			rt.addResults();
	}
	
	public static boolean analyzeWithHole(ImageProcessor ip, RoiManager roiManager) {
		ParticleAnalyzerMT cellParticles = new ParticleAnalyzerMT(SHOW_ROI_MASKS, 0, null, 0.0,
				Double.POSITIVE_INFINITY);
		cellParticles.setHideOutputImage(true);
		if (!cellParticles.analyze(new ImagePlus("Thresholding", ip), ip))
			return false;
		ImagePlus countMask = cellParticles.getOutputImage();
		ImageProcessor countProcessor = countMask.getProcessor();
		ImageStatistics countStat = ImageStatistics.getStatistics(countProcessor, Measurements.MIN_MAX, null);
		ThresholdToSelection tts = new ThresholdToSelection();
		
		for (double iCell = 0.5; iCell < countStat.max; iCell++) {
			countProcessor.setThreshold(iCell, iCell + 1, ImageProcessor.NO_LUT_UPDATE);
			Roi roi = tts.convert(countProcessor);
			if (roi != null)
				roiManager.add(countMask, roi, (int) (iCell + 0.5));
		}
		return true;
	}
}
