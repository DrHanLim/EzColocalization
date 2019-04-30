package ezcol.files;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;

import ezcol.debug.Debugger;
import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginStatic;
import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;

/**
 * This class contains static utility IO method to read files for the plugin
 * 
 * @author Huanjie Sheng
 *
 */
public class FilesIO {

	public static final String VERSION = "version";
	public static final String BUILD_TIME = "buildtime";
	public static final String JAVA_VERSION = "javaVersion";
	public static final String IMAGEJ_VERSION = "imagejVersion";

	private static final String IMAGE_DIRECTORY = "resources/images/";

	public static URL getResource(String name) {
		if (name.charAt(0) != '/')
			name = "/" + IMAGE_DIRECTORY + name;
		return FilesIO.class.getResource(name);
	}

	/**
	 * @param path
	 *            the relative path of the file starting with '/'
	 * @param show
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @see https://imagej.nih.gov/ij/plugins/download/JAR_Resources_Demo.java
	 */
	public static ImagePlus getImagePlus(String path, boolean show) throws IOException, URISyntaxException {
		/*
		 * URL url = FilesIO.class.getResource("/" + IMAGE_DIRECTORY + name); if
		 * (url == null){ return null; }
		 */
		String name = path;
		if (path.lastIndexOf('/') != -1)
			name = path.substring(path.lastIndexOf('/') + 1);
		ImagePlus imp = null;
		InputStream is = FilesIO.class.getResourceAsStream(path);
		if (is != null) {
			Opener opener = new Opener();
			imp = opener.openTiff(is, name);
			if (imp != null && show)
				imp.show();
			return imp;
		} else {
			throw new IOException("Cannot locate " + path);
		}
	}

	public static void openTiffs(boolean show) throws IOException, URISyntaxException {

		String[] strs = null;
		try {
			//I use EzColocalization_.class here because duplication of
			//plugin name is not allowed in ImageJ
			strs = getResourceListing(PluginStatic.getPlugInClass(), IMAGE_DIRECTORY);
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ExceptionHandler.handleException(e);
			IJ.log(""+e);
		}
		boolean hasImg = false;
		for (String str : strs) {
			if (isExtension(str, "tif")) {
				getImagePlus("/" + IMAGE_DIRECTORY + str, show);
				hasImg = true;
			}
		}
		if (!hasImg){
			IJ.error("Test images are lost");
		}
	}

	/**
	 * 
	 * @param clazz
	 * @param path
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 *             Retrieved from
	 *             http://www.uofr.net/~greg/java/get-resource-listing.html
	 */
	public static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
		//Looking for jar using path might end up in a different jar
		//We just assume we are using this jar
		/*URL dirURL = clazz.getClassLoader().getResource(path);
		
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			// A file path: easy enough
			return new File(dirURL.toURI()).list();
		}
		if (dirURL == null) {
			//In case of a jar file, we can't actually find a directory. Have
			// to assume the same jar as clazz.
			 
			String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}*/
		//This will be a bug if and only if there exists another jar
		//which has exactly the same relative path of this class
		String me = clazz.getName().replace(".", "/") + ".class";
		URL dirURL = clazz.getClassLoader().getResource(me);
		
		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
			//strip out only the JAR file
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries
															// in jar
			Set<String> result = new HashSet<String>(); // avoid duplicates in
														// case it is a
														// subdirectory
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(path)) { // filter according to the path
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0) {
						// if it is a subdirectory, we just return the directory
						// name
						entry = entry.substring(0, checkSubdir);
					}
					result.add(entry);
				}
			}
			return result.toArray(new String[result.size()]);
			
		}else if(dirURL.getProtocol().equals("file")) {
			// test mode and the directory is pointed to the class in bin
			File folder = new File(dirURL.getPath().substring(1, dirURL.getPath().indexOf("bin") + 4) + IMAGE_DIRECTORY);
			File[] listOfFiles = folder.listFiles();

			Set<String> result = new HashSet<String>(); // avoid duplicates in
														// case it is a
														// subdirectory
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	  result.add(listOfFiles[i].getName());
		      }
		    }
		    return result.toArray(new String[result.size()]);
		}

		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}

	private static boolean isExtension(String name, String extension) {
		int i = name.lastIndexOf('.');
		if (i > 0 && i < name.length() - 1)
			if (extension.equalsIgnoreCase(name.substring(i + 1).toLowerCase()))
				return true;
		return false;
	}

	public static String getPluginProperties() {
		InputStream fins = FilesIO.class.getResourceAsStream("/project.properties");
		Properties prop = new Properties();
		if (fins == null)
			return ("Plugin properties not found");
		try {
			prop.load(fins);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return ("Error while loading plugin properties");
		}
		String properties = "";

		if (prop.getProperty(VERSION) != null)
			properties += "Plugin Version: " + prop.getProperty(VERSION) + "\n";

		if (prop.getProperty(BUILD_TIME) != null)
			properties += "Built on: " + prop.getProperty(BUILD_TIME) + "\n";

		properties += "------------------------------------------------- \n" + "Recommended Environment: \n";

		if (prop.getProperty(JAVA_VERSION) != null)
			properties += "Java Version: " + prop.getProperty(JAVA_VERSION) + "\n";

		if (prop.getProperty(IMAGEJ_VERSION) != null)
			properties += "ImageJ Version: " + prop.getProperty(IMAGEJ_VERSION) + "\n";

		properties += "------------------------------------------------- \n" + "Your System Properties: \n";

		properties += "Java Version: " + System.getProperty("java.version") + "\n";

		properties += "ImageJ version: " + IJ.getVersion() + "\n";

		properties += "Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.version")
				+ ")\n";

		return properties;

	}

	public static String getPluginProperties(String key) {
		InputStream fins = FilesIO.class.getResourceAsStream("project.properties");
		Properties prop = new Properties();
		if (fins == null)
			return null;
		try {
			prop.load(fins);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
		if (prop.getProperty(key) != null)
			return prop.getProperty(key);
		else
			return null;

	}
}
