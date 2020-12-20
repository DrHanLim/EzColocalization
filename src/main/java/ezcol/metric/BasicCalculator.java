package ezcol.metric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezcol.cell.CellData;
import ezcol.cell.DataSorter;
import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginConstants;

public abstract class BasicCalculator {

	public static final int DEFAULT_FT = PluginConstants.DEFAULT_FT;

	// options determines which metric to calculate
	// the list of options is in MetricResults
	protected int options;
	protected int numOfCell;
	// pixel values of two channels, each element is one cell
	protected CellData[][] cellCs, allCs, costesCs;
	protected Map<Integer, CellData[]>[] ftCs;

	// this should match with defaultColumnNames
	public static final String SHOW_TOS_LINEAR = "TOS(linear)", SHOW_TOS_LOG2 = "TOS(log2)", SHOW_PCC = "PCC",
			SHOW_SRC = "SRCC", SHOW_ICQ = "ICQ", SHOW_M1 = "M1", SHOW_M2 = "M2", SHOW_M3 = "M3",
			SHOW_MCC = "M", SHOW_AVGINT = "Avg.Int.C", SHOW_CUSTOM = "Custom";

	public static final int LAST_METRIC = 8;

	static final String[] DEFAULT_METRIC_ACRONYMS = { SHOW_TOS_LINEAR, SHOW_TOS_LOG2, SHOW_PCC, SHOW_SRC, SHOW_ICQ, SHOW_MCC,
			SHOW_AVGINT, SHOW_CUSTOM };

	// this part include all variables which are calculated here
	// Change in 1.1.0, for every metric, there is a string array with three elements
	// name, interpretation, hyperlink
	static final String[][] DEFAULT_INTPNS = makeIntpns();

	/**
	 * percentages and can be used as markers
	 * MUST use negative numbers here so they do NOT overlap with selected
	 */
	public static final int THOLD_ALL = -1, THOLD_COSTES = -2, THOLD_FT = -3, THOLD_NONE = -4;
	public static final int[] LIST_THOLD_OPTS = { THOLD_ALL, THOLD_COSTES, THOLD_FT };
	protected int[] tholdMetrics;
	protected int[][] tholdFTs;
	// All derived classes should be put here

	public BasicCalculator() {
		cutoffs = new int[] { DEFAULT_FT, DEFAULT_FT };
		numFT2SF();
	}

	public BasicCalculator(int options) {
		this();
		this.options = options;
	}

	public BasicCalculator(BasicCalculator callBase) {
		this.options = callBase.options;
		this.numOfCell = callBase.numOfCell;
		this.cellCs = callBase.cellCs;
		this.allCs = callBase.allCs;
		this.costesCs = callBase.costesCs;
	}

	public static int getThold(int i) {
		if (i < 0 || i >= LIST_THOLD_OPTS.length)
			return THOLD_NONE;
		else
			return LIST_THOLD_OPTS[i];
	}

	/**
	 * get the number of metric names it is also the number of metrics
	 * calculated in the derived class
	 * 
	 * @return
	 */
	public static int getNum() {
		return DEFAULT_METRIC_ACRONYMS.length;
	}

	/**
	 * get the name of the metric
	 * 
	 * @param i
	 *            index of the metric
	 * @return the name of i-th metric
	 */
	public static String getNames(int i) {
		if (i < 0 || i >= DEFAULT_METRIC_ACRONYMS.length)
			return null;
		return DEFAULT_METRIC_ACRONYMS[i];
	}

	/**
	 * get the interpretation of the i-th metric, the extended class should
	 * override this method
	 * 
	 * @param i
	 *            the index
	 * @return
	 */
	public static String[] getIntpn(int i) {
		if (i < 0 || i >= DEFAULT_INTPNS.length)
			return null;
		return DEFAULT_INTPNS[i];
	}
	
	public static String[] getIntpn(int i, int channel) {
		if (i < 0 || i >= DEFAULT_INTPNS.length)
			return null;
		String[] pair =DEFAULT_INTPNS[i];
		return new String[]{pair[0]+" of Channel "+channel, pair[1]};
	}

	public static String[] getAllMetrics() {
		return DEFAULT_METRIC_ACRONYMS.clone();
	}

