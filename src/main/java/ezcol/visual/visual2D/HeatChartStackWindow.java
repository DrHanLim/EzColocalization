package ezcol.visual.visual2D;

import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ezcol.main.PluginConstants;
import ezcol.main.PluginStatic;
import ezcol.metric.BasicCalculator;
import ezcol.metric.MatrixCalculator;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageLayout;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.io.SaveDialog;
import ij.macro.Interpreter;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;

@SuppressWarnings("serial")
public class HeatChartStackWindow extends StackWindow
		implements ActionListener, ClipboardOwner, Runnable, ItemListener {

	private Button list, save, copy, type, plot;
	private Label coordinates;
	// denotes the current plot
	private HeatChart heatChart;
	// private static Plots staticPlot;
	// private Plots plot;
	boolean layoutDone; // becomes true after the layout has been done, used by
						// PlotCanvas
	private String blankLabel = "                       		";

	/**
	 * Save x-values only. To set, use Edit/Options/ Profile Plot Options.
	 */
	public static boolean saveXValues;
	/**
	 * Automatically close window after saving values. To set, use
	 * Edit/Options/Profile Plot Options.
	 */
	public static boolean autoClose;
	/**
	 * Display the XY coordinates in a separate window. To set, use
	 * Edit/Options/Profile Plot Options.
	 */
	public static boolean listValues;

	private Roi[] rangeArrowRois; // these constitute the arrow overlays for
									// changing the range
	private boolean rangeArrowsVisible;
	private int activeRangeArrow = -1;

	// new fields
	private static boolean saveRaw;
	private HeatChart[] heatCharts;

	// default fields from Plot
	@SuppressWarnings("deprecation")
	int leftMargin = Plot.LEFT_MARGIN, rightMargin = Plot.RIGHT_MARGIN, topMargin = Plot.TOP_MARGIN,
			bottomMargin = Plot.BOTTOM_MARGIN;
	int frameWidth; // width corresponding to plot range; frame.width is larger
					// by 1
	int frameHeight; // height corresponding to plot range; frame.height is
						// larger by 1
	Rectangle stackFrame = null;
	String orgtitle = "";

	public void setOrgTitle(String title) {
		orgtitle = title;
	}

	public String getOrgTitle() {
		return this.orgtitle;
	}

	/** Creates a PlotWindow from a Plot object. */
	public HeatChartStackWindow(HeatChart heatChart) {
		super(heatChart.getImagePlus());
		// ((PlotCanvas)getCanvas()).setPlot(plot);
		this.heatChart = heatChart;
		draw();
	}

	/** Creates a PlotWindow from a Plot object. */
	public HeatChartStackWindow(HeatChart[] heatCharts, ImagePlus imp) {
		super(imp);
		// ((PlotCanvas)getCanvas()).setPlot(plot);
		if (heatCharts != null && heatCharts.length > 0) {
			this.heatCharts = heatCharts;
			this.heatChart = heatCharts[0];
			draw();
		}
	}

	/** Displays the plot. */
	protected void draw() {
		Panel bottomPanel = new Panel();
		int hgap = IJ.isMacOSX() ? 1 : 5;

		list = new Button(" List ");
		list.addActionListener(this);
		bottomPanel.add(list);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, hgap, 0));
		save = new Button("Save...");
		save.addActionListener(this);
		bottomPanel.add(save);
		copy = new Button(" Copy ");
		copy.addActionListener(this);
		bottomPanel.add(copy);

		type = new Button(" Proc ");
		type.addActionListener(this);
		bottomPanel.add(type);
		plot = new Button("Options...");
		plot.addActionListener(this);
		bottomPanel.add(plot);

		coordinates = new Label(blankLabel);
		coordinates.setFont(new Font("Monospaced", Font.PLAIN, 12));
		coordinates.setBackground(new Color(220, 220, 220));
		bottomPanel.add(coordinates);
		add(bottomPanel);

		// plot.draw();

		LayoutManager lm = getLayout();
		if (lm instanceof ImageLayout)
			((ImageLayout) lm).ignoreNonImageWidths(true); // don't expand size
															// to make the panel
															// fit
		pack();

		ImageProcessor ip = heatChart.getProcessor();
		if ((ip instanceof ColorProcessor) && (imp.getProcessor() instanceof ByteProcessor))
			imp.setProcessor(null, ip);
		else
			imp.updateAndDraw();
		layoutDone = true;
		if (listValues)
			showList();
		else
			ic.requestFocus(); // have focus on the canvas, not the button, so
								// that pressing the space bar allows panning

		// get the frame from plot because frame is private in plot
		stackFrame = heatChart.getDrawingFrame();
		frameWidth = stackFrame.width;
		frameHeight = stackFrame.height;

	}

	@Deprecated
	public void showStack() {
		if ((IJ.macroRunning() && IJ.getInstance() == null) || Interpreter.isBatchMode()) {
			imp = heatChart.getImagePlus();
			WindowManager.setTempCurrentImage(imp);
			/*
			 * if (getMainCurveObject() != null) { imp.setProperty("XValues",
			 * getXValues()); // Allows values to be retrieved by
			 * imp.setProperty("YValues", getYValues()); // by Plot.getValues()
			 * macro function } Interpreter.addBatchModeImage(imp); return null;
			 */
		}
		if (imp != null) {
			Window win = imp.getWindow();
			if (win instanceof HeatChartStackWindow && win.isVisible()) {
				heatChart.updateImage(); // show in existing window
			}
		}
		if (imp == null)
			imp.setProperty(Plot.PROPERTY_KEY, null);
		imp = getImagePlus();
		imp.setProperty(Plot.PROPERTY_KEY, this);
		if (IJ.isMacro() && imp != null) // wait for plot to be displayed
			IJ.selectWindow(imp.getID());
	}

	/** Shows the data of the backing plot in a Textwindow with columns */
	void showList() {
		ResultsTable rt = heatChart.getResultsTable(saveRaw);
		rt.show(heatChart.getInfo(saveRaw) + " of " + orgtitle);
		if (autoClose) {
			imp.changes = false;
			close();
		}
	}

	/** Copy the first dataset or all values to the clipboard */
	void copyToClipboard(boolean writeAllColumns) {
		float[] xValues = PluginStatic.obj2floatArray(heatChart.getXValues());
		float[] yValues = PluginStatic.obj2floatArray(heatChart.getYValues());
		if (xValues == null)
			return;
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
		CharArrayWriter aw = new CharArrayWriter(10 * xValues.length);
		PrintWriter pw = new PrintWriter(aw); // uses platform's line
												// termination characters

		if (writeAllColumns) {
			ResultsTable rt = heatChart.getResultsTable(saveRaw);
			if (!Prefs.dontSaveHeaders) {
				String headings = rt.getColumnHeadings();
				pw.println(headings);
			}
			for (int i = 0; i < rt.size(); i++)
				pw.println(rt.getRowAsString(i));
		} 
		//legacy
		/*else {
			int xdigits = 0;
			// if (saveXValues)
			xdigits = getPrecision(xValues);
			int ydigits = getPrecision(yValues);
			for (int i = 0; i < Math.min(xValues.length, yValues.length); i++) {
				// if (saveXValues)
				// pw.println(IJ.d2s(xValues[i],xdigits)+"\t"+IJ.d2s(yValues[i],ydigits));
				// else
				pw.println(IJ.d2s(yValues[i], ydigits));
			}
		}*/
		String text = aw.toString();
		pw.close();
		StringSelection contents = new StringSelection(text);
		systemClipboard.setContents(contents, this);
		IJ.showStatus(text.length() + " characters copied to Clipboard");
		if (autoClose) {
			imp.changes = false;
			close();
		}
	}

	/** Returns the plot values as a ResultsTable. */
	public ResultsTable getResultsTable() {
		return heatChart.getResultsTable(saveRaw);
	}

	/** Saves the data of the plot in a text file */
	void saveAsText() {
		if (heatChart.getZValues() == null) {
			IJ.error("Plot has no data");
			return;
		}
		SaveDialog sd = new SaveDialog("Save as Text", "Values", Prefs.defaultResultsExtension());
		String name = sd.getFileName();
		if (name == null)
			return;
		String directory = sd.getDirectory();
		IJ.wait(250); // give system time to redraw ImageJ window
		IJ.showStatus("Saving plot values...");
		ResultsTable rt = getResultsTable();
		try {
			rt.saveAs(directory + name);
		} catch (IOException e) {
			IJ.error("" + e);
			return;
		}
		if (autoClose) {
			imp.changes = false;
			close();
		}
	}

	/**
	 * Creates an overlay with triangular buttons for changing the axis range
	 * limits and shows it
	 */
	void showRangeArrows() {
		if (imp == null)
			return;
		hideRangeArrows(); // in case we have old arrows from a different plot
							// size or so
		rangeArrowRois = new Roi[4 * 2]; // 4 arrows per axis
		int i = 0;
		int height = imp.getHeight();
		int arrowH = topMargin < 14 ? 6 : 8; // height of arrows and distance
												// between them; base is twice
												// that value
		float[] yP = new float[] { height - arrowH / 2, height - 3 * arrowH / 2, height - 5 * arrowH / 2 - 0.1f };
		for (float x : new float[] { leftMargin, leftMargin + frameWidth }) { // create
																				// arrows
																				// for
																				// x
																				// axis
			float[] x0 = new float[] { x - arrowH / 2, x - 3 * arrowH / 2 - 0.1f, x - arrowH / 2 };
			rangeArrowRois[i++] = new PolygonRoi(x0, yP, 3, Roi.POLYGON);
			float[] x1 = new float[] { x + arrowH / 2, x + 3 * arrowH / 2 + 0.1f, x + arrowH / 2 };
			rangeArrowRois[i++] = new PolygonRoi(x1, yP, 3, Roi.POLYGON);
		}
		float[] xP = new float[] { arrowH / 2 - 0.1f, 3 * arrowH / 2, 5 * arrowH / 2 + 0.1f };
		for (float y : new float[] { topMargin + frameHeight, topMargin }) { // create
																				// arrows
																				// for
																				// y
																				// axis
			float[] y0 = new float[] { y + arrowH / 2, y + 3 * arrowH / 2 + 0.1f, y + arrowH / 2 };
			rangeArrowRois[i++] = new PolygonRoi(xP, y0, 3, Roi.POLYGON);
			float[] y1 = new float[] { y - arrowH / 2, y - 3 * arrowH / 2 - 0.1f, y - arrowH / 2 };
			rangeArrowRois[i++] = new PolygonRoi(xP, y1, 3, Roi.POLYGON);
		}
		Overlay ovly = imp.getOverlay();
		if (ovly == null)
			ovly = new Overlay();
		for (Roi roi : rangeArrowRois) {
			roi.setFillColor(Color.GRAY);
			ovly.add(roi);
		}
		imp.setOverlay(ovly);
		ic.repaint();
		rangeArrowsVisible = true;
	}

	void hideRangeArrows() {
		if (imp == null || rangeArrowRois == null)
			return;
		Overlay ovly = imp.getOverlay();
		if (ovly == null)
			return;
		for (Roi roi : rangeArrowRois)
			ovly.remove(roi);
		ic.repaint();
		rangeArrowsVisible = false;
		activeRangeArrow = -1;
	}

	/**
	 * Returns the index of the range arrow at cursor position x,y, or -1 of
	 * none. Index numbers start with 0 at the 'down' arrow of the lower side of
	 * the x axis and end with the up arrow at the upper side of the y axis.
	 */
	int getRangeArrowIndex(int x, int y) {
		if (!rangeArrowsVisible)
			return -1;
		for (int i = 0; i < rangeArrowRois.length; i++)
			if (rangeArrowRois[i].getBounds().contains(x, y))
				return i;
		return -1;
	}

	private String d2s(double n) {
		int digits = Tools.getDecimalPlaces(n);
		if (digits > 2)
			digits = 2;
		return IJ.d2s(n, digits);
	}

	private void toggleDataType() {
		saveRaw = !saveRaw;
		if (saveRaw) {
			type.setForeground(Color.red);
			type.setLabel(" Raw ");
		} else {
			type.setForeground(Color.black);
			type.setLabel(" Proc ");
		}
	}

	private void setLogScale(boolean doLog, boolean doStack) {
		if (doStack && heatCharts != null) {
			for (int i = 0; i < heatCharts.length; i++) {
				if (heatCharts[i] == null)
					continue;
				heatCharts[i].setLogScale(doLog);
			}
		}

		if (heatChart != null)
			heatChart.setLogScale(doLog);

	}

	private void setNumOfFTs(int[] numOfFTs, boolean doStack) {
		if (doStack && heatCharts != null) {
			for (int i = 0; i < heatCharts.length; i++) {
				if (heatCharts[i] == null)
					continue;
				heatCharts[i].setNumOfFTs(numOfFTs);
			}
		}

		if (heatChart != null)
			heatChart.setNumOfFTs(numOfFTs);

	}

	private void setStatsMethod(int statsMethod, boolean doStack) {
		if (doStack && heatCharts != null) {
			for (int i = 0; i < heatCharts.length; i++) {
				if (heatCharts[i] == null)
					continue;
				switch (statsMethod) {
				case MEDIAN:
					heatCharts[i].setStatsMethod(HeatChart.MEDIAN);
					break;
				case MEAN:
					heatCharts[i].setStatsMethod(HeatChart.MEAN);
					break;
				case MODE:
					heatCharts[i].setStatsMethod(HeatChart.MODE);
					break;
				default:
					break;
				}
			}
		}

		if (heatChart != null) {
			switch (statsMethod) {
			case MEDIAN:
				heatChart.setStatsMethod(HeatChart.MEDIAN);
				break;
			case MEAN:
				heatChart.setStatsMethod(HeatChart.MEAN);
				break;
			case MODE:
				heatChart.setStatsMethod(HeatChart.MODE);
				break;
			default:
				break;
			}
		}
	}

	private void setCalculator(int choiceIdx) {
		if (doStack && heatCharts != null) {
			for (int i = 0; i < heatCharts.length; i++) {
				if (heatCharts[i] == null)
					continue;
				heatCharts[i].setCalculator(choiceIdx);
			}
		}

		if (heatChart != null) {
			heatChart.setCalculator(choiceIdx);
		}
	}

	public void updateImage() {
		updateImage(this.doStack);
	}

	private void updateImage(boolean doStack) {
		if (doStack && heatCharts != null) {
			for (int i = 0; i < heatCharts.length; i++) {
				if (heatCharts[i] == null)
					continue;
				heatCharts[i].updateImage();
			}
		}

		if (heatChart != null) {
			heatChart.updateImage();
			stackFrame = heatChart.getDrawingFrame();
		}
		ic.repaint();
	}

	@Override
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		super.adjustmentValueChanged(e);
		if (heatCharts != null && imp.getCurrentSlice() <= heatCharts.length)
			heatChart = heatCharts[imp.getCurrentSlice() - 1];
	}

	/**
	 * Updates the X and Y values when the mouse is moved and, if appropriate,
	 * shows/hides the overlay with the triangular buttons for changing the axis
	 * range limits Overrides mouseMoved() in ImageWindow.
	 * 
	 * @see ij.gui.ImageWindow#mouseMoved
	 */
	@Override
	public void mouseMoved(int x, int y) {
		super.mouseMoved(x, y);
		if (heatChart == null)
			return;

		if (stackFrame == null || coordinates == null)
			return;
		if (stackFrame.contains(x, y))
			coordinates.setText("X=" + d2s(heatChart.getX(x)) + ", Y=" + d2s(heatChart.getY(y)) + ", Z="
					+ d2s(heatChart.getZ(x, y)));
		else
			coordinates.setText(blankLabel);

		// arrows for modifying the plot range
		if (x < leftMargin || y > topMargin + frameHeight) {
			if (!rangeArrowsVisible)// && !plot.isFrozen())
				showRangeArrows();
			if (activeRangeArrow >= 0 && !rangeArrowRois[activeRangeArrow].contains(x, y)) {
				rangeArrowRois[activeRangeArrow].setFillColor(Color.GRAY);
				ic.repaint(); // de-highlight arrow where cursor has moved out
				activeRangeArrow = -1;
			}
			if (activeRangeArrow < 0) { // highlight arrow below cursor (if any)
				int i = getRangeArrowIndex(x, y);
				if (i >= 0) { // we have an arrow at cursor position
					rangeArrowRois[i].setFillColor(Color.RED);
					activeRangeArrow = i;
					ic.repaint();
				}
			}
		} else if (rangeArrowsVisible)
			hideRangeArrows();
	}

	@Override
	/**
	 * Update the slice display by the scrollbar
	 */
	public void run() {
		while (!done) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
			if (done)
				return;
			if (slice > 0) {
				int s = slice;
				slice = 0;
				if (s != imp.getCurrentSlice()) {
					imp.setSlice(s);
					heatChart = heatCharts[s - 1];
				}
			}
		}
	}

	/** Called if user has activated a button or popup menu item */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b == list)
			showList();
		else if (b == save)
			saveAsText();
		else if (b == copy)
			copyToClipboard(true);
		else if (b == type)
			toggleDataType();
		else if (b == plot)
			plotDialog();
		if(b!=list)
			ic.requestFocus(); // have focus on the canvas, not the button, so that
							// pressing the space bar allows panning
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub

	}

	// when writing data in scientific mode, use at least 4 decimals behind the
	// decimal point
	static final int MIN_SCIENTIFIC_DIGITS = 4;
	// when writing float data, precision should be at least 1e-5*data range
	static final double MIN_FLOAT_PRECISION = 1e-5;

	/**
	 * get the number of digits for writing a column to the results table or the
	 * clipboard
	 */
	static int getPrecision(float[] values) {
		int setDigits = Analyzer.getPrecision();
		int measurements = Analyzer.getMeasurements();
		boolean scientificNotation = (measurements & Measurements.SCIENTIFIC_NOTATION) != 0;
		if (scientificNotation) {
			if (setDigits < MIN_SCIENTIFIC_DIGITS)
				setDigits = MIN_SCIENTIFIC_DIGITS;
			return -setDigits;
		}
		boolean allInteger = true;
		float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
		for (int i = 0; i < values.length; i++) {
			if ((int) values[i] != values[i] && !Float.isNaN(values[i])) {
				allInteger = false;
				if (values[i] < min)
					min = values[i];
				if (values[i] > max)
					max = values[i];
			}
		}
		if (allInteger)
			return 0;
		int digits = (max - min) > 0 ? getDigits(min, max, MIN_FLOAT_PRECISION * (max - min), 15)
				: getDigits(max, MIN_FLOAT_PRECISION * Math.abs(max), 15);
		if (setDigits > Math.abs(digits))
			digits = setDigits * (digits < 0 ? -1 : 1); // use scientific
														// notation if needed
		return digits;
	}

	// Number of digits to display the number n with resolution 'resolution';
	// (if n is integer and small enough to display without scientific notation,
	// no decimals are needed, irrespective of 'resolution')
	// Scientific notation is used for more than 'maxDigits' (must be >=3), and
	// indicated
	// by a negative return value
	static int getDigits(double n, double resolution, int maxDigits) {
		if (n == Math.round(n) && Math.abs(n) < Math.pow(10, maxDigits - 1) - 1) // integers
																					// and
																					// not
																					// too
																					// big
			return 0;
		else
			return getDigits2(n, resolution, maxDigits);
	}

	// Number of digits to display the range between n1 and n2 with resolution
	// 'resolution';
	// Scientific notation is used for more than 'maxDigits' (must be >=3), and
	// indicated
	// by a negative return value
	static int getDigits(double n1, double n2, double resolution, int maxDigits) {
		if (n1 == 0 && n2 == 0)
			return 0;
		return getDigits2(Math.max(Math.abs(n1), Math.abs(n2)), resolution, maxDigits);
	}

	static int getDigits2(double n, double resolution, int maxDigits) {
		int log10ofN = (int) Math.floor(Math.log10(Math.abs(n)) + 1e-7);
		int digits = resolution != 0 ? -(int) Math.floor(Math.log10(Math.abs(resolution)) + 1e-7)
				: Math.max(0, -log10ofN + maxDigits - 2);
		int sciDigits = -Math.max((log10ofN + digits), 1);
		// IJ.log("n="+(float)n+"digitsRaw="+digits+" log10ofN="+log10ofN+"
		// sciDigits="+sciDigits);
		if (digits < -2 && log10ofN >= maxDigits)
			digits = sciDigits; // scientific notation for large numbers
		else if (digits < 0)
			digits = 0;
		else if (digits > maxDigits - 1 && log10ofN < -2)
			digits = sciDigits; // scientific notation for small numbers
		return digits;
	}
	// names for popupMenu items
	/*
	 * private static final int COPY_TYPE = 0, AXIS_OPTIONS = 2, LAST_ITEM = 3;
	 * 
	 * PopupMenu getPopupMenu() { popupMenu = new PopupMenu(); menuItems = new
	 * MenuItem[LAST_ITEM]; menuItems[COPY_TYPE] = addPopupItem(popupMenu,
	 * "Raw Data", true); popupMenu.addSeparator(); menuItems[AXIS_OPTIONS] =
	 * addPopupItem(popupMenu, "Plot Options...");
	 * 
	 * return popupMenu; }
	 * 
	 * MenuItem addPopupItem(PopupMenu popupMenu, String s) { return
	 * addPopupItem(popupMenu, s, false); }
	 * 
	 * MenuItem addPopupItem(PopupMenu popupMenu, String s, boolean
	 * isCheckboxItem) { MenuItem mi = null; if (isCheckboxItem) { mi = new
	 * CheckboxMenuItem(s); ((CheckboxMenuItem)mi).addItemListener(this); } else
	 * { mi = new MenuItem(s); mi.addActionListener(this); } popupMenu.add(mi);
	 * return mi; }
	 */

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	private GenericDialog gd;
	private static final String[] STATS_METHODS = { "Median", "Mean", "Mode" };
	public static final int MEDIAN = 0, MEAN = 1, MODE = 2;
	private boolean logScale, doStack;
	private int[] numOfFTs = { PluginConstants.DEFAULT_FT, PluginConstants.DEFAULT_FT };
	private int statsMethod = MEDIAN, choiceIdx = 0;

	public static String getStatsName(int idx) {
		if (idx >= 0 && idx < STATS_METHODS.length)
			return STATS_METHODS[idx];
		else
			return null;
	}

	public static String[] getAllStatsMethods() {
		return STATS_METHODS.clone();
	}

	private void plotDialog() {

		boolean doStack = heatCharts != null && heatCharts.length > 1 && slice > 1;

		gd = new GenericDialog("Plot Options");
		String[] metricNames = BasicCalculator.getAllMetrics();
		if (metricNames != null && metricNames.length > 0)
			gd.addChoice("Metric", metricNames, metricNames[choiceIdx]);
		// gd.addMessage("If "+metricNames[0]+" is chosen, you can also choose
		// the scale:");
		// TOS now is divided into TOS(linear) and TOS(log)
		// gd.addCheckbox(metricNames[0]+" Log Scale", logScale);
		if (doStack)
			gd.addCheckbox("Apply to Stack", this.doStack);
		gd.addChoice("Central Tendency", STATS_METHODS, STATS_METHODS[statsMethod]);
		gd.addNumericField("Percentage (C1)", numOfFTs[0], 0);
		gd.addNumericField("Percentage (C2)", numOfFTs[1], 0);
		gd.showDialog();

		if (gd.wasCanceled())
			return;

		// logScale = gd.getNextBoolean();
		if (doStack)
			this.doStack = gd.getNextBoolean();
		else
			this.doStack = false;
		choiceIdx = gd.getNextChoiceIndex();
		statsMethod = gd.getNextChoiceIndex();
		for (int i = 0; i < numOfFTs.length; i++) {
			numOfFTs[i] = (int) gd.getNextNumber();
		}
		logScale = metricNames[choiceIdx].equals(MatrixCalculator.SHOW_TOS_LOG2);

		setLogScale(logScale, this.doStack);
		setNumOfFTs(numOfFTs, this.doStack);
		setStatsMethod(statsMethod, this.doStack);
		setCalculator(choiceIdx);
		updateImage(this.doStack);

	}

	// For whatever reason, I overrode
	/*
	 * public void windowClosing(WindowEvent e) { //IJ.log("windowClosing: "
	 * +imp.getTitle()+" "+closed); if (closed) return; //For whatever reason,
	 * IJ.doCommand doesn't work here //Probably because this window cannot been
	 * found in WindowManager. if (ij!=null) {
	 * WindowManager.setCurrentWindow(this); IJ.doCommand("Close"); } else {
	 * setVisible(false); dispose(); WindowManager.removeWindow(this); } }
	 */
}
