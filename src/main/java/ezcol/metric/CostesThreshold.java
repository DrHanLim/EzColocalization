package ezcol.metric;

import java.util.ArrayList;
import java.util.Random;

import ezcol.cell.CellData;
import ezcol.cell.DataSorter;
import ezcol.debug.Debugger;
import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginConstants;
import ij.gui.Plot;
import ij.process.ImageProcessor;

public class CostesThreshold {

	public CostesThreshold() {
	};

	public double[] linreg(float[] Aarray, float[] Barray) {
		double[] coeff = new double[2];
		int count = 0;
		double sumA, sumB, sumAB, sumsqrA;

		sumA = 0;
		sumB = 0;
		sumAB = 0;
		sumsqrA = 0;

		for (int m = 0; m < Aarray.length; m++) {
			sumA += Aarray[m];
			sumB += Barray[m];
			sumAB += Aarray[m] * Barray[m];
			sumsqrA += Aarray[m] * Aarray[m];
			count++;
		}
		// 0:a, 1:b
		coeff[0] = (count * sumAB - sumA * sumB) / (count * sumsqrA - sumA * sumA);
		coeff[1] = (sumsqrA * sumB - sumA * sumAB) / (count * sumsqrA - sumA * sumA);
		return coeff;
	}

	private double getPCC(int[] A, int[] B) {
		if (A == null || B == null || A.length <= 0 || B.length <= 0 || A.length != B.length)
			return Double.NaN;
		double Asum = 0.0, Bsum = 0.0, ABsum = 0.0, A2sum = 0.0, B2sum = 0.0;
		for (int i = 0; i < A.length; i++) {
			Asum += A[i];
			Bsum += B[i];
			ABsum += A[i] * B[i];
			A2sum += A[i] * A[i];
			B2sum += B[i] * B[i];
		}
		int n = A.length;
		double PCC = (n * ABsum - Asum * Bsum) / Math.sqrt((n * A2sum - Asum * Asum) * (n * B2sum - Bsum * Bsum));
		return PCC;
	}

	/**
	 * Run Costes Algorithm to find costes thresholds
	 * 
	 * @param Aarray
	 *            input float values of channel 1
	 * @param Barray
	 *            input float values of channel 2
	 * @return a double array with two elements [0]: Threshold of Channel 1 [1]:
	 *         Threshold of Channel 2
	 */
	public double[] getCostesThrd(float[] Aarray, float[] Barray) {
		double[] tmp = linreg(Aarray, Barray);
		return getCostesThrd(Aarray, Barray, tmp[0], tmp[1]);
	}
	
