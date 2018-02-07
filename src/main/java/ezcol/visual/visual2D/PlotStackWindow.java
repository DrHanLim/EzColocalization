package ezcol.visual.visual2D;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ezcol.debug.ExceptionHandler;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageLayout;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PlotCanvas;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.RoiListener;
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
public class PlotStackWindow extends StackWindow implements ActionListener,	ClipboardOwner, Runnable{

	
	/** Display points using a circle 5 pixels in diameter. */
	public static final int CIRCLE = Plot.CIRCLE;
	/** Display points using an X-shaped mark. */
	public static final int X = Plot.X;
	/** Display points using an box-shaped mark. */
	public static final int BOX = Plot.BOX;
	/** Display points using an tiangular mark. */
	public static final int TRIANGLE = Plot.TRIANGLE;
	/** Display points using an cross-shaped mark. */
	public static final int CROSS = Plot.CROSS;
	/** Connect points with solid lines. */
	public static final int LINE = Plot.LINE;
	/** Save x-values only. To set, use Edit/Options/
		Profile Plot Options. */
	public static boolean saveXValues;
	/** Automatically close window after saving values. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean autoClose;
	/** Display the XY coordinates in a separate window. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean listValues;
	/** Interpolate line profiles. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean interpolate;
	// default values for new installations; values will be then saved in prefs
	private static final int WIDTH = 450;
	private static final int HEIGHT = 200;
	private static final int FONT_SIZE = 12;
	/** The width of the plot (without frame) in pixels. */
	public static int plotWidth = WIDTH;
	/** The height of the plot in pixels. */
	public static int plotHeight = HEIGHT;
	/** The plot text size, can be overridden by Plot.setFont, Plot.setFontSize, Plot.setXLabelFont etc. */
	public static int fontSize = FONT_SIZE;
	/** Have axes with no grid lines. If both noGridLines and noTicks are true,
	 *	only min&max value of the axes are given */
	public static boolean noGridLines;
	/** Have axes with no ticks. If both noGridLines and noTicks are true,
	 *	only min&max value of the axes are given */
	public static boolean noTicks;

	
	private static final String PREFS_WIDTH = "pp.width";
	private static final String PREFS_HEIGHT = "pp.height";
	private static final String PREFS_FONT_SIZE = "pp.fontsize";
	private static final String OPTIONS = "pp.options";
	private static final int SAVE_X_VALUES = 1;
	private static final int AUTO_CLOSE = 2;
	private static final int LIST_VALUES = 4;
	private static final int INTERPOLATE = 8;
	private static final int NO_GRID_LINES = 16;
	private static final int NO_TICKS = 32;

	private Button list, save, copy, log;
	private Label coordinates;
	private static int options;
	//denotes the current plot
	private Plot plot;
	//private static Plots staticPlot;
	boolean layoutDone;				// becomes true after the layout has been done, used by PlotCanvas
	private String blankLabel = "                       ";

	private Roi[] rangeArrowRois;	// these constitute the arrow overlays for changing the range
	private boolean rangeArrowsVisible;
	private int activeRangeArrow = -1;
	
	//new fields
	private Plot[] plots;
	
	//default fields from Plot
	@SuppressWarnings("deprecation")
	int leftMargin = Plot.LEFT_MARGIN, rightMargin = Plot.RIGHT_MARGIN, topMargin = Plot.TOP_MARGIN, bottomMargin = Plot.BOTTOM_MARGIN;
	int frameWidth;							//width corresponding to plot range; frame.width is larger by 1
	int frameHeight;						//height corresponding to plot range; frame.height is larger by 1
	Rectangle stackFrame = null;
	private boolean logScale;
	
	// static initializer
	static {
		options = Prefs.getInt(OPTIONS, SAVE_X_VALUES);
		saveXValues = (options&SAVE_X_VALUES)!=0;
		autoClose = (options&AUTO_CLOSE)!=0;
		listValues = (options&LIST_VALUES)!=0;
		plotWidth = Prefs.getInt(PREFS_WIDTH, WIDTH);
		plotHeight = Prefs.getInt(PREFS_HEIGHT, HEIGHT);
		fontSize = Prefs.getInt(PREFS_FONT_SIZE, FONT_SIZE);
		interpolate = (options&INTERPOLATE)==0; // 0=true, 1=false
		noGridLines = (options&NO_GRID_LINES)!=0; 
		noTicks = (options&NO_TICKS)!=0; 
	}
	
