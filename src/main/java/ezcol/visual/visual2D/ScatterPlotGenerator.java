package ezcol.visual.visual2D;

import java.util.Arrays;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;

public class ScatterPlotGenerator {

	public static final int INITIAL_SIZE = 25;

	private ImageStack impStack;
	private ImagePlus imp;
	private PlotStackWindow psw;
	private String title;
	private int flags;
	private int width = 400, height = 400;

	private int nPlots;
	private Plot[] plots;
	private float[][] xValues;
	private float[][] yValues;
	private String[] xLabels;
	private String[] yLabels;
	private String[] sliceLabels;

	public ScatterPlotGenerator() {
		this(null, Plot.getDefaultFlags());
	}

	public ScatterPlotGenerator(String title) {
		this(title, Plot.getDefaultFlags());
	}

	public ScatterPlotGenerator(String title, int flags) {
		this.title = title;
		this.flags = flags;
		this.xValues = new float[INITIAL_SIZE][];
		this.yValues = new float[INITIAL_SIZE][];
		this.xLabels = new String[INITIAL_SIZE];
		this.yLabels = new String[INITIAL_SIZE];
		this.sliceLabels = new String[INITIAL_SIZE];
	}

	public ScatterPlotGenerator(String title, String xLabel, String yLabel, float[][] xValues, float[][] yValues,
			String[] sliceLabels) {
		this(title, xLabel, yLabel, xValues, yValues, sliceLabels, Plot.getDefaultFlags());
	}

	public ScatterPlotGenerator(float[][] xValues, float[][] yValues, String[] sliceLabels) {
		this("Plot", "X", "Y", xValues, yValues, sliceLabels, Plot.getDefaultFlags());
	}

	public ScatterPlotGenerator(String title, String xLabel, String yLabel, float[][] xValues, float[][] yValues,
			String[] sliceLabels, int flags) {
		if (yValues != null && yValues.length > 0) {
			nPlots = yValues.length;
			plots = new Plot[nPlots];
			this.title = title;
			this.flags = flags;

			sliceLabels = alignArray(sliceLabels, nPlots);
			xValues = alignArray(xValues, yValues, nPlots);

			addPlots(xLabel, yLabel, xValues, yValues, sliceLabels);
		}
	}

	public ScatterPlotGenerator(String title, String[] xLabels, String[] yLabels, float[][] xValues, float[][] yValues,
			String[] sliceLabels, int flags) {
		if (yValues != null && yValues.length > 0) {
			nPlots = yValues.length;
			plots = new Plot[nPlots];
			this.title = title;
			this.flags = flags;

			xLabels = alignArray(xLabels, nPlots);
			yLabels = alignArray(yLabels, nPlots);
			sliceLabels = alignArray(sliceLabels, nPlots);
			xValues = alignArray(xValues, yValues, nPlots);

			addPlots(xLabels, yLabels, xValues, yValues, sliceLabels);
		}
	}

	public void addPlot(float[] xValues, float[] yValues) {
		addPlot(null, null, null, xValues, yValues);
	}

	/**
	 * This part of code mimics that of ImageStack addSlice Maybe because of
	 * performance, the array length is increase by 2-fold at each out-of-bound
	 * event
	 * 
	 * @param sliceLabel
	 * @param xLabel
	 * @param yLabel
	 * @param xValues
	 * @param yValues
	 */
	public void addPlot(String sliceLabel, String xLabel, String yLabel, float[] xValue, float[] yValue) {
		int size = xValues.length;
		nPlots++;
		if (nPlots >= size) {
			float[][] tmp1 = new float[size * 2][];
			System.arraycopy(xValues, 0, tmp1, 0, size);
			xValues = tmp1;

			float[][] tmp2 = new float[size * 2][];
			System.arraycopy(yValues, 0, tmp2, 0, size);
			yValues = tmp2;

			String[] tmp3 = new String[size * 2];
			System.arraycopy(xLabels, 0, tmp3, 0, size);
			xLabels = tmp3;

			String[] tmp4 = new String[size * 2];
			System.arraycopy(yLabels, 0, tmp4, 0, size);
			yLabels = tmp4;

			String[] tmp5 = new String[size * 2];
			System.arraycopy(sliceLabels, 0, tmp5, 0, size);
			sliceLabels = tmp5;
		}

		xValues[nPlots - 1] = xValue;
		yValues[nPlots - 1] = yValue;
		xLabels[nPlots - 1] = xLabel;
		yLabels[nPlots - 1] = yLabel;
		sliceLabels[nPlots - 1] = sliceLabel;
	}

	public void showPlot(boolean display) {
		if (yValues != null && yValues.length > 0) {
			// nPlots = yValues.length;
			plots = new Plot[nPlots];
			xLabels = alignArray(xLabels, nPlots);
			yLabels = alignArray(yLabels, nPlots);
			sliceLabels = alignArray(sliceLabels, nPlots);
			xValues = alignArray(xValues, yValues, nPlots);
			yValues = alignArray(yValues, yValues, nPlots);
			addPlots(xLabels, yLabels, xValues, yValues, sliceLabels);
		}
		if (display)
			show();
	}

