package ezcol.metric;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Locale;
 
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginStatic;

/**
 * Dynamic java class compiler and executer <br>
 * Demonstrate how to compile dynamic java source code, <br>
 * instantiate instance of the class, and finally call method of the class <br>
 *
 * http://www.beyondlinux.com
 *
 * @author david 2011/07, modified by Huanjie Sheng 2017
 *
 */
public class StringCompiler {
	/** where shall the compiled class be saved to (should exist already) */
	public static int tabSize = 2;
	private static String classOutputFolder = System.getProperty("java.io.tmpdir");
	private static String classPath = StringCompiler.class.getPackage().getName();
	private static String className = "customCode";
	private static String funcName = "customFunc";
	private static String defaultCode = makeDefaultCode(PluginStatic.nChannels);

	private String runCode = defaultCode;
	// please be aware that paramsObj is not used in execute(c1,c2) to
	// accommodate parallel programming
	private Object paramsObj[];
	private Object instance;
	private boolean compiled;
	private File file;
	private Class<?> thisClass;

	public class MyDiagnosticListener implements DiagnosticListener<JavaFileObject> {
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			/*
			 * System.out.println("Line Number->" + diagnostic.getLineNumber());
			 * System.out.println("code->" + diagnostic.getCode());
			 * System.out.println("Message->" +
			 * diagnostic.getMessage(Locale.ENGLISH));
			 * System.out.println("Source->" + diagnostic.getSource());
			 * System.out.println(" ");
			 */
		}
	}

	/**
	 * java File Object represents an in-memory java source file <br>
	 * so there is no need to put the source file on hard disk
	 **/
	public class InMemoryJavaFileObject extends SimpleJavaFileObject {
		private String contents = null;

		public InMemoryJavaFileObject(String className, String contents) throws Exception {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.contents = contents;
		}

		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return contents;
		}
	}

	/**
	 * Get a simple Java File Object ,<br>
	 * It is just for demo, content of the source code is dynamic in real use
	 * case
	 */
	private JavaFileObject getJavaFileObject() {
		StringBuilder contents = new StringBuilder(runCode);
		JavaFileObject so = null;
		try {
			so = new InMemoryJavaFileObject(classPath + "." + className, contents.toString());
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return so;
	}

	public boolean compile() throws Exception {
		compiled = false;
		JavaFileObject file = getJavaFileObject();
		Iterable<? extends JavaFileObject> files = Arrays.asList(file);
		if (file == null || files == null)
			return false;

		// 2.Compile your files by JavaCompiler as foll
		// get system compiler:
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			return false;
		// for compilation diagnostic message processing on compilation
		// WARNING/ERROR
		MyDiagnosticListener c = new MyDiagnosticListener();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(c, Locale.ENGLISH, null);

		// specify classes output folder
		Iterable<String> options = Arrays.asList("-d", classOutputFolder);
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, c, options, null, files);

		Boolean result = task.call();

		if (result == true) {
			preExecute();
			compiled = true;
			return true;
		} else
			return false;
	}

	public boolean compile(String runCode) throws Exception {
		setCode(runCode);
		return compile();
	}

	private void preExecute() {
		// Create a File object on the root of the directory
		// containing the class file

		file = new File(classOutputFolder);
		try {
			// Convert File to a URL
			URL url = file.toURI().toURL(); // file:/classes/demo
			URL[] urls = new URL[] { url };

			// Create a new class loader with the directory
			@SuppressWarnings("resource")
			ClassLoader loader = new URLClassLoader(urls);

			// Load in the class; Class.childclass should be located in
			// the directory file:/class/demo/
			thisClass = loader.loadClass(classPath + "." + className);
			instance = thisClass.newInstance();
		} catch (MalformedURLException e) {
		} catch (ClassNotFoundException e) {
		} catch (Exception ex) {// ex.printStackTrace();
		}

	}

	public void reset() {
		if (file != null) {
			file.delete();
			file = null;
		}
		instance = null;
		thisClass = null;
		compiled = false;
		paramsObj = null;
	}

	/**
	 * run class from the compiled byte code file by URLClassloader
	 * 
	 * @deprecated
	 * @Warning This is not for parallel programming
	 */
	public Object execute() throws Exception {
		if (!compiled || file == null)
			return null;
		try {
			Class<?> params[] = null;
			if (paramsObj != null) {
				params = new Class<?>[paramsObj.length];
				for (int i = 0; i < params.length; i++)
					params[i] = paramsObj[i].getClass();
			}

			Method thisMethod = thisClass.getDeclaredMethod(funcName, params);
			// run the testAdd() method on the instance:
			Object returnedObj = thisMethod.invoke(instance, paramsObj);
			return returnedObj;
		} catch (Exception ex) {
			file.delete();
			// ex.printStackTrace();
			return null;
		}
	}

	public Object execute(float[] c1, float[] c2) throws Exception {
		// use local variables here to run in parallel
		Object[] paramsObj = new Object[2];
		paramsObj[0] = c1;
		paramsObj[1] = c2;
		return execute(paramsObj);
	}

	public Object execute(float[]... cs) throws Exception {
		// use local variables here to run in parallel
		Object[] paramsObj = new Object[cs.length];
		for (int i = 0; i < cs.length; i++)
			paramsObj[i] = cs[i];
		return execute(paramsObj);
	}

	public Object execute(Object[] paramsObj) throws Exception {
		if (!compiled || file == null)
			return null;
		try {
			Class<?> params[] = null;
			if (paramsObj != null) {
				params = new Class<?>[paramsObj.length];
				for (int i = 0; i < params.length; i++) {
					params[i] = paramsObj[i].getClass();
				}
			}

			Method thisMethod = thisClass.getDeclaredMethod(funcName, params);

			// run the testAdd() method on the instance:
			Object returnedObj = thisMethod.invoke(instance, paramsObj);
			return returnedObj;
		} catch (Exception ex) {
			file.delete();
			for (StackTraceElement ste : ex.getStackTrace())
				ExceptionHandler.addError(ste.toString());
			return null;
		}
	}

	public void setParams(Object[] paramsObj) {
		this.paramsObj = paramsObj;
	}

	public void setParams(float[] c1, float[] c2) {
		paramsObj = new Object[2];
		paramsObj[0] = c1;
		paramsObj[1] = c2;
	}

	public boolean isCompiled() {
		return compiled;
	}

	public void resetCode() {
		runCode = defaultCode;
	}

	public void setCode(String runCode) {
		this.runCode = runCode;
	}

	public String getCode() {
		return runCode;
	}

	public static String getDefaultCode() {
		return defaultCode;
	}
	
	public void setNChannel(int nChannels){
		makeDefaultCode(nChannels);
		resetCode();
	}

    public static String makeDefaultCode(int nChannels){
    	String defaultCode = "package "+ classPath + "; \n"
				           + "public class " + className + " { \n"
				           + "	//DO NOT change the next line except for renaming input variables \n"
    					   + "	public double " + funcName + "(";
    	
    	for(int i=1;i<nChannels;i++)
    		defaultCode += "float[] c"+i+", ";
    	
    	defaultCode += "float[] c"+nChannels+") { \n"
		           + "	\n"
		           + "		/*Please write your code here \n"
		           + "		";
    	for(int i=1;i<nChannels;i++)
    		defaultCode += "c"+i+" and ";

    	defaultCode += "c"+nChannels+" are arrays of pixel values of \n"
		           + "		flourescence channels in the same cell \n"
		           + "		Here is an exmaple of how to calculate \n"
		           + "		"+(nChannels==2?"Pearson's correlation coefficient":nChannels+"-order moment")+" */ \n"
		           + "	\n"
		           + "		float[] c = c1.clone(); \n"
		           + "		for (int i = 0; i < c.length ; i++){ \n";
    	for(int i=2;i<=nChannels;i++)
    		defaultCode += "			c[i] *= c"+i+"[i]; \n";
		           
    	defaultCode += "	} \n"  
		           + "		return ( getMean(c) - \n"
		           + "			";
    	
    	for(int i=1;i<nChannels;i++)
    		defaultCode += "getMean(c"+i+") * ";
    	
    	defaultCode += "getMean(c"+nChannels+")) / \n"
		           + "			(";
    	
    	for(int i=1;i<nChannels;i++)
    		defaultCode += "getSTD(c"+i+") * ";
    	
    	defaultCode += "getSTD(c"+nChannels+")); \n"
		           + "	} \n"
		           + "	\n"
		           
		           + "	private double getMean(float[] x) { \n"
		           + "		double result=0.0; \n"
		           + "		for (int i=0;i<x.length;i++) \n"
		           + "	  	  result+=x[i]; \n"
		           + "		return result/x.length; \n"
		           + "	} \n"
		           + "	\n"
		           
		           + "	private double getSTD(float[] x) { \n"
		           + "		float[] y = x.clone(); \n"
		           + "		for (int i=0;i<x.length;i++) \n"
		           + "			y[i]*=y[i]; \n"
		           + "		return java.lang.Math.sqrt(\n"
		           + "			getMean(y)-getMean(x)*getMean(x)); \n"
		           + "	} \n"
		           + "} \n";
    	StringCompiler.defaultCode = defaultCode;
    	return defaultCode;
    }
}