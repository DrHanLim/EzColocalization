package ezcol.visual.visual2D;

import java.awt.Color;
import java.awt.image.IndexColorModel;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import ezcol.debug.ExceptionHandler;
import ezcol.main.ImageInfo;

public class HeatGenerator {
	
	private FileInfo fi;
	private static final double[] Default_Scalar={0.0,1.0};
	private static final double Default_Back=1.0;
	private static final double[] Default_MAX={255.0,65535.0,255.0,65535.0};
	
	//prepare LUT function modified from ImageJ function
	public void prepColormap(String arg)
	{
		fi = new FileInfo();
		fi.reds = new byte[256];
		fi.greens = new byte[256];
		fi.blues = new byte[256];
		fi.lutSize = 256;

		int nColors = 0;
		if (arg.equals("*None*"))
			return;
		else if (arg.equals("hot"))
			nColors = hot(fi.reds, fi.greens, fi.blues);
		else if (arg.equals("cool"))
			nColors = cool(fi.reds, fi.greens, fi.blues);
		else if (arg.equals("fire"))
			nColors = fire(fi.reds, fi.greens, fi.blues);
		else if (arg.equals("grays"))
			nColors = grays(fi.reds, fi.greens, fi.blues);
		else if (arg.equals("ice"))
			nColors = ice(fi.reds, fi.greens, fi.blues);
		else if (arg.equals("spectrum"))
			nColors = spectrum(fi.reds, fi.greens, fi.blues);
		else if (arg.equals("3-3-2 RGB"))
			nColors = rgb332(fi.reds, fi.greens, fi.blues);
		else if (arg.equals("red"))
			nColors = primaryColor(4, fi.reds, fi.greens, fi.blues);
		else if (arg.equals("green"))
			nColors = primaryColor(2, fi.reds, fi.greens, fi.blues);
		else if (arg.equals("blue"))
			nColors = primaryColor(1, fi.reds, fi.greens, fi.blues);
		else if (arg.equals("cyan"))
			nColors = primaryColor(3, fi.reds, fi.greens, fi.blues);
		else if (arg.equals("magenta"))
			nColors = primaryColor(5, fi.reds, fi.greens, fi.blues);
		else if (arg.equals("yellow"))
			nColors = primaryColor(6, fi.reds, fi.greens, fi.blues);
		else if (arg.equals("redgreen"))
			nColors = redGreen(fi.reds, fi.greens, fi.blues);
		if (nColors<256)
			interpolate(fi.reds, fi.greens, fi.blues, nColors);
	}
	
	@Deprecated 
	/*never used in the code*/
	public void heatmap(ImagePlus imp, RoiManager[] roiTest,String Colormap,double[] scalar,double[] background,boolean Apply)
	{
		if(Colormap.equals(ImageInfo.NONE)||imp==null)
			return;
		if(scalar != null)
		{
			ImageStack iStack=imp.getStack();
			for (int iFrame=1;iFrame<=iStack.getSize();iFrame++)
			{
				ImageProcessor impProcessor=iStack.getProcessor(iFrame);
				if(roiTest==null&&background==null)
					heatmap(impProcessor,null,scalar,Default_Back);
				else if(roiTest==null)
					heatmap(impProcessor,null,scalar,background[iFrame]);
				else if(background==null)
					heatmap(impProcessor,roiTest[iFrame],scalar);
			}
		}
		if(Apply)
			applyHeatMap(imp,Colormap,false,true);
			
	}
	
	public void heatmap(ImageProcessor impProcessor, RoiManager roiTest,double[] scalar)
	{heatmap(impProcessor, roiTest,scalar,Default_Back);	}
	
	public void heatmap(ImageProcessor impProcessor, RoiManager roiTest)
	{heatmap(impProcessor, roiTest,Default_Scalar,Default_Back);	}
	
