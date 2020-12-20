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
public class ExceptionHandler implements PluginConstants {

	// [0] Thread.getStackTrace
	// [1] ExceptionReporter
	// [2] The line where addError is called
	private static final int TRACE_LINE = 2;
	private static final String SEPARATOR = "-------------------------";
	private static Vector<String> errors = new Vector<String>();
	private static Vector<String> warnings = new Vector<String>();
	private static Vector<Throwable> exceptions = new Vector<Throwable>();
	private static Vector<Thread> threads = new Vector<Thread>();
	private static Set<String> synSet = Collections.synchronizedSet(new HashSet<String>());

	public static final String LOG_TITLE = PluginStatic.getPlugInName() + " errors";
	private static TextWindow logWindow;
	
	public static final String NAN_WARNING = "NaN values are usually caused by \n"
			+ "1. Too Many Ties (i.e. saturated pixels) \n"
			+ "2. Too Few Pixels Above Thresholds (i.e. thresholds too high or cell too small)\n";
			//+ "3. No thresholds (i.e. thresholds are set to 100% while calculating TOS)";

	private static UncaughtExceptionHandler threadException = new UncaughtExceptionHandler() {
		public void uncaughtException(Thread th, Throwable ex) {
			threads.add(th);
			exceptions.add(ex);
		}
	};

	public void uncaughtException(Thread th, Throwable ex) {
		threadException.uncaughtException(th, ex);
	}
	
	public static synchronized void addException(Exception ex) {
		addStackTraceElements(ex.getStackTrace());
	}
	
	public static synchronized void addStackTraceElements(StackTraceElement[] stes) {
		for (StackTraceElement ste : stes)
			ExceptionHandler.addError(ste.toString());
	}

	public static synchronized void addError(String str) {
		if (synSet.contains(str))
			return;
		synSet.add(str);
		errors.addElement(str);
	}
	
	private static synchronized void addError(String str, int index) {
		if (synSet.contains(str))
			return;
		synSet.add(str);
		errors.insertElementAt(str, index);
	}
	
	public static void insertError(Thread thread, String str) {
		addError("(" + thread.getStackTrace()[TRACE_LINE] + "): " + str, 0);
	}

	public static void addError(Object obj, String str) {
		addError(obj.getClass().getName() + ": " + str);
	}

	public static void addError(Object obj, Thread thread, String str) {
		addError(obj.getClass().getName() + "(" + thread.getStackTrace()[TRACE_LINE] + "): " + str);
	}

	public static void addError(Thread thread, String str) {
		addError("(" + thread.getStackTrace()[TRACE_LINE] + "): " + str);
	}

	public static synchronized void addWarning(String str) {
		if (synSet.contains(str))
			return;
		synSet.add(str);
		warnings.addElement(str);
	}

	private static synchronized void addWarning(String str, int index) {
		if (synSet.contains(str))
			return;
		synSet.add(str);
		warnings.insertElementAt(str, index);
	}

	public static void insertWarning(Thread thread, String str) {
		addWarning("(" + thread.getStackTrace()[TRACE_LINE] + "): " + str, 0);
	}
	
	public static void addWarning(Object obj, Thread thread, String str) {
		addWarning(obj.getClass().getName() + "(" + thread.getStackTrace()[TRACE_LINE] + "): " + str);
	}

	public static void addWarning(Thread thread, String str) {
		addWarning("(" + thread.getStackTrace()[TRACE_LINE] + "): " + str);
	}

	public static void addWarning(Object obj, String str) {
		addWarning(obj.getClass().getName() + ": " + str);
	}

	public static UncaughtExceptionHandler getExceptionHandler() {
		return threadException;
	}

	public static void flush() {
		errors.clear();
		warnings.clear();
		synSet.clear();
	}

	public static void printStackTrace(int num) {
		if (Thread.currentThread() != null)
			for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
				System.out.println(ste);
				num--;
				if (num <= 0)
					break;
			}
	}

	public static String currentLine() {
		return "(" + Thread.currentThread().getStackTrace()[TRACE_LINE] + ")";
	}

	public static String getError(int index) {
		if (index < errors.size())
			return errors.elementAt(index);
		else
			return null;
	}

	public static String getWarning(int index) {
		if (index < warnings.size())
			return warnings.elementAt(index);
		else
			return null;
	}

	public static int errorSize() {
		return errors.size();
	}

	public static int warningSize() {
		return warnings.size();
	}

	public static int getCounter() {
		return errors.size() + warnings.size();
	}

	public static void dump() {
		dump(true);
	}

	public static void dump(boolean flush) {
		System.out.println("All errors: ");
		for (String str : errors)
			System.out.println(str);
		System.out.println("All warnings: ");
		for (String str : warnings)
			System.out.println(str);
		if (flush)
			flush();
	}

	public static void print2log() {
		print2log(true);
	}

	public static void print2log(boolean flush) {
		if (errors.size() > 0)
			log("******************Errors******************");
		for (String str : errors)
			log(str);
		if (warnings.size() > 0)
			log("******************Warnings******************");
		for (String str : warnings)
			log(str);
		if (flush)
			flush();
	}

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

	public static void printSeparator() {
		System.out.println(SEPARATOR);
	}

	public static void dumpVector(Vector<?> vec) {
		for (int i = 0; i < vec.size(); i++)
			System.out.println(vec.get(i));
	}

	/**
	 * Displays a stack trace.
	 */
	public static void handleException(Throwable e) {
		if (Macro.MACRO_CANCELED.equals(e.getMessage()))
			return;
		CharArrayWriter caw = new CharArrayWriter();
		PrintWriter pw = new PrintWriter(caw);
		e.printStackTrace(pw);
		String s = caw.toString();
		if (IJ.getInstance() != null) {
			s = IJ.getInstance().getInfo() + "\n \n" + PluginStatic.getInfo() + "\n \n" + s;
			new TextWindow(PluginStatic.getPlugInName() + " Exception", s, 500, 340);
		} else {
			s = PluginStatic.getInfo() + "\n \n" + s;
			new TextWindow(PluginStatic.getPlugInName() + " Exception", s, 500, 340);
		}
	}

}