	private static String[][] makeIntpns() {
		String[][] pairs = new String[LAST_METRIC][2];
		List<String> metricNames = Arrays.asList(DEFAULT_METRIC_ACRONYMS);
		pairs[metricNames.indexOf(SHOW_TOS_LINEAR)] = new String[]{
						  "Threshold Overlap Score linearly rescaled [TOS(linear)]",
				
						  "TOS(linear) is a measure of the overlap of pixels above the threshold "
						+ "for two or three reporter channels, normalized for the expected overlap to occur by chance, "
						+ "which is rescaled so the value is a fraction of the difference between the null hypothesis and "
						+ "the maximum possible colocalization or anticolocalization for the selected thresholds.\n"
						+ "For example, 0.5 is halfway between the null distribution and complete overlap for the selected percentages.\n"
						+ "\n"
						+ "-1 = complete anticolocalization\n" 
						+ "0 = noncolocalization\n"
						+ "1 = complete colocalization\n" 
						, 
						
						"http://bio.biologists.org/content/5/12/1882.long"
						};

		pairs[metricNames.indexOf(SHOW_TOS_LOG2)] = new String[]{
						  "Threshold Overlap Score logarithmically rescaled [TOS(log2)]",
				
						  "TOS(log) is the same as TOS(linear) except the rescaling is logarithmic instead of linear.\n"
						+ "For example: 0.5 is halfway between the null distribution and complete overlap "
						+ "of selected percentages on the log scale.\n"
						+ "\n"
						+ "-1 = complete anticolocalization\n" 
						+ "0 = noncolocalization\n"
						+ "1 = complete colocalization\n" 
						,
						
						  "http://bio.biologists.org/content/5/12/1882.long"
						};

		pairs[metricNames.indexOf(SHOW_PCC)] = new String[]{
						  "Pearson's Correlation Coefficient (PCC)",
				
						  "PCC measures the correlation between the pixel values for two reporter channels.\n"
					    + "\n"
						+ "-1 = complete anticolocalization\n" 
						+ "0 = noncolocalization\n"
						+ "1 = complete colocalization\n" 
						,
						
						  "https://en.wikipedia.org/wiki/Pearson_correlation_coefficient"
						};

		pairs[metricNames.indexOf(SHOW_SRC)] = new String[]{
				  		  "Spearman's Rank Correlation Coefficient (SRCC)",
				
				  		  "SRCC measures the ranked correlation of pixel values for two reporter channels.\n"
				  		+ "\n"
					    + "-1 = complete anticolocalization\n" 
					    + "0 = noncolocalization\n"
						+ "1 = complete colocalization\n" 
						 ,
						
						  "https://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient"
						};

		pairs[metricNames.indexOf(SHOW_ICQ)] = new String[]{
						  "Intensity Correlation Quotient (ICQ)",
						  
						  "ICQ measures the proportion of pixels that "
						+ "are below or above the mean for all of two or three reporter channels.\n"
						+ "\n"
						+ "-0.5 = complete anticolocalization\n" 
						+ "0 = noncolocalization\n"
						+ "0.5 = complete colocalization\n" 
						
						,
						  "http://www.jneurosci.org/content/24/16/4070.long"
						};
		
		pairs[metricNames.indexOf(SHOW_MCC)] = new String[]{
						  "Manders' Colocalization Coefficient (MCC; components M1, M2, and M3)",
						  
						  "MCC measures the intensity weighted proportion of signal which overlaps above the thresholds for two or three channels.\n"
					    + "\n"
						+  "0 = complete anticolocalization \n" 
						+ "1 = complete colocalization \n"
						,
						
						  "https://onlinelibrary.wiley.com/doi/abs/10.1111/j.1365-2818.1993.tb03313.x"};

		pairs[metricNames.indexOf(SHOW_AVGINT)] = new String[]{
						  "Average Signal Intensity (Avg.Int.)",
						  
						  "Selection of the average signal intensity option generates a table that "
					    + "has the average signal intensity of each reporter channel and "
					    + "the physical measurements of each cell in the sample.\n",
						
						  null
						  };
		
		pairs[metricNames.indexOf(SHOW_CUSTOM)] = new String[]{ 
						  "Custom Metric",
						  
						  "Custom Metric allows users to code their own analysis of "
						+ "the selected pixel values using mathematical functions in Java. "
						+ "The calculation of PCC is provided as an example.\n",
						
						  null 
						  };
		
		return pairs;
	}

	public void setOptions(int options) {
		this.options = options;
	}

	public double round(double value, int places) {
		if (places < 0)
			places = 0;// throw new IllegalArgumentException();
		try {
			BigDecimal bd = new BigDecimal(value);
			bd = bd.setScale(places, RoundingMode.HALF_UP);
			return bd.doubleValue();
		} catch (Exception e) {
			return value;
		}
	}

	public double getMedian(float[] array) {
		if (array == null)
			return Double.NaN;
		Arrays.sort(array);
		double median;
		if (array.length % 2 == 0)
			median = ((double) array[array.length / 2] + (double) array[array.length / 2 - 1]) / 2;
		else
			median = (double) array[array.length / 2];
		return median;
	}

	/**
	 * This is double because it only applies to the results
	 * 
	 * @param array
	 * @return the median value of the input array
	 */
	public double getMedian(double[] array) {
		if (array == null || array.length <= 0)
			return Double.NaN;
		Arrays.sort(array);
		double median;
		if (array.length % 2 == 0)
			median = (array[array.length / 2] + array[array.length / 2 - 1]) / 2;
		else
			median = array[array.length / 2];
		return median;
	}

	public float[] toFloatArray(double[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (float) arr[i];
		}
		return ret;
	}

