package ezcol.main;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import ezcol.cell.CellFinder;
import ezcol.debug.ExceptionHandler;
import ezcol.files.FilesIO;
import ezcol.metric.MatrixCalculator;
import ezcol.metric.MatrixCalculator3D;
import ezcol.metric.StringCompiler;
import ezcol.visual.visual2D.HeatChartStackWindow;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.macro.Interpreter;
import ij.measure.ResultsTable;
import ij.plugin.frame.Editor;
import ij.plugin.frame.PlugInFrame;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.text.TextWindow;
import ij.util.Tools;

/**
 * This class contains all parameters needed for GUIframe, MacroHandler, and
 * AnalysisOperator as well as static methods for implementation
 * <p>
 * Once a new field of input has been added, please follow the instruction
 * below: <br>
 * The following methods need to be changed: <br>
 * {@code GUIframe.loadPreference, GUIframe.retrieveParams,
 * GUIframe.GUI, MacroHandler} <br>
 * The following methods might need to be changed: <br>
 * {@code this.checkParams,this.retrieveOptions,GUIframe.retrieveOptions, 
 * AnalysisOperator.prepAll(and other methods start with prep)}
 * 
 * @author Huanjie Sheng
 *
 */
public abstract class PluginStatic implements PluginConstants {

	// static variables are shared among derived/extended classes
	// They are also stored in the memory shared by all instances

	// record which result(s) to be calculated
	protected static int options;
	// effectively final
	protected static String pluginName = "";
	public static final String VERSION = FilesIO.getPluginProperties(FilesIO.VERSION);
	public static final String CONTACT = "drhanlim@gmail.com";
	/**
	 * <p>s\
	 * Arrays of options
	 * </p>
	 * They have to be matched with the corresponding name strings and/or
	 * options as the following:
	 * <p>
	 * LIST_METRICS -> METRICNAMES, metricOpts
	 * </p>
	 * <p>
	 * LIST_OTHERS -> OTHERNAMES, otherOpts
	 * </p>
	 * <p>
	 * LIST_ALIGNS -> whichAlings
	 * </p>
	 * <p>
	 * LIST_TOS -> TOSOPTS, mTOSscale
	 * </p>
	 * <p>
	 * LIST_DIST -> whichDist
	 * </p>
	 * <p>
	 * LIST_HEAT -> whichHeatmaps
	 * </p>
	 * <p>
	 * LIST_HEATMAPOPTS -> HEATMAPOPTS, whichHeatmapOpt
	 * </p>
	 * <p>
	 * LIST_OUTPUTMETRICS -> OUTPUTMETRICS, outputMetrics
	 * </p>
	 * <p>
	 * LIST_OUTPUTOTHERS -> OUTPUTOTHERS, outputOthers
	 * </p>
	 */
	public static final int[] LIST_METRICS = { DO_TOS, DO_PCC, DO_SRC, DO_ICQ, DO_MCC };
	public static final int[] LIST_OTHERS = { // DO_CEN2NPOLE, DO_CEN2CEN,
			DO_AVGINT, DO_CUSTOM };
	public static final int[] LIST_TOS = { DO_LINEAR_TOS, DO_LOG2_TOS };
	public static final int[] LIST_DIST = { DO_DIST_THOLD, DO_DIST_FT };
	public static final int[] LIST_HEATMAPOPTS = { DO_HEAT_CELL, DO_HEAT_IMG, DO_HEAT_STACK };
	public static final int[] LIST_OUTPUTMETRICS = { DO_SUMMARY, DO_HIST };
	public static final int[] LIST_OUTPUTOTHERS = { DO_MASKS, DO_ROIS };

	// indexes for metrics
	// public static final int
	// TOSH=0,TOSMAX=1,TOSMIN=2,MTOS=3,PCC=4,SRC=5,ICQ=6,M1_M2=7;

	// image parameters
	public static final int MAX_NCHANNELS = 4, MAX_NREPORTERS = 3, MIN_NREPORTERS = 2;
	public static int nChannels = 3;
	static int nReporters = nChannels - (MAX_NCHANNELS - MAX_NREPORTERS);
	// The last channel is always used for cell identification
	// Additional flourescence channels are added before it
	static String[] imgLabels = makeImgLabels(MAX_NCHANNELS);
	static ImagePlus[] imps = new ImagePlus[MAX_NCHANNELS];
	static int nbImgs;

	// alignment parameters
	static final String[] ALLTHOLDS = AutoThresholder.getMethods();
	static boolean[] align_chckes = newArray(DEFAULT_BOOLEAN, MAX_NREPORTERS);
	static int[] alignThold_combs = newArray(DEFAULT_CHOICE, MAX_NCHANNELS);
	// static boolean[] darkBacks = {true,true,false};
	// DO NOT use DEFAULT_BOOLEAN here because didAlignment is not one of the
	// options
	static boolean didAlignment = false;

	// cell filters parameters
	// A marker used to tell if the filter should be treated as signal to
	// background ratio
	public static final String SB_MARKER = CellFinder.SB_MARKER;
	/**
	 * This needs to be consistent with
	 * <code>ij.measure.ResultsTable.defaultHeadings</code> All strings after
	 * the first space will be cut when transfered to filter strings in
	 * <code>ResultsTable</code>. The number between parentheses will be used as
	 * channel number and the letters will be disregarded Whether the filter
	 * will be applied or not dependes on CellFinder not the string put here
	 * 
	 * @see CellFinder
	 * @see ResultsTable
	 */
	private static Class<?> EzColocalization;

