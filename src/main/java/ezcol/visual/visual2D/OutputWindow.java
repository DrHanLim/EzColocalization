package ezcol.visual.visual2D;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;

import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginStatic;

public class OutputWindow {

	//This class shares the same constants with 
	public static final int DO_SUMMARY   = PluginStatic.DO_SUMMARY;
	
	public static final String CUSTOM_NAME = "Custom Metric";
	public static final String CUSTOM_STRING = "These are the statistics of custom analysis.\n";
	
	public static final String NAME_SUFFIX = "\n";
	
	private String customName = CUSTOM_NAME;
	private String customString = CUSTOM_STRING;
	
	private int options;
	
	private static final String SEPARATOR_LINE=("-----------------------------------------------------");
	private static final String BLANK_SPACE=(" ");
	
	private final Vector<Stats> moreStats = new Vector<Stats>();
	
	private final Vector<ImgLabel> images = new Vector<ImgLabel>();
	
	//use a map here in case different metric have different number of cells
	private final Set<Integer> numOfCell = new HashSet<Integer>();
	
	public void showLogWindow(){
		
		IJ.log("Results Summary:");
		IJ.log(BLANK_SPACE);
		
		//add in image names obtained from plugin (also status of aligned/unaligned) and number of cells, 
		//this could be tricky with all the input combinations
		for(ImgLabel image: images)
			IJ.log(image.getLabel() + " image(s): " + image.getName() + " " 
				+ (image.isAligned()==null ? "" : (image.isAligned()?"(Aligned)":"(Unaligned)")));
		//add in spacing lines and then need to get number of cells identified from plugin
		IJ.log(BLANK_SPACE);
		//if numbers of cell are the same for all metrics, print it here
		//otherwise, print it for each cell
		if(numOfCell.size()==1)
			IJ.log("Number of cells analyzed = "+numOfCell.toArray()[0]+"\n");
		
		IJ.log(BLANK_SPACE);
		
		//All exceptions should be handled here
		//this can be removed without affecting any other functions
		ExceptionHandler.print2log();
		
		IJ.log(SEPARATOR_LINE);
		
		int custom = 0;
		
		for(Stats stat : moreStats){
			if(stat==null)
				continue;
			if(stat.getName()!=null)
				IJ.log(stat.getName()+" "+NAME_SUFFIX);
			else
				IJ.log(customName+"_"+(++custom)+" "+NAME_SUFFIX);
			if((options&DO_SUMMARY)!=0){
				IJ.log("mean = "+stat.getMean());
				IJ.log("standard deviation = "+stat.getStd());
				IJ.log("median = "+stat.getMedian());
			}
			if(numOfCell.size()>1)
				IJ.log("Number of cells = "+stat.getNum());
			IJ.log(BLANK_SPACE);
			IJ.log("Interpretation: ");
			if(stat.getInterpretation()!=null)
				IJ.log(stat.getInterpretation());
			else
				IJ.log(customString);
			IJ.log(SEPARATOR_LINE);
			
		}
		
	}
	
	public void setOption(int options){
		this.options = options;
	}
	
	/**
	 * reset all arrays, vectors, and sets
	 */
	public void clear(){
		moreStats.clear();
		images.clear();
		numOfCell.clear();
	}
	
	//function to calculate mean of array m, will need to get an array from plugin to perform calculation
	private double mean(double[] m) {
		if(m==null||m.length==0)
			return Double.NaN;
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    return sum / m.length;
	}
	//function to calculate median of array m, will need to get an array from plugin to perform calculation
	private double median(double[] m) {
		if(m==null||m.length==0)
			return Double.NaN;
		Arrays.sort(m);
	    int middle = m.length/2;
	    if (m.length%2 == 1) {
	        return m[middle];
	    } else {
	        return (m[middle-1] + m[middle]) / 2.0;
	    }
	}
	//function to calculate standard deviation of array m, will need to get an array from plugin to perform calculation
	private double stdDeviation(double[] m) {
		if(m==null||m.length==0)
			return Double.NaN;
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    double sigma = 0;
	    for (int i = 0; i < m.length; i++) {
	        sigma += (m[i]-(sum / m.length))*(m[i]-(sum / m.length));
	    }
	    return Math.sqrt(sigma/m.length);
	    
	}
	/**
	 * Add a new metric with its interpretation
	 * @param values a double array of metric value, the length will be considered as the number of values
	 * @param name, the name of the metric
	 */
	public void addMetric(double[] values, String[] interpretation){
		if(values == null)
			return;
		
		//The id has not been found so the metric will be added to the vector
		moreStats.add(new Stats(values.length,mean(values),stdDeviation(values),median(values),interpretation));
		numOfCell.add(values.length);
		
		
	}
	
	/**
	 * Add a new metric with its name
	 * @param values a double array of metric value, the length will be considered as the number of values
	 * @param name, the name of the metric
	 */
	public void addMetric(double[] values, String name){
		addMetric(values, new String[]{name, null});
	}
	
	/**
	 * Add a new metric, which will be added as a custom metric
	 * @param values a double array of metric value, the length will be considered as the number of values
	 */
	public void addMetric(double[] values){
		addMetric(values, new String[]{customName+" "+(moreStats.size()+1),customString});
		customName = CUSTOM_NAME;
		customString = CUSTOM_STRING;
	}

	/**
	 * Add a new image channel
	 * @param label the name of the channel
	 * @param imp the image of the channel
	 * @param aligned whether the image was aligned or not
	 */
	public void addImage(String label, ImagePlus imp, Boolean aligned){
		images.add(new ImgLabel(label,imp,aligned));
	}
	
	/**
	 * implements <code>addImage(label, imp, aligned)</code> assuming the image was not aligned
	 * @see OutputWindow#addImage(String, ImagePlus, boolean)
	 */
	public void addImage(String label, ImagePlus imp){
		images.add(new ImgLabel(label,imp));
	}
	
	public void setCustomName(String customName){
		this.customName = customName;
	}
	
	public void setCustomString(String customString){
		this.customString = customString;
	}
	
}

class ImgLabel{
	ImgLabel(String imgLabel,ImagePlus imp, Boolean aligned) {
       this.imp = imp;
       this.imgLabel = imgLabel;
       this.aligned = aligned;
    }
	
	ImgLabel(String imgLabel,ImagePlus imp) {
	       this.imp = imp;
	       this.imgLabel = imgLabel;
	       this.aligned = null;
	    }

	ImagePlus getImp() {return imp;}
    String getName() { return imp==null?"":imp.getTitle(); }
    String getLabel() { return imgLabel; }
    Boolean isAligned() { return aligned; }

    private ImagePlus imp;
    private String imgLabel;
    private Boolean aligned;
}

class Stats{
    Stats(int num, double mean, double std, double median, String[] metric) {
       this.num = num;
       this.mean = mean;
       this.std = std;
       this.median = median;
       if(metric==null){
    	   this.name = null;
	       this.interpretation = null;
       }else{
	       this.name = metric[0];
	       this.interpretation = metric[1];
       }
    }

    int getNum() {return num;}
    double getMedian() { return median; }
    double getMean() { return mean; }
    double getStd() { return std; }
    String getInterpretation() { return interpretation;}
    String getName() { return name;}

    private int num;
    private double mean;
    private double std;
    private double median;
    private String name;
    private String interpretation;
}


