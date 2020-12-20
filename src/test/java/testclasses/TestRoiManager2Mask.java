package testclasses;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class TestRoiManager2Mask {

	public TestRoiManager2Mask(){
		test();
	}
	
	private void test(){
		ImagePlus imp = IJ.createImage("test", 400, 400, 5, 8);
		imp = roiManager2Mask(imp);
		if(imp!=null)
			imp.show();
	}
	
	private ImagePlus roiManager2Mask(ImagePlus imp){

		RoiManager roiManager = RoiManager.getInstance2();
		if(roiManager==null)
			return null;
		Roi[] rois = roiManager.getRoisAsArray();
		Map<Integer,List<Roi>> roiMap = new HashMap<Integer,List<Roi>>();
		for(Roi roi : rois){
			List<Roi> list = roiMap.get(roi.getPosition());
			if(list==null)
				list = new ArrayList<Roi>();
			list.add(roi);
			roiMap.put(roi.getPosition(), list);
		}
		
		int nSlices   = imp.getNSlices(),
			nFrames   = imp.getNFrames(),
			nChannels = imp.getNChannels();
		imp = IJ.createHyperStack("Mask of Roi(s) in Roi Manager", 
				imp.getWidth(), imp.getHeight(), nChannels, nSlices, nChannels, 8);
		ImageStack impStack = imp.getStack();
		
		Roi[] allRois = roiMap.get(0).toArray(new Roi[0]);
		int index;
		for(int channel=1;channel<=nChannels;channel++)
			for(int slice=1;slice<=nSlices;slice++)
				for(int frame=1;frame<=nFrames;frame++){
					index = (frame-1)*nChannels*nSlices + (slice-1)*nChannels + channel ;
					List<Roi> list = roiMap.get(index);
					if(list!=null){
						Roi[] thisRois = new Roi[list.size()+allRois.length];
						System.arraycopy(allRois, 0, thisRois, 0, allRois.length);
						System.arraycopy(list.toArray(), 0, thisRois, allRois.length, list.size());
						impStack.setProcessor(roi2mask(impStack.getProcessor(index),thisRois),index);
					}
					else{
						impStack.setProcessor(roi2mask(impStack.getProcessor(index),allRois),index);
					}
				}
		return imp;
	}
	
	private ByteProcessor roi2mask(ImageProcessor ip,Roi[] rois)
	{
		if(ip==null)
			return null;
		int w=ip.getWidth();
		int h=ip.getHeight();
		
		if(rois==null||rois.length<1)
			return new ByteProcessor(w,h);
		ByteProcessor result=new ByteProcessor(w,h);
		result.setColor(Color.WHITE);
		for (int i=0; i<rois.length; i++){
			if(rois[i]!=null)
				result.fill(rois[i]);
		}
		result.resetRoi();
		result.invert();
		return result;
	}
}
