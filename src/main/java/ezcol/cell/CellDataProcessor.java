package ezcol.cell;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class CellDataProcessor {

	private int width, height, pixelCount, numOfCell;
	private CellData[][] cellCs;
	private int[] numOfPixel;
	private int[][] pixelMask;
	private float[][][] pixelCs;

	private Calibration cal;
	private List<Object> cellIDs;
	private String prefix;

	/*
	 * public boolean mask2data(final ImageProcessor c1, final ImageProcessor
	 * c2,boolean rank) { if(c1==null||c2==null) return false;
	 * 
	 * if(c1.getWidth()!=c2.getWidth()||c1.getHeight()!=c2.getHeight()) return
	 * false;
	 * 
	 * numOfCell=1; pixelC1=getFloatArray(c1); pixelC2=getFloatArray(c2); int
	 * width=c1.getWidth(),height=c1.getHeight(); float[] data1=new
	 * float[width*height],data2=new float[width*height]; double[] x=new
	 * double[width*height],y=new double[width*height]; int count=0;
	 * if(cal==null){ for(int w=0;w<width;w++){ for(int h=0;h<height;h++){
	 * data1[count]=pixelC1[w][h]; data2[count]=pixelC2[w][h]; x[count]=w;
	 * y[count]=h; count++; } } }else{ for(int w=0;w<width;w++){ for(int
	 * h=0;h<height;h++){ data1[count]=pixelC1[w][h];
	 * data2[count]=pixelC2[w][h]; //coordinates need to be calibrated
	 * x[count]=w*cal.pixelWidth; y[count]=h*cal.pixelHeight; count++; } } }
	 * cellC1=new CellData[1]; cellC2=new CellData[1]; cellC1[0]=new
	 * CellData(data1,x,y); cellC2[0]=new CellData(data2,x.clone(),y.clone());
	 * if(rank){ cellC1[0].sort(); cellC2[0].sort(); }
	 * 
	 * return true; }
	 */
	@Deprecated
	public boolean mask2data(ImageProcessor cell, ImageProcessor c1, ImageProcessor c2, boolean rank) {
		return mask2data(cell, new ImageProcessor[] { c1, c2 }, rank);
	}

	public boolean mask2data(ImageProcessor cell, ImageProcessor[] chs, boolean rank) {
		if (chs == null)
			return false;

		ImageProcessor ic = null;
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] != null) {
				if ((ic != null) && (ic.getWidth() != chs[i].getWidth() || ic.getHeight() != chs[i].getHeight()))
					return false;
				ic = chs[i];
			}
		}
		if (ic == null)
			return false;

		width = ic.getWidth();
		height = ic.getHeight();
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] == null)
				chs[i] = new ShortProcessor(width, height);
		}

		if (countMask2NumOfCell(cell) == 0) {
			numOfCell = 1;
			numOfPixel = new int[] { ic.getPixelCount() };
			handleCellData(chs, rank);
			// return false;
		} else {
			iniNumOfPixel();
			pixelCs = new float[chs.length][][];
			for (int i = 0; i < pixelCs.length; i++)
				pixelCs[i] = getFloatArray(chs[i]);
			iniCellCdata(chs.length);
			handleCellData(rank);
		}

		for (int iChannel = 0; iChannel < cellCs.length; iChannel++) {
			for (int iCell = 0; iCell < cellCs[iChannel].length; iCell++) {
				cellCs[iChannel][iCell].setLabel(prefix + " cell " + (iCell + 1) + " (Ch." + (iChannel + 1) + ")");
			}
		}

		return true;
	}

	private int countMask2NumOfCell(ImageProcessor cell) {
		if (cell == null)
			return 0;
		int width = cell.getWidth();
		int height = cell.getHeight();

		if (width != this.width || height != this.height)
			return 0;

		pixelCount = cell.getPixelCount();
		pixelMask = cell.getIntArray();
		numOfCell = 0;
		Set<Integer> setOfIDs = new HashSet<Integer>();
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				if (pixelMask[w][h] == 0)
					continue;
				setOfIDs.add(pixelMask[w][h]);
			}
		}
		numOfCell = setOfIDs.size();
		cellIDs = Arrays.asList(setOfIDs.toArray());
		// The following way is not safe in parallel programming
		/*
		 * cell.resetMinAndMax(); cell.setThreshold(1, cell.getMax(),
		 * ImageProcessor.NO_LUT_UPDATE); ImageStatistics
		 * idxStat=ImageStatistics.getStatistics(cell, 272, null); idxStart =
		 * (int) idxStat.min; idxEnd = (int) idxStat.max; if(idxEnd>=idxStart)
		 * numOfCell = idxEnd-idxStart+1; else numOfCell = 0;
		 */

		return numOfCell;
	}

	/**
	 * This doesn't work if the image is calibrated
	 *
	 * public void setNumOfPixel(final ResultsTable tempCellTable) { float[]
	 * numOfPixel=null; if(tempCellTable!=null&&tempCellTable.getCounter()>0)
	 * numOfPixel=tempCellTable.getColumn(tempCellTable.getColumnIndex("Area"));
	 * if(numOfPixel!=null) this.setNumOfPixel(numOfPixel); }
	 */

	public CellData[] getCellData(int i) {
		if (cellCs == null)
			return null;
		else if (i >= 0 && i < cellCs.length)
			return cellCs[i];
		else
			return null;
	}

	public int getNumOfCell() {
		return numOfCell;
	}

	public void setCalibration(Calibration cal) {
		this.cal = cal;
	}

	public Calibration getCalibration() {
		return cal;
	}

	private void iniNumOfPixel() {
		numOfPixel = new int[numOfCell];
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < height; h++) {
				if (pixelMask[w][h] == 0)
					continue;
				numOfPixel[cellIDs.indexOf(pixelMask[w][h])]++;
			}
		}
	}

	private void iniCellCdata(int n) {
		cellCs = new CellData[n][numOfCell];
		for (int iChannel = 0; iChannel < cellCs.length; iChannel++) {
			cellCs[iChannel] = new CellData[numOfCell];
			if (numOfPixel != null && numOfCell == numOfPixel.length) {
				for (int i = 0; i < numOfCell; i++)
					cellCs[iChannel][i] = new CellData(numOfPixel[i]);
			} else {
				for (int i = 0; i < numOfCell; i++)
					cellCs[iChannel][i] = new CellData(pixelCount);
			}
		}

	}

	private void handleCellData(ImageProcessor[] chs, boolean rank) {
		// numOfCell = 1;
		// numOfPixel = new int[]{chs[0].getPixelCount()};
		cellCs = new CellData[chs.length][1];
		for (int i = 0; i < chs.length; i++) {
			cellCs[i][0] = new CellData(numOfPixel[0]);
			int index = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					cellCs[i][0].setData(chs[i].getPixel(x, y), x, y, index);
					index++;
				}
			}

			if (rank)
				cellCs[i][0].sort();
		}
	}

	private void handleCellData(boolean rank) {
		for (int iChannel = 0; iChannel < cellCs.length; iChannel++) {
			int[] tempNumOfPixel = new int[numOfCell];
			if (cal == null) {
				for (int w = 0; w < width; w++) {
					for (int h = 0; h < height; h++) {
						if (pixelMask[w][h] == 0)
							continue;
						int iCell = cellIDs.indexOf(pixelMask[w][h]);
						cellCs[iChannel][iCell].setData(pixelCs[iChannel][w][h], w, h, tempNumOfPixel[iCell]);
						tempNumOfPixel[iCell]++;
					}

				}
			} else {
				for (int w = 0; w < width; w++) {
					for (int h = 0; h < height; h++) {
						if (pixelMask[w][h] == 0)
							continue;
						int iCell = cellIDs.indexOf(pixelMask[w][h]);
						cellCs[iChannel][iCell].setData(pixelCs[iChannel][w][h], w * cal.pixelWidth,
								h * cal.pixelHeight, tempNumOfPixel[iCell]);
						tempNumOfPixel[iCell]++;
					}

				}
			}
			if (rank) {
				for (int iCell = 0; iCell < numOfCell; iCell++) {
					cellCs[iChannel][iCell].sort();
				}
			}
		}
	}

	private float[][] getFloatArray(final ImageProcessor ip) {
		if (ip == null) {
			float[][] result = new float[width][height];
			for (int w = 0; w < width; w++)
				for (int h = 0; h < height; h++)
					result[w][h] = Float.NaN;
			return result;
		}
		int width = ip.getWidth();
		int height = ip.getHeight();
		float[][] result = new float[width][height];
		float[] cTable = ip.getCalibrationTable();

		if (cTable == null) {
			for (int w = 0; w < width; w++)
				for (int h = 0; h < height; h++)
					result[w][h] = ip.getf(w, h);
		} else {
			for (int w = 0; w < width; w++)
				for (int h = 0; h < height; h++)
					result[w][h] = cTable[ip.get(w, h)];
		}

		return result;
	}

	public void setLabel(String label) {
		this.prefix = label;
	}

	public String getLabel() {
		return this.prefix;
	}

}
