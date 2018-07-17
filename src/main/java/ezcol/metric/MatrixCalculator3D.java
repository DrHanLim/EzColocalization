package ezcol.metric;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import ezcol.cell.CellData;
import ezcol.cell.DataSorter;
import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginConstants;
import ezcol.main.PluginStatic;
import ezcol.visual.visual2D.HeatChart;
import ezcol.visual.visual3D.ScatterPlot3DWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.measure.ResultsTable;

public class MatrixCalculator3D extends BasicCalculator {

	public static final int DO_MATRIX = PluginConstants.DO_MATRIX, DO_LINEAR_TOS = PluginConstants.DO_LINEAR_TOS,
			DO_LOG2_TOS = PluginConstants.DO_LOG2_TOS, DO_LN_TOS = PluginConstants.DO_LN_TOS,
			OPTS_TOS = PluginConstants.OPTS_TOS;

	public static final String SHOW_TOS_LINEAR = "TOS(linear)", SHOW_TOS_LOG2 = "TOS(log2)", SHOW_PCC = "PCC",
			SHOW_SRC = "SRCC", SHOW_ICQ = "ICQ", SHOW_M1 = "M1", SHOW_M2 = "M2", SHOW_M3 = "M3";
	// Although we can calculate n-dimensional results, we can only print up to
	// 3d.
	// Therefore, MCC is set to be M1, M2, and M3.
	private int metricIdx;
	private static final String[] METRIC_NAMES = { SHOW_TOS_LINEAR, SHOW_TOS_LOG2, SHOW_ICQ, SHOW_M1, SHOW_M2, SHOW_M3 };

	// mMetric[iCell][iBox];
	private double[][] mMetric;
	// cellCs[iChannel][iCell];
	// private CellData[][] cellCs;

	private ResultsTable mTOSResultTable;

	public MatrixCalculator3D() {
		options = 0;
	}

	public MatrixCalculator3D(int[] cutoffs) {
		options = DO_LINEAR_TOS | DO_MATRIX;
		if (cutoffs != null && cutoffs.length >= 2)
			this.cutoffs = cutoffs.clone();
		else
			this.cutoffs = new int[] { DEFAULT_FT, DEFAULT_FT };

		numFT2SF();
	}

	public MatrixCalculator3D(int options, int[] cutoffs) {
		this.options = options;
		if (cutoffs != null && cutoffs.length >= 2)
			this.cutoffs = cutoffs.clone();
		else
			this.cutoffs = new int[] { DEFAULT_FT, DEFAULT_FT };
		numFT2SF();
	}

	public MatrixCalculator3D(BasicCalculator callBase) {
		this.options = callBase.options;
		this.numOfCell = callBase.numOfCell;
		this.ftCs = callBase.ftCs;
		this.allCs = callBase.allCs;
		this.costesCs = callBase.costesCs;
	}

	public boolean calMetrics(CellData[][] cellCs, int metricIndex) {
		if (!prepCellData(cellCs, new int[] { BasicCalculator.THOLD_ALL }))
			return false;

		metricIdx = metricIndex;

		if ((options & DO_MATRIX) != 0)
			mMetric = new double[numOfCell][];
		else
			mMetric = null;

		for (int iCell = 0; iCell < numOfCell; iCell++) {
			// also gives TOSmax and TOSmin
			if (mMetric != null) {

				this.cellCs = allCs;
				mMetric[iCell] = getMatrix(iCell, metricIndex);
				if (mMetric[iCell] == null) {
					mMetric = null;
					return false;
				}
			}

			// get TOSmin and TOSmax are in getmTOS function
		}

		return true;
	}

	private void printmTOS() {
		if (mMetric == null || sfsCs == null)
			return;
		if (mTOSResultTable == null)
			mTOSResultTable = new ResultsTable();
		else
			mTOSResultTable.reset();
		for (int iCell = 0; iCell < numOfCell; iCell++)
			addToRT(mTOSResultTable, mMetric[iCell]);
	}

	private void addToRT(ResultsTable rt, double[] values) {

		rt.incrementCounter();
		int numBox = getMatrixSize();

		for (int iBox = 0; iBox < numBox; iBox++) {
			String valTitle = "";
			for (int channel = 0; channel < sfsCs.length; channel++)
				valTitle += "S" + (channel + 1) + "-" + sfsCs[channel][getSfsCsIdx(channel, iBox)];

			rt.addValue(valTitle, values[iBox]);
		}

	}

