package ezcol.metric;

import java.util.Arrays;

import ezcol.cell.CellData;
import ezcol.debug.Debugger;
import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginConstants;
import ezcol.main.PluginStatic;
import javafx.util.Pair;

public class MetricCalculator extends BasicCalculator {

	public static final int DO_TOS = PluginConstants.RUN_TOSHM, DO_PCC = PluginConstants.DO_PCC,
			DO_SRC = PluginConstants.DO_SRC, DO_MCC = PluginConstants.DO_MCC, DO_ICQ = PluginConstants.DO_ICQ,
			DO_CUSTOM = PluginConstants.DO_CUSTOM, DO_AVGINT = PluginConstants.DO_AVGINT;

	// results
	private double[] pcc;
	private double[][] avgINT;
	private double[] srcc;
	private double[][] custom;
	private double[][] mcc;
	private double[] icq;
	// included at 071117 because TOS is just a regular metric now;
	// linear first, log2 second
	private double[][] tos;
	// handle custom function
	private StringCompiler customCompiler;
	// private CostesThreshold costesThrd;

	public MetricCalculator() {
		this.options = 0;
	};

	public MetricCalculator(int options) {
		this.options = options;
	}

	public MetricCalculator(int options, StringCompiler customCompiler) {
		this.options = options;
		this.customCompiler = customCompiler;
	}

	public MetricCalculator(BasicCalculator callBase) {
		this.options = callBase.options;
		this.numOfCell = callBase.numOfCell;
		this.ftCs = callBase.ftCs;
		this.allCs = callBase.allCs;
		this.costesCs = callBase.costesCs;
	}

	/**
	 * @param options
	 *            indicates which metric to calculate
	 */
	public void setOptions(int options) {
		this.options = options;
	}