	/**
	 * <link>http://stackoverflow.com/questions/15725370/write-a-mode-method-in-
	 * java-to-find-the-most-frequently-occurring-element-in-an</link>
	 * 
	 * @author codemania23
	 * @param array
	 * @return
	 */
	public double getMode(double[] array) {
		if (array == null || array.length <= 0)
			return Double.NaN;
		Map<Double, Integer> hm = new HashMap<Double, Integer>();
		double max = 1, temp = array[0];
		for (int i = 0; i < array.length; i++) {
			if (hm.get(array[i]) != null) {
				int count = hm.get(array[i]);
				count = count + 1;
				hm.put(array[i], count);
				if (count > max) {
					max = count;
					temp = array[i];
				}
			} else {
				hm.put(array[i], 1);
			}
		}
		return temp;
	}

	/**
	 * calculate the mean of the array
	 * 
	 * @param array
	 *            input array
	 * @return mean
	 */
	public double getMean(double[] array) {
		if (array == null)
			return Double.NaN;
		double result = 0.0;
		int size = array.length;
		for (int i = 0; i < size; i++)
			result += array[i];
		return result / (double) size;
	}

	/**
	 * calculate the mean of the array
	 * 
	 * @param array
	 *            input array
	 * @return mean
	 */
	public double getMean(float[] array) {
		if (array == null)
			return Double.NaN;
		double result = 0.0;
		int size = array.length;
		for (int i = 0; i < size; i++)
			result += array[i];
		return result / (double) size;
	}

	/**
	 * calculate standard deviation of the array
	 * 
	 * @param a
	 *            input array
	 * @param aMean
	 *            mean of the input array for given size
	 * @return standard deviation
	 */
	public double getSTD(float[] a, double aMean) {
		if (a == null)
			return Double.NaN;
		double[] b = new double[a.length];
		for (int i = 0; i < a.length; i++)
			b[i] = a[i] * a[i];

		return Math.sqrt(getMean(b) - aMean * aMean);
	}

	/**
	 * calculate standard deviation of the array
	 * 
	 * @param a
	 *            input array
	 * @param aMean
	 *            mean of the input array for given size
	 * @return standard deviation
	 */
	public double getSTD(double[] a, double aMean) {
		if (a == null)
			return Double.NaN;
		double[] b = a.clone();
		for (int i = 0; i < a.length; i++)
			b[i] *= b[i];
		return Math.sqrt(getMean(b) - aMean * aMean);
	}

	/**
	 * calculate the sum of the array
	 * 
	 * @param a
	 *            input array
	 * @return sum
	 */
	public double getSum(float[] a) {
		if (a == null)
			return Double.NaN;
		double result = 0.0;
		for (int i = 0; i < a.length; i++)
			result += a[i];
		return result;
	}

	/**
	 * calculate the sum of the array above the threshold
	 * 
	 * @param a
	 *            input array
	 * @param threshold
	 * @return sum of elements above the threshold
	 */
	public double getSum(float[] a, float threshold) {
		if (a == null)
			return Double.NaN;
		double result = 0.0;
		for (int i = 0; i < a.length; i++)
			if (a[i] > threshold)
				result += a[i];
		return result;
	}

	/**
	 * Version 1.2 add: getMatrix
	 * 
	 */
	protected int[] cutoffs;
	protected double[][] sfsCs;

	public BasicCalculator(int options, int[] cutoffs) {
		this(cutoffs);
		this.options = options;
	}

	public BasicCalculator(int[] cutoffs) {
		if (cutoffs != null && cutoffs.length >= 2)
			this.cutoffs = cutoffs.clone();
		else
			this.cutoffs = new int[] { 9, 9 };
		numFT2SF();
	}

	public double[] getSFs(int channel, boolean reverse) {
		if (sfsCs == null)
			return null;
		else if (channel < sfsCs.length) {
			if (reverse) {
				double[] temp = sfsCs[channel].clone();
				for (int i = 0; i < temp.length; i++)
					temp[i] = sfsCs[channel][temp.length - 1 - i];
				return temp;
			} else
				return sfsCs[channel].clone();
		}
		return null;
	}

	/*
	 * get the selected percentages that will be used in the mTOS Update: change
	 * from number of selected percentages to fraction itself disregard the
	 * extra fraction
	 */
	protected void numFT2SF() {
		sfsCs = new double[cutoffs.length][];
		for (int i = 0; i < cutoffs.length; i++) {
			sfsCs[i] = new double[ft2length(cutoffs[i])];
			for (int place = 0; place < sfsCs[i].length; place++)
				if ((place + 1) * cutoffs[i] < 100.0)
					sfsCs[i][place] = (place + 1) * cutoffs[i] / 100.0;
				else
					sfsCs[i][place] = 1.0;
		}

	}

	/*
	 * get the selected fractions that will be used in the matrix heat map
	 * Update: change from number of selected percentages to fraction itself
	 * disregard the extra fraction
	 * 
	 * @return not the fraction but the percentage
	 */
	public int[] numFT2SF(int fraction) {
		if (fraction < 0)
			return null;
		int[] tempsf = new int[ft2length(fraction)];
		for (int place = 1; place <= tempsf.length; place++)
			if (place * fraction < 100)
				tempsf[place - 1] = place * fraction;
			else
				tempsf[place - 1] = 100;
		// tempsf[place-1] = round(place*fraction,SFdecimal);
		return tempsf;
	}

	public static int ft2length(int ft) {
		// return 100/ft-1+(100%ft==0?0:1);
		return 100 / ft + (100 % ft == 0 ? 0 : 1);
	}