	private int getSfsCsIdx(int channel, int iBox) {

		int idx = iBox;

		for (int count2 = sfsCs.length - 1; count2 > channel; count2--) {
			idx /= sfsCs[count2].length;
		}

		idx %= sfsCs[channel].length;

		return idx;
	}

	public ResultsTable getResultsTable() {
		if (mTOSResultTable == null)
			printmTOS();
		return mTOSResultTable;
	}

	/**
	 * Introduced in version 1.2
	 */
	/**
	 * This is used in getMatrices to update matrix heat map
	 * 
	 * @param dataA
	 * @param dataB
	 * @param index
	 * @return
	 */
	protected double calMetric(CellData[] cellData, int index, boolean noTholdTOS) {

		if (cellData == null || cellData.length < 2)
			return Double.NaN;

		/*
		 * int overlap = 0; for(int i=0;i<length;i++){ for(int
		 * iChannel=0;iChannel<cellData.length;iChannel++)
		 * if(Float.isNaN(cellData[iChannel].getPixel(i))){ overlap--; }
		 * overlap++; }
		 * 
		 * CellData copyA = new CellData(overlap); CellData copyB = new
		 * CellData(overlap);
		 * 
		 * for(int i=0, j=0;i<length;i++){ if(!Float.isNaN(dataA.getPixel(i)) &&
		 * !Float.isNaN(dataB.getPixel(i))){ copyA.setData(dataA, i, j);
		 * copyB.setData(dataB, i, j); j++; } }
		 */

		double result;

		if (index < 0 || index >= METRIC_NAMES.length)
			return Double.NaN;
		String metricName = METRIC_NAMES[index];

		switch (metricName) {
		case SHOW_TOS_LINEAR:
			result = getTOS(getCellPixels(cellData))[0];
			break;
		case SHOW_TOS_LOG2:
			result = getTOS(getCellPixels(cellData))[1];
			break;
		case SHOW_PCC:
			result = getPCC(getCellPixels(cellData));
			break;
		case SHOW_SRC:
			result = getSRCC(getCellRanks(cellData));
			break;
		case SHOW_ICQ:
			result = getICQ(getCellPixels(cellData));
			break;
		// case SHOW_CUSTOM:
		// result = getCustom(copyA,copyB);
		// break;
		// case SHOW_AVGINT_C1:
		// result = getAVGINT(copyA,copyB)[0];
		// break;
		// case SHOW_AVGINT_C2:
		// result = getAVGINT(copyA,copyB)[1];
		// break;
		// Use unprocessed data here because points below thresholds are also
		// informative
		case SHOW_M1:
			result = getMCC(getCellPixels(cellData))[0];
			break;
		case SHOW_M2:
			result = getMCC(getCellPixels(cellData))[1];
			break;
		case SHOW_M3:
			result = getMCC(getCellPixels(cellData))[2];
			break;
		default:
			result = Double.NaN;
			break;
		}
		if (Double.isNaN(result)) {
			if (!((METRIC_NAMES[index] == SHOW_TOS_LINEAR || METRIC_NAMES[index] == SHOW_TOS_LOG2) && (noTholdTOS))) {
				ExceptionHandler.addWarning(Thread.currentThread(), cellData[0].getCellLabel() + "(size:"
						+ cellData[0].length() + ") returns a " + metricName + " value of NaN");
				ExceptionHandler.insertWarning(Thread.currentThread(), ExceptionHandler.NAN_WARNING);
			}
		}

		return result;

	}

	/**
	 * only for metric heat map
	 */
	public Object[] getColorValues(int index, int num) {

		if (index < 0 || index >= METRIC_NAMES.length)
			return null;

		switch (METRIC_NAMES[index]) {
		case SHOW_TOS_LINEAR:
		case SHOW_TOS_LOG2:
		case SHOW_PCC:
		case SHOW_SRC:
			return getColorValues(-1, 1, num);
		case SHOW_ICQ:
			return getColorValues(-0.5, 0.5, num);
		case SHOW_M1:
		case SHOW_M2:
		case SHOW_M3:
			return getColorValues(0, 1, num);
		default:
			return null;
		}
	}