	private double[] getCostesThrd(float[] Aarray, float[] Barray, double slope, double intercept) {
		if (Aarray == null || Barray == null)
			return new double[] { Double.NaN, Double.NaN };
		int len = Aarray.length < Barray.length ? Aarray.length : Barray.length;
		double Asum = 0.0, Bsum = 0.0, ABsum = 0.0, A2sum = 0.0, B2sum = 0.0;
		// use a maximum PCC of 2.0 to account for double imprecision
		double PCC = 2.0;
		int n = 0;
		double squarePCC, covariance;
		double temp;
		double[] projBarray = new double[len];
		int[] sortedIdxes;

		// Determine the rank of data points of pixels above Costes thresholds
		// in sortedIdxes
		// The data points should include CostesIdx and everything below it
		// because sortedIdxes is the indexes in descending order.
		int CostesIdx = -1;
		double[] thresholds = new double[] { Double.NaN, Double.NaN };
		// depending on the slope different dynamic programming is implemented
		// It's possible to synthesize all these conditions into one
		// However, I'm too lazy to do so.
		if (slope < 0) {
			
			projBarray = new double[len * 2];
			for (int i = 0; i < len * 2; i++)
				projBarray[i] = Double.NaN;
			for (int i = 0; i < len; i++) {
				temp = slope * Aarray[i] + intercept;
				// If a point is below the line, it will always be below
				// thresholds
				// If a point is above the line, we need to record
				// when it's removed and when it's added back
				if (temp < Barray[i]) {
					projBarray[i] = temp;
					projBarray[i + len] = Barray[i];
				}
			}

			DataSorter mySort = new DataSorter();
			mySort.sort(projBarray);
			sortedIdxes = mySort.getSortIdx();


			for (int i = 0; i < len; i++) {
				Asum += Aarray[i];
				Bsum += Barray[i];
				ABsum += Aarray[i] * Barray[i];
				A2sum += Aarray[i] * Aarray[i];
				B2sum += Barray[i] * Barray[i];
				n++;
			}

			// Hold up the ties
			// We always consider points laying on the thresholds to be below
			// thresholds
			// Therefore, some pixels that need to be added
			ArrayList<Integer> tiesHolder = new ArrayList<Integer>();

			// We use forward indexing here because NaN might be found at the
			// end of
			// sortedIdxes array. We don't want to deal with NaN.
			// NaN is flagged by assigning a sortedIdx that is greater than or
			// equal to the length of the array
			// #see DataSorter.sort for more detail

			for (int i = 0; i < 2 * len;) {

				// spot NaN, it's time to stop
				if (sortedIdxes[i] >= len * 2)
					break;

				// remove all points hold on by tieHolder back
				// They were hold on until the next thresholds being selected
				// This is because we always consider points on the threshold
				// lines
				// to be below thresholds
				if (!tiesHolder.isEmpty()) {
					for (int iTie : tiesHolder) {
						Asum -= Aarray[iTie];
						Bsum -= Barray[iTie];
						ABsum -= Aarray[iTie] * Barray[iTie];
						A2sum -= Aarray[iTie] * Aarray[iTie];
						B2sum -= Barray[iTie] * Barray[iTie];
						n--;
						
					}
					tiesHolder.clear();
				}

				// remove all the following data points on the threshold line
				boolean pointAdded = false;
				while (i < 2 * len) {

					if (sortedIdxes[i] < len) {
						Asum += Aarray[sortedIdxes[i]];
						Bsum += Barray[sortedIdxes[i]];
						ABsum += Aarray[sortedIdxes[i]] * Barray[sortedIdxes[i]];
						A2sum += Aarray[sortedIdxes[i]] * Aarray[sortedIdxes[i]];
						B2sum += Barray[sortedIdxes[i]] * Barray[sortedIdxes[i]];
						
						n++;
						i++;
						pointAdded = true;

					} else if (sortedIdxes[i] < 2 * len) {
						if (pointAdded)
							tiesHolder.add(sortedIdxes[i] - len);
						else {
							Asum -= Aarray[sortedIdxes[i] - len];
							Bsum -= Barray[sortedIdxes[i] - len];
							ABsum -= Aarray[sortedIdxes[i] - len] * Barray[sortedIdxes[i] - len];
							A2sum -= Aarray[sortedIdxes[i] - len] * Aarray[sortedIdxes[i] - len];
							B2sum -= Barray[sortedIdxes[i] - len] * Barray[sortedIdxes[i] - len];
							n--;
						}
						i++;
						
					} else {
						ExceptionHandler.addError(Thread.currentThread(),
								"Unprecedented error occurs in CostesThreshold");
						return null;
					}
					// If the next data point has exactly the same value
					// It should be removed, added or held
					if (i >= 2 * len - 1 || sortedIdxes[i] >= 2 * len
							|| projBarray[sortedIdxes[i - 1]] != projBarray[sortedIdxes[i]])
						break;
				}
				covariance = n * ABsum - Asum * Bsum;
				squarePCC = covariance * covariance / ((n * A2sum - Asum * Asum) * (n * B2sum - Bsum * Bsum));
				// This line is commented out so that the minimum absolute value
				// of
				// PCC will be selected
				// squarePCC=covariance>0 ? squarePCC : (-squarePCC);
				
				if (squarePCC < PCC) {
					//If point is added, use the normal index
					//It should be noted that i has already increased before this recording
					//If point is removed, use the next index
					//There is no need to worry about boundary condition because
					//the last index must be addition of a point/points.
					if(pointAdded)
						CostesIdx = i - 1;
					else
						CostesIdx = i;
					PCC = squarePCC;
				}
			}

			//projBarray[i] = temp;
			//projBarray[i + len] = Barray[i];
			if (sortedIdxes[CostesIdx] < len) {
				thresholds[0] = Aarray[sortedIdxes[CostesIdx]];
				thresholds[1] = projBarray[sortedIdxes[CostesIdx]];
			}else if (sortedIdxes[CostesIdx] < 2 * len) {
				thresholds[0] = (projBarray[sortedIdxes[CostesIdx]] - intercept) / slope;
				thresholds[1] = projBarray[sortedIdxes[CostesIdx]];
			}else{
				ExceptionHandler.addError(Thread.currentThread(),
						"Unprecedented error 2 occurs in CostesThreshold");
			}
		} else {


			if (slope == 0)
				for (int i = 0; i < len; i++) {
					projBarray[i] = Aarray[i];
				}
			else {
				for (int i = 0; i < len; i++) {
					temp = slope * Aarray[i] + intercept;
					projBarray[i] = temp < Barray[i] ? temp : Barray[i];
				}
			}

			// sort the data array to get the order of elimination of pixels
			// The order of elimination is determined by the projected value (to
			// the
			// linear regression line) of data point in Channel B
			DataSorter mySort = new DataSorter();
			mySort.sort(projBarray);
			sortedIdxes = mySort.getSortIdx();

			// avoid using Math.sqrt for better performance

			for (int i = len - 1; i >= 0;) {

				// remove all the following data points on the threshold line
				while (i >= 0) {
					Asum += Aarray[sortedIdxes[i]];
					Bsum += Barray[sortedIdxes[i]];
					ABsum += Aarray[sortedIdxes[i]] * Barray[sortedIdxes[i]];
					A2sum += Aarray[sortedIdxes[i]] * Aarray[sortedIdxes[i]];
					B2sum += Barray[sortedIdxes[i]] * Barray[sortedIdxes[i]];
					
					n++;
					i--;

					if (i < 0 || projBarray[sortedIdxes[i + 1]] != projBarray[sortedIdxes[i]])
						break;
				}
				covariance = n * ABsum - Asum * Bsum;
				squarePCC = covariance * covariance / ((n * A2sum - Asum * Asum) * (n * B2sum - Bsum * Bsum));
				// This line is commented out so that the minimum absolute value
				// of
				// PCC will be selected
				// squarePCC=covariance>0 ? squarePCC : (-squarePCC);
				
				if (squarePCC < PCC) {
					CostesIdx = i + 1;
					PCC = squarePCC;
				}
			}
			
			if(projBarray[sortedIdxes[CostesIdx]] < Barray[sortedIdxes[CostesIdx]]){
				thresholds[0] = Aarray[sortedIdxes[CostesIdx]];
				thresholds[1] = projBarray[sortedIdxes[CostesIdx]];
			}else{
				thresholds[0] = (projBarray[sortedIdxes[CostesIdx]] - intercept) / slope;
				thresholds[1] = projBarray[sortedIdxes[CostesIdx]];
			}
		}
		return thresholds;
	}