	/*
	 * public void setSFDecimal(int places){ SFdecimal = places; }
	 */

	public void setCutoff(int[] icut) {
		if (icut != null && icut.length >= 2) {
			cutoffs = icut.clone();
			numFT2SF();
		}
	}

	protected boolean prepCellData(CellData[] cellC1, CellData[] cellC2, Object obj) {
		return prepCellData(new CellData[][] { cellC1, cellC2 }, obj);
	}

	@SuppressWarnings("unchecked")
	protected boolean prepCellData(CellData[][] cellCs, Object obj) {
		int[] threshold = null;

		CellData[] celliC = null;
		for (int iChannel = 0; iChannel < cellCs.length; iChannel++) {
			if (cellCs[iChannel] == null)
				return false;
			else if (celliC != null && cellCs[iChannel].length != celliC.length)
				return false;
			else
				celliC = cellCs[iChannel];
		}

		try {
			threshold = (int[]) obj;
		} catch (Exception e) {

			if (threshold == null || threshold.length <= 0)
				return false;
			// e.printStackTrace();
		}

		this.numOfCell = celliC.length;

		int iMetric = 0;

		tholdMetrics = new int[threshold.length];
		tholdFTs = new int[threshold.length][cellCs.length];

		for (int iThold = 0; iThold < threshold.length; iThold++) {

			switch (threshold[iThold]) {
			case THOLD_ALL:
				if (allCs == null)
					allCs = new CellData[cellCs.length][];
				for (int iChannel = 0; iChannel < cellCs.length; iChannel++) {
					if (cellCs[iChannel] == null || allCs[iChannel] != null)
						continue;
					allCs[iChannel] = cellCs[iChannel].clone();
				}
				break;
			case THOLD_COSTES:
				if (costesCs == null)
					costesCs = new CellData[cellCs.length][];
				// Higher dimensional Costes' method can be added in the latter
				// version
				if (costesCs[0] != null || costesCs[1] != null)
					break;
				costesCs[0] = new CellData[cellCs[0].length];
				costesCs[1] = new CellData[cellCs[1].length];
				for (int i = 0; i < numOfCell; i++) {
					CostesThreshold costesThold = new CostesThreshold();
					float[] dataA = cellCs[0][i].getData();
					float[] dataB = cellCs[1][i].getData();
					double[] costes = costesThold.getCostesThrd(dataA, dataB);
					int length = dataA.length < dataB.length ? dataA.length : dataB.length;
					costesCs[0][i] = new CellData(cellCs[0][i].length());
					costesCs[1][i] = new CellData(cellCs[1][i].length());
					for (int ip = 0; ip < length; ip++) {
						if (dataA[ip] > costes[0])
							costesCs[0][i].setData(dataA[ip], cellCs[0][i].getPixelX(ip), cellCs[0][i].getPixelY(ip),
									ip);
						else
							costesCs[0][i].setData(Float.NaN, cellCs[0][i].getPixelX(ip), cellCs[0][i].getPixelY(ip),
									ip);

						if (dataB[ip] > costes[1])
							costesCs[1][i].setData(dataB[ip], cellCs[1][i].getPixelX(ip), cellCs[1][i].getPixelY(ip),
									ip);
						else
							costesCs[1][i].setData(Float.NaN, cellCs[1][i].getPixelX(ip), cellCs[1][i].getPixelY(ip),
									ip);
					}
					
					costesCs[0][i].sort();
					costesCs[1][i].sort();
				}
				break;

			case THOLD_FT:
				if (ftCs == null)
					ftCs = new HashMap[cellCs.length];

				int ft = 0;
				
				for (int iChannel = 0; iChannel < cellCs.length; iChannel++) {
					try {
						ft = threshold[iThold + iChannel + 1];
					} catch (Exception e) {
						return false;
					}

					// Record the ft immediately
					tholdFTs[iMetric][iChannel] = ft;
					

					if (ftCs[iChannel] == null)
						ftCs[iChannel] = new HashMap<Integer, CellData[]>();
					if (ftCs[iChannel].get(ft) != null)
						continue;

					CellData[] ftC = new CellData[cellCs[iChannel].length];

					for (int i = 0; i < numOfCell; i++) {

						
						int tholdA = (int) Math.ceil(cellCs[iChannel][i].length() * (double) ft / 100.0);

						double[] rankA = cellCs[iChannel][i].getRank();
						
						int length = rankA.length;
						ftC[i] = new CellData(cellCs[iChannel][i].length());

						for (int ip = 0; ip < length; ip++) {
							if (rankA[ip] <= tholdA)
								ftC[i].setData(cellCs[iChannel][i].getPixel(ip), cellCs[iChannel][i].getPixelX(ip),
										cellCs[iChannel][i].getPixelY(ip), ip);
							else
								ftC[i].setData(Float.NaN, cellCs[iChannel][i].getPixelX(ip),
										cellCs[iChannel][i].getPixelY(ip), ip);

						}
						ftC[i].sort();
						ftCs[iChannel].put(ft, ftC);
					}
				}
				break;
			default:
				break;
			}
			// if threshold[iThold] is a threshold marker, record it
			// otherwise, pass
			switch (threshold[iThold]) {
			case THOLD_ALL:
			case THOLD_COSTES:
			case THOLD_FT:
			case THOLD_NONE:
				tholdMetrics[iMetric++] = threshold[iThold];
			}
		}
		return true;
	}

