package ezcol.visual.visual2D;

import java.util.Arrays;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.HistogramWindow;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import ij.process.ByteProcessor;

public class HistogramGenerator
{
	final private int MAXnBins = 256, MINnBins = 0;
	private int nBins = MAXnBins;
	//private double xMin, xMax;
	//private String yMax = "Auto";
	ImageStack impStack;
	ImageProcessor ip=new ByteProcessor(1,1, new byte[]{1});
	HistogramStackWindow stackhistWindow;
	
	public void setNumOfBins(int n)
	{nBins=n;}
	
	public void addToHistogramStack(String name, double[] tempResult)
	{
		if(tempResult==null||tempResult.length<=0)
			throw new NullPointerException("Input data is null");
		
		double tempMax=Double.MIN_VALUE;
		double tempMin=Double.MAX_VALUE;
		for(int i=0;i<tempResult.length;i++)
		{
			if(tempMax<tempResult[i])
				tempMax=tempResult[i];
			if(tempMin>tempResult[i])
				tempMin=tempResult[i];
		}
		
		
		nBins=(int) (getRange(tempResult)/(2*quartile(tempResult,25,75)*Math.pow(tempResult.length, -1.0/3.0)));
		nBins=nBins<MAXnBins?nBins:MAXnBins;
		nBins=nBins>MINnBins?nBins:MINnBins;
		if(impStack==null)
			impStack=new ImageStack(1,tempResult.length);
		impStack.addSlice(name,new FloatProcessor(1,tempResult.length,tempResult));
	}
	
	public boolean showHistogramStack(String name){
		if(impStack==null)
			return false;
			//throw new NullPointerException("Histogram stack is null");
		else if(impStack.getSize()>0){
			ImagePlus imp = new ImagePlus("Metrics",impStack);
			//imp.show();
			imp.setProperty("label", impStack.getShortSliceLabel(1));
			stackhistWindow = new HistogramStackWindow("Histogram of Metrics", imp, nBins);
		}
		return true;	
	}
	
	public boolean showHistogramStack()
	{return showHistogramStack("Histogram of Metrics");}
	
	public HistogramStackWindow getWindow(){return stackhistWindow;}
	
	/**
	 * @deprecated
	 * Freedman¨CDiaconis rule is used here to calculate the number of bins
	 * This has been replaced by HistogramStackWindow
	 */
	public void getHistogram (String name, double[] data)
	{
		if(data==null||data.length<=0)
			return;
		nBins=(int) (getRange(data)/(2*quartile(data,25,75)*Math.pow(data.length, -1.0/3.0)));
		nBins=nBins<MAXnBins?nBins:MAXnBins;
		nBins=nBins>MINnBins?nBins:MINnBins;
		ImagePlus imp=new ImagePlus(name,new FloatProcessor(1,data.length,data));
		new HistogramWindow("Histogram of "+name, imp, nBins);
	}
	
	/*
	 * The following function comes from : 
	 * http://www.java2s.com/Code/Java/Collections-Data-Structure/Retrivethequartilevaluefromanarray.htm
	 */
	public static double getRange(double[] values) 
	{
		
        double vmin=Double.MAX_VALUE,vmax=Double.MIN_VALUE;
        for(int i=0;i<values.length;i++)
        {
        	if(values[i]<vmin)
        		vmin=values[i];
        	if(values[i]>vmax)
        		vmax=values[i];
        }
        return (vmax-vmin);
    }
	
	
	public static double quartile(double[] values, double lowerPercent,double upperPercent) 
	{
		
        if (values == null || values.length == 0) {
            //throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        	return -1.0;
        }

        // Rank order the values
        double[] v = new double[values.length];
        System.arraycopy(values, 0, v, 0, values.length);
        Arrays.sort(v);

        int n1 = (int) Math.round(v.length * lowerPercent / 100);
        int n2 = (int) Math.round(v.length * upperPercent / 100);
        
        if(n2>=v.length)
        	return (double)v.length/(2*Math.pow(v.length, -1/3));
        else
        	return (v[n2]-v[n1]);

    }
	
	
}