	/**
	 * over for metric heat map use
	 */
	public float[] getMetricRange(int index) {
		if (index < 0 || index >= METRIC_NAMES.length)
			return null;

		switch (METRIC_NAMES[index]) {
		case SHOW_TOS_LINEAR:
		case SHOW_TOS_LOG2:
		case SHOW_PCC:
		case SHOW_SRC:
			return new float[] { -1.0f, 0.0f, 1.0f };
		case SHOW_ICQ:
			return new float[] {-0.5f, 0.0f, 0.5f };
		case SHOW_M1:
		case SHOW_M2:
		case SHOW_M3:
			return new float[] { 0.0f, 0.5f, 1.0f };
		default:
			return null;
		}
	}

	/**
	 * calculate the sums of the arrays above the thresholds in each array and
	 * in both arrays
	 * 
	 * @see <code>MetricCalculator.getSum(float[] ,float[] , double, double)</code>
	 * @param a
	 *            input array 1
	 * @param b
	 *            input array 2
	 * @param thresholdA
	 *            threshold of array a
	 * @param thresholdB
	 *            threshold of array b
	 * @return an array of four values as [0].number of elements in a that is
	 *         not NaN in a & not NaN in b [1].number of elements in b that is
	 *         not NaN in a & not NaN in b [2].number of elements in a that is
	 *         not NaN in a [3].number of elements in b that is not NaN in b
	 */
	public double[] getSum(float[] a, float[] b) {
		if (a == null || b == null || a.length != b.length)
			return new double[] { Double.NaN, Double.NaN, Double.NaN, Double.NaN };

		double[] results = new double[4];
		for (int i = 0; i < a.length; i++) {
			if (!Float.isNaN(a[i]) && !Float.isNaN(b[i])) {
				results[0]++;
				results[1]++;
			}
			if (!Float.isNaN(a[i]))
				results[2]++;
			if (!Float.isNaN(b[i]))
				results[3]++;
		}
		return results;
	}

	protected Object[] getColorValues(double min, double max, int numColorBar) {
		Object[] inputC = new Object[numColorBar];
		double increColorBar = (max - min) / (numColorBar - 1);
		for (int i = 0; i < numColorBar; i++)
			inputC[i] = round(min + increColorBar * i, 3);
		return inputC;
	}

	public ResultsTable getMatrix(CellData[][] cellCs, int index) {
		if (cellCs == null)
			return null;
		this.cellCs = cellCs;

		numOfCell = Integer.MAX_VALUE;
		for (int i = 0; i < cellCs.length; i++) {
			if (cellCs[i] == null)
				return null;
			if (cellCs[i].length < numOfCell)
				numOfCell = cellCs[i].length;
		}

		ResultsTable rt = new ResultsTable();

		for (int iCell = 0; iCell < numOfCell; iCell++) {
			double[] results = getMatrix(iCell, index);
			rt.incrementCounter();
			rt.addLabel(cellCs[0][iCell].getCellLabel());
			addToRT(rt, results);
		}

		return rt;
	}

	/**
	 * 
	 * @param iCell
	 * @param callBases
	 * @return
	 */
	private double[] getMatrix(int iCell, int show_idx) {
		if (cellCs == null || sfsCs == null) {
			ExceptionHandler.addError(Thread.currentThread(), "Null pointer in cell data and parameter");
			return null;
		}

		double[] result = new double[getMatrixSize()];

		int size = Integer.MAX_VALUE;

		@SuppressWarnings("unchecked")
		Map<Integer, CellData>[] trimMap = new HashMap[cellCs.length];

		// sorting is done together to save time
		for (int iChannel = 0; iChannel < cellCs.length; iChannel++) {
			if (!cellCs[iChannel][iCell].isSorted())
				cellCs[iChannel][iCell].sort();
			if (cellCs[iChannel][iCell].length() < size)
				size = cellCs[iChannel][iCell].length();
			trimMap[iChannel] = new HashMap<Integer, CellData>();
		}

		int numBox = getMatrixSize();

		for (int iBox = 0; iBox < numBox; iBox++) {

			boolean noThold = false;
			CellData[] trims = new CellData[cellCs.length];
			for (int iChannel = 0; iChannel < sfsCs.length; iChannel++) {

				int positive = (int) Math.ceil((size * sfsCs[iChannel][getSfsCsIdx(iChannel, iBox)]));

				if (sfsCs[iChannel][getSfsCsIdx(iChannel, iBox)] >= 1.0)
					noThold = true;

				if (positive > size)
					positive = size;

				if (trimMap[iChannel].containsKey(positive)) {
					trims[iChannel] = trimMap[iChannel].get(positive);
				} else {
					trims[iChannel] = cellCs[iChannel][iCell].getData(positive);
					trimMap[iChannel].put(positive, trims[iChannel]);
				}

			}
			double metricDouble = calMetric(trims, show_idx, noThold);
			result[iBox] = metricDouble;

		}

		/*
		 * for (int selection1=0; selection1<sfsCs[0].length; selection1++){ int
		 * positiveA=(int) Math.ceil((size*sfsCs[0][selection1]));
		 * positiveA=positiveA<size?positiveA:size; CellData trimA =
		 * cellC1[iCell].getData(positiveA);
		 * 
		 * for (int selection2=0; selection2<sfsCs[1].length; selection2++){ int
		 * positiveB=(int) Math.ceil((size*sfsCs[1][selection2]));
		 * positiveB=positiveB<size?positiveB:size; CellData trimB =
		 * cellC2[iCell].getData(positiveB);
		 * 
		 * if(selection1==0&&selection2==sfsCs[1].length-1){ ; }
		 * 
		 * double metricDouble = calMetric(trimA, trimB, show_idx);
		 * 
		 * //store channel 1 selection as row, channel 2 as column
		 * result[selection2+selection1*sfsCs[1].length] = metricDouble;
		 * 
		 * } }
		 */

		return result;
	}

