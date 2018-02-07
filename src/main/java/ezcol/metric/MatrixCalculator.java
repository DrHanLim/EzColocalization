package ezcol.metric;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import ezcol.cell.CellData;
import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginConstants;
import ezcol.visual.visual2D.HeatChart;
import ij.ImagePlus;
import ij.measure.ResultsTable;

public class MatrixCalculator extends BasicCalculator {

	public static final int DO_MATRIX = PluginConstants.DO_MATRIX, DO_LINEAR_TOS = PluginConstants.DO_LINEAR_TOS,
			DO_LOG2_TOS = PluginConstants.DO_LOG2_TOS, DO_LN_TOS = PluginConstants.DO_LN_TOS,
			OPTS_TOS = PluginConstants.OPTS_TOS, RUN_TOS = PluginConstants.RUN_TOS;

	public static final String SHOW_TOS_LINEAR = "TOS(linear)", SHOW_TOS_LOG2 = "TOS(log2)", SHOW_PCC = "PCC",
			SHOW_SRC = "SRCC", SHOW_ICQ = "ICQ", SHOW_M1 = "M1", SHOW_M2 = "M2", SHOW_M3 = "M3";

	private static final String[] METRIC_NAMES = { SHOW_TOS_LINEAR, SHOW_TOS_LOG2, SHOW_PCC, SHOW_SRC, SHOW_ICQ,
			SHOW_M1, SHOW_M2 };

	private double[][] mMetric;
	private CellData[] cellC1, cellC2;

	private ResultsTable mTOSResultTable;
	// private ImagePlus mTOSmedianHeatmap;
	// private ImagePlus mTOSallHeatmap;

	public MatrixCalculator() {
		options = 0;
	}

	public MatrixCalculator(int[] cutoffs) {
		options = DO_LINEAR_TOS | DO_MATRIX;
		if (cutoffs != null && cutoffs.length >= 2)
			this.cutoffs = cutoffs.clone();
		else
			this.cutoffs = new int[] { DEFAULT_FT, DEFAULT_FT };

		numFT2SF();
	}

	public MatrixCalculator(int options, int[] cutoffs) {
		this.options = options;
		if (cutoffs != null && cutoffs.length >= 2)
			this.cutoffs = cutoffs.clone();
		else
			this.cutoffs = new int[] { DEFAULT_FT, DEFAULT_FT };
		numFT2SF();
	}

	public MatrixCalculator(BasicCalculator callBase) {
		this.options = callBase.options;
		this.numOfCell = callBase.numOfCell;
		this.ftCs = callBase.ftCs;
		this.allCs = callBase.allCs;
		this.costesCs = callBase.costesCs;
	}

	public boolean calMetrics(CellData[] cellC1, CellData[] cellC2, int metricIndex) {
		if (!prepCellData(cellC1, cellC2, new int[] { BasicCalculator.THOLD_ALL }))
			return false;

		if ((options & RUN_TOS) != 0) {
			if ((options & DO_MATRIX) != 0)
				mMetric = new double[numOfCell][cutoffs[0] * cutoffs[1]];
			else
				mMetric = null;

			for (int iCell = 0; iCell < numOfCell; iCell++) {
				// also gives TOSmax and TOSmin
				if (mMetric != null) {

					this.cellC1 = allCs[0];
					this.cellC2 = allCs[1];

					mMetric[iCell] = getMatrix(iCell, metricIndex);
					if (mMetric[iCell] == null) {
						mMetric = null;
						return false;
					}
				}

				// get TOSmin and TOSmax are in getmTOS function
			}

		} else {
			mMetric = null;
		}

		if ((options & (DO_MATRIX)) != 0)
			printmTOS();

		return true;
	}

	private void printmTOS() {
		if (mMetric == null || sfsCs == null)
			return;
		if (mTOSResultTable == null)
			mTOSResultTable = new ResultsTable();
		else
			mTOSResultTable.reset();
		for (int iCell = 0; iCell < numOfCell; iCell++) {
			mTOSResultTable.incrementCounter();
			// mTOSResultTable.addLabel("shot-"+(iFrame)+" "+"cell-"+(iCell+1));
			for (int count1 = 0; count1 < sfsCs[0].length; count1++) {
				for (int count2 = 0; count2 < sfsCs[1].length; count2++) {
					mTOSResultTable.addValue("S1-" + sfsCs[0][count1] + "_S2-" + sfsCs[1][count2],
							mMetric[iCell][count2 + sfsCs[1].length * count1]);
				}
			}

		}
	}

