package ezcol.cell;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.HashMap;

import ezcol.debug.Debugger;
import ij.*;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.Binary;
import ij.plugin.filter.EDM;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class CellFinder {

	private static final String[] FILTERS_SHAPE = { "Area", "X", "Y", "XM", "YM", "Perim.", "BX", "BY", "Width",
			"Height", "Major", "Minor", "Angle", "Circ.", "Feret", "FeretX", "FeretY", "FeretAngle", "MinFeret", "AR",
			"Round", "Solidity" };
	private static final String[] FILTERS_FIRST = { "Mean", "Mode", "Min", "Max", "Median" };
	private static final String[] FILTERS_HIGH = { "StdDev", "Skew", "Kurt", "%Area", "IntDen", "RawIntDen" };
	public static final String SB_MARKER = "BgndRatio";

	/**
	 * Terrible design here, why should these be consistent with the GUI GUI
	 * should get the list from here.
	 */
	private static int measurePhase = Measurements.AREA + Measurements.CENTROID + Measurements.CENTER_OF_MASS
			+ Measurements.PERIMETER + Measurements.ELLIPSE + Measurements.RECT + Measurements.SHAPE_DESCRIPTORS
			+ Measurements.FERET;

	private static int measureFlourescent = Measurements.MEAN + Measurements.STD_DEV + Measurements.MODE
			+ Measurements.MIN_MAX + Measurements.INTEGRATED_DENSITY + Measurements.MEDIAN + Measurements.SKEWNESS
			+ Measurements.KURTOSIS + Measurements.AREA_FRACTION;

	private static int measureBackground = Measurements.MEAN + Measurements.MEDIAN;

	// Do Not Exclude Edge Particles before watershed to avoid false negative
	private static int preAnalysis = ParticleAnalyzerMT.SHOW_MASKS;

	private static int postAnalysis = ParticleAnalyzerMT.EXCLUDE_EDGE_PARTICLES + ParticleAnalyzerMT.ADD_TO_MANAGER;

	private ImagePlus mask;
	private ResultsTable rt1, rt2;
	private ResultsTable[] rtChannels;
	private ResultsTable[] rtBacks;
	private ParticleAnalyzerMT cellParticles1, cellParticles2;
	private double minSize1, maxSize1, minSize2, maxSize2;
	private double minCirc1, maxCirc1, minCirc2, maxCirc2;
	private String[] filterNames;
	private double[] filterMin, filterMax;
	// private boolean[] filterBackground;
	private int numFilter;
	private boolean[] deletedIndex;
	private RoiManager roiParticles;
	private ImagePlus impParticles;
	private int[] cellIndexes;
	private ImagePlus impParticlesFiltered;
	private RoiManager roiParticlesFiltered;
	private int[] cellIndexesFiltered;
	private ResultsTable rtFiltered;

	public CellFinder() {
		this(null);
	}

	public CellFinder(ByteProcessor ip) {
		if (ip != null)
			mask = new ImagePlus("Mask", ip);
		else
			mask = new ImagePlus();
		minCirc1 = 0.0;
		minCirc2 = 0.0;
		maxCirc1 = 1.0;
		maxCirc2 = 1.0;
		minSize1 = 0.0;
		maxSize1 = Double.POSITIVE_INFINITY;
		minSize2 = 0.0;
		maxSize2 = Double.POSITIVE_INFINITY;
		double[] inputmin = { 0 };
		double[] inputmax = { Double.POSITIVE_INFINITY };
		String[] inputnames = { "Area" };
		initialFilters(inputnames, inputmin, inputmax);
	}

	public void setMask(ByteProcessor ip) {
		if (ip != null)
			mask = new ImagePlus("Mask", ip);
		else
			mask = new ImagePlus();
	}

	public void setMask(ByteProcessor ip, Calibration cal) {
		if (ip != null) {
			mask = new ImagePlus("Mask", ip.duplicate());
			mask.setCalibration(cal);
		} else {
			mask = new ImagePlus();
			mask.setCalibration(cal);
		}
	}

	public void setSizeFilters(double[] minSize, double[] maxSize) {

		if (minSize == null || maxSize == null)
			return;
		int len = minSize.length < maxSize.length ? minSize.length : maxSize.length;
		switch (len) {
		case 1:
			minSize1 = minSize[0];
			maxSize1 = maxSize[0];
			break;
		case 2:
			minSize1 = minSize[0];
			maxSize1 = maxSize[0];
			minSize2 = minSize[1];
			maxSize2 = maxSize[1];
			break;
		}

	}

	public void setCircFilters(double[] minCirc, double[] maxCirc) {
		if (minCirc == null || maxCirc == null) {
			return;
		}
		int len = minCirc.length < maxCirc.length ? minCirc.length : maxCirc.length;
		switch (len) {
		case 1:
			minCirc1 = minCirc[0];
			maxCirc1 = maxCirc[0];
			break;
		case 2:
			minCirc1 = minCirc[0];
			maxCirc1 = maxCirc[0];
			minCirc2 = minCirc[1];
			maxCirc2 = maxCirc[1];
			break;
		}
	}

	/** use two step intensity based thresholding method to get particles
	 *  It's interesting that ParticleAnalyzer is robust to LUT and Inverting if it's run twice
	 * @param waterShed
	 */
	public void getParticles(boolean waterShed) {
		ImagePlus mask1;
		rt1 = new ResultsTable();
		// 2616063 WITHOUT STACK
		// 4188927 EVERYTHING
		
		cellParticles1 = new ParticleAnalyzerMT(preAnalysis, measurePhase, rt1, minSize1, maxSize1, minCirc1, maxCirc1);
		cellParticles1.setHideOutputImage(true);
		cellParticles1.setHyperstack(mask);
		// cellParticles1.setThreadResultsTable(rt1);
		cellParticles1.analyze(mask);
		mask1 = cellParticles1.getOutputImage();
		
		if (waterShed) {
			EDM waterSheded = new EDM();
			waterSheded.toWatershed(mask1.getProcessor());
		}
		
		if(Prefs.blackBackground)
			mask1.getProcessor().invert();
		
		Binary fillHoles = new Binary();
		fillHoles.setup("fill", mask1);
		fillHoles.run(mask1.getProcessor());
		if (rt2 == null)
			rt2 = new ResultsTable();
		cellParticles2 = new ParticleAnalyzerMT(postAnalysis, measurePhase, rt2, minSize2, maxSize2, minCirc2,
				maxCirc2);
		cellParticles2.setHideOutputImage(true);
		if (roiParticles == null)
			roiParticles = new RoiManager(false);
		cellParticles2.setThreadRoiManager(roiParticles);
		cellParticles2.setHyperstack(mask1);
		// cellParticles2.setResultsTable(rt2);
		// This part has been moved to the main function to keep safe in
		// parallel programming
		// ImagePlus tempCurrentImg=WindowManager.getCurrentImage();
		// WindowManager.setTempCurrentImage(mask1);
		cellParticles2.analyze(mask1);
		// WindowManager.setTempCurrentImage(tempCurrentImg);
		// Sometimes ImageJ generate discontinuous numbers in count mask when
		// doing multithreading
		// In another word, count mask is not thread safe
		// Therefore, we create our own count mask here
		impParticles = roi2CountMask(roiParticles.getRoisAsArray(), mask1.getWidth(), mask1.getHeight());
		cellIndexes = new int[roiParticles.getCount()];
		for (int iCount = 0; iCount < roiParticles.getCount(); iCount++)
			cellIndexes[iCount] = iCount;
	}

	// use ROIs to measure another image
	private ResultsTable getMeasurements(ImageProcessor ip) {
		ResultsTable tempResultTable = new ResultsTable();
		if (ip == null)
			return tempResultTable;
		ImagePlus imp = new ImagePlus("ImageData", ip);
		Analyzer aSys = new Analyzer(imp, measureFlourescent, tempResultTable); // System
																				// Analyzer
		if (roiParticles == null)
			return tempResultTable;
		for (int iCount = 0; iCount < roiParticles.getCount(); iCount++) {
			imp.setRoi(roiParticles.getRoi(iCount));
			// Possible problem
			aSys.measure();
		}
		imp.getProcessor().resetRoi();
		return tempResultTable;
	}

	// use ROIs to measure another image and save the result as channel 1 or 2
	private ResultsTable getBackgrounds(ImageProcessor ip) {
		ResultsTable tempResultTable = new ResultsTable();
		if (ip == null)
			return tempResultTable;

		ImagePlus imp = new ImagePlus("ImageData", ip);
		RoiManager roiManager = new RoiManager(false);
		Analyzer aSys = new Analyzer(imp, measureBackground, tempResultTable); // System

		ParticleAnalyzerMT analyzer = new ParticleAnalyzerMT(ParticleAnalyzerMT.ADD_TO_MANAGER, 0, null, 0.0,
				Double.POSITIVE_INFINITY);
		analyzer.setThreadRoiManager(roiManager);
		analyzer.analyze(mask.duplicate());
		Roi[] rois = roiManager.getRoisAsArray();

		ip.resetRoi();
		ShapeRoi shapeRoi = new ShapeRoi(ip.getRoi());
		for (Roi roi : rois) {
			shapeRoi.xor(new ShapeRoi(roi));
		}
		imp.setRoi(shapeRoi);
		// Possible problem
		aSys.measure();
		ip.resetRoi();
		return tempResultTable;
	}

	public void getMeasurements(ImageProcessor[] ips) {
		rtChannels = new ResultsTable[ips.length];
		rtBacks = new ResultsTable[ips.length];
		for (int i = 0; i < ips.length; i++) {
			ResultsTable tempResultTable = getMeasurements(ips[i]);
			rtChannels[i] = tempResultTable;
			tempResultTable = getBackgrounds(ips[i]);
			rtBacks[i] = tempResultTable;
		}
	}

	public void setResultsTable(ResultsTable rt2) {
		this.rt2 = rt2;
	}

	public void setRoiManager(RoiManager roiParticles) {
		this.roiParticles = roiParticles;
	}

	public RoiManager getOutputManager(boolean which) {
		if (which)
			return roiParticlesFiltered;
		else
			return roiParticles;
	}

	public ImagePlus getOutputImg(boolean which) {
		if (which)
			return impParticlesFiltered;
		else
			return impParticles;
	}

	public int[] getOutputIdx(boolean which) {
		if (which)
			return cellIndexesFiltered;
		else
			return cellIndexes;
	}

	public ResultsTable getOutputTable(boolean which) {
		if (which)
			return rtFiltered;
		else
			return rt2;
	}

	public boolean[] getDeletedIndexes() {
		return deletedIndex;
	}

	/**
	 * filter cells based on cell shape measurements from the input resultstable
	 * The input ResultsTable should only contain cell shape filters
	 * 
	 * @param rt
	 * @return
	 */
	private boolean[] filterParticles(ResultsTable rt) {
		if (rt == null)
			return null;
		int numCells = rt.getCounter();
		boolean[] deletedIndex = new boolean[numCells];
		for (int iFilter = 0; iFilter < numFilter; iFilter++) {
			// There should be no changed to the filterNames in this method
			// But the following line is kept just in case.
			String tempName = filterNames[iFilter];
			if (filterNames[iFilter].indexOf(" ") != -1)
				tempName = filterNames[iFilter].substring(0, filterNames[iFilter].indexOf(" "));
			if (rt.columnExists(rt.getColumnIndex(tempName))) {
				double[] metrics = rt.getColumnAsDoubles(rt.getColumnIndex(tempName));
				for (int iCell = 0; iCell < numCells; iCell++) {
					if (deletedIndex[iCell])
						continue;
					if (metrics[iCell] > filterMax[iFilter] || metrics[iCell] < filterMin[iFilter])
						deletedIndex[iCell] = true;
				}
			}
		}
		return deletedIndex;
	}

	/**
	 * filter cells based on intensity measurements from the input resultstables
	 * of channel1 and channel2 The input ResultsTables should only contain
	 * intensity filters
	 * 
	 * @param rt1
	 * @param rt2
	 * @return
	 */
	private boolean[] filterParticles(ResultsTable[] rts) {
		if (rts == null || rts.length == 0)
			return null;

		int numCells = 0;
		for (int i = 0; i < rts.length; i++) {
			if (rts[i] == null)
				continue;
			if (numCells < rts[i].getCounter())
				numCells = rts[i].getCounter();
		}

		boolean[] deletedIndex = new boolean[numCells];
		for (int iFilter = 0; iFilter < numFilter; iFilter++) {
			String tempName = filterNames[iFilter];
			if (filterNames[iFilter].indexOf(" ") != -1)
				tempName = filterNames[iFilter].substring(0, filterNames[iFilter].indexOf(" "));
			// The assumption here is that filterNames do not contain any number
			// except for channel number
			int idxChannel = 0;
			/**
			 * To explain: .* means any character from 0 to infinite occurrence,
			 * than the \\d+ (double backslash I think is just to escape the
			 * second backslash) and \d+ means a digit from 1 time to infinite.
			 */
			if (filterNames[iFilter].matches(".*\\d+.*"))
				idxChannel = Integer.parseInt(filterNames[iFilter].replaceAll("[^0-9]", "")) - 1;
			else
				continue;

			if (rts[idxChannel] != null && rts[idxChannel].columnExists(rts[idxChannel].getColumnIndex(tempName))) {
				double[] metrics = rts[idxChannel].getColumnAsDoubles(rts[idxChannel].getColumnIndex(tempName));
				for (int iCell = 0; iCell < rts[idxChannel].getCounter(); iCell++) {
					if (deletedIndex[iCell])
						continue;

					boolean isRatio = isRatio(filterNames[iFilter]);
					double background;
					if (isRatio)
						background = (rtBacks == null || idxChannel >= rtBacks.length || idxChannel < 0) ? 1.0
								: (rtBacks[idxChannel].columnExists(rtBacks[idxChannel].getColumnIndex(tempName))
										? rtBacks[idxChannel].getValue(tempName, 0) : 1.0);
					else
						background = 1.0;
					if (metrics[iCell] > filterMax[iFilter] * background || metrics[iCell] < filterMin[iFilter] * background)
						deletedIndex[iCell] = true;

				}
			}
		}
		return deletedIndex;
	}

	// perform two filterParticles functions
	private void filterParticles() {
		boolean[] deletedIndex1 = filterParticles(rt2);
		boolean[] deletedIndex2 = filterParticles(rtChannels);
		if (deletedIndex1 == null || deletedIndex2 == null)
			return;
		int minLength = deletedIndex1.length > deletedIndex2.length ? deletedIndex2.length : deletedIndex1.length;
		this.deletedIndex = new boolean[minLength];
		for (int i = 0; i < minLength; i++) {
			this.deletedIndex[i] = deletedIndex1[i] || deletedIndex2[i];
		}
	}

	// apply the result of filterParticle (an array of whether to remove
	// particular cell or not)
	public int[] applyFilters(boolean[] deletedIndex, int[] listOfObjects) {
		if (deletedIndex == null)
			return null;
		int minLength = deletedIndex.length < listOfObjects.length ? deletedIndex.length : listOfObjects.length;
		int numRemains = 0;
		int[] indexes = new int[listOfObjects.length];
		for (int iObj = 0; iObj < minLength; iObj++) {
			if (!deletedIndex[iObj]) {
				indexes[numRemains] = iObj;
				numRemains++;
			}

		}
		for (int iObj = minLength; iObj < listOfObjects.length; iObj++) {
			indexes[numRemains] = iObj;
			numRemains++;
		}
		int[] results = new int[numRemains];
		for (int iObj = 0; iObj < numRemains; iObj++) {
			results[iObj] = listOfObjects[indexes[iObj]];
		}

		return results;
	}

	// apply filters to any input Object
	public Object[] applyFilters(boolean[] deletedIndex, Object[] listOfObjects) {
		if (deletedIndex == null)
			return null;
		int minLength = deletedIndex.length < listOfObjects.length ? deletedIndex.length : listOfObjects.length;
		int numRemains = 0;
		int[] indexes = new int[listOfObjects.length];
		for (int iObj = 0; iObj < minLength; iObj++) {
			if (!deletedIndex[iObj]) {
				indexes[numRemains] = iObj;
				numRemains++;
			}
		}
		for (int iObj = minLength; iObj < listOfObjects.length; iObj++) {
			indexes[numRemains] = iObj;
			numRemains++;
		}
		Object[] results = new Object[numRemains];
		for (int iObj = 0; iObj < numRemains; iObj++) {
			results[iObj] = listOfObjects[indexes[iObj]];
		}

		return results;
	}

	// apply filters to any input resulttable
	public ResultsTable applyFilters(boolean[] deletedIndex, ResultsTable rt) {
		if (deletedIndex == null)
			return null;
		int minLength = deletedIndex.length < rt.getCounter() ? deletedIndex.length : rt.getCounter();
		ResultsTable results = new ResultsTable();
		String[] columnNames = rt.getHeadings();
		for (int iObj = 0; iObj < minLength; iObj++) {
			if (deletedIndex[iObj])
				continue;
			results.incrementCounter();
			if (columnNames[0].equals("Label")) {
				results.addLabel(rt.getLabel(iObj));
			} else {
				results.addValue(columnNames[0], rt.getValue(columnNames[0], iObj));
			}
			for (int iColumn = 1; iColumn < columnNames.length; iColumn++)
				results.addValue(columnNames[iColumn], rt.getValue(columnNames[iColumn], iObj));
		}
		for (int iObj = minLength; iObj < rt.getCounter(); iObj++) {
			results.incrementCounter();
			if (columnNames[0].equals("Label")) {
				results.addLabel(rt.getLabel(iObj));
			} else {
				results.addValue(columnNames[0], rt.getValue(columnNames[0], iObj));
			}
			for (int iColumn = 1; iColumn < columnNames.length; iColumn++)
				results.addValue(columnNames[iColumn], rt.getValue(columnNames[iColumn], iObj));
		}
		return results;
	}

	// apply filters to any input RoiManager
	public RoiManager applyFilters(boolean[] deletedIndex, RoiManager roiManager) {
		if (deletedIndex == null)
			return null;
		int minLength = deletedIndex.length < roiManager.getCount() ? deletedIndex.length : roiManager.getCount();
		RoiManager roiFiltered = new RoiManager(false);
		for (int iObj = 0; iObj < minLength; iObj++) {
			if (!deletedIndex[iObj])
				roiFiltered.addRoi(roiManager.getRoi(iObj));
		}
		for (int iObj = minLength; iObj < roiManager.getCount(); iObj++) {
			roiFiltered.addRoi(roiManager.getRoi(iObj));
		}
		return roiFiltered;
	}

	// apply filters to any input image
	public ImagePlus applyFilters(boolean[] deletedIndex, ImagePlus countMask) {
		if (deletedIndex == null)
			return null;
		ImagePlus result = countMask.duplicate();
		ImageProcessor ipResult = result.getProcessor();
		int indexLength = (int) countMask.getProcessor().getStatistics().max;
		int minLength = deletedIndex.length < indexLength ? deletedIndex.length : indexLength;
		int[][] pixel = ipResult.getIntArray();
		int width = result.getWidth();
		int height = result.getHeight();
		int[] newIndexes = new int[indexLength + 1];
		int numCells = 1;
		for (int i = 1; i <= minLength; i++) {
			if (deletedIndex[i - 1])
				newIndexes[i] = 0;
			else {
				newIndexes[i] = numCells;
				numCells++;
			}
		}
		for (int i = minLength + 1; i < newIndexes.length; i++) {
			newIndexes[i] = numCells;
			numCells++;
		}
		for (int w = width - 1; w >= 0; w--) {
			for (int h = height - 1; h >= 0; h--) {
				if (pixel[w][h] != 0)
					ipResult.set(w, h, newIndexes[pixel[w][h]]);
			}
		}
		ipResult.resetMinAndMax();
		return result;
	}

	// apply filters automatically
	public void applyFilters() {
		filterParticles();
		if (deletedIndex == null)
			return;
		impParticlesFiltered = applyFilters(deletedIndex, impParticles);
		roiParticlesFiltered = applyFilters(deletedIndex, roiParticles);
		cellIndexesFiltered = applyFilters(deletedIndex, cellIndexes);
		rtFiltered = applyFilters(deletedIndex, rt2);
	}

	public void initialFilters(final String[] names, final double[] mins, final double[] maxes) {
		initialFilters(names, mins, maxes, new boolean[names.length]);
	}

	public void initialFilters(final String[] names, final double[] mins, final double[] maxes,
			final boolean[] backgrounds) {
		if (!isDimensionEqual(names.length + " " + mins.length + " " + maxes.length)) {
			numFilter = 0;
			return;
		}
		numFilter = names.length;
		filterNames = names;
		filterMin = mins;
		filterMax = maxes;

		// filterBackground=backgrounds;
	}

	private boolean isDimensionEqual(String str) {
		String[] strs = str.split(" ");
		String temp;
		if (strs.length > 0)
			temp = strs[0];
		else
			return true;
		for (int i = 0; i < strs.length; i++) {
			if (!strs[i].equals(temp)) {
				return false;
			}
		}
		return true;
	}

	private ImagePlus roi2CountMask(Roi[] rois, int width, int height) {
		if (rois == null)
			return null;
		ShortProcessor drawIP = new ShortProcessor(width, height);
		drawIP.setColor(Color.BLACK);
		drawIP.fill();
		drawIP.setColor(Color.WHITE);
		int grayLevel = 0;
		for (int i = 0; i < rois.length; i++) {
			grayLevel = i <= 65535 ? (i + 1) : 65535;
			drawIP.setValue((double) grayLevel);
			drawIP.setRoi(rois[i]);
			drawIP.fill(drawIP.getMask());
		}
		drawIP.setRoi((Rectangle) null);

		return new ImagePlus("Count Mask", drawIP);

	}

	private boolean isRatio(String filter) {
		return filter.contains(SB_MARKER);
		// return Arrays.asList(FILTERS_FIRST).contains(filter);}
	}

}