	// number of boundaries not interval (interval+!)
	public static int numColorBar = 6;

	public HeatChart getHeatChart(double[][] medianmValues, String title) {
		if (medianmValues == null)
			return null;

		double minTOS = -1.0, maxTOS = 1.0, nullTOS = 0.0;
		double increColorBar = (maxTOS - minTOS) / (numColorBar - 1);
		Object[] inputC1 = new Object[sfsCs[0].length];
		Object[] inputC2 = new Object[sfsCs[1].length];
		Object[] inputC = new Object[numColorBar];

		HeatChart mTOSHeatChart = new HeatChart(medianmValues, minTOS, nullTOS, maxTOS);
		// mTOSHeatChart.setTitle(title);

		for (int i = 0; i < sfsCs[0].length; i++)
			inputC1[sfsCs[0].length - 1 - i] = sfsCs[0][i] + "%";

		for (int i = 0; i < sfsCs[1].length; i++)
			inputC2[i] = sfsCs[1][i] + "%";

		for (int i = 0; i < numColorBar; i++)
			inputC[i] = round(minTOS + increColorBar * i, 3);

		mTOSHeatChart.setXValues(inputC1);
		mTOSHeatChart.setYValues(inputC2);
		mTOSHeatChart.setTitleFont(new Font("Arial", Font.PLAIN, 32));
		mTOSHeatChart.setXAxisLabel("Selected % 1");
		mTOSHeatChart.setYAxisLabel("Selected % 2");
		mTOSHeatChart.setAxisLabelsFont(new Font("Arial", Font.PLAIN, 18));
		mTOSHeatChart.setAxisValuesFont(new Font("Arial", Font.PLAIN, 16));
		mTOSHeatChart.setColorValuesFont(new Font("Arial", Font.PLAIN, 16));
		mTOSHeatChart.setAxisThickness(0);
		mTOSHeatChart.setBackgroundColour(Color.LIGHT_GRAY);
		mTOSHeatChart.setLowValueColour(Color.BLUE);
		mTOSHeatChart.setHighValueColour(Color.RED);
		mTOSHeatChart.setMiddleValueColour(Color.WHITE);
		mTOSHeatChart.setCellSize(new Dimension(50, 50));
		mTOSHeatChart.setColorBarWidth(25);
		mTOSHeatChart.setColorBarValues(inputC);
		return mTOSHeatChart;
	}

	public ImageWindow getD3Heatmap(double[] synValues, int[] dimensions) {
		
		
		float[] customScales = getMetricRange(metricIdx);
		Color[] colorScales = { Color.BLUE, Color.WHITE, Color.RED };

		int numBox = getMatrixSize(dimensions);
		
		if(numBox == 0 || synValues == null || synValues.length < numBox)
			return null;

		float[][] xValues = new float[numBox][1];
		float[][] yValues = new float[numBox][1];
		float[][] zValues = new float[numBox][1];
		float[] customColors = new float[numBox];

		for (int iBox = 0; iBox < numBox; iBox++) {
			xValues[iBox][0] = (float) sfsCs[0][getSfsCsIdx(0, iBox)];
			yValues[iBox][0] = (float) sfsCs[1][getSfsCsIdx(1, iBox)];
			zValues[iBox][0] = (float) sfsCs[2][getSfsCsIdx(2, iBox)];
			customColors[iBox] = (float) synValues[iBox];
		}
		String title = getNames(metricIdx) + " " + getStatsName(statsMethod) + " Matrix";
		ScatterPlot3DWindow sdw = new ScatterPlot3DWindow(title, "Channel 1", "Channel 2", "Channel 3", xValues,
				yValues, zValues, 77, customColors, customScales, colorScales);
		ScatterPlot3DWindow.setPlotTitle(title);
		sdw.setRawResultsTable(getResultsTable());
		sdw.setDefaultRotation(-240, 0, -45);
		sdw.draw();

		return sdw;

	}