	/*
	 * What needs to be calculated and universally defined must be stated here
	 */

	protected int TOSdecimal = 3;

	public void setDecimal(int places) {
		TOSdecimal = places;
	}

	double[] getTOS(CellData dataA, CellData dataB) {

		if (dataA == null || dataB == null)
			return new double[] { Double.NaN, Double.NaN };

		final float[] a = dataA.getData();
		final float[] b = dataB.getData();
		if (a == null || b == null || a.length != b.length || a.length == 0) {
			ExceptionHandler.addError(Thread.currentThread(), "Null pointer in cell data and parameter");
			return new double[] { Double.NaN, Double.NaN };
		}
		int size = a.length;

		double A = 0.0, AandB = 0.0, B = 0.0;
		for (int idx = 0; idx < size; idx++) {
			if (!Double.isNaN(a[idx]))
				A++;
			if (!Double.isNaN(b[idx]))
				B++;
			if ((!Double.isNaN(a[idx])) && (!Double.isNaN(b[idx])))
				AandB++;
		}

		if (A == 0 || B == 0) {
			ExceptionHandler.addError(Thread.currentThread(), "Some cells are either too small or have equal pixel values");
		}

		double exp1 = B / size, exp2 = A / size;

		double poratioselected = AandB / size / exp1 / exp2;

		double maxPOratio = 1 / Math.max(exp1, exp2);
		double minPOratio;
		double tos_linear = Double.NaN, tos_log2 = Double.NaN;
		if (exp1 + exp2 <= 1)
			minPOratio = 0;
		else
			minPOratio = (exp1 + exp2 - 1) / (exp1 * exp2);

		// tos_linear
		if (poratioselected == 1) {
			if (maxPOratio == 1 || minPOratio == 1)
				tos_linear = Double.NaN;
			else
				tos_linear = 0;
		} else if (poratioselected < 1) {
			tos_linear = (1 - poratioselected) / (minPOratio - 1);
		} else if (poratioselected > 1) {
			tos_linear = (poratioselected - 1) / (maxPOratio - 1);
		} else if (!Double.isNaN(poratioselected)) {
			ExceptionHandler.addError(Thread.currentThread(), "Error in calculating linear TOS");
			tos_linear = Double.NaN;
		} else {
			tos_linear = Double.NaN;
		}

		// tos_log2
		if (poratioselected == 1) {
			if (maxPOratio == 1 || minPOratio == 1)
				tos_log2 = Double.NaN;
			else
				tos_log2 = 0;
		} else if (poratioselected > maxPOratio) {
			tos_log2 = 1;
		} else if (poratioselected < minPOratio) {
			tos_log2 = -1;
		} else if (exp1 != 0.5 && exp2 != 0.5) {
			double log2 = Math.log(2);
			double m = log2 / Math.log((maxPOratio - 1) / (1 - minPOratio));
			double n = (maxPOratio + minPOratio - 2) / ((maxPOratio - 1) * (1 - minPOratio));
			tos_log2 = m * Math.log(n * (poratioselected - 1) + 1) / log2;

			if (Double.isNaN(tos_log2)) {
				ExceptionHandler.addError(Thread.currentThread(), "Error in calculationg log TOS");
			}
		} else if (exp1 == 0.5 || exp2 == 0.5) {
			tos_log2 = 2 / (maxPOratio - minPOratio) * (poratioselected - 1);
		} else if (!Double.isNaN(poratioselected)) {
			ExceptionHandler.addError(Thread.currentThread(), "Error in calculating log TOS");
			tos_log2 = Double.NaN;
		}

		tos_linear = round(tos_linear, TOSdecimal);
		if (tos_linear > 1)
			tos_linear = 1;
		if (tos_linear < -1)
			tos_linear = -1;

		tos_log2 = round(tos_log2, TOSdecimal);
		if (tos_log2 > 1)
			tos_log2 = 1;
		if (tos_log2 < -1)
			tos_log2 = -1;

		return new double[] { tos_linear, tos_log2 };
	}

	double getPCC(CellData dataA, CellData dataB) {

		if (dataA == null || dataB == null)
			return Double.NaN;

		float[] ta = dataA.getData();
		float[] tb = dataB.getData();

		if (ta == null || tb == null || ta.length != tb.length)
			return Double.NaN;

		float[] a = ta;
		float[] b = tb;
		int num = 0;
		for (int i = 0; i < ta.length; i++) {
			if (Float.isNaN(ta[i]) || Float.isNaN(tb[i]))
				continue;
			a[num] = ta[i];
			b[num] = tb[i];
			num++;
		}
		a = Arrays.copyOfRange(a, 0, num);
		b = Arrays.copyOfRange(b, 0, num);
		double aMean = getMean(a);
		double bMean = getMean(b);

		float[] c = a.clone();
		for (int i = 0; i < a.length; i++)
			c[i] *= b[i];
		return (getMean(c) - aMean * bMean) / (getSTD(a, aMean) * getSTD(b, bMean));
	}