	//change the pixel values to rescale the heat map
	public void heatmap(ImageProcessor impProcessor, RoiManager roiTest,double[] scalar,double background){
			if(scalar==null||impProcessor==null)
				return;
			if(roiTest==null){
				impProcessor.resetMinAndMax();
				impProcessor.subtract(scalar[0]*background);
				impProcessor.multiply(getPosMax(impProcessor)/((scalar[1]-scalar[0])*background));			
			}
			else{
				impProcessor.setValue(0.0);
				Roi[] roiAll=roiTest.getRoisAsArray();
				int numRois=roiTest.getCount();
				if(numRois==0){
					impProcessor.resetRoi();
					impProcessor.fill();
					return;
				}
				ImageStatistics roiStatistics;
				ImageProcessor tempMask=impProcessor.createProcessor(impProcessor.getWidth(),impProcessor.getHeight());
				//combining rois is too slow, clear outside first before doing this
				//ShapeRoi combinedRoi=new ShapeRoi(roiAll[0]);
				for (int iRoi=0;iRoi<numRois;iRoi++){
					impProcessor.setRoi(roiAll[iRoi]);
					roiStatistics=impProcessor.getStatistics();
					impProcessor.snapshot();
					impProcessor.subtract(roiStatistics.min);
					impProcessor.multiply(getPosMax(impProcessor)/(roiStatistics.max-roiStatistics.min));
					impProcessor.reset(impProcessor.getMask());
					tempMask.setColor(Color.WHITE);
					tempMask.fill(roiAll[iRoi]);
					//combinedRoi.or(new ShapeRoi(roiAll[iRoi]));
				}	
				tempMask.invert();
				imgCalculator("sub",impProcessor,tempMask);
				tempMask=null;
				//impProcessor.fillOutside(combinedRoi);
			}
			
	}
	
	//apply LUT
	public ImagePlus applyHeatMap(ImagePlus imp,String Colormap,boolean toRGB,boolean Show){
		if(Colormap.equals(ImageInfo.NONE)||imp==null)
			return imp;
		prepColormap(Colormap);
		showLut(fi,imp);
		imp.deleteRoi();
		imp.getProcessor().setMinAndMax(0.0,getPosMax(imp));
		imp.updateImage();
		if(toRGB)
			new ImageConverter(imp).convertToRGB();
		if(Show)
			imp.show();
		return imp;
	}
	
	//ImageCalculator using imageprocessor as inputs to clear the background outside the cell
	private ImageProcessor imgCalculator(String Operator,ImageProcessor ip1, ImageProcessor ip2)
	{
		ImageCalculator imgCalcu=new ImageCalculator();
		ImagePlus tempimp=imgCalcu.run(Operator,new ImagePlus("ip1",ip1),new ImagePlus("ip2",ip2));
		if(tempimp!=null)
			return tempimp.getProcessor();
		else
			return null;
	}
	