	public ImageWindow getD3Heatmap() {

		int[] dimensions = new int[sfsCs.length];
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = sfsCs[i].length;
		}

		return getD3Heatmap(getStatsMatrix(mMetric), dimensions);

	}

	public ImagePlus getHeatmap(double[][] medianmTValues, boolean show) {
		if (medianmTValues == null)
			return null;
		HeatChart mTOSHeatChart = getHeatChart(medianmTValues, "metric matrix");
		ImagePlus mTOSHeatmap = mTOSHeatChart.getImagePlus("metric matrix heatmap");
		if (show)
			mTOSHeatmap.show();
		return mTOSHeatmap;
	}

	/**
	 * get the number of metric names it is also the number of metrics
	 * calculated in the derived class please use BasicCalculator.getNum() for
	 * DEFAULT_NAMES
	 * 
	 * @return
	 */
	public static int getNum() {
		return METRIC_NAMES.length < METRIC_NAMES.length ? METRIC_NAMES.length : METRIC_NAMES.length;
	}

	/**
	 * get the name of the metric please use BasicCalculator.getNum() for
	 * DEFAULT_IDXES
	 * 
	 * @param i
	 *            index of the metric
	 * @return the name of i-th metric
	 */
	public static String getNames(int i) {
		if (i < 0 || i >= METRIC_NAMES.length)
			return null;
		return METRIC_NAMES[i];
	}

	public static String[] getAllMetrics() {
		return METRIC_NAMES.clone();
	}

	private int getMatrixSize() {
		int result = 1;
		for (int i = 0; i < sfsCs.length; i++) {
			result *= sfsCs[i].length;
		}
		return result;
	}

	private int getMatrixSize(int[] dimensions) {
		if(dimensions == null || dimensions.length <= 0)
			return 0;
		int result = 1;
		for (int i = 0; i < dimensions.length; i++) {
			result *= dimensions[i];
		}
		return result;
	}

	public static final String MEDIAN = "Median", MEAN = "Mean", MODE = "Mode";
	private int statsMethod = 0;
	private static final String[] STATS_NAMES = { MEDIAN, MEAN, MODE };

	private double[] getStatsMatrix(double[][] value) {

		if(value == null || value.length <= 0)
			return null;
		
		double[][] tpValue = transpose(value);
		
		double[] matrix = new double[value[0].length];

		switch (STATS_NAMES[statsMethod]) {
		case MEDIAN:
			for (int iBox = 0; iBox < tpValue.length; iBox++)
				matrix[iBox] = getMedian(tpValue[iBox]);
			break;
		case MEAN:
			for (int iBox = 0; iBox < tpValue.length; iBox++)
				matrix[iBox] = getMean(tpValue[iBox]);
			break;
		case MODE:
			for (int iBox = 0; iBox < tpValue.length; iBox++)
				matrix[iBox] = getMode(tpValue[iBox]);
			break;
		default:
			return null;
		}

		return matrix;
	}

	public void setStatsMethod(int i) {
		if (i < 0 || i >= STATS_NAMES.length)
			statsMethod = i;
	}

	public void setStatsMethod(String name) {
		for (int i = 0; i < STATS_NAMES.length; i++)
			if (STATS_NAMES[i].equalsIgnoreCase(name)) {
				statsMethod = i;
			}
	}

	public String getStatsName(int i) {
		if (i >= 0 || i < STATS_NAMES.length)
			return STATS_NAMES[i];
		else
			return "";
	}

	private float[][] getCellPixels(CellData[] cellData) {
		float[][] results = new float[cellData.length][];
		for (int i = 0; i < cellData.length; i++)
			results[i] = cellData[i].getData();
		return results;
	}

	private double[][] getCellRanks(CellData[] cellData) {
		double[][] results = new double[cellData.length][];
		for (int i = 0; i < cellData.length; i++)
			results[i] = cellData[i].getRank();
		return results;
	}
}