	/**
	 * calculating metrics for input CellData
	 * 
	 * @param cellC1
	 *            input CellData array
	 * @param cellC2
	 *            input CellData array
	 * @param obj
	 *            additional input for more information £¨e.x. numOfCell£©
	 * @return true if the calculation is successful, false if it is interrupted
	 */
	public boolean calMetrics(CellData[][] cellCs, Object obj) {

		if (!prepCellData(cellCs, obj))
			return false;

		if ((options & DO_TOS) != 0)
			tos = new double[numOfCell][2];
		else
			tos = null;

		if ((options & DO_PCC) != 0)
			pcc = new double[numOfCell];
		else
			pcc = null;

		if ((options & DO_SRC) != 0)
			srcc = new double[numOfCell];
		else
			srcc = null;

		if ((options & DO_AVGINT) != 0)
			avgINT = new double[numOfCell][cellCs.length];
		else
			avgINT = null;

		// DO NOT USE COSTES THRESHOLD
		// Because the new version process the celldata first
		// So there is no need to apply costes again
		if ((options & DO_MCC) != 0) {
			mcc = new double[numOfCell][cellCs.length];
			// costesThrd = new CostesThreshold();
		} else {
			mcc = null;
			// costesThrd = null;
		}

		if ((options & DO_ICQ) != 0)
			icq = new double[numOfCell];
		else
			icq = null;

		if ((options & DO_CUSTOM) != 0 && (customCompiler != null))
			custom = new double[numOfCell][];
		else
			custom = null;

		boolean hasNaN = false;

		for (int iCell = 0; iCell < numOfCell; iCell++) {
			if (tos != null) {
				boolean noThold = false;
				switch (tholdMetrics[PluginStatic.TOS]) {
				case THOLD_ALL:
					this.cellCs = allCs;
					break;
				case THOLD_COSTES:
					this.cellCs = costesCs;
					break;
				case THOLD_FT:
					this.cellCs = new CellData[ftCs.length][];
					for (int iChannel = 0; iChannel < this.cellCs.length; iChannel++) {
						this.cellCs[iChannel] = ftCs[iChannel].get(tholdFTs[PluginStatic.TOS][iChannel]);
						if (tholdFTs[PluginStatic.TOS][iChannel] == 100)
							noThold = true;
					}
					break;
				default:
					break;
				}

				tos[iCell] = getTOS(getCellPixels(iCell));

				if (Double.isNaN(tos[iCell][0]) || Double.isNaN(tos[iCell][1])) {
					if (noThold) {
						hasNaN = true;
						ExceptionHandler.addWarning(Thread.currentThread(), cellCs[0][iCell].getCellLabel() + " (size:"
								+ cellCs[0][iCell].length() + ") returns a TOS value(s) of NaN");

					}
					noThold = false;
				}

			}

			if (pcc != null) {
				switch (tholdMetrics[PluginStatic.PCC]) {
				case THOLD_ALL:
					this.cellCs = allCs;
					break;
				case THOLD_COSTES:
					this.cellCs = costesCs;
					break;
				case THOLD_FT:
					this.cellCs = new CellData[ftCs.length][];
					for (int i = 0; i < this.cellCs.length; i++)
						this.cellCs[i] = ftCs[i].get(tholdFTs[PluginStatic.PCC][i]);
					break;
				default:
					break;
				}
				pcc[iCell] = getPCC(getCellPixels(iCell));

				if (Double.isNaN(pcc[iCell])) {
					hasNaN = true;
					ExceptionHandler.addWarning(Thread.currentThread(), cellCs[0][iCell].getLabel() + " (size:"
							+ cellCs[0][iCell].length() + ") returns a PCC value of NaN");
				}
			}

			if (srcc != null) {
				switch (tholdMetrics[PluginStatic.SRCC]) {
				case THOLD_ALL:
					this.cellCs = allCs;
					break;
				case THOLD_COSTES:
					this.cellCs = costesCs;
					break;
				case THOLD_FT:
					this.cellCs = new CellData[ftCs.length][];
					for (int i = 0; i < this.cellCs.length; i++)
						this.cellCs[i] = ftCs[i].get(tholdFTs[PluginStatic.SRCC][i]);
					break;
				default:
					break;
				}
				srcc[iCell] = getSRCC(getCellRanks(iCell));

				if (Double.isNaN(srcc[iCell])) {
					hasNaN = true;
					ExceptionHandler.addWarning(Thread.currentThread(), cellCs[0][iCell].getLabel() + " (size:"
							+ cellCs[0][iCell].length() + ") returns a SRCC value of NaN");
				}
			}

			if (mcc != null) {
				switch (tholdMetrics[PluginStatic.MCC]) {
				case THOLD_ALL:
					this.cellCs = allCs;
					break;
				case THOLD_COSTES:
					this.cellCs = costesCs;
					break;
				case THOLD_FT:
					this.cellCs = new CellData[ftCs.length][];
					for (int i = 0; i < this.cellCs.length; i++)
						this.cellCs[i] = ftCs[i].get(tholdFTs[PluginStatic.MCC][i]);
					break;
				default:
					break;
				}
				mcc[iCell] = getMCC(getCellPixels(iCell));

				if (Double.isNaN(mcc[iCell][0]) || Double.isNaN(mcc[iCell][1])) {
					hasNaN = true;
					ExceptionHandler.addWarning(Thread.currentThread(), cellCs[0][iCell].getLabel() + " (size:"
							+ cellCs[0][iCell].length() + ") returns a MCC value(s) of NaN");
				}
			}

			if (icq != null) {
				switch (tholdMetrics[PluginStatic.ICQ]) {
				case THOLD_ALL:
					this.cellCs = allCs;
					break;
				case THOLD_COSTES:
					this.cellCs = costesCs;
					break;
				case THOLD_FT:
					this.cellCs = new CellData[ftCs.length][];
					for (int i = 0; i < this.cellCs.length; i++)
						this.cellCs[i] = ftCs[i].get(tholdFTs[PluginStatic.ICQ][i]);
					break;
				default:
					break;
				}
				icq[iCell] = getICQ(getCellPixels(iCell));

				if (Double.isNaN(icq[iCell])) {
					hasNaN = true;
					ExceptionHandler.addWarning(Thread.currentThread(), cellCs[0][iCell].getLabel() + " (size:"
							+ cellCs[0][iCell].length() + ") returns a ICQ value of NaN");
				}
			}

			if (avgINT != null) {
				this.cellCs = allCs;
				avgINT[iCell] = getAVGINT(getCellPixels(iCell));

				if (Double.isNaN(avgINT[iCell][0]) || Double.isNaN(avgINT[iCell][1])) {
					hasNaN = true;
					ExceptionHandler.addWarning(Thread.currentThread(), cellCs[0][iCell].getLabel() + " (size:"
							+ cellCs[0][iCell].length() + ") returns a Avg.Int. value(s) of NaN");
				}
			}

			if (custom != null) {
				this.cellCs = allCs;
				custom[iCell] = getCustom(getCellPixels(iCell));

				for (int i = 0; i < custom[iCell].length; i++) {
					if (Double.isNaN(custom[iCell][i])) {
						ExceptionHandler.addWarning(Thread.currentThread(), cellCs[0][iCell].getLabel() + " (size:"
								+ cellCs[0][iCell].length() + ") returns a Custom value(s) of NaN");
						break;
					}
				}
			}

		}

		if (hasNaN)
			ExceptionHandler.insertWarning(Thread.currentThread(), ExceptionHandler.NAN_WARNING);

		return true;
	}