	/**
	 * @deprecated This is a naive approach, and it's very slow
	 * @param Aarray
	 * @param Barray
	 * @return
	 */
	public double[] getCostesThrdSlow(float[] Aarray, float[] Barray){
		if (Aarray == null || Barray == null)
			return new double[] { Double.NaN, Double.NaN };
		int len = Aarray.length < Barray.length ? Aarray.length : Barray.length;
		double[] tmp = linreg(Aarray, Barray);
		double slope = tmp[0];
		double intercept = tmp[1];
		double[] projAarray = new double[len * 2];
		double[] projBarray = new double[len * 2];
		double temp;
		double[] thresholds = new double[2];
		double PCC = 2.0;
		for (int i = 0; i < len; i++) {
			// add all possible threshold combinations
			projAarray[i] = Aarray[i];
			projAarray[i + len] = (Barray[i] - intercept) / slope;
			
			projBarray[i] = slope * Aarray[i] + intercept;
			projBarray[i + len] = Barray[i];
			
			/*if(projAarray[i + len] < 0)
				Debugger.printCurrentLine("projAarray[i + len]: "+ projAarray[i + len]);
			
			if(projBarray[i] < 0)
				Debugger.printCurrentLine("projBarray[i]: "+ projBarray[i]);*/
		}
		
		for (int i = 0; i < 2 * len; i++) {
			temp = linregCostes(Aarray, Barray, projAarray[i], projBarray[i], false)[2];
			if (temp * temp < PCC){
				PCC = temp * temp;
				thresholds[0] = projAarray[i];
				thresholds[1] = projBarray[i];
			}
		}
		
		Debugger.printCurrentLine("slow PCC^2: " + PCC);		
		
		return thresholds;
	}
	
	/**
	 * @param Aarray image A data
	 * @param Barray image B data
	 * @param TA threshold A
	 * @param TB threshold B
	 * @param direction: True above both (exclusive), False below either (inclusive)
	 * @return {slope, intercept, pcc}
	 */
	public double[] linregCostes(float[] Aarray, float[] Barray, double TA, double TB, boolean direction) {
		
		if(Aarray == null || Barray == null){
			return null;
		}
		
		int len = Aarray.length < Barray.length ? Aarray.length : Barray.length;
		
		double cov = 0;
		double varA = 0;
		double varB = 0;
		double[] coeff = new double[3];
		int count = 0;
		double sumA, sumB, sumAB, sumsqrA, Aarraymean, Barraymean;
		sumA = 0;
		sumB = 0;
		sumAB = 0;
		sumsqrA = 0;
		Aarraymean = 0;
		Barraymean = 0;

		for (int m = 0; m < len; m++) {
			if ((!direction && (Aarray[m] <= TA || Barray[m] <= TB)) ||
				(direction && (Aarray[m] > TA && Barray[m] > TB))){
				sumA += Aarray[m];
				sumB += Barray[m];
				sumAB += Aarray[m] * Barray[m];
				sumsqrA += Aarray[m] * Aarray[m];
				count++;
			}
		}

		Aarraymean = sumA / count;
		Barraymean = sumB / count;

		for (int m = 0; m < len; m++) {
			if ((!direction && (Aarray[m] <= TA || Barray[m] <= TB)) ||
					(direction && (Aarray[m] > TA && Barray[m] > TB))){
				cov += (Aarray[m] - Aarraymean) * (Barray[m] - Barraymean);
				varA += (Aarray[m] - Aarraymean) * (Aarray[m] - Aarraymean);
				varB += (Barray[m] - Barraymean) * (Barray[m] - Barraymean);
			}
		}

		coeff[0] = (count * sumAB - sumA * sumB) / (count * sumsqrA - sumA * sumA);
		coeff[1] = (sumsqrA * sumB - sumA * sumAB) / (count * sumsqrA - sumA * sumA);
		coeff[2] = cov / (Math.sqrt(varA * varB));
		
		return coeff;
	}

}
