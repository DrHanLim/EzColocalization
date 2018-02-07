package ezcol.cell;

/*
 * This class is only for storing pixel values of cells so that the number of pixel
 * of each cell could be varied to save memory
 */
public class CellData {

	// all data are calibrated
	private float[] data;
	private double[] x;
	private double[] y;
	private double[] rank;
	private int[] sortedIdx;
	private float max;
	private float min;
	private boolean isSorted;
	private String label;

	public CellData() {
		data = null;
		x = null;
		y = null;
		isSorted = false;
	}

	public CellData(CellData cellData) {
		data = cellData.data.clone();
		x = cellData.x.clone();
		y = cellData.y.clone();
		rank = cellData.rank.clone();
		sortedIdx = cellData.sortedIdx.clone();
		max = cellData.max;
		min = cellData.min;
		isSorted = cellData.isSorted;
		label = cellData.label;
	}

	public CellData(int numOfPixel) {
		data = new float[numOfPixel];
		x = new double[numOfPixel];
		y = new double[numOfPixel];
		isSorted = false;
	}

	public CellData(float[] data) {
		this.data = data;
		this.x = null;
		this.y = null;
		isSorted = false;
	}

	public CellData(float[] data, double[] x, double[] y) {
		this.data = data;
		this.x = x;
		this.y = y;
		isSorted = false;
	}

	public void setData(float[] data, double[] x, double[] y) {
		this.data = data;
		this.x = x;
		this.y = y;
		isSorted = false;
	}

	public void setData(float data, int x, int y, int idx) {
		if (this.data != null && this.data.length > idx)
			this.data[idx] = data;
		if (this.x != null && this.x.length > idx)
			this.x[idx] = x;
		if (this.y != null && this.y.length > idx)
			this.y[idx] = y;
		isSorted = false;
	}

	public void setData(float[] data) {
		this.data = data;
		this.x = null;
		this.y = null;
		isSorted = false;
	}

	public void setData(float data, int idx) {
		if (this.data != null && this.data.length > idx)
			this.data[idx] = data;
		isSorted = false;
	}

	public void setData(float data, double x, double y, int idx) {
		if (this.data != null && this.data.length > idx)
			this.data[idx] = data;
		if (this.x != null && this.x.length > idx)
			this.x[idx] = x;
		if (this.y != null && this.y.length > idx)
			this.y[idx] = y;
		isSorted = false;
	}

	/**
	 * @param cellData
	 * @param fromidx
	 * @param toidx
	 */
	public void setData(CellData cellData, int fromidx, int toidx) {
		if (cellData == null)
			return;
		data[toidx] = cellData.data[fromidx];
		x[toidx] = cellData.x[fromidx];
		y[toidx] = cellData.y[fromidx];
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	public String getCellLabel() {
		if(label.indexOf("(")!=-1)
			return label.substring(0, label.indexOf("("));
		else
			return label;
	}

	public void sort() {
		if (data == null || data.length == 0)
			return;
		DataSorter sortArr = new DataSorter();
		sortArr.sort(data, data.length);
		sortedIdx = sortArr.getSortIdx();
		rank = sortArr.getRank();
		if (sortedIdx != null) {
			// NaN has an index which is larger than data.length
			if (sortedIdx[0] < data.length)
				max = data[sortedIdx[0]];
			else
				max = Float.NaN;
			if (sortedIdx[sortedIdx.length - 1] < data.length)
				min = data[sortedIdx[sortedIdx.length - 1]];
			else
				min = Float.NaN;

		}
		isSorted = true;
	}

	public boolean checkData() {
		if (data == null || x == null || y == null)
			return false;
		if (data.length != x.length)
			return false;
		if (data.length != y.length)
			return false;
		return true;
	}

	public double getMax() {
		return (double) max;
	}

	public double getMin() {
		return (double) min;
	}

	public boolean isSorted() {
		return isSorted;
	}

	// clone all arrhdays before handing out to prevent other classes
	// from changing the arrays
	public double[] getRank() {
		return rank == null ? null : rank.clone();
	}

	public int[] getSortedIdx() {
		return sortedIdx == null ? null : sortedIdx.clone();
	}

	public float[] getData() {
		return data.clone();
	}

	public double[] getX() {
		return x.clone();
	}

	public double[] getY() {
		return y.clone();
	}

	public int length() {
		if (data == null)
			return 0;
		else
			return data.length;
	}

	public double getPixelRank(int i) {
		if (rank != null && rank.length > i)
			return rank[i];
		else
			return -1;
	}

	public int getPixelSortedIdx(int i) {
		if (sortedIdx != null && sortedIdx.length > i)
			return sortedIdx[i];
		else
			return -1;
	}

	public int getPX(int i) {
		if (x != null && x.length > i)
			return (int) x[i];
		else
			return -1;
	}

	public int getPY(int i) {
		if (y != null && y.length > i)
			return (int) y[i];
		else
			return -1;
	}

	public double getPixelX(int i) {
		if (x != null && x.length > i)
			return x[i];
		else
			return -1.0;
	}

	public double getPixelY(int i) {
		if (y != null && y.length > i)
			return y[i];
		else
			return -1.0;
	}

	public float getPixel(int i) {
		if (data != null && data.length > i)
			return data[i];
		else
			return Float.NaN;
	}

	public boolean equalData(CellData cellData) {
		if (cellData == null)
			return false;
		float[] data = cellData.getData();
		if (data == null || data.length != this.data.length)
			return false;
		for (int i = 0; i < data.length; i++)
			if (data[i] != this.data[i])
				return false;
		return true;
	}

	/**
	 * Please be aware, the data points in new CellData though are marked NaN
	 * for those after the index(below the threshold) The sortedIdx and rank
	 * remain the same, and the data will NOT be sorted again.
	 * 
	 * @param index/threshold
	 * @return
	 */
	public CellData getData(int index) {
		if (index > length())
			return this;
		if (!isSorted())
			sort();
		CellData data = new CellData(this);
		for (int i = 0; i < data.length(); i++)
			data.data[i] = Float.NaN;
		if (index < 0)
			return data;
		for (int i = 0; i < index; i++) {
			data.data[data.sortedIdx[i] < data.length() ? data.sortedIdx[i]
					: data.sortedIdx[i] - data.length()] = this.data[sortedIdx[i] < length() ? sortedIdx[i]
							: sortedIdx[i] - length()];
		}
		return data;
	}

}
