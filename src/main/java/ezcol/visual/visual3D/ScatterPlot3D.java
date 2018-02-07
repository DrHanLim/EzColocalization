/*******************************************************************************
 * EDIT: This plugin is modified by Huanjie Sheng from Plot3D by Jay Unruh.
 * EDIT: This version will take in zValues with the same dimension as x and y
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import ij.process.*;

public class ScatterPlot3D {
	// private Label coordinates;
	protected float[][] xValues;
	protected float[][] yValues;
	protected float[][] zValues;
	// npts[i] denote the number of pixel of j-th series.
	// This is used to coordinate the number of pixel plotted in case of
	// dimension mismatch
	protected int[] npts;

	protected float xMin, xMax, yMin, yMax, zMin, zMax, xScale, yScale, zScale;
	protected float logxmin, logymin, logxscale, logyscale, logxmax, logymax, logzmin, logzmax, logzscale;
	protected int maxpts, nseries;
	protected int selected;
	protected double rotx, roty, rotz;
	protected int[] shapes, colors;
	protected String xLabel, yLabel, zLabel;
	protected boolean logx, logy, logz;
	public static final int WIDTH = 250;
	public static final int HEIGHT = 150;
	public static final int TICK_LENGTH = 3; // length of ticks
	public final Color gridColor = new Color(0xc0c0c0); // light gray
	public static final int LEFT_MARGIN = 125;
	public static final int RIGHT_MARGIN = 125;
	public static final int TOP_MARGIN = 175;
	public static final int BOTTOM_MARGIN = 175;
	public static final int shapesize = 5;

	// EDIT:
	protected int currSeries;
	protected String[] labels;
	// the same length as nseries, customized colors for nseries
	protected float[] customColors;
	// denote color ladder
	protected float[] customScales;
	protected Color[] colorScales;

	protected Renderer jr;
	protected boolean plotChanged = true, plotRotated = true;

	public ScatterPlot3D() {
	} // empty constructor for subclasses

	public ScatterPlot3D(String xLabel1, String yLabel1, String zLabel1, float[][] xValues1, float[][] yValues1,
			float[][] zValues1) {
		xValues = xValues1;
		yValues = yValues1;
		zValues = zValues1;
		xLabel = xLabel1;
		yLabel = yLabel1;
		zLabel = zLabel1;
		nseries = zValues.length;

		npts = new int[nseries];
		maxpts = zValues[0].length;
		for (int i = 0; i < nseries; i++) {
			npts[i] = zValues[i].length;
			if (maxpts < npts[i])
				maxpts = npts[i];
		}

		float[] temp = findminmax(xValues, npts);
		xMin = temp[0];
		xMax = temp[1];
		temp = findminmax(yValues, npts);
		yMin = temp[0];
		yMax = temp[1];
		temp = findminmax(zValues, npts);
		zMin = temp[0];
		zMax = temp[1];
		logx = false;
		logy = false;
		logz = false;
		shapes = new int[nseries];
		colors = new int[nseries];
		for (int i = 0; i < nseries; i++) {
			colors[i] = i;
		}
		selected = -1;
		rotx = -60.0;
		roty = 0.0;
		rotz = -45.0;
	}

	public ScatterPlot3D(String xLabel1, String yLabel1, String zLabel1, float[] xValues1, float[] yValues1,
			float[] zValues1) {

		xValues = new float[1][zValues1.length];
		for (int i = 0; i < zValues1.length; i++) {
			xValues[0][i] = xValues1[i];
		}
		yValues = new float[1][zValues1.length];
		for (int i = 0; i < zValues1.length; i++) {
			yValues[0][i] = yValues1[i];
		}
		zValues = new float[1][zValues1.length];
		for (int i = 0; i < zValues1.length; i++) {
			zValues[0][i] = zValues1[i];
		}

		xLabel = xLabel1;
		yLabel = yLabel1;
		zLabel = zLabel1;
		maxpts = zValues1.length;
		npts = new int[1];
		npts[0] = maxpts;
		nseries = 1;
		float[] temp = findminmax(xValues, npts);
		xMin = temp[0];
		xMax = temp[1];
		temp = findminmax(yValues, npts);
		yMin = temp[0];
		yMax = temp[1];
		temp = findminmax(zValues, npts);
		zMin = temp[0];
		zMax = temp[1];
		logx = false;
		logy = false;
		logz = false;
		shapes = new int[nseries];
		colors = new int[nseries];
		for (int i = 0; i < nseries; i++) {
			colors[i] = i;
		}
		selected = -1;
		rotx = -60.0;
		roty = 0.0;
		rotz = -45.0;
	}

	public ScatterPlot3D(String xLabel1, String yLabel1, String zLabel1, float[] zValues1, int startxy) {
		xValues = new float[1][zValues1.length];
		for (int i = 0; i < zValues1.length; i++) {
			xValues[0][i] = (float) (i + startxy);
		}
		yValues = new float[1][zValues1.length];
		for (int i = 0; i < zValues1.length; i++) {
			yValues[0][i] = (float) (i + startxy);
		}
		zValues = new float[1][zValues1.length];
		for (int i = 0; i < zValues1.length; i++) {
			zValues[0][i] = zValues1[i];
		}
		xLabel = xLabel1;
		yLabel = yLabel1;
		zLabel = zLabel1;
		maxpts = zValues1.length;
		npts = new int[1];
		npts[0] = maxpts;
		nseries = 1;
		float[] temp = findminmax(xValues, npts);
		xMin = temp[0];
		xMax = temp[1];
		temp = findminmax(yValues, npts);
		yMin = temp[0];
		yMax = temp[1];
		temp = findminmax(zValues, npts);
		zMin = temp[0];
		zMax = temp[1];
		logx = false;
		logy = false;
		logz = false;
		shapes = new int[nseries];
		colors = new int[nseries];
		for (int i = 0; i < nseries; i++) {
			colors[i] = i;
		}
		selected = -1;
		rotx = -60.0;
		roty = 0.0;
		rotz = -45.0;
	}

	public ScatterPlot3D(String xLabel1, String yLabel1, String zLabel1, float[][] zValues1, int startxy) {
		zValues = zValues1;
		xLabel = xLabel1;
		yLabel = yLabel1;
		zLabel = zLabel1;
		nseries = zValues.length;
		npts = new int[nseries];
		maxpts = zValues[0].length;
		for (int i = 0; i < nseries; i++) {
			npts[i] = zValues[i].length;
			if (maxpts < npts[i])
				maxpts = npts[i];
		}

		xValues = new float[nseries][maxpts];
		yValues = new float[nseries][maxpts];
		for (int i = 0; i < nseries; i++) {
			for (int j = 0; j < npts[i]; j++) {
				xValues[i][j] = (float) (j + startxy);
			}
			for (int j = 0; j < npts[i]; j++) {
				yValues[i][j] = (float) (j + startxy);
			}
		}
		float[] temp = findminmax(xValues, npts);
		xMin = temp[0];
		xMax = temp[1];
		temp = findminmax(yValues, npts);
		yMin = temp[0];
		yMax = temp[1];
		temp = findminmax(zValues, npts);
		zMin = temp[0];
		zMax = temp[1];
		logx = false;
		logy = false;
		logz = false;
		shapes = new int[nseries];
		colors = new int[nseries];
		for (int i = 0; i < nseries; i++) {
			colors[i] = i;
		}
		selected = -1;
		rotx = -60.0;
		roty = 0.0;
		rotz = -45.0;
	}

	public ScatterPlot3D(String xLabel1, String yLabel1, String zLabel1, float[] xValues1, float[] yValues1,
			float[] zValues1, int shape) {
		this(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1);
		shapes[0] = shape;
	}

	public ScatterPlot3D(String xLabel1, String yLabel1, String zLabel1, float[][] xValues1, float[][] yValues1,
			float[][] zValues1, int[] shapes) {
		this(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1);
		this.shapes = shapes;
	}

	public ScatterPlot3D(String xLabel1, String yLabel1, String zLabel1, float[][] xValues1, float[][] yValues1,
			float[][] zValues1, int[] shapes, String[] labels) {
		this(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1, shapes);
		this.labels = labels;
	}

	public ScatterPlot3D(String xLabel1, String yLabel1, String zLabel1, float[][] xValues1, float[][] yValues1,
			float[][] zValues1, int[] shapes, float[] customColors, float[] customScales, Color[] colorScales) {
		this(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1, shapes);
		this.customColors = customColors;
		this.customScales = customScales;
		this.colorScales = colorScales;
		this.currSeries = nseries;
	}

	/*****
	 * File saving is currently unavailable. Might be released later
	 */
	/*
	 * public Scatter3DPlot(InputStream is) { init_from_is(is); }
	 * 
	 * public Scatter3DPlot(String filename) { try { InputStream is = new
	 * BufferedInputStream(new FileInputStream(filename)); init_from_is(is);
	 * is.close(); } catch (IOException e) { return; } }
	 * 
	 * private void init_from_is(InputStream is) { jdataio jdio = new jdataio();
	 * jdio.readstring(is); // read the label jdio.readintelint(is); // now the
	 * identifier xLabel = jdio.readstring(is); yLabel = jdio.readstring(is);
	 * zLabel = jdio.readstring(is); nseries = jdio.readintelint(is); maxpts =
	 * jdio.readintelint(is); npts = new int[nseries]; xValues = new
	 * float[nseries][maxpts]; yValues = new float[nseries][maxpts]; zValues =
	 * new float[nseries][maxpts]; shapes = new int[nseries]; colors = new
	 * int[nseries]; xMin = jdio.readintelfloat(is); xMax =
	 * jdio.readintelfloat(is); yMin = jdio.readintelfloat(is); yMax =
	 * jdio.readintelfloat(is); zMin = jdio.readintelfloat(is); zMax =
	 * jdio.readintelfloat(is); logx = jdio.readintelint(is) == 1; logy =
	 * jdio.readintelint(is) == 1; logz = jdio.readintelint(is) == 1; for (int l
	 * = 0; l < nseries; l++) { npts[l] = jdio.readintelint(is); shapes[l] =
	 * jdio.readintelint(is); colors[l] = jdio.readintelint(is);
	 * jdio.readintelfloatfile(is, npts[l], xValues[l]);
	 * jdio.readintelfloatfile(is, npts[l], yValues[l]);
	 * jdio.readintelfloatfile(is, npts[l], zValues[l]); // y // values }
	 * selected = -1; rotx = -60.0; roty = 0.0; rotz = -45.0; }
	 * 
	 * public static boolean is_this(String filename) { int temp = -1; try {
	 * InputStream is = new BufferedInputStream(new FileInputStream(filename));
	 * jdataio jdio = new jdataio(); jdio.readstring(is); // read the label temp
	 * = jdio.readintelint(is); // now the identifier is.close(); } catch
	 * (IOException e) { return false; } if (temp == 1) return true; else return
	 * false; }
	 */

	protected float[] findminmax(float[][] arr, int[] npts1) {
		float[] temp = new float[2];
		temp[0] = arr[0][0];
		temp[1] = arr[0][0];
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < npts1[i]; j++) {

				if (arr[i][j] < temp[0]) {
					temp[0] = arr[i][j];
				}
				if (arr[i][j] > temp[1]) {
					temp[1] = arr[i][j];
				}
			}
		}
		return temp;
	}

	protected float findmingt0(float[][] arr, int[] npts1, float max) {
		float temp = max;
		if (max <= 0.0f) {
			return 0.0f;
		}
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < npts1[i]; j++) {
				if (arr[i][j] < temp && arr[i][j] > 0.0f) {
					temp = arr[i][j];
				}
			}
		}
		return temp;
	}

	/** Sets the x-axis and y-axis range. */
	public void setLimits(double xMin1, double xMax1, double yMin1, double yMax1, double zMin1, double zMax1) {
		xMin = (float) xMin1;
		xMax = (float) xMax1;
		yMin = (float) yMin1;
		yMax = (float) yMax1;
		zMin = (float) zMin1;
		zMax = (float) zMax1;
		plotChanged = true;
	}

	public void setLimits(float[] limits) {
		xMin = limits[0];
		xMax = limits[1];
		yMin = limits[2];
		yMax = limits[3];
		zMin = limits[4];
		zMax = limits[5];
		plotChanged = true;
	}

	/** Sets the x-axis and y-axis range. */
	public void setLogAxes(boolean logx1, boolean logy1, boolean logz1) {
		logx = logx1;
		logy = logy1;
		logz = logz1;
		plotChanged = true;
	}

	public void autoscale() {
		float[] temp = findminmax(xValues, npts);
		xMin = temp[0];
		xMax = temp[1];
		temp = findminmax(yValues, npts);
		yMin = temp[0];
		yMax = temp[1];
		temp = findminmax(zValues, npts);
		zMin = temp[0];
		zMax = temp[1];
	}

	public void xautoscale() {
		float[] temp = findminmax(xValues, npts);
		xMin = temp[0];
		xMax = temp[1];
	}

	public void yautoscale() {
		float[] temp = findminmax(yValues, npts);
		yMin = temp[0];
		yMax = temp[1];
	}

	public void zautoscale() {
		float[] temp = findminmax(zValues, npts);
		zMin = temp[0];
		zMax = temp[1];
	}

	public void setrotation(double xrot1, double yrot1, double zrot1) {
		rotx = xrot1;
		roty = yrot1;
		rotz = zrot1;
		plotRotated = true;
	}

	public void setrotation(double[] rotation) {
		rotx = rotation[0];
		roty = rotation[1];
		rotz = rotation[2];
		plotRotated = true;
	}

	public double[] getrotation() {
		double[] rotation = { rotx, roty, rotz };
		return rotation;
	}

	public void updateSeries(float[] xValues1, float[] yValues1, float[] zValues1, int series, boolean rescale) {
		int length = zValues1.length;
		npts[series] = length;
		if (length > maxpts) {
			if (length > maxpts) {
				maxpts = length;
			}
			float[][] newxValues = new float[nseries][maxpts];
			float[][] newyValues = new float[nseries][maxpts];
			float[][] newzValues = new float[nseries][maxpts];
			for (int i = 0; i < series; i++) {
				for (int j = 0; j < npts[i]; j++) {
					newxValues[i][j] = xValues[i][j];
					newyValues[i][j] = yValues[i][j];
					newzValues[i][j] = zValues[i][j];
				}
			}
			for (int j = 0; j < npts[series]; j++) {
				newxValues[series][j] = xValues1[j];
				newyValues[series][j] = yValues1[j];
				newzValues[series][j] = zValues1[j];
			}

			for (int i = series + 1; i < nseries; i++) {
				for (int j = 0; j < npts[i]; j++) {
					newxValues[i][j] = xValues[i][j];
					newyValues[i][j] = yValues[i][j];
					newzValues[i][j] = zValues[i][j];
				}
			}
			xValues = newxValues;
			yValues = newyValues;
			zValues = newzValues;
			if (rescale) {
				autoscale();
			}
		} else {
			for (int i = 0; i < length; i++) {
				xValues[series][i] = xValues1[i];
				yValues[series][i] = yValues1[i];
				zValues[series][i] = zValues1[i];
			}

			if (rescale) {
				autoscale();
			}
		}
		plotChanged = true;
	}

	public void updateSeries(float[] zValues1, int series, boolean rescale) {
		float[] xValues1 = getXValues(series);
		float[] yValues1 = getYValues(series);
		updateSeries(xValues1, yValues1, zValues1, series, rescale);
		plotChanged = true;
	}

	public void deleteSeries(int series, boolean rescale) {
		nseries -= 1;
		float[][] newxValues;
		float[][] newyValues;
		float[][] newzValues;
		int[] newnpts = new int[nseries];
		int[] newshapes = new int[nseries];
		int[] newcolors = new int[nseries];
		int newmaxpts = 0;
		if (npts[series] == maxpts) {
			for (int i = 0; i <= nseries; i++) {
				if (i != series) {
					if (npts[i] > newmaxpts) {
						newmaxpts = npts[i];
					}
				}
			}
		} else {
			newmaxpts = maxpts;
		}

		newxValues = new float[nseries][newmaxpts];
		newyValues = new float[nseries][newmaxpts];
		newzValues = new float[nseries][newmaxpts];
		for (int i = 0; i < series; i++) {
			newnpts[i] = npts[i];
			newshapes[i] = shapes[i];
			newcolors[i] = colors[i];
			for (int j = 0; j < newmaxpts; j++) {
				newxValues[i][j] = xValues[i][j];
				newyValues[i][j] = yValues[i][j];
				newzValues[i][j] = zValues[i][j];
			}
		}
		for (int i = series + 1; i <= nseries; i++) {
			newnpts[i - 1] = npts[i];
			newshapes[i - 1] = shapes[i];
			newcolors[i - 1] = colors[i];
			for (int j = 0; j < newmaxpts; j++) {
				newxValues[i - 1][j] = xValues[i][j];
				newyValues[i - 1][j] = yValues[i][j];
				newzValues[i - 1][j] = zValues[i][j];
			}
		}
		maxpts = newmaxpts;
		npts = newnpts;
		xValues = newxValues;
		yValues = newyValues;
		zValues = newzValues;
		shapes = newshapes;
		colors = newcolors;
		if (rescale) {
			autoscale();
		}
		// IJ.showMessage("Plot Deleted");
		if (selected >= nseries) {
			selected = -1;
		}
		plotChanged = true;
		// IJ.showMessage("Selected = "+selected);
	}

	public void addPoints(float[] xValues1, float[] yValues1, float[] zValues1, boolean rescale) {
		nseries++;
		float[][] newxValues;
		float[][] newyValues;
		float[][] newzValues;
		int[] newnpts = new int[nseries];
		int[] newshapes = new int[nseries];
		int[] newcolors = new int[nseries];
		if (yValues1.length > maxpts || xValues1.length > maxpts | zValues1.length > maxpts) {
			maxpts = findmax(new int[] { xValues1.length, yValues1.length, zValues1.length });
			newxValues = new float[nseries][maxpts];
			newyValues = new float[nseries][maxpts];
			newzValues = new float[nseries][maxpts];
			for (int i = 0; i < (nseries - 1); i++) {
				newnpts[i] = npts[i];
				newshapes[i] = shapes[i];
				newcolors[i] = colors[i];
				for (int j = 0; j < npts[i]; j++) {
					newxValues[i][j] = xValues[i][j];
					newyValues[i][j] = yValues[i][j];
					newzValues[i][j] = zValues[i][j];
				}
			}
			newnpts[nseries - 1] = maxpts;
			newshapes[nseries - 1] = 0;
			newcolors[nseries - 1] = nseries - 1;
			for (int j = 0; j < maxpts; j++) {
				newxValues[nseries - 1][j] = xValues1[j];
				newyValues[nseries - 1][j] = yValues1[j];
				newzValues[nseries - 1][j] = zValues1[j];
			}
		} else {
			newxValues = new float[nseries][maxpts];
			newyValues = new float[nseries][maxpts];
			newzValues = new float[nseries][maxpts];
			for (int i = 0; i < (nseries - 1); i++) {
				newnpts[i] = npts[i];
				newshapes[i] = shapes[i];
				newcolors[i] = colors[i];
				for (int j = 0; j < maxpts; j++) {
					newxValues[i][j] = xValues[i][j];
					newyValues[i][j] = yValues[i][j];
					newzValues[i][j] = zValues[i][j];
				}
			}
			newnpts[nseries - 1] = zValues1.length;
			newshapes[nseries - 1] = 0;
			newcolors[nseries - 1] = nseries - 1;
			for (int j = 0; j < newnpts[nseries - 1]; j++) {
				newxValues[nseries - 1][j] = xValues1[j];
				newyValues[nseries - 1][j] = yValues1[j];
				newzValues[nseries - 1][j] = zValues1[j];
			}
		}
		npts = newnpts;
		shapes = newshapes;
		colors = newcolors;
		xValues = newxValues;
		yValues = newyValues;
		zValues = newzValues;
		if (selected >= nseries) {
			selected = -1;
		}
		if (rescale) {
			autoscale();
		}
		plotChanged = true;
	}

	public void addPoints(float[] zValues1, boolean rescale, int startxy) {
		float[] xValues1 = new float[zValues1.length];
		for (int i = 0; i < zValues1.length; i++) {
			xValues1[i] = (float) (i + startxy);
		}
		float[] yValues1 = new float[zValues1.length];
		for (int i = 0; i < zValues1.length; i++) {
			yValues1[i] = (float) (i + startxy);
		}
		addPoints(xValues1, yValues1, zValues1, rescale);
		plotChanged = true;
	}

	public int getWidth() {
		return WIDTH + LEFT_MARGIN + RIGHT_MARGIN;
	}

	public int getHeight() {
		return HEIGHT + TOP_MARGIN + BOTTOM_MARGIN;
	}

	protected void drawPlot(Renderer jr) {

		logxmin = 0;
		logymin = 0;
		logxscale = 0;
		logyscale = 0;
		logxmax = 0;
		logymax = 0;
		logzmin = 0;
		logzmax = 0;
		logzscale = 0;
		if (logx) {
			if (xMin <= 0.0f) {
				logxmin = (float) Math.log((double) findmingt0(xValues, npts, xMax));
			} else {
				logxmin = (float) Math.log((double) xMin);
			}
			logxmax = (float) Math.log((double) xMax);
			logxscale = (float) WIDTH / (logxmax - logxmin);
		}
		if (logy) {
			if (yMin <= 0.0f) {
				logymin = (float) Math.log((double) findmingt0(yValues, npts, yMax));
			} else {
				logymin = (float) Math.log((double) yMin);
			}
			logymax = (float) Math.log((double) yMax);
			logyscale = (float) WIDTH / (logymax - logymin);
		}
		if (logz) {
			if (zMin <= 0.0f) {
				logzmin = (float) Math.log((double) findmingt0(zValues, npts, zMax));
			} else {
				logzmin = (float) Math.log((double) zMin);
			}
			logzmax = (float) Math.log((double) zMax);
			logzscale = (float) HEIGHT / (logzmax - logzmin);
		}
		// IJ.showMessage("testdraw1");

		xScale = (float) WIDTH / (xMax - xMin);
		yScale = (float) WIDTH / (yMax - yMin);
		zScale = (float) HEIGHT / (zMax - zMin);

		drawAxisLabels(jr);

		int startj, endj;
		if (currSeries == nseries) {
			startj = 0;
			endj = nseries;
		} else {
			startj = currSeries;
			endj = currSeries + 1;
		}

		if (!logx && !logy && !logz) {
			for (int j = startj; j < endj; j++) {
				Color tempcolor = getCustomColor(j);
				int xpoints[] = new int[npts[j]];
				int ypoints[] = new int[npts[j]];
				int zpoints[] = new int[npts[j]];
				for (int i = 0; i < npts[j]; i++) {
					xpoints[i] = LEFT_MARGIN + (int) ((xValues[j][i] - xMin) * xScale);
					if (xpoints[i] < LEFT_MARGIN) {
						xpoints[i] = LEFT_MARGIN;
					}
					if (xpoints[i] > LEFT_MARGIN + WIDTH) {
						xpoints[i] = LEFT_MARGIN + WIDTH;
					}

					ypoints[i] = LEFT_MARGIN + (int) ((yValues[j][i] - yMin) * yScale);
					if (ypoints[i] < LEFT_MARGIN) {
						ypoints[i] = LEFT_MARGIN;
					}
					if (ypoints[i] > LEFT_MARGIN + WIDTH) {
						ypoints[i] = LEFT_MARGIN + WIDTH;
					}

					zpoints[i] = TOP_MARGIN + HEIGHT - (int) ((zValues[j][i] - zMin) * zScale);
					if (zpoints[i] < TOP_MARGIN) {
						zpoints[i] = TOP_MARGIN;
					}
					if (zpoints[i] > TOP_MARGIN + HEIGHT) {
						zpoints[i] = TOP_MARGIN + HEIGHT;
					}

				}

				if (j != selected) {
					if (shapes[j] == 0) {
						drawPolyline(jr, xpoints, ypoints, zpoints, npts[j], tempcolor);
					} else if (shapes[j] < 0) {
						drawCubes(jr, xpoints, ypoints, zpoints, npts[j], shapes[j], tempcolor);
					} else {
						drawPolyshape(jr, xpoints, ypoints, zpoints, npts[j], shapes[j], tempcolor);
					}
				} else {
					if (shapes[j] == 0) {
						drawPolyshape(jr, xpoints, ypoints, zpoints, npts[j], 1, tempcolor);
					} else if (shapes[j] < 0) {
						drawCubes(jr, xpoints, ypoints, zpoints, npts[j], shapes[j], tempcolor);
					} else {
						drawPolyline(jr, xpoints, ypoints, zpoints, npts[j], tempcolor);
					}
				}
			}
		} else {
			for (int j = startj; j < endj; j++) {
				Color tempcolor = getCustomColor(j);
				int xpoints[] = new int[npts[j]];
				int ypoints[] = new int[npts[j]];
				int zpoints[] = new int[npts[j]];
				for (int i = 0; i < npts[j]; i++) {
					if (logx) {
						float xtemp;
						if (xValues[j][i] > 0.0f) {
							xtemp = (float) Math.log((double) xValues[j][i]);
						} else {
							xtemp = logxmin;
						}
						xpoints[i] = LEFT_MARGIN + (int) ((xtemp - logxmin) * logxscale);
					} else {
						xpoints[i] = LEFT_MARGIN + (int) ((xValues[j][i] - xMin) * xScale);
					}
					if (xpoints[i] < LEFT_MARGIN) {
						xpoints[i] = LEFT_MARGIN;
					}
					if (xpoints[i] > LEFT_MARGIN + WIDTH) {
						xpoints[i] = LEFT_MARGIN + WIDTH;
					}

					if (logy) {
						float ytemp;
						if (yValues[j][i] > 0.0f) {
							ytemp = (float) Math.log((double) yValues[j][i]);
						} else {
							ytemp = logymin;
						}
						ypoints[i] = LEFT_MARGIN + (int) ((ytemp - logymin) * logyscale);
					} else {
						ypoints[i] = LEFT_MARGIN + (int) ((yValues[j][i] - yMin) * yScale);
					}
					if (ypoints[i] < LEFT_MARGIN) {
						ypoints[i] = LEFT_MARGIN;
					}
					if (ypoints[i] > LEFT_MARGIN + WIDTH) {
						ypoints[i] = LEFT_MARGIN + WIDTH;
					}

					if (logz) {
						float ztemp;
						if (zValues[j][i] > 0.0f) {
							ztemp = (float) Math.log((double) zValues[j][i]);
						} else {
							ztemp = logzmin;
						}
						zpoints[i] = TOP_MARGIN + HEIGHT - (int) ((ztemp - logzmin) * logzscale);
					} else {
						zpoints[i] = TOP_MARGIN + HEIGHT - (int) ((zValues[j][i] - zMin) * zScale);
					}
					if (zpoints[i] < TOP_MARGIN) {
						zpoints[i] = TOP_MARGIN;
					}
					if (zpoints[i] > TOP_MARGIN + HEIGHT) {
						zpoints[i] = TOP_MARGIN + HEIGHT;
					}
				}

				if (j != selected) {
					if (shapes[j] == 0) {
						drawPolyline(jr, xpoints, ypoints, zpoints, npts[j], tempcolor);
					} else if (shapes[j] < 0) {
						drawCubes(jr, xpoints, ypoints, zpoints, npts[j], shapes[j], tempcolor);
					} else {
						drawPolyshape(jr, xpoints, ypoints, zpoints, npts[j], shapes[j], tempcolor);
					}
				} else {
					if (shapes[j] == 0) {
						drawPolyshape(jr, xpoints, ypoints, zpoints, npts[j], 1, tempcolor);
					} else if (shapes[j] < 0) {
						drawCubes(jr, xpoints, ypoints, zpoints, npts[j], shapes[j], tempcolor);
					} else {
						drawPolyline(jr, xpoints, ypoints, zpoints, npts[j], tempcolor);
					}
				}
			}
		}
		rotatePlot(jr);
		plotChanged = false;
	}

	private void drawAxisLabels(Renderer jr) {

		// calculate the appropriate label numbers
		float[] xticklabels = new float[4];
		float[] yticklabels = new float[4];
		float[] zticklabels = new float[4];
		for (int i = 0; i < 4; i++) {
			if (logx) {
				float tempx = logxmin + ((float) i / 3.0f) * (logxmax - logxmin);
				xticklabels[i] = (float) Math.exp((double) tempx);
			} else {
				xticklabels[i] = xMin + ((float) i / 3.0f) * (xMax - xMin);
			}
			if (logy) {
				float tempy = logymin + ((float) i / 3.0f) * (logymax - logymin);
				yticklabels[i] = (float) Math.exp((double) tempy);
			} else {
				yticklabels[i] = yMin + ((float) i / 3.0f) * (yMax - yMin);
			}
			if (logz) {
				float tempz = logzmin + ((float) i / 3.0f) * (logzmax - logzmin);
				zticklabels[i] = (float) Math.exp((double) tempz);
			} else {
				zticklabels[i] = zMin + ((float) i / 3.0f) * (zMax - zMin);
			}
		}

		// draw the z axis labels
		String s = formatted_string((double) zticklabels[0]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH + 10, LEFT_MARGIN, TOP_MARGIN + HEIGHT - 5, Color.BLACK);
		s = formatted_string((double) zticklabels[1]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH + 10, LEFT_MARGIN, TOP_MARGIN + (int) ((2 * HEIGHT) / 3) - 5, Color.BLACK);
		s = formatted_string((double) zticklabels[2]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH + 10, LEFT_MARGIN, TOP_MARGIN + (int) (HEIGHT / 3) - 5, Color.BLACK);
		s = formatted_string((double) zticklabels[3]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH + 10, LEFT_MARGIN, TOP_MARGIN - 5, Color.BLACK);

		jr.addText3D(zLabel, LEFT_MARGIN + WIDTH + 25, LEFT_MARGIN, TOP_MARGIN + HEIGHT / 2 - 15, Color.BLACK);

		// now the x axis labels
		s = formatted_string((double) xticklabels[0]);
		jr.addText3D(s, LEFT_MARGIN - 10, LEFT_MARGIN + WIDTH + 40, TOP_MARGIN + HEIGHT, Color.BLACK);
		s = formatted_string((double) xticklabels[1]);
		jr.addText3D(s, LEFT_MARGIN + (int) (WIDTH / 3) - 10, LEFT_MARGIN + WIDTH + 40, TOP_MARGIN + HEIGHT,
				Color.BLACK);
		s = formatted_string((double) xticklabels[2]);
		jr.addText3D(s, LEFT_MARGIN + (int) (2 * WIDTH / 3) - 10, LEFT_MARGIN + WIDTH + 40, TOP_MARGIN + HEIGHT,
				Color.BLACK);
		s = formatted_string((double) xticklabels[3]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH - 10, LEFT_MARGIN + WIDTH + 40, TOP_MARGIN + HEIGHT, Color.BLACK);

		jr.addText3D(xLabel, LEFT_MARGIN + WIDTH / 2, LEFT_MARGIN + WIDTH + 60, TOP_MARGIN + HEIGHT, Color.BLACK);

		// and the y axis labels
		s = formatted_string((double) yticklabels[0]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH + 20, LEFT_MARGIN + 10, TOP_MARGIN + HEIGHT, Color.BLACK);
		s = formatted_string((double) yticklabels[1]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH + 20, LEFT_MARGIN + (int) (WIDTH / 3) + 10, TOP_MARGIN + HEIGHT,
				Color.BLACK);
		s = formatted_string((double) yticklabels[2]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH + 20, LEFT_MARGIN + (int) (2 * WIDTH / 3) + 10, TOP_MARGIN + HEIGHT,
				Color.BLACK);
		s = formatted_string((double) yticklabels[3]);
		jr.addText3D(s, LEFT_MARGIN + WIDTH + 20, LEFT_MARGIN + WIDTH + 10, TOP_MARGIN + HEIGHT, Color.BLACK);

		jr.addText3D(yLabel, LEFT_MARGIN + WIDTH + 60, LEFT_MARGIN + WIDTH / 2, TOP_MARGIN + HEIGHT, Color.BLACK);

		// finally the grid lines
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN + HEIGHT, Color.BLACK);
		jr.addLine3D(LEFT_MARGIN + WIDTH / 3, LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN + WIDTH / 3, LEFT_MARGIN,
				TOP_MARGIN + HEIGHT, gridColor);
		jr.addLine3D(LEFT_MARGIN + (2 * WIDTH) / 3, LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN + (2 * WIDTH) / 3, LEFT_MARGIN,
				TOP_MARGIN + HEIGHT, gridColor);
		jr.addLine3D(LEFT_MARGIN + WIDTH, LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN + WIDTH, LEFT_MARGIN,
				TOP_MARGIN + HEIGHT, Color.BLACK);

		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN + WIDTH, LEFT_MARGIN, TOP_MARGIN, Color.BLACK);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN + HEIGHT / 3, LEFT_MARGIN + WIDTH, LEFT_MARGIN,
				TOP_MARGIN + HEIGHT / 3, gridColor);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN + (2 * HEIGHT) / 3, LEFT_MARGIN + WIDTH, LEFT_MARGIN,
				TOP_MARGIN + (2 * HEIGHT) / 3, gridColor);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN + HEIGHT, LEFT_MARGIN + WIDTH, LEFT_MARGIN,
				TOP_MARGIN + HEIGHT, Color.BLACK);

		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN + WIDTH / 3, TOP_MARGIN, LEFT_MARGIN, LEFT_MARGIN + WIDTH / 3,
				TOP_MARGIN + HEIGHT, gridColor);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN + (2 * WIDTH) / 3, TOP_MARGIN, LEFT_MARGIN, LEFT_MARGIN + (2 * WIDTH) / 3,
				TOP_MARGIN + HEIGHT, gridColor);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN + WIDTH, TOP_MARGIN, LEFT_MARGIN, LEFT_MARGIN + WIDTH,
				TOP_MARGIN + HEIGHT, Color.BLACK);

		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN, LEFT_MARGIN, LEFT_MARGIN + WIDTH, TOP_MARGIN, Color.BLACK);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN + HEIGHT / 3, LEFT_MARGIN, LEFT_MARGIN + WIDTH,
				TOP_MARGIN + HEIGHT / 3, gridColor);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN + (2 * HEIGHT) / 3, LEFT_MARGIN, LEFT_MARGIN + WIDTH,
				TOP_MARGIN + (2 * HEIGHT) / 3, gridColor);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN, TOP_MARGIN + HEIGHT, LEFT_MARGIN, LEFT_MARGIN + WIDTH,
				TOP_MARGIN + HEIGHT, Color.BLACK);

		jr.addLine3D(LEFT_MARGIN + WIDTH / 3, LEFT_MARGIN, TOP_MARGIN + HEIGHT, LEFT_MARGIN + WIDTH / 3,
				LEFT_MARGIN + WIDTH, TOP_MARGIN + HEIGHT, gridColor);
		jr.addLine3D(LEFT_MARGIN + (2 * WIDTH) / 3, LEFT_MARGIN, TOP_MARGIN + HEIGHT, LEFT_MARGIN + (2 * WIDTH) / 3,
				LEFT_MARGIN + WIDTH, TOP_MARGIN + HEIGHT, gridColor);
		jr.addLine3D(LEFT_MARGIN + WIDTH, LEFT_MARGIN, TOP_MARGIN + HEIGHT, LEFT_MARGIN + WIDTH, LEFT_MARGIN + WIDTH,
				TOP_MARGIN + HEIGHT, Color.BLACK);

		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN + WIDTH / 3, TOP_MARGIN + HEIGHT, LEFT_MARGIN + WIDTH,
				LEFT_MARGIN + WIDTH / 3, TOP_MARGIN + HEIGHT, gridColor);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN + (2 * WIDTH) / 3, TOP_MARGIN + HEIGHT, LEFT_MARGIN + WIDTH,
				LEFT_MARGIN + (2 * WIDTH) / 3, TOP_MARGIN + HEIGHT, gridColor);
		jr.addLine3D(LEFT_MARGIN, LEFT_MARGIN + WIDTH, TOP_MARGIN + HEIGHT, LEFT_MARGIN + WIDTH, LEFT_MARGIN + WIDTH,
				TOP_MARGIN + HEIGHT, Color.BLACK);
	}

	private void drawPolyline(Renderer jr, int[] xpoints, int[] ypoints, int[] zpoints, int npts, Color color) {
		for (int i = 1; i < npts; i++) {
			jr.addLine3D(xpoints[i - 1], ypoints[i], zpoints[i - 1], xpoints[i], ypoints[i], zpoints[i], color);
		}
	}

	private void drawPolyshape(Renderer jr, int[] xpoints, int[] ypoints, int[] zpoints, int npts, int shape,
			Color color) {
		for (int i = 0; i < npts; i++) {
			// jr.addPoint3D(xpoints[j],ypoints[i],zpoints[j][i],shapesize,color,Point3D.CIRCLE);
			jr.addPoint3D(xpoints[i], ypoints[i], zpoints[i], shape - 1, color);
		}
	}

	private void drawCubes(Renderer jr, int[] xpoints, int[] ypoints, int[] zpoints, int npts, int shape, Color color) {
		for (int i = 0; i < npts; i++) {
			// jr.addPoint3D(xpoints[j],ypoints[i],zpoints[j][i],shapesize,color,Point3D.CIRCLE);
			jr.addCube3D(xpoints[i], ypoints[i], zpoints[i], -shape, color);
		}
	}

	Color getColor(int index) {
		int temp = index;
		if (temp >= 8) {
			temp = index % 8;
		}
		Color[] temp2 = { Color.black, Color.blue, new Color(0, 175, 0), Color.red, Color.magenta,
				new Color(0, 175, 175), new Color(175, 175, 0), new Color(255, 175, 0) };
		return temp2[temp];
	}

	public void selectSeries(int series) {
		selected = series;
		if (selected >= nseries) {
			selected = -1;
		}
	}

	public int getSelected() {
		return selected;
	}

	public float[][] getXValues() {
		return xValues;
	}

	public float[] getXValues(int series) {
		return xValues[series];
	}

	public float[][] getYValues() {
		return yValues;
	}

	public float[] getYValues(int series) {
		return yValues[series];
	}

	public float[][] getZValues() {
		return zValues;
	}

	public float[] getZValues(int series) {
		return zValues[series];
	}

	public String getxLabel() {
		return xLabel;
	}

	public void setxLabel(String xLabel1) {
		xLabel = xLabel1;
		plotChanged = true;
	}

	public String getyLabel() {
		return yLabel;
	}

	public void setyLabel(String yLabel1) {
		yLabel = yLabel1;
		plotChanged = true;
	}

	public String getzLabel() {
		return zLabel;
	}

	public void setzLabel(String zLabel1) {
		zLabel = zLabel1;
		plotChanged = true;
	}

	public int[] getNpts() {
		return npts;
	}

	public int getNSeries() {
		return nseries;
	}

	public int getmaxpts() {
		return maxpts;
	}

	public float[] getLimits() {
		float[] temp = { xMin, xMax, yMin, yMax, zMin, zMax };
		return temp;
	}

	public boolean[] getLogAxes() {
		boolean[] temp = { logx, logy, logz };
		return temp;
	}

	public int[] getShapes() {
		return shapes;
	}

	public int[] getColors() {
		return colors;
	}

	public ColorProcessor getProcessor() {
		return new ColorProcessor(getImage());
	}

	public void rotatePlot(Renderer jr) {
		jr.setrotation((int) rotx, (int) roty, (int) rotz);
		plotRotated = false;
	}

	public Image getImage() {

		if (jr == null)
			jr = new Renderer(getWidth(), getHeight());

		if (plotChanged){
			jr.flush();
			drawPlot(jr);
		}else if (plotRotated)
			rotatePlot(jr);

		if (!isCustomColor())
			jr.setBackground(Color.WHITE);

		Image renderedImg = jr.renderimage();

		Graphics g = renderedImg.getGraphics();
		if (isCustomColor()) {
			Dimension chartSize = new Dimension(getWidth() / 2, TOP_MARGIN / 8);
			g.drawImage(getColorBar(chartSize), getWidth() / 4, TOP_MARGIN / 20, chartSize.width, chartSize.height,
					null);

		}
		
		return renderedImg;
	}

	/*
	 * public void saveAsEMF(String path){ byte[] binaryEMF=getEMFBinary();
	 * jdataio jdio=new jdataio(); try{ OutputStream os=new
	 * BufferedOutputStream(new FileOutputStream(path));
	 * jdio.writebytearray(os,binaryEMF); os.close(); }catch(IOException e){
	 * return; } }
	 */

	/**
	 * Saveplot2os is not currently available public byte[] getEMFBinary(){
	 * renderer jr=new renderer(getWidth(),getHeight()); drawPlot(jr); return
	 * jr.renderEMF(); }
	 */
	/*
	 * public void saveplot2file(String filename) { try { OutputStream os = new
	 * BufferedOutputStream(new FileOutputStream(filename)); saveplot2os(os);
	 * os.close(); } catch (IOException e) { return; } }
	 */

	/**
	 * Saveplot2os is not currently available
	 * 
	 * @param arr
	 * @return
	 */
	/*
	 * public void saveplot2os(OutputStream os) { jdataio jdio = new jdataio();
	 * // start with unique identifier for a 3D plot jdio.writestring(os,
	 * "pw2_file_type"); jdio.writeintelint(os, 1); jdio.writestring(os,
	 * getxLabel()); jdio.writestring(os, getyLabel()); jdio.writestring(os,
	 * getzLabel()); jdio.writeintelint(os, nseries); // number of series'
	 * jdio.writeintelint(os, getmaxpts()); // max number of pts
	 * jdio.writeintelfloat(os, xMin); // min x axis jdio.writeintelfloat(os,
	 * xMax); // max x axis jdio.writeintelfloat(os, yMin); // min y axis
	 * jdio.writeintelfloat(os, yMax); // max y axis jdio.writeintelfloat(os,
	 * zMin); // min z axis jdio.writeintelfloat(os, zMax); // max z axis
	 * jdio.writeintelint(os, logx ? 1 : 0); // logx? jdio.writeintelint(os,
	 * logy ? 1 : 0); // logy? jdio.writeintelint(os, logz ? 1 : 0); // logz?
	 * for (int l = 0; l < nseries; l++) { jdio.writeintelint(os, npts[l]); //
	 * number of points in this // series jdio.writeintelint(os, shapes[l]); //
	 * shape index jdio.writeintelint(os, colors[l]); // color index
	 * jdio.writeintelfloatarray(os, xValues[l], npts[l]); // x values
	 * jdio.writeintelfloatarray(os, yValues[l], npts[l]); // y values
	 * jdio.writeintelfloatarray(os, zValues[l], npts[l]); // z values // values
	 * } // save the errors if they exist }
	 */

	private int findmax(int[] arr) {
		int r = arr[0];
		for (int i = 0; i < arr.length; i++)
			if (r < arr[i])
				r = arr[i];
		return r;
	}

	/*
	 * private int findmin(int[] arr){ int r=arr[0]; for(int
	 * i=0;i<arr.length;i++) if(r > arr[i]) r = arr[i]; return r; }
	 */

	// EDIT:
	public void setCurrSeries(int currSeries) {
		int lastSeries = this.currSeries;
		this.currSeries = currSeries % (nseries + 1);
		if (this.currSeries < 0)
			this.currSeries += (nseries + 1);
		if (lastSeries != this.currSeries)
			plotChanged = true;
	}

	public int getCurrSeries() {
		return currSeries;
	}

	public boolean isLabeled() {
		return labels != null;
	}

	public String getLabel(int i) {
		if (labels == null || i < 0 || i > labels.length)
			return null;
		else
			return labels[i];
	}

	Color getCustomColor(int index) {
		if (isCustomColor())
			return getDotColour(customColors[index], customScales, colorScales);
		else
			return getColor(colors[index]);
	}

	private Color getDotColour(float data, float[] scales, Color[] colors) {

		// What proportion of the way through the possible values is that.
		int index = binOf(data, scales);
		if (index == 0)
			return Color.BLACK;
		double percentPosition = percentPosition(data, scales[index - 1], scales[index]);

		int r, g, b;
		r = getPositionValues(percentPosition, colors[index - 1].getRed(), colors[index].getRed());
		g = getPositionValues(percentPosition, colors[index - 1].getGreen(), colors[index].getGreen());
		b = getPositionValues(percentPosition, colors[index - 1].getBlue(), colors[index].getBlue());

		return new Color(r, g, b);
	}

	private double percentPosition(float data, float b1, float b2) {
		if (b1 > b2)
			return (data - b2) / (b1 - b2);
		else
			return (data - b1) / (b2 - b1);
	}

	private int getPositionValues(double percent, int start, int end) {
		return (int) (start + (end - start) * percent);
	}

	private int binOf(float data, float[] arr) {
		for (int i = 1; i < arr.length; i++) {
			if (data <= arr[i] && data >= arr[i - 1])
				return i;
		}

		return 0;
	}

	public boolean isCustomColor() {
		return (customColors != null && colorScales != null);
	}

	public float[] getColorValues() {
		return customColors;
	}

	public Image getColorBar(Dimension chartSize) {

		if (!isCustomColor())
			return null;

		int xoffset = 10, yoffset = 10, colorBarHeight = chartSize.height - yoffset,
				colorBarWidth = chartSize.width - xoffset * 2;

		BufferedImage chartImage = new BufferedImage(chartSize.width, chartSize.height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D chartGraphics = chartImage.createGraphics();

		// Calculate the available size for the heatmap.
		float[] data = new float[colorBarWidth];
		double increment = (customScales[customScales.length - 1] - customScales[0]) / colorBarWidth;
		for (int i = 0; i < data.length; i++) {
			data[i] = (float) (customScales[0] + increment * i);
		}

		chartGraphics.setColor(new Color(0, 255, 0, 0));
		chartGraphics.fillRect(0, 0, chartSize.width, chartSize.height);

		for (int bar = 0; bar < colorBarWidth; bar++) {
			// Set colour depending on zValues.
			int index = binOf(data[bar], customScales);
			chartGraphics.setColor(getDotColour(data[bar], new float[] { customScales[index - 1], customScales[index] },
					new Color[] { colorScales[index - 1], colorScales[index] }));
			chartGraphics.fillRect(xoffset + bar, yoffset, 1, colorBarHeight);
		}

		for (int iScale = 0; iScale < customScales.length; iScale++) {
			chartGraphics.setColor(Color.BLACK);
			chartGraphics.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			chartGraphics
					.drawString("" + customScales[iScale],
							xoffset + iScale * colorBarWidth / (customScales.length - 1)
									- chartGraphics.getFontMetrics().stringWidth("" + customScales[iScale]) / 2,
							yoffset);
		}

		// new ImagePlus("colorBar",chartImage).show();
		return chartImage;
		// Draw the heat map onto the chart.
	}

	public static String formatted_string(double number) {
		double absnumber = Math.abs(number);
		if (absnumber >= 1000.0 || absnumber < 0.01) {
			DecimalFormat expformat = new DecimalFormat("0.00E0");
			return expformat.format(number);
		} else {
			if (absnumber >= 100.0) {
				DecimalFormat tempformat = new DecimalFormat("000.0");
				return tempformat.format(number);
			} else {
				if (absnumber >= 10.0) {
					DecimalFormat tempformat = new DecimalFormat("00.00");
					return tempformat.format(number);
				} else {
					DecimalFormat tempformat = new DecimalFormat("0.000");
					return tempformat.format(number);
				}
			}
		}
	}

}