	private float[][] getCellPixels(int iCell) {
		if (cellCs == null)
			return null;
		float[][] cellData = new float[cellCs.length][];
		for (int i = 0; i < cellCs.length; i++) {
			if (cellCs[i] != null && cellCs[i][iCell] != null)
				cellData[i] = cellCs[i][iCell].getData();
			else
				cellData[i] = null;
		}
		return cellData;
	}

	private double[][] getCellRanks(int iCell) {
		if (cellCs == null)
			return null;
		double[][] cellData = new double[cellCs.length][];
		for (int i = 0; i < cellCs.length; i++)
			cellData[i] = cellCs[i][iCell].getRank();
		return cellData;
	}

	/**
	 * calculate the sums of the arrays above the thresholds in each array and
	 * in both arrays
	 * 
	 * @param a
	 *            input array 1
	 * @param b
	 *            input array 2
	 * @param thresholdA
	 *            threshold of array a
	 * @param thresholdB
	 *            threshold of array b
	 * @return an array of four values as [0].sum of elements in a above
	 *         thresholdA in a & thresholdB in b [1].sum of elements in b above
	 *         thresholdA in a & thresholdB in b [2].sum of elements in a above
	 *         thresholdA in a [3].sum of elements in b above thresholdB in b
	 */
	public double[] getSum(float[] a, float[] b, double thresholdA, double thresholdB) {
		if (a == null || b == null || a.length != b.length)
			return new double[] { Double.NaN, Double.NaN, Double.NaN, Double.NaN };

		double[] results = new double[4];
		for (int i = 0; i < a.length; i++) {
			if (a[i] > thresholdA && b[i] > thresholdB) {
				results[0] += a[i];
				results[1] += b[i];
			}
			if (a[i] > thresholdA)
				results[2] += a[i];
			if (b[i] > thresholdB)
				results[3] += b[i];
		}
		return results;
	}

	/**
	 * return the corresponding metric using metric index facilitate metric
	 * retrieval in a for loop a method subclasses may want to override
	 * 
	 * @param i
	 *            metric index
	 * @param iCell
	 *            the index of cell in CellData cellC1 and cellC2
	 * @return the metric value of the cell
	 */
	public double[] getMetrics(int i, int iCell) {
		if (i < 0 || i >= DEFAULT_METRIC_NAMES.length || iCell < 0)
			return null;

		switch (DEFAULT_METRIC_NAMES[i]) {
		case SHOW_TOS_LINEAR:
			if (tos != null && iCell < tos.length && tos[iCell] != null && tos[iCell].length > 0)
				return new double[] { tos[iCell][0] };
			else
				return null;
		case SHOW_TOS_LOG2:
			if (tos != null && iCell < tos.length && tos[iCell] != null && tos[iCell].length > 1)
				return new double[] { tos[iCell][1] };
			else
				return null;
		case SHOW_PCC:
			if (pcc != null && iCell < pcc.length)
				return new double[] { pcc[iCell] };
			else
				return null;
		case SHOW_SRC:
			if (srcc != null && iCell < srcc.length)
				return new double[] { srcc[iCell] };
			else
				return null;
		case SHOW_ICQ:
			if (icq != null && iCell < icq.length)
				return new double[] { icq[iCell] };
			else
				return null;
		case SHOW_AVGINT:
			if (avgINT != null && iCell < avgINT.length && avgINT[iCell] != null && avgINT[iCell].length > 0)
				return avgINT[iCell];
			else
				return null;
		case SHOW_MCC:
			if (mcc != null && iCell < mcc.length && mcc[iCell] != null && mcc[iCell].length > 0)
				return mcc[iCell];
			else
				return null;
		case SHOW_CUSTOM:
			if (custom != null && iCell < custom.length)
				return custom[iCell];
			else
				return null;
		default:
			return null;
		}
	}