	double getSRCC(CellData dataA, CellData dataB) {

		if (dataA == null || dataB == null)
			return Double.NaN;

		if (!dataA.isSorted())
			dataA.sort();
		if (!dataB.isSorted())
			dataB.sort();

		// Sorting is done together to increase performance
		// The sorted indexes and ranks can be found in CellData
		double[] ranktA = dataA.getRank();
		double[] ranktB = dataB.getRank();

		if (ranktA == null || ranktB == null || ranktA.length != ranktB.length)
			return Double.NaN;

		double[] rankA = ranktA;
		double[] rankB = ranktB;
		int num = 0;
		for (int i = 0; i < ranktA.length; i++) {
			if (Double.isNaN(ranktA[i]) || Double.isNaN(ranktB[i]))
				continue;
			rankA[num] = ranktA[i];
			rankB[num] = ranktB[i];
			num++;
		}
		rankA = Arrays.copyOfRange(rankA, 0, num);
		rankB = Arrays.copyOfRange(rankB, 0, num);

		// we use double instead of integer here to avoid out of range
		// because the number of pixels in one image could be very large

		double aMean = getMean(rankA);
		double bMean = getMean(rankB);
		double[] c = rankA.clone();
		for (int i = 0; i < rankA.length; i++)
			c[i] *= rankB[i];
		return (getMean(c) - aMean * bMean) / (getSTD(rankA, aMean) * getSTD(rankB, bMean));

	}

	double getICQ(CellData dataA, CellData dataB) {

		if (dataA == null || dataB == null)
			return Double.NaN;

		float[] ta = dataA.getData();
		float[] tb = dataB.getData();

		if (ta == null || tb == null || ta.length != tb.length)
			return Double.NaN;

		float[] a = ta;
		float[] b = tb;
		int num = 0;
		for (int i = 0; i < ta.length; i++) {
			if (Float.isNaN(ta[i]) || Float.isNaN(tb[i]))
				continue;
			a[num] = ta[i];
			b[num] = tb[i];
			num++;
		}
		a = Arrays.copyOfRange(a, 0, num);
		b = Arrays.copyOfRange(b, 0, num);

		double aMean = getMean(a);
		double bMean = getMean(b);
		double ICQ = 0;
		for (int i = 0; i < a.length; i++) {
			if ((a[i] > aMean && b[i] > bMean) || (a[i] < aMean && b[i] < bMean))
				ICQ++;
		}
		ICQ = ICQ / a.length - 0.5;

		return ICQ;
	}

	/**
	 * Please use unprocessed data as inputs
	 * 
	 * @param dataA
	 * @param dataB
	 * @return
	 */
	double[] getMCC(CellData dataA, CellData dataB) {

		if (dataA == null || dataB == null)
			return new double[] { Double.NaN, Double.NaN };

		float[] a = dataA.getData();
		float[] b = dataB.getData();

		if (a == null || b == null || a.length != b.length)
			return new double[] { Double.NaN, Double.NaN };

		double[] AandB = getSum(a, b);

		double[] result = new double[2];
		result[0] = AandB[0] / AandB[2];
		result[1] = AandB[1] / AandB[3];
		return result;
	}

	/**
	 * calculate the sums of the arrays above the thresholds in each array and
	 * in both arrays
	 * 
	 * @see <code>getSum(float[] ,float[] , double, double)</code>
	 * @param a
	 *            input array 1
	 * @param b
	 *            input array 2
	 * @param thresholdA
	 *            threshold of array a
	 * @param thresholdB
	 *            threshold of array b
	 * @return an array of four values as [0].sum of elements in a that is not
	 *         NaN in a & not NaN in b [1].sum of elements in b that is not NaN
	 *         in a & not NaN in b [2].sum of elements in a that is not NaN in a
	 *         [3].sum of elements in b that is not NaN in b
	 */
	public double[] getSum(float[] a, float[] b) {
		if (a == null || b == null || a.length != b.length)
			return new double[] { Double.NaN, Double.NaN, Double.NaN, Double.NaN };

		double[] results = new double[4];
		for (int i = 0; i < a.length; i++) {
			if (!Float.isNaN(a[i]) && !Float.isNaN(b[i])) {
				results[0] += a[i];
				results[1] += b[i];
			}
			if (!Float.isNaN(a[i]))
				results[2] += a[i];
			if (!Float.isNaN(b[i]))
				results[3] += b[i];
		}
		return results;
	}

	public double[][] transpose(double[][] value) {
		if (value == null || value.length <= 0)
			return null;
		double[][] result = new double[value[0].length][value.length];
		for (int i = 0; i < value.length; i++) {
			if (value[i].length != value[0].length)
				return null;
			for (int j = 0; j < value[i].length; j++)
				result[j][i] = value[i][j];
		}
		return result;
	}

