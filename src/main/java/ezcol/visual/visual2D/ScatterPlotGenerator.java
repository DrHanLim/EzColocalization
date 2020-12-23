package ezcol.visual.visual2D;

import java.util.Arrays;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;

import ezcol.metric.CostesThreshold;

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

			this.xValues = xValues.clone();
			this.yValues = yValues.clone();
			this.xLabels = new String[nPlots];
			this.yLabels = new String[nPlots];
			for (int i = 0; i < nPlots; i++) {
				this.xLabels[i] = xLabel;
				this.yLabels[i] = yLabel;
			}
			this.sliceLabels = sliceLabels.clone();

			addPlots(this.xLabels, this.yLabels, this.xValues, this.yValues, this.sliceLabels);
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

			this.xValues = xValues.clone();
			this.yValues = yValues.clone();
			this.xLabels = xLabels.clone();
			this.yLabels = yLabels.clone();
			this.sliceLabels = sliceLabels.clone();

			addPlots(this.xLabels, this.yLabels, this.xValues, this.yValues, this.sliceLabels);
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
		plots[iSlice].addPoints(xValues, yValues, null, Plot.toShape("circle"), sliceLabel);
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
		for (int i = 0; i < nPlots; i++) {
			addPlot(xLabels[i], yLabels[i], xValues[i], yValues[i], sliceLabels[i], i);
		}
		imp = new ImagePlus(title, impStack);
	}

	public PlotStackWindow show() {
		
		if(plots != null && imp != null)
			psw = new PlotStackWindow(plots, imp, this);
		else
			psw = null;
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

	public void addCostes() {
		if (plots == null)
			return;
		CostesThreshold costesTholder = new CostesThreshold();
		// Must call updateImage() to update the current plot
		// addLegend will automatically do that
		// Otherwise, it won't show up on the plot until the user zoom
		addRegressionLine();
		for (int iSlice = 0; iSlice < nPlots ; iSlice++) {
			double[] tholds = costesTholder.getCostesThrd(xValues[iSlice], yValues[iSlice]);
			addDashLines((float) tholds[0], (float) tholds[1], iSlice);
			plots[iSlice].addLegend("Data    --- Costes");
			plots[iSlice].updateImage();
		}

	}

	/*
	 * Used for additional threshold lines
	 */

	public void addDashLines(float xThold, float yThold, int iSlice) {
		if (plots == null)
			return;
		int step = 10;
		double[] limits = getTrueLimits(plots[iSlice]);
		double[] plotLimits = plots[iSlice].getLimits();
		boolean[] logScales = getTrueLogs(plots[iSlice]);

		double ystep = ((plotLimits[1] - plotLimits[0]) / step / 2);
		double xstep = ((plotLimits[3] - plotLimits[2]) / step / 2);
		ystep = ystep > 0 ? ystep : 1;
		xstep = xstep > 0 ? xstep : 1;

		if (yThold < limits[2])
			yThold = (float) limits[2];
		else if (yThold > limits[3])
			yThold = (float) limits[3];

		if (xThold < limits[0])
			xThold = (float) limits[0];
		else if (xThold > limits[1])
			xThold = (float) limits[1];

		for (int i = 0; i < step; i++) {
			if (logScales[0])
				plots[iSlice].drawLine(Math.pow(10, plotLimits[0] + ystep * i * 2), yThold,
						Math.pow(10, plotLimits[0] + ystep * (2 * i + 1)), yThold);
			else
				plots[iSlice].drawLine(plotLimits[0] + ystep * i * 2, yThold, plotLimits[0] + ystep * (2 * i + 1),
						yThold);
			if (logScales[1])
				plots[iSlice].drawLine(xThold, Math.pow(10, plotLimits[2] + xstep * i * 2), xThold,
						Math.pow(10, plotLimits[2] + xstep * (2 * i + 1)));
			else
				plots[iSlice].drawLine(xThold, plotLimits[2] + xstep * i * 2, xThold,
						plotLimits[2] + xstep * (2 * i + 1));

		}
	}

	public void addSolidLines(float xThold, float yThold, int iSlice) {
		if (plots == null)
			return;
		int step = 20;
		double[] limits = getTrueLimits(plots[iSlice]);

		int ystep = (int) ((limits[1] - limits[0]) / step / 2);
		int xstep = (int) ((limits[3] - limits[2]) / step / 2);
		ystep = ystep > 0 ? ystep : 1;
		xstep = xstep > 0 ? xstep : 1;

		if (yThold < limits[2])
			yThold = (float) limits[2];
		else if (yThold > limits[3])
			yThold = (float) limits[3];

		if (xThold < limits[0])
			xThold = (float) limits[0];
		else if (xThold > limits[1])
			xThold = (float) limits[1];

		plots[iSlice].drawLine(limits[0], yThold, limits[1], yThold);
		plots[iSlice].drawLine(xThold, limits[2], xThold, limits[3]);
	}

	private void addRegressionLine() {
		addRegressionLine(xValues, yValues);
	}

	private void addRegressionLine(float[][] xValues, float[][] yValues) {
		if (xValues == null || yValues == null)
			return;
		CostesThreshold costes = new CostesThreshold();
		for (int i = 0; i < nPlots; i++) {
			double[] lngs = costes.linreg(xValues[i], yValues[i]);
			addLine(lngs, i);
		}
	}

	// draw linear regression line
	public void addLine(double[] tmps, int iSlice) {
		if (plots == null || tmps == null || tmps.length < 2)
			return;

		double[] limits = getTrueLimits(plots[iSlice]);
		boolean[] logScales = getTrueLogs(plots[iSlice]);
		//double[] plotLimits = plots[iSlice].getLimits();
		// limits: {xMin, xMax, yMin, yMax}
		double x1, x2;
		//double y1, y2;

		if (tmps[0] < 0) {
			x1 = ((limits[3] - tmps[1]) / tmps[0]);
			//y1 = (limits[0] * tmps[0] + tmps[1]);
			x2 = ((limits[2] - tmps[1]) / tmps[0]);
			//y2 = (limits[1] * tmps[0] + tmps[1]);
		} else {
			x1 = ((limits[3] - tmps[1]) / tmps[0]);
			//y1 = (limits[1] * tmps[0] + tmps[1]);
			x2 = ((limits[2] - tmps[1]) / tmps[0]);
			//y2 = (limits[0] * tmps[0] + tmps[1]);
		}

		if (x1 < limits[0])
			x1 = limits[0];
		else if (x1 > limits[1])
			x1 = limits[1];

		/*if (y1 < limits[2])
			y1 = limits[2];
		else if (y1 > limits[3])
			y1 = limits[3];*/

		if (x2 < limits[0])
			x2 = limits[0];
		else if (x2 > limits[1])
			x2 = limits[1];

		/*if (y2 < limits[2])
			y2 = limits[2];
		else if (y2 > limits[3])
			y2 = limits[3];*/

		int n = 100;

		float[] xValues = new float[n];
		float[] yValues = new float[n];
		if (logScales[0]) {
			x1 = Math.log10(x1);
			x2 = Math.log10(x2);
		}
		double xInterval = (x2 - x1) / n;
		for (int i = 0; i < n; i++) {

			if (logScales[0])
				xValues[i] = (float) Math.pow(10, x1 + xInterval * i);
			else
				xValues[i] = (float) (x1 + xInterval * i);

			yValues[i] = (float) (xValues[i] * tmps[0] + tmps[1]);

		}

		plots[iSlice].addPoints(xValues, yValues, null, Plot.LINE, "Linear Regression");

	}

	/**
	 * Because ImageJ Plot class doesn't offer a way to remove PlotObjects I
	 * have to replot the whole thing to remove extra Costes' threshold lines
	 */
	public ImagePlus replot() {

		addPlots(xLabels, yLabels, xValues, yValues, sliceLabels);
		return imp;
	}

	/**
	 * Tell if a plot really uses log scale
	 * 
	 * @param plot
	 * @return {xTruelogScale, yTruelogScale}
	 */
	public boolean[] getTrueLogs(Plot plot) {
		boolean[] trueLogs = new boolean[2];
		double xScale1 = plot.scaleXtoPxl(10) - plot.scaleXtoPxl(1);
		// if (plot.logXAxis == true) xScale1 = 9 * plot.xScale
		// else xScale1 = plot.xScale
		double xScale2 = plot.scaleXtoPxl(100) - plot.scaleXtoPxl(10);
		// if (plot.logXAxis == true) xScale2 = 99 * plot.xScale
		// else xScale2 = plot.xScale
		// Assuming xScale != 0, we can tell whether logXAxis is true or not
		trueLogs[0] = xScale2 < 5 * xScale1;
		double yScale1 = plot.scaleYtoPxl(1) - plot.scaleYtoPxl(10);
		// if (plot.logYAxis == true) yScale1 = 9 * plot.yScale
		// else yScale1 = plot.yScale
		double yScale2 = plot.scaleYtoPxl(10) - plot.scaleYtoPxl(100);
		// if (plot.logYAxis == true) yScale2 = 99 * plot.yScale
		// else yScale2 = plot.xScale
		trueLogs[1] = yScale2 < 5 * yScale1;
		return trueLogs;
	}

	public double[] getTrueLimits(Plot plot) {
		boolean[] trueLogs = getTrueLogs(plot);
		double[] limits = plot.getLimits();
		if (trueLogs[0]) {
			limits[0] = Math.pow(10, limits[0]);
			limits[1] = Math.pow(10, limits[1]);
		}
		if (trueLogs[1]) {
			limits[2] = Math.pow(10, limits[2]);
			limits[3] = Math.pow(10, limits[3]);
		}
		return limits;
	}

}