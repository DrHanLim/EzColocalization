/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import ezcol.main.PluginStatic;

import java.awt.datatransfer.*;

import ij.*;
import ij.gui.*;
import ij.io.SaveDialog;
import ij.measure.ResultsTable;
import ij.process.*;
import ij.text.TextWindow;

@SuppressWarnings("serial")
public class ScatterPlot3DWindow extends ImageWindow
		implements ActionListener, ClipboardOwner, MouseMotionListener, MouseListener {
	private Button list, save, copy, editbutton, selbutton, seldbutton;
	private Button rotrightbutton, rotleftbutton, rotupbutton, rotdownbutton, rotcounterbutton, rotclockbutton,
			rotresetbutton;
	// private Label coordinates;
	private static String defaultDirectory = null;
	private static String title;
	public ScatterPlot3D p3;
	private static ColorProcessor cp;

	// EDIT: drag lock
	private boolean rotLock = false, rawLock = false;
	private Button rotLockButton, typeButton;
	private ResultsTable rawRT;
	private double rotx = -60, roty = 0, rotz = -45;

	// EDIT: call Scatter3DPlot
	public ScatterPlot3DWindow(String title1, String xLabel1, String yLabel1, String zLabel1, float[][] xValues1,
			float[][] yValues1, float[][] zValues1) {
		super(createImage(title1));
		p3 = new ScatterPlot3D(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1);
	}

	public ScatterPlot3DWindow(String title1, String xLabel1, String yLabel1, String zLabel1, float[] xValues1,
			float[] yValues1, float[] zValues1) {
		super(createImage(title1));
		p3 = new ScatterPlot3D(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1);
	}

	public ScatterPlot3DWindow(String title1, String xLabel1, String yLabel1, String zLabel1, float[] xValues1,
			float[] yValues1, float[] zValues1, int shape) {
		super(createImage(title1));
		p3 = new ScatterPlot3D(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1, shape);
	}

	public ScatterPlot3DWindow(String title1, String xLabel1, String yLabel1, String zLabel1, float[][] xValues1,
			float[][] yValues1, float[][] zValues1, int[] shapes) {
		super(createImage(title1));
		p3 = new ScatterPlot3D(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1, shapes);
	}

	public ScatterPlot3DWindow(String title1, String xLabel1, String yLabel1, String zLabel1, float[][] xValues1,
			float[][] yValues1, float[][] zValues1, int shape) {
		super(createImage(title1));
		int[] shapes = new int[zValues1.length];
		for (int i = 0; i < shapes.length; i++)
			shapes[i] = shape;
		p3 = new ScatterPlot3D(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1, shapes);
	}

	public ScatterPlot3DWindow(String title1, String xLabel1, String yLabel1, String zLabel1, float[][] xValues1,
			float[][] yValues1, float[][] zValues1, int shape, String[] labels) {
		super(createImage(title1));
		int[] shapes = new int[zValues1.length];
		for (int i = 0; i < shapes.length; i++)
			shapes[i] = shape;
		p3 = new ScatterPlot3D(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1, shapes, labels);
	}

	public ScatterPlot3DWindow(String title1, String xLabel1, String yLabel1, String zLabel1, float[][] xValues1,
			float[][] yValues1, float[][] zValues1, int shape, float[] customColors, float[] customScales,
			Color[] colorScales) {
		super(createImage(title1));
		int[] shapes = new int[zValues1.length];
		for (int i = 0; i < shapes.length; i++)
			shapes[i] = shape;
		p3 = new ScatterPlot3D(xLabel1, yLabel1, zLabel1, xValues1, yValues1, zValues1, shapes, customColors,
				customScales, colorScales);
	}

	public ScatterPlot3DWindow(String title1, ScatterPlot3D p3) {
		super(createImage(title1));
		this.p3 = p3;
	}

	public ScatterPlot3DWindow(ImagePlus imp, ScatterPlot3D p3) {
		// turns the passed ImagePlus into a plot window
		// used for bioformats which wants to make the window for me
		super(imp);
		int width = ScatterPlot3D.WIDTH + ScatterPlot3D.LEFT_MARGIN + ScatterPlot3D.RIGHT_MARGIN;
		int height = ScatterPlot3D.HEIGHT + ScatterPlot3D.TOP_MARGIN + ScatterPlot3D.BOTTOM_MARGIN;
		int[] temp = new int[width * height];
		for (int i = 0; i < width * height; i++) {
			temp[i] = 0xffffffff;
		}
		cp = new ColorProcessor(width, height, temp);
		imp.setProcessor(cp);
		// cp=(ColorProcessor)this.imp.getProcessor();
		this.p3 = p3;
	}

	static ImagePlus createImage(String title1) {
		int width = ScatterPlot3D.WIDTH + ScatterPlot3D.LEFT_MARGIN + ScatterPlot3D.RIGHT_MARGIN;
		int height = ScatterPlot3D.HEIGHT + ScatterPlot3D.TOP_MARGIN + ScatterPlot3D.BOTTOM_MARGIN;
		int[] temp = new int[width * height];
		for (int i = 0; i < width * height; i++) {
			temp[i] = 0xffffffff;
		}
		cp = new ColorProcessor(width, height, temp);
		title = title1;
		return new ImagePlus(title, cp);
	}

	/** Sets the x-axis and y-axis range. */
	public void setLimits(double xMin1, double xMax1, double yMin1, double yMax1, double zMin1, double zMax1) {
		p3.setLimits(xMin1, xMax1, yMin1, yMax1, zMin1, zMax1);
		updatePlot();
	}

	public void setLimits(float[] limits) {
		p3.setLimits(limits);
		updatePlot();
	}

	/** Sets the x-axis and y-axis range. */
	public void setLogAxes(boolean logx1, boolean logy1, boolean logz1) {
		p3.setLogAxes(logx1, logy1, logz1);
		updatePlot();
	}

	public void autoscale() {
		p3.autoscale();
		updatePlot();
	}

	public void xautoscale() {
		p3.xautoscale();
		updatePlot();
	}

	public void yautoscale() {
		p3.yautoscale();
		updatePlot();
	}

	public void zautoscale() {
		p3.zautoscale();
		updatePlot();
	}

	public void updateSeries(float[] xValues1, float[] yValues1, float[] zValues1, int series, boolean rescale) {
		p3.updateSeries(xValues1, yValues1, zValues1, series, rescale);
		updatePlot();
	}

	public void updateSeries(float[] zValues1, int series, boolean rescale) {
		p3.updateSeries(zValues1, series, rescale);
		updatePlot();
	}

	public void deleteSeries(int series, boolean rescale) {
		p3.deleteSeries(series, rescale);
		updatePlot();
	}

	public void addPoints(float[] xValues1, float[] yValues1, float[] zValues1, boolean rescale) {
		p3.addPoints(xValues1, yValues1, zValues1, rescale);
		updatePlot();
	}

	public void addPoints(float[] zValues1, boolean rescale, int startxy) {
		p3.addPoints(zValues1, rescale, startxy);
		updatePlot();
	}

	public void updatePlot() {
		cp = p3.getProcessor();
		imp.setProcessor(null, cp);
		imp.updateAndDraw();
	}

	/** Displays the plot. */
	public void draw() {
		Panel buttons = new Panel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		list = new Button(" List ");
		list.addActionListener(this);
		buttons.add(list);
		save = new Button("Save...");
		save.addActionListener(this);
		buttons.add(save);
		copy = new Button("Copy...");
		copy.addActionListener(this);
		buttons.add(copy);
		editbutton = new Button("Edit...");
		editbutton.addActionListener(this);
		buttons.add(editbutton);
		selbutton = new Button("Select+");
		selbutton.addActionListener(this);
		buttons.add(selbutton);
		seldbutton = new Button("Select-");
		seldbutton.addActionListener(this);
		buttons.add(seldbutton);

		typeButton = new Button("Proc");
		typeButton.addActionListener(this);
		buttons.add(typeButton);
		rotLockButton = new Button("Dynamic");
		rotLockButton.setForeground(Color.RED);
		rotLockButton.addActionListener(this);
		buttons.add(rotLockButton);

		Panel rotbuttons = new Panel();
		rotbuttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		rotrightbutton = new Button(">");
		rotrightbutton.addActionListener(this);
		rotbuttons.add(rotrightbutton);
		rotleftbutton = new Button("<");
		rotleftbutton.addActionListener(this);
		rotbuttons.add(rotleftbutton);
		rotupbutton = new Button("^");
		rotupbutton.addActionListener(this);
		rotbuttons.add(rotupbutton);
		rotdownbutton = new Button("v");
		rotdownbutton.addActionListener(this);
		rotbuttons.add(rotdownbutton);
		rotclockbutton = new Button("Clock+");
		rotclockbutton.addActionListener(this);
		rotbuttons.add(rotclockbutton);
		rotcounterbutton = new Button("Clock-");
		rotcounterbutton.addActionListener(this);
		rotbuttons.add(rotcounterbutton);
		rotresetbutton = new Button("Reset Rot.");
		rotresetbutton.addActionListener(this);
		rotbuttons.add(rotresetbutton);
		// coordinates = new Label("X=12345678, Y=12345678");
		// coordinates.setFont(new Font("Monospaced", Font.PLAIN, 12));
		// buttons.add(coordinates);
		add(buttons);
		add(rotbuttons);
		pack();
		// coordinates.setText("");
		getCanvas().addMouseMotionListener(this);
		getCanvas().addMouseListener(this);
		updatePlot();
	}

	/**
	 * Updates the graph X and Y values when the mouse is moved. Overrides
	 * mouseMoved() in ImageWindow.
	 * 
	 * @see ij.gui.ImageWindow#mouseMoved
	 */
	public void mouseMoved(int x, int y) {
		super.mouseMoved(x, y);
		/*
		 * float[] temp=getPlotCoords(x,y); coordinates.setText("X="+temp[0]+
		 * ", Y="+temp[1]);
		 */
	}

	void showList() {

		if (rawLock && rawRT != null) {
			rawRT.show(title + " Raw Values");
			return;
		}

		if (p3.isCustomColor()) {
			ResultsTable rt = new ResultsTable();
			float[][] tempxvals = p3.getXValues();
			float[][] tempyvals = p3.getYValues();
			float[][] tempzvals = p3.getZValues();
			float[] tempColors = p3.getColorValues();
			for (int i = 0; i < tempColors.length; i++) {
				rt.incrementCounter();
				rt.addValue("Channel 1 FT", tempxvals[i][0]);
				rt.addValue("Channel 2 FT", tempyvals[i][0]);
				rt.addValue("Channel 3 FT", tempzvals[i][0]);
				rt.addValue("Metric ", tempColors[i]);
			}
			rt.show(title + " Plot Values");
			return;
		}

		StringBuffer sb = new StringBuffer();
		StringBuffer headings = new StringBuffer();

		int maxpts = p3.getmaxpts();
		int nser = p3.getNSeries();
		float[][] tempxvals = p3.getXValues();
		float[][] tempyvals = p3.getYValues();
		float[][] tempzvals = p3.getZValues();
		for (int i = 0; i < nser; i++) {
			headings.append("x" + i + "\ty" + i + "\tz" + i + "\t");
		}
		for (int i = 0; i < maxpts; i++) {
			for (int j = 0; j < nser; j++) {
				sb.append("" + tempxvals[j][i] + "\t" + tempyvals[j][i] + "\t" + tempzvals[j][i] + "\t");
			}
			sb.append("\n");
		}
		new TextWindow(title + " Plot Values", headings.toString(), sb.toString(), 200, 400);

	}

	void saveAsText() {
		/*
		 * FileDialog fd = new FileDialog(this, "Save as Text...",
		 * FileDialog.SAVE); if (defaultDirectory != null)
		 * fd.setDirectory(defaultDirectory); fd.show(); String name =
		 * fd.getFile(); String directory = fd.getDirectory(); defaultDirectory
		 * = directory; fd.dispose(); PrintWriter pw = null; try {
		 * FileOutputStream fos = new FileOutputStream(directory + name);
		 * BufferedOutputStream bos = new BufferedOutputStream(fos); pw = new
		 * PrintWriter(bos); } catch (IOException e) { IJ.error("" + e); return;
		 * } IJ.wait(250); // give system time to redraw ImageJ window
		 * IJ.showStatus("Saving plot values...");
		 */

		SaveDialog sd = new SaveDialog("Save as Text", "Values", Prefs.defaultResultsExtension());
		String name = sd.getFileName();
		if (name == null)
			return;
		String directory = sd.getDirectory();
		IJ.wait(250); // give system time to redraw ImageJ window
		IJ.showStatus("Saving plot values...");

		if (rawLock && rawRT != null) {
			try {
				rawRT.saveAs(directory + name);
			} catch (IOException e) {
				IJ.error("" + e);
			}
		} else if (p3.isCustomColor()) {
			ResultsTable rt = new ResultsTable();
			float[][] tempxvals = p3.getXValues();
			float[][] tempyvals = p3.getYValues();
			float[][] tempzvals = p3.getZValues();
			float[] tempColors = p3.getColorValues();
			for (int i = 0; i < tempColors.length; i++) {
				rt.incrementCounter();
				rt.addValue("Channel 1 FT", tempxvals[i][0]);
				rt.addValue("Channel 2 FT", tempyvals[i][0]);
				rt.addValue("Channel 3 FT", tempzvals[i][0]);
				rt.addValue("Metric ", tempColors[i]);
			}
			try {
				rt.saveAs(directory + name);
			} catch (IOException e) {
				IJ.error("" + e);
				return;
			}
		} else {

			PrintWriter pw = null;
			try {
				FileOutputStream fos = new FileOutputStream(directory + name);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				pw = new PrintWriter(bos);
			} catch (IOException e) {
				IJ.error("" + e);
				return;
			}
			int maxpts = p3.getmaxpts();
			int nser = p3.getNSeries();
			float[][] xValues = p3.getXValues();
			float[][] yValues = p3.getYValues();
			float[][] zValues = p3.getZValues();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < maxpts; i++) {
				for (int j = 0; j < nser; j++) {
					sb.append("" + xValues[j][i] + "\t" + yValues[j][i] + "\t" + zValues[j][i] + "\t");
					pw.println(sb.toString());
				}
				pw.println("\n");
			}
			pw.close();
		}
	}

	/**
	 * SaveAsObject is not currently available
	 */
	/*
	 * void saveAsObject() { FileDialog fd = new FileDialog(this,
	 * "Save as Plot Object...", FileDialog.SAVE); if (defaultDirectory != null)
	 * fd.setDirectory(defaultDirectory); String temptitle = getTitle(); if
	 * (!temptitle.endsWith(".pw2")) { temptitle += ".pw2"; }
	 * fd.setFile(temptitle); //fd.setFilenameFilter( new FilenameFilter(){
	 * public boolean //accept(File dir,String name){ if(name.endsWith(".pw")){
	 * return true; //} else { return false; } } } ); fd.show(); String name =
	 * fd.getFile(); String directory = fd.getDirectory(); defaultDirectory =
	 * directory; fd.dispose(); if (name == null || name == "" || directory ==
	 * null || directory == "") return; imp.setTitle(name);
	 * saveAsObject(directory + File.separator + name); }
	 */

	/**
	 * SaveAsObject is not currently available
	 * 
	 * @param filename
	 */
	/*
	 * public void saveAsObject(String filename) { p3.saveplot2file(filename); }
	 */

	void copyToClipboard() {
		Clipboard systemClipboard = null;
		try {
			systemClipboard = getToolkit().getSystemClipboard();
		} catch (Exception e) {
			systemClipboard = null;
		}
		if (systemClipboard == null) {
			IJ.error("Unable to copy to Clipboard.");
			return;
		}
		IJ.showStatus("Copying plot values...");

		CharArrayWriter aw = new CharArrayWriter();
		PrintWriter pw = new PrintWriter(aw); // uses platform's line
		String text = ""; // termination characters

		if (rawLock && rawRT != null) {
			if (!Prefs.dontSaveHeaders) {
				String headings = rawRT.getColumnHeadings();
				pw.println(headings);
			}
			for (int i = 0; i < rawRT.size(); i++)
				pw.println(rawRT.getRowAsString(i));
			text = aw.toString();
		}
		if (p3.isCustomColor()) {
			ResultsTable rt = new ResultsTable();
			float[][] tempxvals = p3.getXValues();
			float[][] tempyvals = p3.getYValues();
			float[][] tempzvals = p3.getZValues();
			float[] tempColors = p3.getColorValues();
			for (int i = 0; i < tempColors.length; i++) {
				rt.incrementCounter();
				rt.addValue("Channel 1 FT", tempxvals[i][0]);
				rt.addValue("Channel 2 FT", tempyvals[i][0]);
				rt.addValue("Channel 3 FT", tempzvals[i][0]);
				rt.addValue("Metric ", tempColors[i]);
			}
			if (!Prefs.dontSaveHeaders) {
				String headings = rt.getColumnHeadings();
				pw.println(headings);
			}
			for (int i = 0; i < rt.size(); i++)
				pw.println(rt.getRowAsString(i));
			text = aw.toString();
		} else {
			StringBuffer sb = new StringBuffer();
			int maxpts = p3.getmaxpts();
			int nser = p3.getNSeries();
			float[][] xValues = p3.getXValues();
			float[][] yValues = p3.getYValues();
			float[][] zValues = p3.getZValues();
			for (int i = 0; i < maxpts; i++) {
				for (int j = 0; j < nser; j++) {
					sb.append("" + xValues[j][i] + "\t" + yValues[j][i] + "\t" + zValues[j][i] + "\t\n");
				}
				sb.append("\n");
				text = sb.toString();
			}
		}
		StringSelection contents = new StringSelection(text);
		systemClipboard.setContents(contents, this);
		IJ.showStatus(text.length() + " characters copied to Clipboard");
	}

	void editPlot() {
		GenericDialog gd = new GenericDialog("Plot Options");
		float[] limits = p3.getLimits();
		gd.addNumericField("x min", limits[0], 5, 10, null);
		gd.addNumericField("x max", limits[1], 5, 10, null);
		gd.addNumericField("y min", limits[2], 5, 10, null);
		gd.addNumericField("y max", limits[3], 5, 10, null);
		gd.addNumericField("z min", limits[4], 5, 10, null);
		gd.addNumericField("z max", limits[5], 5, 10, null);
		boolean[] logs = p3.getLogAxes();
		gd.addCheckbox("Log x?", logs[0]);
		gd.addCheckbox("Log y?", logs[1]);
		gd.addCheckbox("Log z?", logs[2]);
		gd.addStringField("x label", p3.getxLabel());
		gd.addStringField("y label", p3.getyLabel());
		gd.addStringField("z label", p3.getzLabel());
		boolean delsel = false;
		gd.addCheckbox("Delete Selected", delsel);
		boolean ascalex = false;
		gd.addCheckbox("AutoScale x", ascalex);
		boolean ascaley = false;
		gd.addCheckbox("AutoScale y", ascalex);
		boolean ascalez = false;
		gd.addCheckbox("AutoScale z", ascalez);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		limits[0] = (float) gd.getNextNumber();
		limits[1] = (float) gd.getNextNumber();
		limits[2] = (float) gd.getNextNumber();
		limits[3] = (float) gd.getNextNumber();
		limits[4] = (float) gd.getNextNumber();
		limits[5] = (float) gd.getNextNumber();
		p3.setLimits(limits);
		logs[0] = gd.getNextBoolean();
		logs[1] = gd.getNextBoolean();
		logs[2] = gd.getNextBoolean();
		p3.setLogAxes(logs[0], logs[1], logs[2]);
		p3.setxLabel(gd.getNextString());
		p3.setyLabel(gd.getNextString());
		p3.setzLabel(gd.getNextString());
		delsel = gd.getNextBoolean();
		ascalex = gd.getNextBoolean();
		ascaley = gd.getNextBoolean();
		ascalez = gd.getNextBoolean();
		if (delsel) {
			delsel = false;
			p3.deleteSeries(p3.getSelected(), false);
		}
		if (ascalex) {
			ascalex = false;
			p3.xautoscale();
		}
		if (ascaley) {
			ascaley = false;
			p3.yautoscale();
		}
		if (ascalez) {
			ascalez = false;
			p3.zautoscale();
		}
		updatePlot();
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b == list) {
			showList();
		} else {
			if (b == save) {
				saveAsText();
				/*
				 * GenericDialog gd = new GenericDialog("Save Options");
				 * String[] savechoice = { "Text", "Binary", "Plot Object" };
				 * gd.addChoice("File Type?", savechoice, savechoice[2]); int
				 * saveseries = 0; gd.addNumericField(
				 * "Save Series # (for binary)", saveseries, 0); String[]
				 * binarytypechoice = { "Float", "Integer", "Short" };
				 * gd.addChoice("Binary File Type?", binarytypechoice,
				 * binarytypechoice[0]); gd.showDialog(); if (gd.wasCanceled())
				 * { return; } int choiceindex = gd.getNextChoiceIndex(); if
				 * (choiceindex == 0) { saveAsText(); } else { saveAsObject(); }
				 */
			} else {
				if (b == editbutton) {
					editPlot();
				} else {
					if (b == copy) {
						copyToClipboard();
					} else {
						if (b == selbutton) {
							// p3.selectSeries(p3.getSelected() + 1);
							p3.setCurrSeries(p3.getCurrSeries() + 1);
							setCurrLabel();
							updatePlot();
						} else {
							if (b == seldbutton) {
								// p3.selectSeries(p3.getSelected() - 1);
								p3.setCurrSeries(p3.getCurrSeries() - 1);
								setCurrLabel();
								updatePlot();
							} else {
								if (b == typeButton) {
									rawLock = !rawLock;
									if (rawLock) {
										typeButton.setLabel("Raw");
										typeButton.setForeground(Color.RED);
									} else {
										typeButton.setLabel("Proc");
										typeButton.setForeground(Color.BLACK);
									}
								} else {
									if (b == rotLockButton) {
										rotLock = !rotLock;
										if (rotLock) {
											rotLockButton.setLabel("Locked");
											rotLockButton.setForeground(Color.BLACK);
										} else {
											rotLockButton.setLabel("Dynamic");
											rotLockButton.setForeground(Color.RED);
										}
									} else {
										if (b == rotrightbutton) {
											p3.setrotation(p3.getrotation()[0], p3.getrotation()[1],
													p3.getrotation()[2] + 10.0);
											updatePlot();
										} else {
											if (b == rotleftbutton) {
												p3.setrotation(p3.getrotation()[0], p3.getrotation()[1],
														p3.getrotation()[2] - 10.0);
												updatePlot();
											} else {
												if (b == rotupbutton) {
													p3.setrotation(p3.getrotation()[0] - 10.0, p3.getrotation()[1],
															p3.getrotation()[2]);
													updatePlot();
												} else {
													if (b == rotdownbutton) {
														p3.setrotation(p3.getrotation()[0] + 10.0, p3.getrotation()[1],
																p3.getrotation()[2]);
														updatePlot();
													} else {
														if (b == rotclockbutton) {
															p3.setrotation(p3.getrotation()[0],
																	p3.getrotation()[1] - 10.0, p3.getrotation()[2]);
															updatePlot();
														} else {
															if (b == rotcounterbutton) {
																p3.setrotation(p3.getrotation()[0],
																		p3.getrotation()[1] + 10.0,
																		p3.getrotation()[2]);
																updatePlot();
															} else {
																if (b == rotresetbutton) {
																	p3.setrotation(rotx, roty, rotz);
																	updatePlot();
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void selectSeries(int series) {
		p3.selectSeries(series);
		updatePlot();
	}

	public float[][] getXValues() {
		return p3.getXValues();
	}

	public float[] getXValues(int series) {
		return p3.getXValues(series);
	}

	public float[][] getYValues() {
		return p3.getYValues();
	}

	public float[] getYValues(int series) {
		return p3.getYValues(series);
	}

	public float[][] getZValues() {
		return p3.getZValues();
	}

	public float[] getZValues(int series) {
		return p3.getZValues(series);
	}

	public String getPlotTitle() {
		return imp.getTitle();
	}

	public String getxLabel() {
		return p3.getxLabel();
	}

	public String getyLabel() {
		return p3.getyLabel();
	}

	public String getzLabel() {
		return p3.getzLabel();
	}

	public int[] getNpts() {
		return p3.getNpts();
	}

	public int getNSeries() {
		return p3.getNSeries();
	}

	public float[] getLimits() {
		return p3.getLimits();
	}

	public int[] getShapes() {
		return p3.getShapes();
	}

	public int[] getColors() {
		return p3.getColors();
	}

	public ScatterPlot3D getPlot() {
		return p3;
	}

	// EDIT: Implement drag and lock
	private int xdiff, ydiff, xStart, yStart;
	private int toolID = Toolbar.HAND;

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		if (!rotLock) {

			int xAct = e.getX();
			int yAct = e.getY();
			xdiff = xAct - xStart;
			ydiff = yAct - yStart;
			xStart = xAct;
			yStart = yAct;
			p3.setrotation(p3.getrotation()[0] - ydiff / 2.0, p3.getrotation()[1], p3.getrotation()[2] + xdiff / 2.0);
			updatePlot();
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		if (!rotLock && toolID != Toolbar.HAND) {
			Toolbar.getInstance().setTool(toolID);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		if (!rotLock) {
			toolID = Toolbar.getToolId();
			if (toolID != Toolbar.HAND) {
				Toolbar.getInstance().setTool(Toolbar.HAND);
			}
			xStart = e.getX();
			yStart = e.getY();
			xdiff = 0;
			ydiff = 0;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void setCurrLabel() {
		if (p3.isLabeled()) {
			if (p3.getCurrSeries() == p3.getNSeries())
				imp.setProperty("Label", "All Data");
			else
				imp.setProperty("Label", p3.getLabel(p3.getCurrSeries()));
		} else
			imp.setProperty("Label", null);
	}

	public void setDefaultRotation(int rotx, int roty, int rotz) {
		this.rotx = rotx;
		this.roty = roty;
		this.rotz = rotz;
		if (p3 != null)
			p3.setrotation(rotx, roty, rotz);
	}

	public void setRawResultsTable(ResultsTable rawRT) {
		this.rawRT = rawRT;
	}

	public static void setPlotTitle(String title) {
		ScatterPlot3DWindow.title = title;
	}

}