	private float[][] alignArray(float[][] array, float[][] fillarray, int length) {
		if (length < 0)
			return null;
		float[][] result = new float[length][];
		if (array == null) {
			System.arraycopy(fillarray, 0, result, 0, length);
		} else if (array.length < length) {
			System.arraycopy(array, 0, result, 0, array.length);
			if (fillarray.length >= length)
				System.arraycopy(fillarray, array.length, result, array.length, length - array.length);
		} else if (array.length > length)
			System.arraycopy(array, 0, result, 0, length);
		else
			return array;
		return result;
	}

	private String[] alignArray(String[] array, int length) {
		if (length < 0)
			return null;
		String[] result = new String[length];
		if (array == null) {
			Arrays.fill(result, "");
		} else if (array.length < length) {
			System.arraycopy(array, 0, result, 0, array.length);
			Arrays.fill(array, array.length, length - 1, "");
		} else if (array.length > length)
			System.arraycopy(array, 0, result, 0, length);
		else
			return array;
		return result;
	}

	private void addPlot(String xLabel, String yLabel, float[] xValues, float[] yValues, String sliceLabel,
			int iSlice) {
		if (plots == null)
			return;

		plots[iSlice] = new Plot(title, xLabel, yLabel, (float[]) null, (float[]) null, flags);
		plots[iSlice].setFrameSize(width, height);
		plots[iSlice].addPoints(xValues, yValues, Plot.toShape("circle"));
		// Plot bug: the plot cannot automatically generate if there is only one
		// data point
		// The range must be manually set
		// limits will only be generated after Plot.draw();
		// Therefore, no need to getLimits here
		// double[] limits = plots[iSlice].getLimits();
		double[] limits = new double[4];

		if (yValues.length == 1) {
			limits[2] = yValues[0] * 0.8;
			limits[3] = yValues[0] * 1.2;
		}
		if (xValues.length == 1) {
			limits[0] = xValues[0] * 0.8;
			limits[1] = xValues[0] * 1.2;
		}

		if (yValues.length == 1 || xValues.length == 1)
			plots[iSlice].setLimits(limits[0], limits[1], limits[2], limits[3]);

		if (iSlice == 0) {
			imp = plots[iSlice].getImagePlus();
			impStack = imp.getStack();
			impStack.setSliceLabel(sliceLabel, 1);
		} else {
			impStack.addSlice(sliceLabel, plots[iSlice].getProcessor());
		}

	}

	private void addPlots(String[] xLabels, String[] yLabels, float[][] xValues, float[][] yValues,
			String[] sliceLabels) {
		for (int i = 0; i < nPlots; i++)
			addPlot(xLabels[i], yLabels[i], xValues[i], yValues[i], sliceLabels[i], i);
		imp = new ImagePlus(title, impStack);
	}

	private void addPlots(String xLabel, String yLabel, float[][] xValues, float[][] yValues, String[] sliceLabel) {
		for (int i = 0; i < nPlots; i++)
			addPlot(xLabel, yLabel, xValues[i], yValues[i], sliceLabel[i], i);
		imp = new ImagePlus(title, impStack);
	}

	public PlotStackWindow show() {
		psw = new PlotStackWindow(plots, imp);
		// psw.showStack();
		return psw;
	}

	public void setFrameSize(int width, int height) {
		this.width = width;
		this.height = height;
		if (plots == null)
			return;
		String[] labels = impStack.getSliceLabels();
		for (int iSlice = 0; iSlice < plots.length; iSlice++) {
			plots[iSlice].setFrameSize(width, height);
			if (iSlice == 0) {
				imp = plots[iSlice].getImagePlus();
				impStack = imp.getStack();
				impStack.setSliceLabel(labels[iSlice], iSlice + 1);
			}
			impStack.addSlice(labels[iSlice], plots[iSlice].getProcessor());
		}
		imp = new ImagePlus(title, impStack);
	}
	
	/*
	 * Used for additional threshold lines
	 */
	public void addLines(float[] xThold, float[] yThold){
		for(int i=0;i<nPlots;i++)
			addLines(xThold[i],yThold[i],i);
	}
	
	public void addLines(float xThold, float yThold, int iSlice){
		if(plots ==null)
			return;
		int step = 100;
		double[] limits = plots[iSlice].getLimits();
		plots[iSlice].drawDottedLine(limits[0], yThold, limits[1], yThold, (int)((limits[1]-limits[0])/step));
		plots[iSlice].drawDottedLine(xThold, limits[2], xThold, limits[3], (int)((limits[3]-limits[2])/step));
		plots[iSlice].addLegend("Data ( --- Costes )");
	}

}