	double getPCC(float[]... data) {

		if (data == null || data.length == 0)
			return Double.NaN;

		float[][] realData = data;

		int numData = 0;
		runCells: for (int iData = 0; iData < data[0].length; iData++) {
			for (int iChannel = 0; iChannel < data.length; iChannel++) {
				if (Float.isNaN(data[iChannel][iData])) {
					continue runCells;
				}
			}
			for (int iChannel = 0; iChannel < data.length; iChannel++) {
				realData[iChannel][numData] = data[iChannel][iData];
			}
			numData++;
		}

		double[] means = new double[data.length];
		for (int iChannel = 0; iChannel < data.length; iChannel++) {
			realData[iChannel] = Arrays.copyOfRange(realData[iChannel], 0, numData);
			means[iChannel] = getMean(realData[iChannel]);
		}

		double expProd = 0.0;
		for (int iData = 0; iData < numData; iData++) {
			double temp = 1.0;
			for (int iChannel = 0; iChannel < realData.length; iChannel++) {
				temp *= (realData[iChannel][iData] - means[iChannel]);
			}
			expProd += temp;
		}
		expProd /= numData;

		double stdProd = 1.0;
		for (int iChannel = 0; iChannel < realData.length; iChannel++) {
			stdProd *= getSTD(realData[iChannel], means[iChannel]);
		}

		return expProd / stdProd;
	}

	double getSRCC(double[]... ranks) {

		if (ranks == null || ranks.length == 0)
			return Double.NaN;

		double[][] realData = ranks;

		int numData = 0;
		runCells: for (int iData = 0; iData < ranks[0].length; iData++) {
			for (int iChannel = 0; iChannel < ranks.length; iChannel++) {
				if (Double.isNaN(ranks[iChannel][iData])) {
					continue runCells;
				}
			}
			for (int iChannel = 0; iChannel < ranks.length; iChannel++) {
				realData[iChannel][numData] = ranks[iChannel][iData];
			}
			numData++;
		}

		double[] means = new double[ranks.length];
		for (int iChannel = 0; iChannel < ranks.length; iChannel++) {
			realData[iChannel] = Arrays.copyOfRange(realData[iChannel], 0, numData);
			means[iChannel] = getMean(realData[iChannel]);
		}

		double expProd = 0.0;
		for (int iData = 0; iData < numData; iData++) {
			double temp = 1.0;
			for (int iChannel = 0; iChannel < realData.length; iChannel++) {
				temp *= (realData[iChannel][iData] - means[iChannel]);
			}
			expProd += temp;
		}
		expProd /= numData;

		double stdProd = 1.0;
		for (int iChannel = 0; iChannel < realData.length; iChannel++) {
			stdProd *= getSTD(realData[iChannel], means[iChannel]);
		}

		return expProd / stdProd;

	}

	double getICQ(float[]... data) {

		if (data == null || data.length == 0)
			return Double.NaN;

		float[][] realData = data;

		int numData = 0;
		runCells: for (int iData = 0; iData < data[0].length; iData++) {
			for (int iChannel = 0; iChannel < data.length; iChannel++) {
				if (Float.isNaN(data[iChannel][iData])) {
					continue runCells;
				}
			}
			for (int iChannel = 0; iChannel < data.length; iChannel++) {
				realData[iChannel][numData] = data[iChannel][iData];
			}
			numData++;
		}

		double[] means = new double[data.length];
		for (int iChannel = 0; iChannel < data.length; iChannel++) {
			realData[iChannel] = Arrays.copyOfRange(realData[iChannel], 0, numData);
			means[iChannel] = getMean(realData[iChannel]);
		}

		double ICQ = 0;
		for (int iData = 0; iData < numData; iData++) {
			Boolean toAdd = null;
			for (int iChannel = 0; iChannel < data.length; iChannel++) {
				if (realData[iChannel][iData] > means[iChannel]) {
					if (toAdd == null) {
						toAdd = true;
						continue;
					} else if (!toAdd) {
						toAdd = null;
						break;
					} else
						continue;
				} else if (realData[iChannel][iData] < means[iChannel]) {
					if (toAdd == null) {
						toAdd = false;
						continue;
					} else if (toAdd) {
						toAdd = null;
						break;
					} else
						continue;
				} else {
					toAdd = null;
					break;
				}
			}
			if (toAdd != null)
				ICQ++;
		}
		ICQ = ICQ / numData - 0.5;

		return ICQ;
	}

	double[] getMCC(float[]... data) {

		if (data == null || data.length == 0)
			return null;

		double[] overlaps = new double[data.length];
		double[] denominators = new double[data.length];

		for (int iData = 0; iData < data[0].length; iData++) {
			boolean isOverlap = true;
			for (int iChannel = 0; iChannel < data.length; iChannel++) {
				if (Float.isNaN(data[iChannel][iData])) {
					isOverlap = false;
				} else {
					denominators[iChannel] += data[iChannel][iData];
				}
			}
			if (isOverlap) {
				for (int iChannel = 0; iChannel < data.length; iChannel++) {
					overlaps[iChannel] += data[iChannel][iData];
				}
			}
		}
		for (int iChannel = 0; iChannel < data.length; iChannel++) {
			overlaps[iChannel] /= denominators[iChannel];
		}

		return overlaps;
	}