	static final String[] SHAPE_FILTERS = { "Area", "X", "Y", "Perim.", "BX", "BY", "Width", "Height", "Major", "Minor",
			"Angle", "Circ.", "Feret", "FeretX", "FeretY", "FeretAngle", "MinFeret", "AR", "Round", "Solidity",
			"%Area" };

	static final String[] INTEN_FILTERS = { "Mean", "StdDev", "Mode", "Min", "Max", "Median", "Skew", "Kurt", "IntDen",
			"RawIntDen", "Mean " + SB_MARKER, "Median " + SB_MARKER };

	static String[] filterStrings = makeAllFilters(nReporters);

	static final String[] SIZE_FILTERS = { "Area" };
	static double[] filterMinSize_texts = newArray(DEFAULT_MIN, SIZE_FILTERS.length);
	static double[] filterMaxSize_texts = newArray(DEFAULT_MAX, SIZE_FILTERS.length);
	// filterChoices, minRanges, maxRanges, backRatios should have the same
	// length
	// we don't use default choice here, but default choice is still used in
	// smart recording
	// This is because the judgment to record is based on minRanges and
	// maxRanges, so the filter choice doesn't matter
	static int[] staticFilterChoices = { 0, 17, 21, 33, 24, 36, 25, 37 };
	static int[] filter_combs = staticFilterChoices.clone();
	static double[] filterMinRange_texts = newArray(DEFAULT_MIN, filter_combs.length);
	static double[] filterMaxRange_texts = newArray(DEFAULT_MAX, filter_combs.length);
	@Deprecated
	static boolean[] filterBackRatio_texts = newArray(DEFAULT_BOOLEAN, filter_combs.length);

	static ArrayList<Integer> adFilterChoices = new ArrayList<Integer>();
	static ArrayList<Double> adMinRanges = new ArrayList<Double>();
	static ArrayList<Double> adMaxRanges = new ArrayList<Double>();
	static ArrayList<Boolean> adBackRatios = new ArrayList<Boolean>();

	// roiCells might be problematic in parallel streams/multiple threads
	static RoiManager roiCells = null;
	static boolean waterShed_chck = DEFAULT_BOOLEAN;

	// scatterplot parameter
	static boolean scatter_chck = DEFAULT_BOOLEAN;

	// metric matrices
	static boolean matrix_chck = DEFAULT_BOOLEAN;
	static final String[] STATS_METHODS = HeatChartStackWindow.getAllStatsMethods();
	static String[] matrixMetricList = getMatrixMetrics(nReporters);
	static int matrixMetric_comb = DEFAULT_CHOICE;
	static int matrixStats_comb = DEFAULT_CHOICE;
	static final int MINFT = DEFAULT_MIN_FT;
	static final int MAXFT = DEFAULT_MAX_FT;
	static final int STEPFT = 1;
	static int[] matrixFT_spin = newArray(DEFAULT_FT, MAX_NREPORTERS);

	/** mTOS parameters **/
	static final String[] TOSOPTS = { "linear", "log2" };
	static int mTOSscale = DEFAULT_CHOICE;

	// heatmaps parameters
	static final String[] HEATMAPS = { "hot", "cool", "fire", "grays", "ice", "spectrum", "3-3-2 RGB", "red", "green",
			"blue", "cyan", "magenta", "yellow", "redgreen" };
	static final String[] HEATMAPOPTS = { "cell", "image", "stack" };
	// static final String[] WHICHHEATMAPS = {"Channel 1", "Channel 2"};
	static boolean[] heatmap_chckes = newArray(DEFAULT_BOOLEAN, MAX_NREPORTERS);
	static int heatmap_radio = DEFAULT_CHOICE;
	// Again we don't use default choice here because it's determined by whether
	// check box is checked or not
	// If heat map is requested, then the choice must be recorded
	static int[] heatmapColor_combs = newArray(MAX_NREPORTERS);

	// metric parameters
	static final String[] OTHERNAMES = { "Average Signal", "Custom Metric" };
	public static final String[] METRICNAMES = { "TOS", "PCC", "SRCC", "ICQ", "MCC" };
	public static final int AVG_INT = 0, CUSTOM = 1;
	public static final int TOS = 0, PCC = 1, SRCC = 2, ICQ = 3, MCC = 4;
	// The index of "All" in METRIC_THOLDS
	protected static final int IDX_THOLD_ALL = 0, IDX_THOLD_COSTES = 1, IDX_THOLD_FT = 2;
	static final String[] METRIC_THOLDS = { "All", "Costes'", "FT" };
	static final String[] METRIC_THOLDS_TIPS = { "All pixels", "Costes' algorithm",
			"Selected fraction percentage" };

	protected static final List<Integer> NO_THOLD_ALL = Arrays.asList(new Integer[] { TOS, MCC });

	static boolean[] metric_chckes = newArray(DEFAULT_BOOLEAN, METRICNAMES.length);
	static boolean[] other_chckes = newArray(DEFAULT_BOOLEAN, OTHERNAMES.length);