	/**
	 * check if the corresponding metric has been calculated by metric index a
	 * method subclasses may want to override
	 * 
	 * @param i
	 *            metric index
	 * @return true if it is calculated otherwise, false
	 */
	public boolean isMetrics(int i) {
		if (i < 0 || i >= DEFAULT_METRIC_NAMES.length)
			return false;

		switch (DEFAULT_METRIC_NAMES[i]) {
		case SHOW_TOS_LINEAR:
			if (tos != null && tos.length > 0 && tos[0] != null && tos[0].length > 0)
				return true;
			else
				return false;
		case SHOW_TOS_LOG2:
			if (tos != null && tos.length > 0 && tos[0] != null && tos[0].length > 1)
				return true;
			else
				return false;
		case SHOW_PCC:
			if (pcc != null && pcc.length > 0)
				return true;
			else
				return false;
		case SHOW_SRC:
			if (srcc != null && srcc.length > 0)
				return true;
			else
				return false;
		case SHOW_ICQ:
			if (icq != null && icq.length > 0)
				return true;
			else
				return false;
		case SHOW_AVGINT:
			if (avgINT != null && avgINT.length > 0 && avgINT[0] != null && avgINT[0].length > 0)
				return true;
			else
				return false;
		case SHOW_MCC:
			if (mcc != null && mcc.length > 0 && mcc[0] != null && mcc[0].length > 0)
				return true;
			else
				return false;
		case SHOW_CUSTOM:
			if (custom != null && custom.length > 0 && custom[0] != null && custom[0].length > 0)
				return true;
			else
				return false;
		default:
			return false;
		}

	}

	public void setCompiler(StringCompiler customCompiler) {
		this.customCompiler = customCompiler;
	}

	/*
	 * Those metrics not universally defined will be stated below
	 */
	private double[] getAVGINT(float[]... allData) {

		if (allData == null || allData.length == 0)
			return null;

		double[] result = new double[allData.length];

		for (int iChannel = 0; iChannel < allData.length; iChannel++) {

			float[] a = allData[iChannel];

			int num = 0;
			for (int iData = 0; iData < allData[iChannel].length; iData++) {
				if (Float.isNaN(allData[iChannel][iData]))
					continue;
				a[num] = allData[iChannel][iData];
				num++;
			}
			a = Arrays.copyOfRange(a, 0, num);

			double aMean = getMean(a);
			result[iChannel] = aMean;

		}
		return result;
	}

	/**
	 * Customized function to be editted by the user put your own function here
	 * 
	 * @param c1
	 *            input pixel array of channel 1
	 * @param c2
	 *            input pixel array of channel 2
	 * @return custom metric value
	 */
	private double[] getCustom(float[]... cs) {
		/***
		 * Custom code can be put here to replace the customCompiler below
		 ***/
		try {
			Object returnObj = customCompiler.execute(cs);
			if (returnObj != null) {
				if (returnObj instanceof Number)
					return new double[] { ((Number) returnObj).doubleValue() };
				else if (returnObj instanceof double[])
					return (double[]) returnObj;
				else if (returnObj instanceof float[]) {
					double[] results = new double[((float[]) returnObj).length];
					for (int i = 0; i < results.length; i++)
						results[i] = ((float[]) returnObj)[i];
					return results;
				} else if (returnObj instanceof int[]) {
					double[] results = new double[((int[]) returnObj).length];
					for (int i = 0; i < results.length; i++)
						results[i] = ((int[]) returnObj)[i];
					return results;
				} else if (returnObj instanceof byte[]) {
					double[] results = new double[((byte[]) returnObj).length];
					for (int i = 0; i < results.length; i++)
						results[i] = ((byte[]) returnObj)[i];
					return results;
				} else if (returnObj instanceof Double[]) {
					double[] results = new double[((Double[]) returnObj).length];
					for (int i = 0; i < results.length; i++)
						results[i] = ((Double[]) returnObj)[i];
					return results;
				} else if (returnObj instanceof Float[]) {
					double[] results = new double[((Float[]) returnObj).length];
					for (int i = 0; i < results.length; i++)
						results[i] = ((Float[]) returnObj)[i];
					return results;
				} else if (returnObj instanceof Integer[]) {
					double[] results = new double[((Integer[]) returnObj).length];
					for (int i = 0; i < results.length; i++)
						results[i] = ((Integer[]) returnObj)[i];
					return results;
				} else if (returnObj instanceof Byte[]) {
					double[] results = new double[((Byte[]) returnObj).length];
					for (int i = 0; i < results.length; i++)
						results[i] = ((Byte[]) returnObj)[i];
					return results;
				} else {
					ExceptionHandler.addError(Thread.currentThread(),
							"Unknown data type returned as the custom metric");
					return null;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.handleException(e);;
			return null;
		}
		return null;
	}

}