	double[] getTOS(float[]... data) {

		if (data == null || data.length == 0)
			return null;

		int size = data[0].length;

		double[] sumOnes = new double[data.length];
		double sumAll = 0.0;
		for (int idx = 0; idx < size; idx++) {
			boolean isAll = true;
			for (int iChannel = 0; iChannel < data.length; iChannel++) {
				if (!Double.isNaN(data[iChannel][idx]))
					sumOnes[iChannel]++;
				else
					isAll = false;
			}
			if (isAll)
				sumAll++;
		}

		double[] exps = new double[data.length];

		for (int iChannel = 0; iChannel < data.length; iChannel++) {
			exps[iChannel] = sumOnes[iChannel] / size;
		}

		double prodExps = prodExp(exps, -1);

		double aoRatioSelected = sumAll / size / prodExps;
		
		double maxAOratio = findMin(exps) / prodExps;
		double minAOratio;
		double tos_linear = Double.NaN, tos_log2 = Double.NaN;
		if (noOverlap(exps))
			minAOratio = 0;
		else
			minAOratio = (getSum(exps) - data.length + 1) / prodExps;

		// tos_linear
		if (aoRatioSelected == 1) {
			if (maxAOratio == 1 || minAOratio == 1)
				tos_linear = Double.NaN;
			else
				tos_linear = 0;
		} else if (aoRatioSelected < 1) {
			tos_linear = (1 - aoRatioSelected) / (minAOratio - 1);
		} else if (aoRatioSelected > 1) {
			tos_linear = (aoRatioSelected - 1) / (maxAOratio - 1);
		} else if (!Double.isNaN(aoRatioSelected)) {
			ExceptionHandler.addError(Thread.currentThread(), "Error in calculating linear TOS");
			tos_linear = Double.NaN;
		} else {
			tos_linear = Double.NaN;
		}
		

		// tos_log2
		if (aoRatioSelected == 1) {
			if (maxAOratio == 1 || minAOratio == 1)
				tos_log2 = Double.NaN;
			else
				tos_log2 = 0;
		} else if (aoRatioSelected > maxAOratio) {
			tos_log2 = 1;
		} else if (aoRatioSelected < minAOratio) {
			tos_log2 = -1;
		} else if (maxAOratio + minAOratio != 2) {
			double log2 = Math.log(2);
			double m = log2 / Math.log((maxAOratio - 1) / (1 - minAOratio));
			double n = (maxAOratio + minAOratio - 2) / ((maxAOratio - 1) * (1 - minAOratio));
			tos_log2 = m * Math.log(n * (aoRatioSelected - 1) + 1) / log2;
		} else if (maxAOratio + minAOratio == 2) {
			tos_log2 = 2 / (maxAOratio - minAOratio) * (aoRatioSelected - 1);
		} else if (!Double.isNaN(aoRatioSelected)) {
			ExceptionHandler.addError(Thread.currentThread(), "Error in calculating log TOS");
			tos_log2 = Double.NaN;
		}

		tos_linear = round(tos_linear, TOSdecimal);
		if (tos_linear > 1)
			tos_linear = 1;
		if (tos_linear < -1)
			tos_linear = -1;

		tos_log2 = round(tos_log2, TOSdecimal);
		if (tos_log2 > 1)
			tos_log2 = 1;
		if (tos_log2 < -1)
			tos_log2 = -1;
		
		return new double[] { tos_linear, tos_log2 };
	}

	private double getSum(double[] a) {
		if (a == null)
			return Double.NaN;
		double result = 0.0;
		for (int i = 0; i < a.length; i++)
			result += a[i];
		return result;
	}

	private boolean noOverlap(double[] exp) {
		DataSorter expSorter = new DataSorter();
		expSorter.sort(exp);
		double[] sortedExp = (double[]) expSorter.getResult();
		for (int i = 1; i < sortedExp.length; i++) {
			if (sumExp(sortedExp, sortedExp.length - 1 - i, sortedExp.length - 1) < i) {
				return true;
			}
		}
		return false;
	}

	private double findMax(double[] data) {
		double result = Double.NEGATIVE_INFINITY;
		for (double d : data) {
			if (result < d)
				result = d;
		}
		return result;
	}

	private double findMin(double[] data) {
		double result = Double.POSITIVE_INFINITY;
		for (double d : data) {
			if (result > d)
				result = d;
		}
		return result;
	}

	private double sumExp(double[] data, int startIdx, int endIdx) {
		double result = 0.0;
		if (data == null || data.length == 0)
			return Double.NaN;
		if (startIdx < 0 || startIdx >= data.length || endIdx < 0 || endIdx >= data.length || endIdx < startIdx) {
			for (double d : data)
				result += d;
			return result;
		}
		for (int i = startIdx; i <= endIdx; i++) {
			result += data[i];
		}
		return result;
	}

	private double prodExp(double[] data, int exp) {
		double result = 1.0;
		if (data == null || data.length == 0)
			return Double.NaN;
		if (exp < 0 || exp >= data.length) {
			for (double d : data)
				result *= d;
			return result;
		}
		for (int i = 0; i < data.length; i++) {
			if (i == exp)
				continue;
			else
				result *= data[i];
		}
		return result;
	}
}