	static int[] metricThold_radios = newArray(DEFAULT_CHOICE, METRICNAMES.length);
	static int[][] allFT_spins = newArray(DEFAULT_FT, MAX_NREPORTERS, METRICNAMES.length);
	// NO NEED TO LOAD BUT TO RETRIEVE
	static int[] allTholds;

	// 3D TOS parameters
	// public static final int D3_TOS = 0;
	// public static final String D3_NAME = "3D-TOS";

	// DistancesX parameters
	static final String[] ALLDISTTHOLDS = AutoThresholder.getMethods();
	static final int MINDISTFT = DEFAULT_MIN_FT;
	static final int MAXDISTFT = DEFAULT_MAX_FT;
	// We need two markers here for macro to know which to record
	static final int DIST_THOLD = 0, DIST_FT = 1;
	static final String[] DIST_CHOICES = { "threshold", "fraction" };
	static int whichDist = DEFAULT_CHOICE;
	static int[] numOfDistFTs = newArray(DEFAULT_FT, MAX_NREPORTERS);
	static int[] whichDistTholds = newArray(DEFAULT_CHOICE, MAX_NREPORTERS);

	// Custom parameters
	static String customCode_text = "";
	static boolean custom_chck = DEFAULT_BOOLEAN;

	// 3D TOS parameters
	static final String IMGTIPS3DTOS = "Choose Channel 3 for 3D TOS";
	static boolean doD3TOS = DEFAULT_BOOLEAN;
	static int[] tosFTs = newArray(DEFAULT_FT, 3);
	static int[] tosTholds;

	// Output parameters
	// public static final int RT = 0, STD = 1, MEAN = 2, MEDIAN = 3, HIST = 4;
	// public static final int MASK = 0, ROI = 1;
	static final String[] OUTPUTMETRICS = { "Summary", "Histogram(s)" };
	static final String[] OUTPUTOTHERS = { "Mask(s)", "ROI(s)" };
	static boolean[] outputMetric_chckes = newArray(DEFAULT_BOOLEAN, OUTPUTMETRICS.length);
	static boolean[] outputOpt_chckes = newArray(DEFAULT_BOOLEAN, OUTPUTOTHERS.length);

	public static Vector<Frame> allFrames = new Vector<Frame>();

	public static double parseDouble(String s) {
		if (s == null)
			return Double.NaN;
		double value = Tools.parseDouble(s);
		if (Double.isNaN(value)) {
			if (s.startsWith("&"))
				s = s.substring(1);
			Interpreter interp = Interpreter.getInstance();
			value = interp != null ? interp.getVariable2(s) : Double.NaN;
		}
		return value;
	}

	public static double[] str2doubles(String str) {
		String[] minAndMax;
		double mins, maxs;
		minAndMax = Tools.split(str, "-");
		mins = minAndMax.length >= 1 ? parseDouble(minAndMax[0].replaceAll("[[\\D+]&&[^\\.]]", "")) : Double.NaN;
		maxs = minAndMax.length == 2 ? parseDouble(minAndMax[1].replaceAll("[[\\D+]&&[^\\.]]", "")) : Double.NaN;
		mins = Double.isNaN(mins) ? DEFAULT_MIN : mins;
		maxs = Double.isNaN(maxs) ? DEFAULT_MAX : maxs;
		if (mins < DEFAULT_MIN)
			mins = DEFAULT_MIN;
		if (maxs < mins)
			maxs = DEFAULT_MAX;
		return new double[] { mins, maxs };
	}

	public static int getOptions() {
		return options;
	}

	public static String[] getAllfilters() {

		String[] allFilters = new String[filter_combs.length + adFilterChoices.size()];
		for (int iFilter = 0; iFilter < filter_combs.length; iFilter++)
			allFilters[iFilter] = filterStrings[filter_combs[iFilter]];
		for (int iFilter = 0; iFilter < adFilterChoices.size(); iFilter++)
			allFilters[iFilter + filter_combs.length] = filterStrings[adFilterChoices.get(iFilter)];

		return allFilters;
	}

	public static double[] getAllMinRanges() {

		double[] allMinRanges = new double[filterMinRange_texts.length + adMinRanges.size()];
		System.arraycopy(filterMinRange_texts, 0, allMinRanges, 0, filterMinRange_texts.length);
		for (int iFilter = 0; iFilter < adMinRanges.size(); iFilter++)
			allMinRanges[iFilter + filterMinRange_texts.length] = adMinRanges.get(iFilter);
		return allMinRanges;
	}

	public static double[] getAllMaxRanges() {

		double[] allMaxRanges = new double[filterMaxRange_texts.length + adMaxRanges.size()];
		System.arraycopy(filterMaxRange_texts, 0, allMaxRanges, 0, filterMaxRange_texts.length);
		for (int iFilter = 0; iFilter < adMaxRanges.size(); iFilter++)
			allMaxRanges[iFilter + filterMaxRange_texts.length] = adMaxRanges.get(iFilter);
		return allMaxRanges;
	}

