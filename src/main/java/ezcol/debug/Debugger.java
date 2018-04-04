package ezcol.debug;

import java.awt.Font;
import java.awt.Frame;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ezcol.align.TurboRegMod;
import ezcol.cell.CellData;
import ezcol.main.PluginConstants;
import ezcol.main.PluginStatic;
import ezcol.visual.visual2D.HistogramStackWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.StackWindow;
import ij.io.LogStream;
import ij.measure.ResultsTable;
import ij.text.TextPanel;
import ij.text.TextWindow;
import ij.util.Tools;

/**
 * This class catches most if not all waring and error messages during analysis
 * and print it in the log window.
 * 
 * @author Huanjie Sheng
 *
 */
public class Debugger implements PluginConstants {

	// [0] Thread.getStackTrace
	// [1] ExceptionReporter
	// [2] The line where addError is called
	private static final int TRACE_LINE = 2;
	private static Map<String, Long> timeMap = new HashMap<String, Long>();
	private static final String SEPARATOR = "_____________________________________________";

	public static void printStackTrace(int num) {
		if (Thread.currentThread() != null)
			for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
				System.out.println(ste);
				num--;
				if (num <= 0)
					break;
			}
	}

	public static void printCurrentLine(String str) {
		System.out.println(SEPARATOR);
		System.out.println("(" + Thread.currentThread().getStackTrace()[TRACE_LINE] + "): ");
		System.out.println(str);
	}

	public static void printCurrentLine() {
		System.out.println("(" + Thread.currentThread().getStackTrace()[TRACE_LINE] + ")");
	}

	public static String currentLine() {
		return "(" + Thread.currentThread().getStackTrace()[TRACE_LINE] + ")";
	}

	public static ResultsTable print2RT(float[] cellData, String title) {
		ResultsTable rt = new ResultsTable();
		if (cellData == null)
			return rt;
		for (int i = 0; i < cellData.length; i++) {
			rt.incrementCounter();
			rt.addValue(title, cellData[i]);
		}
		return rt;
	}

	public static ResultsTable print2RT(double[] cellData, String title) {
		ResultsTable rt = new ResultsTable();
		if (cellData == null)
			return rt;
		for (int i = 0; i < cellData.length; i++) {
			rt.incrementCounter();
			rt.addValue(title, cellData[i]);
		}
		return rt;
	}

	public static ResultsTable print2RT(int[] cellData, String title) {
		ResultsTable rt = new ResultsTable();
		if (cellData == null)
			return rt;
		for (int i = 0; i < cellData.length; i++) {
			rt.incrementCounter();
			rt.addValue(title, cellData[i]);
		}
		return rt;
	}

	public static void printMinMax(double[] cellData) {
		if (cellData == null) {
			System.out.println("input data is null");
			return;
		}
		if (cellData.length == 0) {
			System.out.println("input data has 0 element");
			return;
		}
		double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < cellData.length; i++) {
			if (min > cellData[i])
				min = cellData[i];
			if (max < cellData[i])
				max = cellData[i];
		}
		System.out.println("max: " + max + ", min: " + min);
	}
	
	public static void print(Vector<?> data){
		Iterator<?> it = data.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}

	public static void print(int[] cellData) {

		if (cellData == null) {
			System.out.println("input data is null");
			return;
		}
		if (cellData.length == 0) {
			System.out.println("input data has 0 element");
			return;
		}
		for (int i = 0; i < cellData.length; i++)
			System.out.print(cellData[i] + " ");
		System.out.println();
	}

	public static void print(float[] cellData) {
		if (cellData == null) {
			System.out.println("input data is null");
			return;
		}
		if (cellData.length == 0) {
			System.out.println("input data has 0 element");
			return;
		}
		for (int i = 0; i < cellData.length; i++)
			System.out.print(cellData[i] + " ");
		System.out.println();
	}

	public static void print(double[] cellData) {
		if (cellData == null) {
			System.out.println("input data is null");
			return;
		}
		if (cellData.length == 0) {
			System.out.println("input data has 0 element");
			return;
		}
		for (int i = 0; i < cellData.length; i++)
			System.out.print(cellData[i] + " ");
		System.out.println();
	}

	public static void print(float[] cellData1, float[] cellData2, int stopID) {
		int len = cellData1.length > cellData2.length ? cellData1.length : cellData2.length;
		for (int i = 0; i < len && i < stopID; i++)
			System.out.println(i + " " + cellData1[i] + ", " + cellData2[i]);
	}

	public static void printCellData(CellData cellData1, CellData cellData2, int stopID) {
		int len = cellData1.length() > cellData2.length() ? cellData1.length() : cellData2.length();
		for (int i = 0; i < len && i < stopID; i++)
			System.out.println(i + " " + cellData1.getPixel(i) + ", " + cellData2.getPixel(i));
	}

	public static void printCellData(CellData cellData1, CellData cellData2) {
		int len = cellData1.length() > cellData2.length() ? cellData1.length() : cellData2.length();
		for (int i = 0; i < len; i++)
			if (!(Float.isNaN(cellData1.getPixel(i)) || Float.isNaN(cellData2.getPixel(i))))
				System.out.println(i + " " + cellData1.getPixel(i) + ", " + cellData2.getPixel(i));
	}

	public static ResultsTable printCellData(CellData cellData1, CellData cellData2, ResultsTable rt) {
		int len = cellData1.length() > cellData2.length() ? cellData1.length() : cellData2.length();
		if (rt == null)
			rt = new ResultsTable();
		else
			rt.reset();

		for (int i = 0; i < len; i++) {
			rt.incrementCounter();
			rt.addValue("index", i);
			rt.addValue("C1", cellData1.getPixel(i));
			rt.addValue("C2", cellData2.getPixel(i));
		}

		return rt;
	}

	/**
	 * Print only the first time this method is called on a particular line
	 */
	private static StackTraceElement printLock = null;

	public static void print1(String str) {
		if (!Thread.currentThread().getStackTrace()[TRACE_LINE].equals(printLock)) {
			printLock = Thread.currentThread().getStackTrace()[TRACE_LINE];
			System.out.println(str);
		}
	}

	public static long myTimer = System.currentTimeMillis();

	public void debugHistogram() {
		ImagePlus imp = IJ.openImage("D:\\360Sync\\OneDrive\\Temp\\test\\sodB-716_100_Phase-1.tif");

		// ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			System.out.println("imp");
			return;
		}
		imp.show();
		IJ.run(imp, "Fire", "");
		HistogramStackWindow plotstack = new HistogramStackWindow(imp);
		if (plotstack instanceof StackWindow)
			System.out.println("plotstack instanceof StackWindow");
		if (plotstack instanceof HistogramStackWindow)
			System.out.println("plotstack instanceof StackHistogramWindow");
	}

	public static void printCellData(CellData[] cellData) {
		for (int i = 0; i < cellData.length; i++) {
			if (cellData[i] == null)
				continue;
			System.out.print("Cell-" + i + ": ");
			for (int iPixel = 0; iPixel < (cellData[i].length() <= 20 ? cellData[i].length() : 20); iPixel++) {
				System.out.print(cellData[i].getPixel(iPixel) + " ");
			}
			System.out.println();
		}
	}

	public static void printAllOptions(int options) {
		System.out.println(String.format("%32s", Integer.toBinaryString(options)).replace(' ', '0') + " : options");
		printAllOptions();
	}

	public static void printAllOptions() {

		// System.out.println(String.format("%32s",
		// Integer.toBinaryString(DO_TOSH)).replace(' ', '0') + " : DO_TOSH");
		// System.out.println(String.format("%32s",
		// Integer.toBinaryString(DO_TOSMAX)).replace(' ', '0') + " :
		// DO_TOSMAX");
		// System.out.println(String.format("%32s",
		// Integer.toBinaryString(DO_TOSMIN)).replace(' ', '0') + " :
		// DO_TOSMIN");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_MATRIX)).replace(' ', '0') + " : DO_MTOS");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_PCC)).replace(' ', '0') + " : DO_PCC");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_SRC)).replace(' ', '0') + " : DO_SRC");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_MCC)).replace(' ', '0') + " : DO_MCCT");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_ICQ)).replace(' ', '0') + " : DO_ICQ");

		System.out.println(String.format("%32s", Integer.toBinaryString(DO_AVGINT)).replace(' ', '0') + " : DO_AVGINT");
		System.out.println(
				String.format("%32s", Integer.toBinaryString(DO_CEN2NPOLE)).replace(' ', '0') + " : DO_CEN2NPOLE");
		System.out.println(
				String.format("%32s", Integer.toBinaryString(DO_DIST_THOLD)).replace(' ', '0') + " : DO_DIST_THOLD");
		System.out
				.println(String.format("%32s", Integer.toBinaryString(DO_DIST_FT)).replace(' ', '0') + " : DO_DIST_FT");
		// System.out.println(String.format("%32s",
		// Integer.toBinaryString(DO_ALIGN1)).replace(' ', '0') + " :
		// DO_ALIGN1");
		// System.out.println(String.format("%32s",
		// Integer.toBinaryString(DO_ALIGN2)).replace(' ', '0') + " :
		// DO_ALIGN2");
		System.out
				.println(String.format("%32s", Integer.toBinaryString(DO_SCATTER)).replace(' ', '0') + " : DO_SCATTER");
		// System.out.println(String.format("%32s",
		// Integer.toBinaryString(DO_HEAT1)).replace(' ', '0') + " : DO_HEAT1");
		// System.out.println(String.format("%32s",
		// Integer.toBinaryString(DO_HEAT2)).replace(' ', '0') + " : DO_HEAT2");
		System.out.println(
				String.format("%32s", Integer.toBinaryString(DO_HEAT_CELL)).replace(' ', '0') + " : DO_HEAT_CELL");
		System.out.println(
				String.format("%32s", Integer.toBinaryString(DO_HEAT_IMG)).replace(' ', '0') + " : DO_HEAT_IMG");
		System.out.println(
				String.format("%32s", Integer.toBinaryString(DO_HEAT_STACK)).replace(' ', '0') + " : DO_HEAT_STACK");
		System.out.println(
				String.format("%32s", Integer.toBinaryString(DO_LINEAR_TOS)).replace(' ', '0') + " : DO_LINEAR_TOS");
		System.out.println(
				String.format("%32s", Integer.toBinaryString(DO_LOG2_TOS)).replace(' ', '0') + " : DO_LOG10_TOS");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_LN_TOS)).replace(' ', '0') + " : DO_LN_TOS");

		System.out.println(
				String.format("%32s", Integer.toBinaryString(DO_RESULTTABLE)).replace(' ', '0') + " : DO_RESULTTABLE");
		System.out
				.println(String.format("%32s", Integer.toBinaryString(DO_SUMMARY)).replace(' ', '0') + " : DO_SUMMARY");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_HIST)).replace(' ', '0') + " : DO_HIST");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_CUSTOM)).replace(' ', '0') + " : DO_CUSTOM");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_MASKS)).replace(' ', '0') + " : DO_MASKS");
		System.out.println(String.format("%32s", Integer.toBinaryString(DO_ROIS)).replace(' ', '0') + " : DO_ROIS");

	}

	public static void printSelectedOptions(int options) {

		System.out.println(String.format("%32s", Integer.toBinaryString(options)).replace(' ', '0') + " : options");

		if (options == 0) {
			System.out.println("No option is selected");
			return;
		}

		// if((options & DO_TOSH)!=0)
		// System.out.println("DO_TOSH");
		// if((options & DO_TOSMAX)!=0)
		// System.out.println("DO_TOSMAX");
		// if((options & DO_TOSMIN)!=0)
		// System.out.println("DO_TOSMIN");
		if ((options & DO_MATRIX) != 0)
			System.out.println("DO_MTOS");
		if ((options & DO_PCC) != 0)
			System.out.println("DO_PCC");
		if ((options & DO_SRC) != 0)
			System.out.println("DO_SRC");
		if ((options & DO_MCC) != 0)
			System.out.println("DO_MCC");
		if ((options & DO_ICQ) != 0)
			System.out.println("DO_ICQ");

		if ((options & DO_AVGINT) != 0)
			System.out.println("DO_AVGINT");
		if ((options & DO_CEN2NPOLE) != 0)
			System.out.println("DO_CEN2NPOLE");
		if ((options & DO_DIST_THOLD) != 0)
			System.out.println("DO_DIST_THOLD");
		if ((options & DO_DIST_FT) != 0)
			System.out.println("DO_DIST_FT");
		// if((options & DO_ALIGN1)!=0)
		// System.out.println("DO_ALIGN1");
		// if((options & DO_ALIGN2)!=0)
		// System.out.println("DO_ALIGN2");
		if ((options & DO_SCATTER) != 0)
			System.out.println("DO_SCATTER");
		// if((options & DO_HEAT1)!=0)
		// System.out.println("DO_HEAT1");
		// if((options & DO_HEAT2)!=0)
		// System.out.println("DO_HEAT2");
		if ((options & DO_HEAT_CELL) != 0)
			System.out.println("DO_HEAT_CELL");
		if ((options & DO_HEAT_IMG) != 0)
			System.out.println("DO_HEAT_IMG");
		if ((options & DO_HEAT_STACK) != 0)
			System.out.println("DO_HEAT_STACK");
		if ((options & DO_LINEAR_TOS) != 0)
			System.out.println("DO_LINEAR_TOS");
		if ((options & DO_LOG2_TOS) != 0)
			System.out.println("DO_LOG10_TOS");
		if ((options & DO_LN_TOS) != 0)
			System.out.println("DO_LN_TOS");

		if ((options & DO_RESULTTABLE) != 0)
			System.out.println("DO_RESULTTABLE");
		if ((options & DO_SUMMARY) != 0)
			System.out.println("DO_SUMMARY");
		if ((options & DO_HIST) != 0)
			System.out.println("DO_HIST");
		if ((options & DO_CUSTOM) != 0)
			System.out.println("DO_CUSTOM");
		if ((options & DO_MASKS) != 0)
			System.out.println("DO_MASKS");
		if ((options & DO_ROIS) != 0)
			System.out.println("DO_ROIS");

	}

	public static final String LOG_TITLE = PluginStatic.getPlugInName() + " errors";
	private static TextWindow logWindow;

	public static synchronized void log(String s) {
		if (s == null)
			return;
		TextPanel logPanel = null;
		if (logWindow == null && IJ.getInstance() != null) {
			logWindow = new TextWindow(LOG_TITLE, "", 400, 250);
			logPanel = logWindow.getTextPanel();
			logPanel.setFont(new Font("SansSerif", Font.PLAIN, 16));
		} else
			logPanel = logWindow.getTextPanel();
		if (logPanel != null) {
			if (s.startsWith("\\"))
				handleLogCommand(s);
			else
				logPanel.append(s);
		} else {
			LogStream.redirectSystem(false);
			System.out.println(s);
		}
		logWindow.setVisible(true);
	}

	static void handleLogCommand(String s) {
		if (logWindow == null)
			return;
		TextPanel logPanel = logWindow.getTextPanel();

		if (s.equals("\\Closed"))
			logPanel = null;
		else if (s.startsWith("\\Update:")) {
			int n = logPanel.getLineCount();
			String s2 = s.substring(8, s.length());
			if (n == 0)
				logPanel.append(s2);
			else
				logPanel.setLine(n - 1, s2);
		} else if (s.startsWith("\\Update")) {
			int cindex = s.indexOf(":");
			if (cindex == -1) {
				logPanel.append(s);
				return;
			}
			String nstr = s.substring(7, cindex);
			int line = (int) Tools.parseDouble(nstr, -1);
			if (line < 0 || line > 25) {
				logPanel.append(s);
				return;
			}
			int count = logPanel.getLineCount();
			while (line >= count) {
				log("");
				count++;
			}
			String s2 = s.substring(cindex + 1, s.length());
			logPanel.setLine(line, s2);
		} else if (s.equals("\\Clear")) {
			logPanel.clear();
		} else if (s.startsWith("\\Heading:")) {
			logPanel.updateColumnHeadings(s.substring(10));
		} else if (s.equals("\\Close")) {
			Frame f = WindowManager.getFrame("Log");
			if (f != null && (f instanceof TextWindow))
				((TextWindow) f).close();
		} else
			logPanel.append(s);
	}

	public static void getFields(String name, int modifier, Class<?> mother) {
		Class<?> c = null;
		try {
			c = Class.forName(name);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		Field[] declaredFields = c.getDeclaredFields();
		List<Field> staticFields = new ArrayList<Field>();
		for (Field field : declaredFields) {
			if ((field.getModifiers() == modifier) && (mother == null || mother.isAssignableFrom(field.getType())))
				staticFields.add(field);
		}
		for (Field f : staticFields)
			IJ.log(f.getName());

	}

	private static double roundDouble(double d) {
		return roundDouble(d, 3);
	}

	private static double roundDouble(double d, int digit) {
		BigDecimal bd = new BigDecimal(d);
		bd = bd.round(new MathContext(3));
		return bd.doubleValue();
	}

	public static String getTime(String name, String unit) {

		double scalor;

		switch (unit) {
		case "s":
			scalor = 1000000000;
			break;
		case "ms":
			scalor = 1000000;
			break;
		case "ns":
			scalor = 1;
			break;
		default:
			scalor = 1000000;
			unit = "ms";
			break;
		}

		if (timeMap.containsKey(name))
			return roundDouble((System.nanoTime() - timeMap.get(name)) / scalor) + " " + unit;
		else
			return "Time was not set";
	}

	public static void setTime(String name) {
		timeMap.put(name, System.nanoTime());
	}

	public static long getTime(String name) {

		if (timeMap.containsKey(name))
			return System.nanoTime() - timeMap.get(name);
		else
			return System.nanoTime();

	}
	
	public static void printTime(String name, String unit){
		System.out.println(getTime(name, unit));
	}

	public static int countNaN(float[] data) {
		int count = 0;
		for (int i = 0; i < data.length; i++) {
			if(Float.isNaN(data[i]))
				count ++;
		}
		return count;
	}

}