	int hot(byte[] reds, byte[] greens, byte[] blues) {
		int[] r = {0,23,46,70,93,116,139,162,185,209,232,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255};
		int[] g = {0,0,0,0,0,0,0,0,0,0,0,0,23,46,70,93,116,139,162,185,209,232,255,255,255,255,255,255,255,255,255,255};
		int[] b = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,28,57,85,113,142,170,198,227,255};
		for (int i=0; i<r.length; i++) {
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
		}
		return r.length;
	}
	
	int cool(byte[] reds, byte[] greens, byte[] blues) {
		int[] r = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,17,34,51,68,85,102,119,136,153,170,187,204,221,238,255};
		int[] g = {0,18,34,51,68,85,102,119,136,153,170,187,204,221,238,255,255,238,221,204,187,170,153,136,119,102,85,68,51,34,17,0};
		int[] b = {0,18,34,51,68,85,102,119,136,153,170,187,204,221,238,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255};
		for (int i=0; i<r.length; i++) {
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
		}
		return r.length;
	}
	
	
	int fire(byte[] reds, byte[] greens, byte[] blues) {
		int[] r = {0,0,1,25,49,73,98,122,146,162,173,184,195,207,217,229,240,252,255,255,255,255,255,255,255,255,255,255,255,255,255,255};
		int[] g = {0,0,0,0,0,0,0,0,0,0,0,0,0,14,35,57,79,101,117,133,147,161,175,190,205,219,234,248,255,255,255,255};
		int[] b = {0,61,96,130,165,192,220,227,210,181,151,122,93,64,35,5,0,0,0,0,0,0,0,0,0,0,0,35,98,160,223,255};
		for (int i=0; i<r.length; i++) {
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
		}
		return r.length;
	}
	int grays(byte[] reds, byte[] greens, byte[] blues) {
		for (int i=0; i<256; i++) {
			reds[i] = (byte)i;
			greens[i] = (byte)i;
			blues[i] = (byte)i;
		}
		return 256;
	}
	
	int primaryColor(int color, byte[] reds, byte[] greens, byte[] blues) {
		for (int i=0; i<256; i++) {
			if ((color&4)!=0)
				reds[i] = (byte)i;
			if ((color&2)!=0)
				greens[i] = (byte)i;
			if ((color&1)!=0)
				blues[i] = (byte)i;
		}
		return 256;
	}
	
	int ice(byte[] reds, byte[] greens, byte[] blues) {
		int[] r = {0,0,0,0,0,0,19,29,50,48,79,112,134,158,186,201,217,229,242,250,250,250,250,251,250,250,250,250,251,251,243,230};
		int[] g = {156,165,176,184,190,196,193,184,171,162,146,125,107,93,81,87,92,97,95,93,93,90,85,69,64,54,47,35,19,0,4,0};
		int[] b = {140,147,158,166,170,176,209,220,234,225,236,246,250,251,250,250,245,230,230,222,202,180,163,142,123,114,106,94,84,64,26,27};
		for (int i=0; i<r.length; i++) {
			reds[i] = (byte)r[i];
			greens[i] = (byte)g[i];
			blues[i] = (byte)b[i];
		}
		return r.length;
	}

	int spectrum(byte[] reds, byte[] greens, byte[] blues) {
		Color c;
		for (int i=0; i<256; i++) {
			c = Color.getHSBColor(i/255f, 1f, 1f);
			reds[i] = (byte)c.getRed();
			greens[i] = (byte)c.getGreen();
			blues[i] = (byte)c.getBlue();
		}
		return 256;
	}
	
	int rgb332(byte[] reds, byte[] greens, byte[] blues) 
	{
		for (int i=0; i<256; i++) {
			reds[i] = (byte)(i&0xe0);
			greens[i] = (byte)((i<<3)&0xe0);
			blues[i] = (byte)((i<<6)&0xc0);
		}
		return 256;
	}

	int redGreen(byte[] reds, byte[] greens, byte[] blues) {
		for (int i=0; i<128; i++) {
			reds[i] = (byte)(i*2);
			greens[i] = (byte)0;
			blues[i] = (byte)0;
		}
		for (int i=128; i<256; i++) {
			reds[i] = (byte)0;
			greens[i] = (byte)(i*2);
			blues[i] = (byte)0;
		}
		return 256;
	}
	//showLut function from ImageJ
	void showLut(FileInfo fi, ImagePlus imp) {
		//ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) { imp = WindowManager.getCurrentImage();}
		
			if (imp.getType()==ImagePlus.COLOR_RGB)
				ExceptionHandler.addError(Thread.currentThread(),"LUTs cannot be assiged to RGB Images.");
			else if (imp.isComposite() && ((CompositeImage)imp).getMode()==IJ.GRAYSCALE) {
				CompositeImage cimp = (CompositeImage)imp;
				cimp.setMode(IJ.COLOR);
				int saveC = cimp.getChannel();
				IndexColorModel cm = new IndexColorModel(8, 256, fi.reds, fi.greens, fi.blues);
				for (int c=1; c<=cimp.getNChannels(); c++) {
					cimp.setC(c);
					cimp.setChannelColorModel(cm);
				}
				imp.setC(saveC);
				imp.updateAndRepaintWindow();
			} else {
				ImageProcessor ip = imp.getChannelProcessor();
				IndexColorModel cm = new IndexColorModel(8, 256, fi.reds, fi.greens, fi.blues);
				if (imp.isComposite())
					((CompositeImage)imp).setChannelColorModel(cm);
				else
					ip.setColorModel(cm);
				if (imp.getStackSize()>1)
					imp.getStack().setColorModel(cm);
				imp.updateAndRepaintWindow();
			}
	}
	void interpolate(byte[] reds, byte[] greens, byte[] blues, int nColors) {
		byte[] r = new byte[nColors]; 
		byte[] g = new byte[nColors]; 
		byte[] b = new byte[nColors];
		System.arraycopy(reds, 0, r, 0, nColors);
		System.arraycopy(greens, 0, g, 0, nColors);
		System.arraycopy(blues, 0, b, 0, nColors);
		double scale = nColors/256.0;
		int i1, i2;
		double fraction;
		for (int i=0; i<256; i++) {
			i1 = (int)(i*scale);
			i2 = i1+1;
			if (i2==nColors) i2 = nColors-1;
			fraction = i*scale - i1;
			//IJ.write(i+" "+i1+" "+i2+" "+fraction);
			reds[i] = (byte)((1.0-fraction)*(r[i1]&255) + fraction*(r[i2]&255));
			greens[i] = (byte)((1.0-fraction)*(g[i1]&255) + fraction*(g[i2]&255));
			blues[i] = (byte)((1.0-fraction)*(b[i1]&255) + fraction*(b[i2]&255));
		}
	}
	
	public double getPosMax(ImageProcessor impProcessor)
	{
		double MaxDepth=(double)impProcessor.getBitDepth();
		if(MaxDepth==8)
			MaxDepth=Default_MAX[0];
		else if(MaxDepth==16)
			MaxDepth=Default_MAX[1];
		else if(MaxDepth==24)
			MaxDepth=Default_MAX[2];
		else if(MaxDepth==32)
			MaxDepth=Default_MAX[3];
		return MaxDepth;
	}
	
	public double getPosMax(ImagePlus imp)
	{
		return getPosMax(imp.getProcessor());
	}

}