	public static boolean[] getAllBackRatios() {

		boolean[] allBackRatios = new boolean[filterBackRatio_texts.length + adBackRatios.size()];
		System.arraycopy(filterBackRatio_texts, 0, allBackRatios, 0, filterBackRatio_texts.length);
		for (int iFilter = 0; iFilter < adBackRatios.size(); iFilter++)
			allBackRatios[iFilter + filterBackRatio_texts.length] = adBackRatios.get(iFilter);
		return allBackRatios;
	}

	public static String getFilterRange(double cmin, double cmax) {
		return getFilterRange(cmin, cmax, false);
	}

	public static String getFilterRange(double cmin, double cmax, boolean back) {
		// make filter range
		int places = 0;
		if ((int) cmin != cmin)
			places = 2;
		if ((int) cmax != cmax && cmax != Double.POSITIVE_INFINITY)
			places = 2;
		String minStr = ResultsTable.d2s(cmin, places);
		if (minStr.indexOf("-") != -1) {
			for (int i = places; i <= 6; i++) {
				minStr = ResultsTable.d2s(cmin, i);
				if (minStr.indexOf("-") == -1)
					break;
			}
		}
		String maxStr = ResultsTable.d2s(cmax, places);
		String sizeStr;
		if (back)
			sizeStr = minStr + "X-" + maxStr + "X";
		else
			sizeStr = minStr + "-" + maxStr;
		return sizeStr;
	}

	static boolean checkParams() {
		boolean changed = false;

		for (int iThold = 0; iThold < alignThold_combs.length; iThold++) {
			if (alignThold_combs[iThold] >= ALLTHOLDS.length || alignThold_combs[iThold] < 0) {
				alignThold_combs[iThold] = DEFAULT_CHOICE;
				changed = true;
			}
		}

		for (int iSize = 0; iSize < filterMinSize_texts.length; iSize++) {
			if (filterMaxSize_texts[iSize] < filterMinSize_texts[iSize]) {
				filterMinSize_texts[iSize] = DEFAULT_MIN;
				filterMaxSize_texts[iSize] = DEFAULT_MAX;
				changed = true;
			} else {
				if (filterMinSize_texts[iSize] < DEFAULT_MIN) {
					filterMinSize_texts[iSize] = DEFAULT_MIN;
					changed = true;
				}
				if (filterMaxSize_texts[iSize] > DEFAULT_MAX) {
					filterMaxSize_texts[iSize] = DEFAULT_MAX;
					changed = true;
				}
			}
		}

		for (int iFilter = 0; iFilter < filter_combs.length; iFilter++) {
			if (filter_combs[iFilter] < 0 || filter_combs[iFilter] >= filterStrings.length) {
				filter_combs[iFilter] = 0;
				changed = true;
			}
			if (filterMaxRange_texts[iFilter] < filterMinRange_texts[iFilter]) {
				filterMinRange_texts[iFilter] = DEFAULT_MIN;
				filterMaxRange_texts[iFilter] = DEFAULT_MAX;
				changed = true;
			} else {
				if (filterMinRange_texts[iFilter] < DEFAULT_MIN) {
					filterMinRange_texts[iFilter] = DEFAULT_MIN;
					changed = true;
				}
				if (filterMaxRange_texts[iFilter] > DEFAULT_MAX) {
					filterMaxRange_texts[iFilter] = DEFAULT_MAX;
					changed = true;
				}
			}
		}
		for (int iHeat = 0; iHeat < heatmapColor_combs.length; iHeat++) {
			if (heatmapColor_combs[iHeat] < 0 || heatmapColor_combs[iHeat] >= HEATMAPS.length) {
				heatmapColor_combs[iHeat] = HEATMAPS.length - 1;
				changed = true;
			}
		}

		if (heatmap_radio < 0 || heatmap_radio >= HEATMAPOPTS.length) {
			heatmap_radio = DEFAULT_CHOICE;
			changed = true;
		}

		if (mTOSscale < 0 || mTOSscale >= TOSOPTS.length) {
			mTOSscale = DEFAULT_CHOICE;
			changed = true;
		}

		for (int iTOS = 0; iTOS < matrixFT_spin.length; iTOS++) {
			if (matrixFT_spin[iTOS] < MINFT || matrixFT_spin[iTOS] > MAXFT) {
				matrixFT_spin[iTOS] = DEFAULT_FT;
				changed = true;
			}

		}

		if (whichDist < 0 || whichDist >= DIST_CHOICES.length) {
			whichDist = DEFAULT_CHOICE;
			changed = true;
		}

		for (int iDist = 0; iDist < whichDistTholds.length; iDist++) {
			if (whichDistTholds[iDist] >= ALLDISTTHOLDS.length || whichDistTholds[iDist] < 0) {
				whichDistTholds[iDist] = DEFAULT_CHOICE;
				changed = true;
			}
		}

		for (int iDist = 0; iDist < numOfDistFTs.length; iDist++) {
			if (numOfDistFTs[iDist] < MINDISTFT || numOfDistFTs[iDist] > MAXDISTFT) {
				numOfDistFTs[iDist] = DEFAULT_FT;
				changed = true;
			}
		}

		for (int iTOS = 0; iTOS < tosFTs.length; iTOS++) {
			if (tosFTs[iTOS] < MINFT || tosFTs[iTOS] > MAXFT) {
				tosFTs[iTOS] = DEFAULT_FT;
				changed = true;
			}

		}

		return changed;

	}