	public ResultsTable getResultsTable() {
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
	protected double calMetric(CellData dataA, CellData dataB, int index, boolean noTholdA, boolean noTholdB) {

		if (dataA == null || dataB == null)
			return Double.NaN;

		int length = dataA.length() < dataB.length() ? dataA.length() : dataB.length();
		int overlap = 0;
		for (int i = 0; i < length; i++) {
			if (!Float.isNaN(dataA.getPixel(i)) && !Float.isNaN(dataB.getPixel(i)))
				overlap++;
		}
		CellData copyA = new CellData(overlap);
		CellData copyB = new CellData(overlap);

		for (int i = 0, j = 0; i < length; i++) {
			if (!Float.isNaN(dataA.getPixel(i)) && !Float.isNaN(dataB.getPixel(i))) {
				copyA.setData(dataA, i, j);
				copyB.setData(dataB, i, j);
				j++;
			}
		}

		double result;

		if (index < 0 || index >= METRIC_NAMES.length)
			return Double.NaN;

		switch (METRIC_NAMES[index]) {
		case SHOW_TOS_LINEAR:
			result = getTOS(dataA, dataB)[0];
			break;
		case SHOW_TOS_LOG2:
			result = getTOS(dataA, dataB)[1];
			break;
		case SHOW_PCC:
			result = getPCC(copyA, copyB);
			break;
		case SHOW_SRC:
			result = getSRCC(copyA, copyB);
			break;
		case SHOW_ICQ:
			result = getICQ(copyA, copyB);
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
			result = getMCC(dataA, dataB)[0];
			break;
		case SHOW_M2:
			result = getMCC(dataA, dataB)[1];
			break;
		default:
			result = Double.NaN;
			break;
		}

		if (Double.isNaN(result)) {
			if (!((METRIC_NAMES[index] == SHOW_TOS_LINEAR || METRIC_NAMES[index] == SHOW_TOS_LOG2) && (noTholdA || noTholdB))) {
				ExceptionHandler.addWarning(Thread.currentThread(), dataA.getCellLabel() + "(size:" + dataA.length()
						+ ") returns a " + METRIC_NAMES[index] + " value of NaN");
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
			return getColorValues(-1, 1, num);
		case SHOW_PCC:
			return getColorValues(-1, 1, num);
		case SHOW_SRC:
			return getColorValues(-1, 1, num);
		case SHOW_ICQ:
			return getColorValues(0, 1, num);
		case SHOW_MCC:
		case SHOW_M1:
		case SHOW_M2:
		case SHOW_M3:
			return getColorValues(0, 1, num);
		// case SHOW_M2:
		// return getColorValues(0,1,num);
		default:
			return null;
		}
	}

	/**
	 * over for metric heat map use
	 */
	public double[] getMetricRange(int index) {
		if (index < 0 || index >= METRIC_NAMES.length)
			return null;

		switch (METRIC_NAMES[index]) {
		case SHOW_TOS_LINEAR:
		case SHOW_TOS_LOG2:
			return new double[] { -1, 0, 1 };
		case SHOW_PCC:
			return new double[] { -1, 0, 1 };
		case SHOW_SRC:
			return new double[] { -1, 0, 1 };
		case SHOW_ICQ:
			return new double[] { 0, 0.5, 1 };
		case SHOW_MCC:
		case SHOW_M1:
		case SHOW_M2:
		case SHOW_M3:
			return new double[] { 0, 0.5, 1 };
		// case SHOW_M2:
		// return new double[]{0,0.5,1};
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

	public ResultsTable getMatrix(CellData[] cellC1, CellData[] cellC2, int index) {
		if (cellC1 == null || cellC2 == null)
			return null;
		this.cellC1 = cellC1;
		this.cellC2 = cellC2;
		numOfCell = cellC1.length < cellC2.length ? cellC1.length : cellC2.length;

		ResultsTable rt = new ResultsTable();

		for (int iCell = 0; iCell < numOfCell; iCell++) {
			double[] results = getMatrix(iCell, index);
			rt.incrementCounter();
			rt.addLabel(cellC1[iCell].getCellLabel());
			for (int count1 = 0; count1 < sfsCs[0].length; count1++) {
				for (int count2 = 0; count2 < sfsCs[1].length; count2++) {
					rt.addValue("S1-" + sfsCs[0][count1] + "_S2-" + sfsCs[1][count2],
							results[count2 + sfsCs[1].length * count1]);
				}
			}
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
		if (cellC1 == null || cellC2 == null || sfsCs[0] == null || sfsCs[1] == null) {
			ExceptionHandler.addError(Thread.currentThread(), "Null pointer in cell data and parameter");
			return null;
		}

		double[] result = new double[sfsCs[0].length * sfsCs[1].length];

		int size = cellC1[iCell].length() < cellC2[iCell].length() ? cellC1[iCell].length() : cellC2[iCell].length();

		// sorting is done together to save time
		if (!cellC1[iCell].isSorted())
			cellC1[iCell].sort();

		if (!cellC2[iCell].isSorted())
			cellC2[iCell].sort();

		// ResultsTable test =

		for (int selection1 = 0; selection1 < sfsCs[0].length; selection1++) {
			int positiveA = (int) Math.ceil((size * sfsCs[0][selection1]));
			positiveA = positiveA < size ? positiveA : size;
			CellData trimA = cellC1[iCell].getData(positiveA);

			for (int selection2 = 0; selection2 < sfsCs[1].length; selection2++) {
				int positiveB = (int) Math.ceil((size * sfsCs[1][selection2]));
				positiveB = positiveB < size ? positiveB : size;
				CellData trimB = cellC2[iCell].getData(positiveB);

				if (selection1 == 0 && selection2 == sfsCs[1].length - 1) {
					;
				}
				// System.out.println("s1: "+selection1+", s2:
				// "+selection2+"positiveA: "+positiveA+", positiveB:
				// "+positiveB);

				/*
				 * Vector<Double> metrics = new Vector<Double>();
				 * for(BasicCalculator callbase : callBases){ double[] temp =
				 * callbase.calMetric(trimA, trimB); if(temp!=null){ for(int
				 * i=0;i<temp.length;i++) metrics.add(temp[i]); } } double[]
				 * metricDouble = new double[metrics.size()]; for(int
				 * i=0;i<metricDouble.length;i++) metricDouble[i] =
				 * metrics.get(i);
				 */

				double metricDouble = calMetric(trimA, trimB, show_idx, sfsCs[0][selection1] >= 1.0,
						sfsCs[1][selection2] >= 1.0);

				// store channel 1 selection as row, channel 2 as column
				result[selection2 + selection1 * sfsCs[1].length] = metricDouble;

			}
		}

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
		return METRIC_NAMES.length;
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

}