	/** Creates a PlotWindow from a Plot object. */
	public PlotStackWindow(Plot plot) {
		super(plot.getImagePlus(),new PlotCanvas(plot.getImagePlus()));
		this.plot = plot;
		//((PlotCanvas)getCanvas()).setPlot(plot);
		((PlotCanvas)getCanvas()).setPlot(plot);
		draw();
	}
	
	/** Creates a PlotWindow from a Plot object. */
	PlotStackWindow(Plot[] plots, ImagePlus imp) {
		super(imp,new PlotCanvas(imp));
		//((PlotCanvas)getCanvas()).setPlot(plot);
		if(plots!=null&&plots.length>0)
		{
			this.plots = plots;
			this.plot = plots[0];
			((PlotCanvas)getCanvas()).setPlot(plot);
			//not sure if it is a bug in ImageJ but ImageJ will not update after zoom out
			//this is because the displayed ImagePlus doesn't match the one in Plot
			//Fix it here to get zoom out work properly
			for(int i=0;i<this.plots.length;i++)
				this.plots[i].setImagePlus(imp);
			draw();
		}
	}
	
	/** Displays the plot. */
	protected void draw() {
		Panel bottomPanel = new Panel();
		int hgap = IJ.isMacOSX()?1:5;

		list = new Button(" List ");
		list.addActionListener(this);
		bottomPanel.add(list);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,hgap,0));
		save = new Button("Save...");
		save.addActionListener(this);
		bottomPanel.add(save);
		copy = new Button(" Copy ");
		copy.addActionListener(this);
		bottomPanel.add(copy);
		log = new Button(" Log ");
		log.addActionListener(this);
		bottomPanel.add(log);
		
		coordinates = new Label(blankLabel);
		coordinates.setFont(new Font("Monospaced", Font.PLAIN, 12));
		coordinates.setBackground(new Color(220, 220, 220));
		bottomPanel.add(coordinates);
		add(bottomPanel);
		
		plot.draw();
		
		LayoutManager lm = getLayout();
		if (lm instanceof ImageLayout)
			((ImageLayout)lm).ignoreNonImageWidths(true);  //don't expand size to make the panel fit
		pack();

		ImageProcessor ip = plot.getProcessor();
		if ((ip instanceof ColorProcessor) && (imp.getProcessor() instanceof ByteProcessor))
			imp.setProcessor(null, ip);
		else
			imp.updateAndDraw();
		layoutDone = true;
		if (listValues)
			showList();
		else
			ic.requestFocus();	//have focus on the canvas, not the button, so that pressing the space bar allows panning
	
		//get the frame from plot because frame is private in plot
		stackFrame = plot.getDrawingFrame();
		frameWidth = stackFrame.width;
		frameHeight = stackFrame.height;
		
	}
	
	/** Draws a new plot in this window. */
	public void drawPlot(Plot plot) {
		if(plot!=null){
			this.plot = plot;
			plots[imp.getCurrentSlice()-1]=this.plot;
			if (imp!=null) {
				if (ic instanceof PlotCanvas)
					((PlotCanvas)ic).setPlot(plot);
				imp.setProcessor(null, plot.getProcessor());
				plot.setImagePlus(imp); //also adjusts the calibration of imp
			}
		}
	}
	
	@Deprecated
	public void showStack(){
		if ((IJ.macroRunning() && IJ.getInstance()==null) || Interpreter.isBatchMode()) {
			imp = plot.getImagePlus();
			WindowManager.setTempCurrentImage(imp);
			/*if (getMainCurveObject() != null) {
				imp.setProperty("XValues", getXValues()); // Allows values to be retrieved by 
				imp.setProperty("YValues", getYValues()); // by Plot.getValues() macro function
			}
			Interpreter.addBatchModeImage(imp);
			return null;*/
		}
		if (imp != null) {
			Window win = imp.getWindow();
			if (win instanceof PlotStackWindow && win.isVisible()) {
				plot.updateImage();			// show in existing window
			}
		}
		if (imp == null)
			imp.setProperty(Plot.PROPERTY_KEY, null);
		imp = getImagePlus();
		imp.setProperty(Plot.PROPERTY_KEY, this);
		if (IJ.isMacro() && imp!=null) // wait for plot to be displayed
			IJ.selectWindow(imp.getID());
	}
	
	/** Shows the data of the backing plot in a Textwindow with columns */
	void showList(){
		ResultsTable rt = plot.getResultsTable(saveXValues);
		rt.show("Plot Values");
		if (autoClose) {
			imp.changes=false;
			close();
		}
	}
	
	/** Copy the first dataset or all values to the clipboard */
	void copyToClipboard(boolean writeAllColumns) {
		float[] xValues = plot.getXValues();
		float[] yValues = plot.getYValues();
		if (xValues == null) return;
		Clipboard systemClipboard = null;
		try {systemClipboard = getToolkit().getSystemClipboard();}
		catch (Exception e) {systemClipboard = null; }
		if (systemClipboard==null)
			{IJ.error("Unable to copy to Clipboard."); return;}
		IJ.showStatus("Copying plot values...");
		CharArrayWriter aw = new CharArrayWriter(10*xValues.length);
		PrintWriter pw = new PrintWriter(aw); //uses platform's line termination characters

		if (writeAllColumns) {
			ResultsTable rt = plot.getResultsTable(true);
			if (!Prefs.dontSaveHeaders) {
				String headings = rt.getColumnHeadings();
				pw.println(headings);
			}
			for (int i=0; i<rt.size(); i++)
				pw.println(rt.getRowAsString(i));
		} else {
			int xdigits = 0;
			if (saveXValues)
				xdigits = getPrecision(xValues);
			int ydigits = getPrecision(yValues);
			for (int i=0; i<Math.min(xValues.length, yValues.length); i++) {
				if (saveXValues)
					pw.println(IJ.d2s(xValues[i],xdigits)+"\t"+IJ.d2s(yValues[i],ydigits));
				else
					pw.println(IJ.d2s(yValues[i],ydigits));
			}
		}
		String text = aw.toString();
		pw.close();
		StringSelection contents = new StringSelection(text);
		systemClipboard.setContents(contents, this);
		IJ.showStatus(text.length() + " characters copied to Clipboard");
		if (autoClose)
			{imp.changes=false; close();}
	}
	
	/** Returns the plot values as a ResultsTable. */
	public ResultsTable getResultsTable() {
		return plot.getResultsTable(saveXValues);
	}
	
	/** Saves the data of the plot in a text file */
	void saveAsText() {
		if (plot.getXValues() == null) {
			IJ.error("Plot has no data");
			return;
		}
		SaveDialog sd = new SaveDialog("Save as Text", "Values", Prefs.defaultResultsExtension());
		String name = sd.getFileName();
		if (name==null) return;
		String directory = sd.getDirectory();
		IJ.wait(250);  // give system time to redraw ImageJ window
		IJ.showStatus("Saving plot values...");
		ResultsTable rt = getResultsTable();
		try {
			rt.saveAs(directory+name);
		} catch (IOException e) {
			IJ.error("" + e);
			return;
		}
		if (autoClose)
			{imp.changes=false; close();}
	}
	
	/** Creates an overlay with triangular buttons for changing the axis range limits and shows it */
	void showRangeArrows() {
		if (imp == null) return;
		hideRangeArrows(); //in case we have old arrows from a different plot size or so
		rangeArrowRois = new Roi[4*2]; //4 arrows per axis
		int i=0;
		int height = imp.getHeight();
		int arrowH = topMargin < 14 ? 6 : 8; //height of arrows and distance between them; base is twice that value
		float[] yP = new float[]{height-arrowH/2, height-3*arrowH/2, height-5*arrowH/2-0.1f};
		for (float x : new float[]{leftMargin, leftMargin+frameWidth}) { //create arrows for x axis
			float[] x0 = new float[]{x-arrowH/2, x-3*arrowH/2-0.1f, x-arrowH/2};
			rangeArrowRois[i++] = new PolygonRoi(x0, yP, 3, Roi.POLYGON);
			float[] x1 = new float[]{x+arrowH/2, x+3*arrowH/2+0.1f, x+arrowH/2};
			rangeArrowRois[i++] = new PolygonRoi(x1, yP, 3, Roi.POLYGON);
		}
		float[] xP = new float[]{arrowH/2-0.1f, 3*arrowH/2, 5*arrowH/2+0.1f};
		for (float y : new float[]{topMargin+frameHeight, topMargin}) { //create arrows for y axis
			float[] y0 = new float[]{y+arrowH/2, y+3*arrowH/2+0.1f, y+arrowH/2};
			rangeArrowRois[i++] = new PolygonRoi(xP, y0, 3, Roi.POLYGON);
			float[] y1 = new float[]{y-arrowH/2, y-3*arrowH/2-0.1f, y-arrowH/2};
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
		if (imp == null || rangeArrowRois==null) return;
		Overlay ovly = imp.getOverlay();
		if (ovly == null) return;
		for (Roi roi : rangeArrowRois)
			ovly.remove(roi);
		ic.repaint();
		rangeArrowsVisible = false;
		activeRangeArrow = -1;
	}
	
	/** Returns the index of the range arrow at cursor position x,y, or -1 of none.
	 *	Index numbers start with 0 at the 'down' arrow of the lower side of the x axis
	 *	and end with the up arrow at the upper side of the y axis. */
	int getRangeArrowIndex(int x, int y) {
		if (!rangeArrowsVisible) return -1;
		for (int i=0; i<rangeArrowRois.length; i++)
			if (rangeArrowRois[i].getBounds().contains(x,y))
				return i;
		return -1;
	}
	
	private String d2s(double n) {
		int digits = Tools.getDecimalPlaces(n);
		if (digits>2) digits=2;
		return IJ.d2s(n,digits);
    }
	
	private void setLogScale(boolean doLog){
		if(plots!=null){
			for(int i=0;i<plots.length;i++){
				if(plots[i]==null)
					continue;
				plots[i].setAxisXLog(doLog);
				plots[i].setAxisYLog(doLog);
				plots[i].updateImage();
			}
		}
		if(plot!=null){
			plot.setAxisXLog(doLog);
			plot.setAxisYLog(doLog);
			plot.updateImage();
		} 
		ic.repaint();
	}
	
	@Override
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		super.adjustmentValueChanged(e);
		if(plots!=null && imp.getCurrentSlice()<=plots.length){
			plot = plots[imp.getCurrentSlice()-1];
			((PlotCanvas)getCanvas()).setPlot(plot);
		}
	}
	
	/** Updates the X and Y values when the mouse is moved and, if appropriate, shows/hides
	 *	the overlay with the triangular buttons for changing the axis range limits
	 *	Overrides mouseMoved() in ImageWindow. 
	 *	@see ij.gui.ImageWindow#mouseMoved
	 */
	@Override
	public void mouseMoved(int x, int y) {
		super.mouseMoved(x, y);
		if (plot==null) return;
		
		if (stackFrame==null || coordinates==null)
			return;
		if (stackFrame.contains(x, y))
			coordinates.setText("X=" + d2s(plot.descaleX(x))+", Y=" + d2s(plot.descaleY(y)));
		else
			coordinates.setText("");
		
		//arrows for modifying the plot range
		if (x<leftMargin || y>topMargin+frameHeight) {
			if (!rangeArrowsVisible && !plot.isFrozen())
				showRangeArrows();
			if (activeRangeArrow >= 0 && !rangeArrowRois[activeRangeArrow].contains(x,y)) {
				rangeArrowRois[activeRangeArrow].setFillColor(Color.GRAY);
				ic.repaint();			//de-highlight arrow where cursor has moved out
				activeRangeArrow = -1;
			}
			if (activeRangeArrow < 0) { //highlight arrow below cursor (if any)
				int i = getRangeArrowIndex(x,y);
				if (i >= 0) {			//we have an arrow at cursor position
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
			IJ.wait(50);	//delay to make sure the roi has been updated
			synchronized(this) {
				try {wait();}
				catch(InterruptedException e) {}
			}
			if (done) return;
			if (slice>0) {
				int s = slice;
				slice = 0;
				if (s!=imp.getCurrentSlice())
				{
					imp.setSlice(s);
					plot = plots[s-1];
					((PlotCanvas)getCanvas()).setPlot(plot);
				}
			}
		}
	}
	
	/** Called if user has activated a button or popup menu item */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b==list)
			showList();
		else if (b==save)
			saveAsText();
		else if (b==copy)
			copyToClipboard(true);
		else if (b==log)
		{
			logScale=!logScale;
			if(logScale)
				log.setForeground(Color.RED);
			else
				log.setForeground(Color.BLACK);
			setLogScale(logScale);
		}
		if(b!=list)
			ic.requestFocus();	//have focus on the canvas, not the button, so that pressing the space bar allows panning
	}
	
	

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub
		
	}
	
	// when writing data in scientific mode, use at least 4 decimals behind the decimal point
	static final int MIN_SCIENTIFIC_DIGITS = 4;
	// when writing float data, precision should be at least 1e-5*data range
	static final double MIN_FLOAT_PRECISION = 1e-5;
		
	/** get the number of digits for writing a column to the results table or the clipboard */
	static int getPrecision(float[] values) {
		int setDigits = Analyzer.getPrecision();
		int measurements = Analyzer.getMeasurements();
		boolean scientificNotation = (measurements&Measurements.SCIENTIFIC_NOTATION)!=0;
		if (scientificNotation) {
			if (setDigits<MIN_SCIENTIFIC_DIGITS)
				setDigits = MIN_SCIENTIFIC_DIGITS;
			return -setDigits;
		}
		boolean allInteger = true;
		float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
		for (int i=0; i<values.length; i++) {
			if ((int)values[i]!=values[i] && !Float.isNaN(values[i])) {
				allInteger = false;
			if (values[i] < min) min = values[i];
			if (values[i] > max) max = values[i];
			}
		}
		if (allInteger)
			return 0;
		int digits = (max - min) > 0 ? getDigits(min, max, MIN_FLOAT_PRECISION*(max-min), 15) :
				getDigits(max, MIN_FLOAT_PRECISION*Math.abs(max), 15);
		if (setDigits>Math.abs(digits))
			digits = setDigits * (digits < 0 ? -1 : 1);		//use scientific notation if needed
		return digits;
	}
	
	// Number of digits to display the number n with resolution 'resolution';
	// (if n is integer and small enough to display without scientific notation,
	// no decimals are needed, irrespective of 'resolution')
	// Scientific notation is used for more than 'maxDigits' (must be >=3), and indicated
	// by a negative return value
	static int getDigits(double n, double resolution, int maxDigits) {
		if (n==Math.round(n) && Math.abs(n) < Math.pow(10,maxDigits-1)-1) //integers and not too big
			return 0;
		else
			return getDigits2(n, resolution, maxDigits);
	}

	// Number of digits to display the range between n1 and n2 with resolution 'resolution';
	// Scientific notation is used for more than 'maxDigits' (must be >=3), and indicated
	// by a negative return value
	static int getDigits(double n1, double n2, double resolution, int maxDigits) {
		if (n1==0 && n2==0) return 0;
		return getDigits2(Math.max(Math.abs(n1),Math.abs(n2)), resolution, maxDigits);
	}

	static int getDigits2(double n, double resolution, int maxDigits) {
		int log10ofN = (int)Math.floor(Math.log10(Math.abs(n))+1e-7);
		int digits = resolution != 0 ?
				-(int)Math.floor(Math.log10(Math.abs(resolution))+1e-7) : 
				Math.max(0, -log10ofN+maxDigits-2);
		int sciDigits = -Math.max((log10ofN+digits),1);
		//IJ.log("n="+(float)n+"digitsRaw="+digits+" log10ofN="+log10ofN+" sciDigits="+sciDigits);
		if (digits < -2 && log10ofN >= maxDigits)
			digits = sciDigits; //scientific notation for large numbers
		else if (digits < 0)
			digits = 0;
		else if (digits > maxDigits-1 && log10ofN < -2)
			digits = sciDigits; // scientific notation for small numbers
		return digits;
	}

	
	/** Mouse wheel: zooms when shift or ctrl is pressed, scrolls in x if space bar down, in y otherwise. */
	@Override
	public synchronized void mouseWheelMoved(MouseWheelEvent e) {
		if (plot.isFrozen() || !(ic instanceof PlotCanvas)) {	   //frozen plots are like normal images
			super.mouseWheelMoved(e);
			return;
		}
		int rotation = e.getWheelRotation();
		int amount = e.getScrollAmount();
		boolean ctrl = (e.getModifiers()&Event.CTRL_MASK)!=0;
		if (amount<1) amount=1;
		if (rotation==0)
			return;
		if (ctrl||IJ.shiftKeyDown()) {
			Point loc = ic.getCursorLoc();
			int x = ic.screenX(loc.x);
			int y = ic.screenY(loc.y);
			if(rotation<0)
				((PlotCanvas)ic).zoomIn(x, y);
			else
				((PlotCanvas)ic).zoomOut(x, y);
			plot.updateImage();
			
		} else if (IJ.spaceBarDown()){
			scroll(plot,rotation*amount*Math.max(imp.getWidth()/50, 1), 0);
		}
		else{
			scroll(plot,0, rotation*amount*Math.max(imp.getHeight()/50, 1));
		}
		ic.repaint();
	}
	
	private void scroll(Plot plot, int dx, int dy){
		double[] currentMinMax = plot.getLimits();
		Rectangle rct = plot.getDrawingFrame();
		double xScale, yScale;
		xScale = rct.width / (currentMinMax[1]-currentMinMax[0]);
		yScale = rct.height / (currentMinMax[3]-currentMinMax[2]);
		if (logScale) {
			currentMinMax[0] /= Math.pow(10, dx/xScale);
			currentMinMax[1] /= Math.pow(10, dx/xScale);
		} else {
			currentMinMax[0] -= dx/xScale;
			currentMinMax[1] -= dx/xScale;
		}
		if (logScale) {
			currentMinMax[2] *= Math.pow(10, dy/yScale);
			currentMinMax[3] *= Math.pow(10, dy/yScale);
		} else {
			currentMinMax[2] += dy/yScale;
			currentMinMax[3] += dy/yScale;
		}
		plot.setLimits(currentMinMax[0], currentMinMax[1],currentMinMax[2], currentMinMax[3]);
		//plot.getImagePlus().duplicate().show();
		//ic.repaint();
	}
	
	/** Called when the canvas is resized */
	void updateMinimumSize() {
		if (plot == null) return;
		Dimension d1 = getExtraSizeThis();
		Dimension d2 = plot.getMinimumSize();
		setMinimumSize(new Dimension(d1.width + d2.width, d1.height + d2.height));
	}
	
	private Dimension getExtraSizeThis(){
		Insets insets = getInsets();
		int extraWidth = insets.left+insets.right + 10;
		int extraHeight = insets.top+insets.bottom + 10;
		if (extraHeight==20) extraHeight = 42;
		int members = getComponentCount();
		//if (IJ.debugMode) IJ.log("getExtraSize: "+members+" "+insets);
		for (int i=1; i<members; i++) {
		    Component m = getComponent(i);
		    Dimension d = m.getPreferredSize();
			extraHeight += d.height + 5;
			if (IJ.debugMode) IJ.log(i+"  "+d.height+" "+extraHeight);
		}
		return new Dimension(extraWidth, extraHeight);
	}
	
	public Plot getPlot() {
		return plot;
	}

}