	static void retrieveOptions() {
		// This is important to remove all previous options because options is
		// static
		options = DO_NOTHING;

		for (int iAlign = 0; iAlign < align_chckes.length; iAlign++)
			if (imps[iAlign] != null && align_chckes[iAlign]) {
				options |= DO_ALIGN;
				break;
			}

		if (scatter_chck)
			options |= DO_SCATTER;

		if (matrix_chck)
			options |= DO_MATRIX;

		for (int iHeat = 0; iHeat < heatmap_chckes.length; iHeat++) {
			if (heatmap_chckes[iHeat]) {
				options |= DO_HEAT;
				break;
			}
		}

		if ((options & RUN_HEAT) != 0)
			options |= LIST_HEATMAPOPTS[heatmap_radio];

		for (int iMetric = 0; iMetric < metric_chckes.length; iMetric++)
			if (metric_chckes[iMetric]) {
				options |= LIST_METRICS[iMetric];
				options |= DO_RESULTTABLE;
			}

		for (int iMetric = 0; iMetric < other_chckes.length; iMetric++)
			if (other_chckes[iMetric]) {
				options |= LIST_OTHERS[iMetric];
				options |= DO_RESULTTABLE;
			}

		/*
		 * if((options&RUN_TOS)!=0) options|=LIST_TOS[mTOSscale];
		 * 
		 * if((options&RUN_DIST)!=0) options|=LIST_DIST[whichDist];
		 */

		if (custom_chck)
			options |= DO_CUSTOM;

		for (int iMetric = 0; iMetric < outputMetric_chckes.length; iMetric++)
			if (outputMetric_chckes[iMetric])
				options |= LIST_OUTPUTMETRICS[iMetric];

		for (int iMetric = 0; iMetric < outputOpt_chckes.length; iMetric++)
			if (outputOpt_chckes[iMetric])
				options |= LIST_OUTPUTOTHERS[iMetric];

		// I don't know why I put this condition here, might be for a reason
		// We will see.
		// reset options to zero each time the options are retrieved
		// if((options&RUN_METRICS_TOS)!=0)
		// options |= DO_RESULTTABLE ;

	}

	/**
	 * This method is used to pass the filter strings to cell filter dialog
	 * 
	 * @return a copy of <code>FILTERSTRINGS</code>
	 */
	public static String[] getFilterStrings() {
		return filterStrings.clone();
	}

	public static double[] newArray(double value, int length) {
		double[] result = new double[length];
		for (int i = 0; i < length; i++)
			result[i] = value;
		return result;
	}

	public static int[] newArray(int value, int length) {
		int[] result = new int[length];
		for (int i = 0; i < length; i++)
			result[i] = value;
		return result;
	}

	public static boolean[] newArray(boolean value, int length) {
		boolean[] result = new boolean[length];
		for (int i = 0; i < length; i++)
			result[i] = value;
		return result;
	}

	public static int[] newArray(int length) {
		int[] result = new int[length];
		for (int i = 0; i < length; i++)
			result[i] = i;
		return result;
	}

	public static int[][] newArray(int value, int width, int height) {
		int[][] result = new int[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				result[i][j] = value;
		return result;
	}

	public static int[][] newArray(int[] arr, int length) {
		int[][] result = new int[length][arr.length];
		for (int i = 0; i < length; i++)
			result[i] = arr.clone();
		return result;
	}

	public static double[][] newArray(double[] arr, int length) {
		double[][] result = new double[length][arr.length];
		for (int i = 0; i < length; i++)
			result[i] = arr.clone();
		return result;
	}

	public static float[][] newArray(float[] arr, int length) {
		float[][] result = new float[length][arr.length];
		for (int i = 0; i < length; i++)
			result[i] = arr.clone();
		return result;
	}

	public static ByteProcessor roi2mask(ImageProcessor ip, Roi[] rois) {
		if (ip == null)
			return null;
		int w = ip.getWidth();
		int h = ip.getHeight();

		if (rois == null || rois.length < 1)
			return new ByteProcessor(w, h);
		ByteProcessor result = new ByteProcessor(w, h);
		result.setColor(Color.WHITE);
		for (int i = 0; i < rois.length; i++) {
			if (rois[i] != null)
				result.fill(rois[i]);
		}
		result.resetRoi();
		result.invert();
		return result;
	}

	public static ImagePlus roiManager2Mask(ImagePlus imp) {

		RoiManager roiManager = RoiManager.getInstance2();
		if (roiManager == null) {
			return null;
		}

		Roi[] rois = roiManager.getRoisAsArray();
		int nSlices = 1, nFrames = 1, nChannels = 1, width = 0, height = 0;
		if (imp == null) {
			for (Roi roi : rois) {
				if (nSlices < roi.getZPosition())
					nSlices = roi.getZPosition();
				if (nFrames < roi.getTPosition())
					nFrames = roi.getTPosition();
				if (nChannels < roi.getCPosition())
					nChannels = roi.getCPosition();
				if (roi.getImage() != null) {
					if (width < roi.getImage().getWidth())
						width = roi.getImage().getWidth();
					if (height < roi.getImage().getHeight())
						height = roi.getImage().getHeight();
				}
			}
			if (width == 0)
				width = 400;
			if (height == 0)
				height = 400;
		} else {
			width = imp.getWidth();
			height = imp.getHeight();
			nSlices = imp.getNSlices();
			nFrames = imp.getNFrames();
			nChannels = imp.getNChannels();
		}

		Map<Integer, List<Roi>> roiMap = new HashMap<Integer, List<Roi>>();

		for (Roi roi : rois) {
			List<Roi> list = roiMap.get(roi.getPosition());
			if (list == null)
				list = new ArrayList<Roi>();
			list.add(roi);
			roiMap.put(roi.getPosition(), list);
		}

		imp = IJ.createHyperStack(ImageInfo.ROI_MANAGER, width, height, nChannels, nSlices, nChannels, 8);
		ImageStack impStack = imp.getStack();
		Roi[] allRois = roiMap.get(0) == null ? null : roiMap.get(0).toArray(new Roi[0]);

		if (nSlices != 1 || nFrames != 1 || nChannels != 1) {
			boolean reporter;
			reporter = false;
			if (allRois != null) {
				String str = "The following ROI(s) are not associated with a particular slice (they are applied to all slices): \n";
				for (Roi roi : allRois) {
					str += roi.getName() + "\n";
					reporter = true;
				}
				if (reporter)
					ExceptionHandler.addWarning(str);
			}

			reporter = false;
			String warning = "The following ROI(s) are not included (index out of range): \n";
			for (Roi roi : rois) {
				if (roi.getPosition() > impStack.size()) {
					warning += roi.getName() + "\n";
					reporter = true;
				}
			}
			if (reporter)
				ExceptionHandler.addWarning(warning);

			reporter = false;
			warning = "The following ROI(s) might be mismatched (associated with hyperstack): \n";
			for (Roi roi : rois) {
				if (roi.getCPosition() > 1 || (roi.getZPosition() > 1 && roi.getTPosition() > 1)) {
					warning += roi.getName() + "\n";
					reporter = true;
				}
			}
			if (reporter)
				ExceptionHandler.addWarning(warning);
		}

		int index;
		for (int channel = 1; channel <= nChannels; channel++)
			for (int slice = 1; slice <= nSlices; slice++)
				for (int frame = 1; frame <= nFrames; frame++) {
					index = (frame - 1) * nChannels * nSlices + (slice - 1) * nChannels + channel;
					List<Roi> list = roiMap.get(index);
					if (list != null) {
						if(allRois!=null){
							Roi[] thisRois = new Roi[list.size() + allRois.length];
							System.arraycopy(allRois, 0, thisRois, 0, allRois.length);
							System.arraycopy(list.toArray(), 0, thisRois, allRois.length, list.size());
							impStack.setProcessor(roi2mask(impStack.getProcessor(index), thisRois), index);
						}else{
							impStack.setProcessor(roi2mask(impStack.getProcessor(index), list.toArray(new Roi[0])), index);
						}
					} else {
						impStack.setProcessor(roi2mask(impStack.getProcessor(index), allRois), index);
					}
				}
		imp.setStack(impStack);
		return imp;
	}

	public static void resetParams() {

		Arrays.fill(align_chckes, DEFAULT_BOOLEAN);
		Arrays.fill(alignThold_combs, DEFAULT_CHOICE);
		didAlignment = false;

		// cell filters parameters
		Arrays.fill(filterMinSize_texts, DEFAULT_MIN);
		Arrays.fill(filterMaxSize_texts, DEFAULT_MAX);

		// filterChoices, minRanges, maxRanges, backRatios should have the same
		// length
		// we don't use default choice here, but default choice is still used in
		// smart recording
		// This is because the judgment to record is based on minRanges and
		// maxRanges, so the filter choice doesn't matter

		filter_combs = staticFilterChoices.clone();
		Arrays.fill(filterMinRange_texts, DEFAULT_MIN);
		Arrays.fill(filterMaxRange_texts, DEFAULT_MAX);
		Arrays.fill(filterBackRatio_texts, DEFAULT_BOOLEAN);

		adFilterChoices.clear();
		adMinRanges.clear();
		adMaxRanges.clear();
		adBackRatios.clear();

		// roiCells might be problematic in parallel streams/multiple threads
		roiCells = null;
		waterShed_chck = DEFAULT_BOOLEAN;

		// scatterplot parameter
		scatter_chck = DEFAULT_BOOLEAN;

		// matrix heat map parameter
		matrix_chck = DEFAULT_BOOLEAN;
		matrixMetric_comb = DEFAULT_CHOICE;
		matrixStats_comb = DEFAULT_CHOICE;
		Arrays.fill(matrixFT_spin, DEFAULT_FT);

		// heatmaps parameters
		Arrays.fill(heatmap_chckes, DEFAULT_BOOLEAN);
		heatmap_radio = DEFAULT_CHOICE;

		// This might not be the same as the initialization
		for (int i = 0; i < heatmapColor_combs.length; i++)
			heatmapColor_combs[i] = i;

		// metric parameters
		Arrays.fill(metric_chckes, DEFAULT_BOOLEAN);
		Arrays.fill(other_chckes, DEFAULT_BOOLEAN);
		Arrays.fill(metricThold_radios, DEFAULT_CHOICE);
		for (int[] fts : allFT_spins)
			Arrays.fill(fts, DEFAULT_FT);
		allTholds = null;

		// mTOS parameters
		// mTOSscale = DEFAULT_CHOICE;

		// DistancesX parameters
		// whichDist = DEFAULT_CHOICE;
		// Arrays.fill(numOfDistFTs,DEFAULT_FT);
		// Arrays.fill(whichDistTholds,DEFAULT_CHOICE);

		// Custom parameters
		customCode_text = StringCompiler.getDefaultCode();
		custom_chck = DEFAULT_BOOLEAN;

		// Output parameters
		Arrays.fill(outputMetric_chckes, DEFAULT_BOOLEAN);
		Arrays.fill(outputOpt_chckes, DEFAULT_BOOLEAN);

		// 3d TOS
		Arrays.fill(tosFTs, DEFAULT_FT);
		tosTholds = null;
	}

	public static void chooseAll() {

		Arrays.fill(align_chckes, true);
		// didAlignment = true;

		// roiCells might be problematic in parallel streams/multiple threads
		waterShed_chck = true;

		// scatterplot parameter
		scatter_chck = true;

		matrix_chck = true;

		// heatmaps parameters
		Arrays.fill(heatmap_chckes, true);

		// metric parameters
		Arrays.fill(metric_chckes, true);
		Arrays.fill(other_chckes, true);

		// Custom parameters
		custom_chck = true;

		// Output parameters
		Arrays.fill(outputMetric_chckes, true);
		Arrays.fill(outputOpt_chckes, true);
	}

	public static void closeAllWindows() {

		Frame currentFrame = WindowManager.getFrontWindow();
		ImageWindow currentImage = WindowManager.getCurrentWindow();

		for (Frame frame : allFrames) {
			if (frame != null && (frame instanceof Editor)) {
				((Editor) frame).close();
				if (((Editor) frame).fileChanged())
					return;
			}
		}
		ImageJ ij = IJ.getInstance();
		if (ij != null && ij.quitting() && IJ.getApplet() == null)
			return;
		for (Frame frame : allFrames) {
			if (frame == null || !frame.isVisible())
				continue;
			if ((frame instanceof PlugInFrame) && !(frame instanceof Editor)) {
				// frame.setVisible(false);
				((PlugInFrame) frame).close();
			} else if (frame instanceof TextWindow) {
				// frame.setVisible(false);
				((TextWindow) frame).close();
			} else {
				// frame.setVisible(false);
				frame.dispose();
				WindowManager.removeWindow(frame);
			}
		}
		allFrames.clear();

		if (currentFrame.isVisible())
			WindowManager.setWindow(currentFrame);
		if (currentImage.isVisible())
			WindowManager.setCurrentWindow(currentImage);
	}

	public static void saveAllWindows(String dir) {

		if (!(new File(dir)).isDirectory())
			return;

		Frame currentFrame = WindowManager.getFrontWindow();
		ImageWindow currentImage = WindowManager.getCurrentWindow();

		for (Iterator<Frame> iterator = allFrames.iterator(); iterator.hasNext();) {
			Frame frame = iterator.next();
			if (frame == null || !frame.isVisible()) {
				iterator.remove();
				continue;
			}
			WindowManager.setWindow(frame);
			String name = frame.getTitle().replaceAll("\\.", "_");
			if (frame instanceof TextWindow)
				IJ.saveAs("Results", dir + name + ".txt");
			else if (frame instanceof ImageWindow) {
				WindowManager.setCurrentWindow((ImageWindow) frame);
				IJ.saveAs("tif", dir + name + ".tif");
			} else if (frame instanceof RoiManager)
				IJ.saveAs("zip", dir + name + ".zip");
			else {
				IJ.error("Unknown window type cannot be saved");
				WindowManager.setWindow(frame);
				return;
			}
		}
		WindowManager.setWindow(currentFrame);
		WindowManager.setCurrentWindow(currentImage);
	}

	public static void cleanWindow() {
		for (Iterator<Frame> iterator = allFrames.iterator(); iterator.hasNext();) {
			Frame frame = iterator.next();
			if (frame == null || !frame.isVisible()) {
				iterator.remove();
				continue;
			}
		}
	}

	public static synchronized void addWindow(Frame win) {
		if (win != null)
			allFrames.addElement(win);
	}

	public static synchronized void addWindow(ImagePlus imp) {
		if (imp != null)
			addWindow(imp.getWindow());
	}

	public static ResultsTable matrix2ResultsTable(double[][] data, double[] x, double[] y, boolean invertX,
			boolean invertY) {
		String[] strX = null, strY = null;
		if (x != null) {
			strX = new String[data.length];
			for (int i = 0; i < strX.length; i++)
				strX[i] = Double.toString(x[i]);
		}

		if (y != null) {
			strY = new String[data[0].length];
			for (int i = 0; i < strY.length; i++)
				strY[i] = Double.toString(y[i]);
		}

		return matrix2ResultsTable(data, strX, strY, invertX, invertY);
	}

	public static ResultsTable matrix2ResultsTable(double[][] data, float[] x, float[] y, boolean invertX,
			boolean invertY) {
		String[] strX = null, strY = null;
		if (x != null) {
			strX = new String[data.length];
			for (int i = 0; i < strX.length; i++)
				strX[i] = Float.toString(x[i]);
		}

		if (y != null) {
			strY = new String[data[0].length];
			for (int i = 0; i < strY.length; i++)
				strY[i] = Float.toString(y[i]);
		}

		return matrix2ResultsTable(data, strX, strY, invertX, invertY);
	}

	/**
	 * Convert a double matrix to a ResultsTable
	 * 
	 * @param data
	 *            must be a matrix, cannot be jagged array
	 * @param x
	 * @param y
	 * @param invertX
	 * @param invertY
	 * @return
	 */
	public static ResultsTable matrix2ResultsTable(double[][] data, String[] x, String[] y, boolean invertX,
			boolean invertY) {
		if (data == null)
			return null;
		ResultsTable rt = new ResultsTable();
		if (x == null) {
			x = new String[data.length];
			for (int i = 0; i < x.length; i++)
				x[i] = Integer.toString(i + 1);
		}
		if (y == null) {
			y = new String[data[0].length];
			for (int i = 0; i < y.length; i++)
				y[i] = Integer.toString(i + 1);
		}

		int increX, startX, endX, increY, startY, endY;
		if (invertX) {
			increX = -1;
			startX = data.length - 1;
			endX = -1;
		} else {
			increX = 1;
			startX = 0;
			endX = data.length;
		}

		if (invertY) {
			increY = -1;
			startY = data[0].length - 1;
			endY = -1;
		} else {
			increY = 1;
			startY = 0;
			endY = data[0].length;
		}

		rt.incrementCounter();
		rt.addLabel("");
		for (int iColumn = startX; iColumn != endX; iColumn += increX) {
			rt.addValue(Integer.toString(iColumn + 1), x[iColumn]);
		}

		for (int iRow = startY; iRow != endY; iRow += increY) {
			rt.incrementCounter();
			rt.addLabel(y[iRow]);
			for (int iColumn = startX; iColumn != endX; iColumn += increX) {
				rt.addValue(Integer.toString(iColumn + 1), data[iColumn][iRow]);
			}
		}
		rt.showRowNumbers(false);
		return rt;
	}

	public static <E> Object[] other2objArray(E[] array) {
		if (array == null)
			return null;
		Object[] objs = new Object[array.length];
		for (int i = 0; i < objs.length; i++)
			objs[i] = array[i];
		return objs;
	}

	public static Object[] double2objArray(double[] array) {
		if (array == null)
			return null;
		Object[] objs = new Object[array.length];
		for (int i = 0; i < objs.length; i++)
			objs[i] = array[i];
		return objs;
	}

	public static <E> E[] flipArray(E[] array) {
		if (array == null)
			return null;
		E temp;
		for (int i = 0; i < array.length / 2; i++) {
			temp = array[array.length - 1 - i];
			array[array.length - 1 - i] = array[i];
			array[i] = temp;
		}
		return array;
	}

	public static float[] obj2floatArray(Object[] array) {
		if (array == null)
			return null;
		float[] objs = new float[array.length];
		for (int i = 0; i < objs.length; i++)
			try {
				objs[i] = (float) array[i];
			} catch (Exception e) {
				objs[i] = Float.NaN;
			}

		return objs;
	}

	public static double round(double value, int places) {
		if (places < 0)
			places = 0;// throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static float round(float value, int places) {
		if (places < 0)
			places = 0;// throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.floatValue();
	}

	static String[] makeAllFilters(int nReporters) {

		String[] result = new String[SHAPE_FILTERS.length + INTEN_FILTERS.length * nReporters];
		System.arraycopy(SHAPE_FILTERS, 0, result, 0, SHAPE_FILTERS.length);
		for (int iChannel = 0; iChannel < nReporters; iChannel++)
			for (int iFilter = 0; iFilter < INTEN_FILTERS.length; iFilter++)
				result[SHAPE_FILTERS.length + iFilter + INTEN_FILTERS.length * iChannel] = makeIntFilterString(
						INTEN_FILTERS[iFilter], iChannel);
		return result;
	}

	static String makeIntFilterString(String prefix, int iChannel) {
		return prefix + " (Ch." + (iChannel + 1) + ")";
	}

	static String[] makeImgLabels(int nChannel) {
		String[] result = new String[nChannel];
		for (int i = 1; i < nChannel; i++)
			result[i - 1] = "Reporter " + i +" (Ch."+i+")";
		result[nChannel - 1] = "Cell identification input";
		return result;
	}

	public static void setPlugIn(Class<?> clazz) {
		EzColocalization = clazz;
		PluginStatic.pluginName = clazz.getSimpleName().replace("_", " ");
	}

	public static String getPlugInName() {
		return pluginName;
	}
	
	public static Class<?> getPlugInClass(){
		return EzColocalization;
	}

	public static String getInfo() {
		return getPlugInName() + "(" + VERSION + ")" + ", Please contact Han Lim at " + CONTACT + " for any question";
	}

	static String[] getMatrixMetrics(int dimension) {
		if (dimension == 2)
			return MatrixCalculator.getAllMetrics();
		else if (dimension == 3)
			return MatrixCalculator3D.getAllMetrics();
		else
			return null;
	}

}
