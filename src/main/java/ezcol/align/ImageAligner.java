package ezcol.align;
/*
 * This class is modified by Huanjie Sheng (UC Berkeley)
 * from StackRegJ by Jay, Unruh from Stowers Institute for Medical Research
 * http://research.stowers.org/imagejplugins/
 */
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import ezcol.debug.ExceptionHandler;
import ezcol.main.PluginStatic;
import ij.*;
import ij.gui.GenericDialog;
//import ij.plugin.PlugIn;
import ij.process.*;

public class ImageAligner{
	//this plugin is a highly modified version of the StackReg plugin available from http://bigwww.epfl.ch/thevenaz/stackreg/
	//this version will align a hyperstack based on a selected slice and channel
	//the alignment outputs a translation trajectory for further alignments
	public static double PHASE_ROLINGBALL_SIZE = PluginStatic.PHASE_ROLINGBALL_SIZE;
	
	private int slices,channels,frames,
				targetFrame,targetChannel,targetSlice,
				slices2,channels2,frames2;
	private final String[] transformationItem = {
			"Translation",
			"Rigid Body",
			"Scaled Rotation",
			"Affine",
		};
	
	public ImageProcessor runAlignment (ImageProcessor imp1, ImageProcessor imp2, String transformationChoice, String[] inputalgorithm, boolean[] invert)
	{
		return runAlignment (imp1, imp2, transformationChoice, inputalgorithm, invert, false);
	}
	
	public ImageProcessor runAlignment (ImageProcessor imp1, ImageProcessor imp2, String transformationChoice, String[] inputalgorithm, boolean[] invert, boolean subtractBackground)
	{
		if(imp1==null||imp2==null)
		{
			ExceptionHandler.addError("Missing Images for alignment");
			return null;
		}
		if(imp1.getHeight()!=imp2.getHeight()||imp1.getWidth()!=imp2.getWidth())
		{
			ExceptionHandler.addError("Dimensions mismatch");
			return null;
		}
		
		ImageProcessor imp1Byte,imp2Byte;
		if(!imp1.isBinary())
		{
			imp1Byte=imp1.duplicate();
			if(subtractBackground)
				new BackgroundProcessor().rollSubBackground(imp1Byte,PHASE_ROLINGBALL_SIZE,true);
			imp1Byte=BackgroundProcessor.thredImp(imp1Byte,inputalgorithm[0], invert[0]);
		}
		else
			imp1Byte=imp1.duplicate();
		
		if(!imp2.isBinary())
		{
			imp2Byte=imp2.duplicate();
			if(subtractBackground)
				new BackgroundProcessor().rollSubBackground(imp2Byte,BackgroundProcessor.Default_radius,false);
			imp2Byte=BackgroundProcessor.thredImp(imp2Byte,inputalgorithm[1], invert[1]);
		}
		else
		{
			imp2Byte=imp2.duplicate();
		}
		
		ImageStack stack1=new ImageStack(imp1Byte.getWidth(),imp1Byte.getHeight());
		ImageStack stack2=new ImageStack(imp1.getWidth(),imp1.getHeight());
		
		// Assume raw image should have dark background, 
		// we need to make sure that all images have normal LUT
		if(imp1Byte.isInvertedLut())
			imp1Byte.invertLut();
		if(imp2Byte.isInvertedLut())
			imp2Byte.invertLut();
		//It is not necessary to check whether original images have normal LUT
		//Just let them stay the same
		/*if(imp1.isInvertedLut())
			imp1.invertLut();
		if(imp2.isInvertedLut())
			imp2.invertLut();*/
		stack1.addSlice(imp1Byte);
		stack1.addSlice(imp2Byte);
		stack2.addSlice(imp2);
		stack2.addSlice(imp2);
		ImagePlus align1=new ImagePlus("Align1",stack1);
		ImagePlus align2=new ImagePlus("Align2",stack2);
		
		runAlignment (align1,align2, transformationChoice);
		//align1.show();
		return align2.getStack().getProcessor(2);
		
	}
	
	private void runAlignment (ImagePlus imp, ImagePlus simp, String transformationChoice) 
	{

		List<String> transformationList = Arrays.asList(transformationItem);  
		final int transformation = transformationList.indexOf(transformationChoice);
		final int width = imp.getWidth();
		final int height = imp.getHeight();
		slices=imp.getNSlices();
		channels=imp.getNChannels();
		frames=imp.getNFrames();
		targetFrame = imp.getFrame();
		targetChannel = imp.getChannel();
		targetSlice=imp.getSlice();
		if(simp!=null)
		{
			slices2=simp.getNSlices();
			channels2=simp.getNChannels();
			frames2=simp.getNFrames();
		}
		if(frames==1){
			frames=slices;
			slices=1;
			targetFrame=targetSlice;
			targetSlice=1;
			frames2=slices2;
			slices2=1;
		}
		
		if(frames2!=frames){
			ExceptionHandler.addError("Number of frames in images doesn't match, ignoring secondary");
			simp=null;
		}
		
		double[][] globalTransform = {
			{1.0, 0.0, 0.0},
			{0.0, 1.0, 0.0},
			{0.0, 0.0, 1.0}
		};
		double[][] anchorPoints = null;
		switch (transformation) {
			case 0: {
				anchorPoints = new double[1][3];
				anchorPoints[0][0] = (double)(width / 2);
				anchorPoints[0][1] = (double)(height / 2);
				anchorPoints[0][2] = 1.0;
				break;
			}
			case 1: {
				anchorPoints = new double[3][3];
				anchorPoints[0][0] = (double)(width / 2);
				anchorPoints[0][1] = (double)(height / 2);
				anchorPoints[0][2] = 1.0;
				anchorPoints[1][0] = (double)(width / 2);
				anchorPoints[1][1] = (double)(height / 4);
				anchorPoints[1][2] = 1.0;
				anchorPoints[2][0] = (double)(width / 2);
				anchorPoints[2][1] = (double)((3 * height) / 4);
				anchorPoints[2][2] = 1.0;
				break;
			}
			case 2: {
				anchorPoints = new double[2][3];
				anchorPoints[0][0] = (double)(width / 4);
				anchorPoints[0][1] = (double)(height / 2);
				anchorPoints[0][2] = 1.0;
				anchorPoints[1][0] = (double)((3 * width) / 4);
				anchorPoints[1][1] = (double)(height / 2);
				anchorPoints[1][2] = 1.0;
				break;
			}
			case 3: {
				anchorPoints = new double[3][3];
				anchorPoints[0][0] = (double)(width / 2);
				anchorPoints[0][1] = (double)(height / 4);
				anchorPoints[0][2] = 1.0;
				anchorPoints[1][0] = (double)(width / 4);
				anchorPoints[1][1] = (double)((3 * height) / 4);
				anchorPoints[1][2] = 1.0;
				anchorPoints[2][0] = (double)((3 * width) / 4);
				anchorPoints[2][1] = (double)((3 * height) / 4);
				anchorPoints[2][2] = 1.0;
				break;
			}
			default: {
				ExceptionHandler.addError(Thread.currentThread(),"Unexpected transformation");
				return;
			}
		}
		
		ImagePlus target = new ImagePlus("StackRegTarget",getImpProcessor(imp,targetChannel,targetSlice,targetFrame));
		float[][] trans=new float[2][frames];

		for (int f = (targetFrame - 1); f>0; f--) {
			if(!registerSlice(target, imp, width, height,transformation, globalTransform, anchorPoints, f,simp)) return;
			float[] trans2=get_translation(globalTransform,width,height);
			trans[0][f-1]=trans2[0]; trans[1][f-1]=trans2[1];

		}
		if ((1 < targetFrame) && (targetFrame < frames)) {
			globalTransform[0][0] = 1.0;
			globalTransform[0][1] = 0.0;
			globalTransform[0][2] = 0.0;
			globalTransform[1][0] = 0.0;
			globalTransform[1][1] = 1.0;
			globalTransform[1][2] = 0.0;
			globalTransform[2][0] = 0.0;
			globalTransform[2][1] = 0.0;
			globalTransform[2][2] = 1.0;
			target.getProcessor().copyBits(getImpProcessor(imp,targetChannel,targetSlice,targetFrame), 0, 0, Blitter.COPY);
		}
		for (int f=(targetFrame+1); f<=frames; f++) {
			if(!registerSlice(target, imp, width, height,transformation, globalTransform, anchorPoints, f,simp)) return;
			float[] trans2=get_translation(globalTransform,width,height);
			trans[0][f-1]=trans2[0]; trans[1][f-1]=trans2[1];
			
		}
		//imp.updateAndDraw();
	}
	
	private float[] get_translation(double[][] globalTransform,int width,int height){
		double[][] anchorPoints = new double[1][3];
		anchorPoints[0][0] =0.5*(double)width;
		anchorPoints[0][1] =0.5*(double)height;
		anchorPoints[0][2] = 1.0;
		double[][] sourcePoints = new double[1][3];
		for (int i = 0; (i < 3); i++) {
			sourcePoints[0][i] = 0.0;
			for (int j = 0; (j < 3); j++) {
				sourcePoints[0][i] += globalTransform[i][j]* anchorPoints[0][j];
			}
		}
		return new float[]{(float)(sourcePoints[0][0]-anchorPoints[0][0]),(float)(sourcePoints[0][1]-anchorPoints[0][1])};
	}

	private double[][] getTransformationMatrix (final double[][] fromCoord,final double[][] toCoord,final int transformation) {
		//this was copied essentially as is from StackReg
		double[][] matrix = new double[3][3];
		switch (transformation) {
			case 0: {
				matrix[0][0] = 1.0;
				matrix[0][1] = 0.0;
				matrix[0][2] = toCoord[0][0] - fromCoord[0][0];
				matrix[1][0] = 0.0;
				matrix[1][1] = 1.0;
				matrix[1][2] = toCoord[0][1] - fromCoord[0][1];
				break;
			}
			case 1: {
				final double angle = Math.atan2(fromCoord[2][0] - fromCoord[1][0],
					fromCoord[2][1] - fromCoord[1][1]) - Math.atan2(toCoord[2][0] - toCoord[1][0],
					toCoord[2][1] - toCoord[1][1]);
				final double c = Math.cos(angle);
				final double s = Math.sin(angle);
				matrix[0][0] = c;
				matrix[0][1] = -s;
				matrix[0][2] = toCoord[0][0] - c * fromCoord[0][0] + s * fromCoord[0][1];
				matrix[1][0] = s;
				matrix[1][1] = c;
				matrix[1][2] = toCoord[0][1] - s * fromCoord[0][0] - c * fromCoord[0][1];
				break;
			}
			case 2: {
				double[][] a = new double[3][3];
				double[] v = new double[3];
				a[0][0] = fromCoord[0][0];
				a[0][1] = fromCoord[0][1];
				a[0][2] = 1.0;
				a[1][0] = fromCoord[1][0];
				a[1][1] = fromCoord[1][1];
				a[1][2] = 1.0;
				a[2][0] = fromCoord[0][1] - fromCoord[1][1] + fromCoord[1][0];
				a[2][1] = fromCoord[1][0] + fromCoord[1][1] - fromCoord[0][0];
				a[2][2] = 1.0;
				invertGauss(a);
				v[0] = toCoord[0][0];
				v[1] = toCoord[1][0];
				v[2] = toCoord[0][1] - toCoord[1][1] + toCoord[1][0];
				for (int i = 0; (i < 3); i++) {
					matrix[0][i] = 0.0;
					for (int j = 0; (j < 3); j++) {
						matrix[0][i] += a[i][j] * v[j];
					}
				}
				v[0] = toCoord[0][1];
				v[1] = toCoord[1][1];
				v[2] = toCoord[1][0] + toCoord[1][1] - toCoord[0][0];
				for (int i = 0; (i < 3); i++) {
					matrix[1][i] = 0.0;
					for (int j = 0; (j < 3); j++) {
						matrix[1][i] += a[i][j] * v[j];
					}
				}
				break;
			}
			case 3: {
				double[][] a = new double[3][3];
				double[] v = new double[3];
				a[0][0] = fromCoord[0][0];
				a[0][1] = fromCoord[0][1];
				a[0][2] = 1.0;
				a[1][0] = fromCoord[1][0];
				a[1][1] = fromCoord[1][1];
				a[1][2] = 1.0;
				a[2][0] = fromCoord[2][0];
				a[2][1] = fromCoord[2][1];
				a[2][2] = 1.0;
				invertGauss(a);
				v[0] = toCoord[0][0];
				v[1] = toCoord[1][0];
				v[2] = toCoord[2][0];
				for (int i = 0; (i < 3); i++) {
					matrix[0][i] = 0.0;
					for (int j = 0; (j < 3); j++) {
						matrix[0][i] += a[i][j] * v[j];
					}
				}
				v[0] = toCoord[0][1];
				v[1] = toCoord[1][1];
				v[2] = toCoord[2][1];
				for (int i = 0; (i < 3); i++) {
					matrix[1][i] = 0.0;
					for (int j = 0; (j < 3); j++) {
						matrix[1][i] += a[i][j] * v[j];
					}
				}
				break;
			}
			default: {
				ExceptionHandler.addError(Thread.currentThread(),"Unexpected transformation");
			}
		}
		matrix[2][0] = 0.0;
		matrix[2][1] = 0.0;
		matrix[2][2] = 1.0;
		return(matrix);
	} /* end getTransformationMatrix */

	/*------------------------------------------------------------------*/
	private void invertGauss (final double[][] matrix) {
		//also copied from StackReg
		final int n = matrix.length;
		final double[][] inverse = new double[n][n];
		for (int i = 0; (i < n); i++) {
			double max = matrix[i][0];
			double absMax = Math.abs(max);
			for (int j = 0; (j < n); j++) {
				inverse[i][j] = 0.0;
				if (absMax < Math.abs(matrix[i][j])) {
					max = matrix[i][j];
					absMax = Math.abs(max);
				}
			}
			inverse[i][i] = 1.0 / max;
			for (int j = 0; (j < n); j++) {
				matrix[i][j] /= max;
			}
		}
		for (int j = 0; (j < n); j++) {
			double max = matrix[j][j];
			double absMax = Math.abs(max);
			int k = j;
			for (int i = j + 1; (i < n); i++) {
				if (absMax < Math.abs(matrix[i][j])) {
					max = matrix[i][j];
					absMax = Math.abs(max);
					k = i;
				}
			}
			if (k != j) {
				final double[] partialLine = new double[n - j];
				final double[] fullLine = new double[n];
				System.arraycopy(matrix[j], j, partialLine, 0, n - j);
				System.arraycopy(matrix[k], j, matrix[j], j, n - j);
				System.arraycopy(partialLine, 0, matrix[k], j, n - j);
				System.arraycopy(inverse[j], 0, fullLine, 0, n);
				System.arraycopy(inverse[k], 0, inverse[j], 0, n);
				System.arraycopy(fullLine, 0, inverse[k], 0, n);
			}
			for (k = 0; (k <= j); k++) {
				inverse[j][k] /= max;
			}
			for (k = j + 1; (k < n); k++) {
				matrix[j][k] /= max;
				inverse[j][k] /= max;
			}
			for (int i = j + 1; (i < n); i++) {
				for (k = 0; (k <= j); k++) {
					inverse[i][k] -= matrix[i][j] * inverse[j][k];
				}
				for (k = j + 1; (k < n); k++) {
					matrix[i][k] -= matrix[i][j] * matrix[j][k];
					inverse[i][k] -= matrix[i][j] * inverse[j][k];
				}
			}
		}
		for (int j = n - 1; (1 <= j); j--) {
			for (int i = j - 1; (0 <= i); i--) {
				for (int k = 0; (k <= j); k++) {
					inverse[i][k] -= matrix[i][j] * inverse[j][k];
				}
				for (int k = j + 1; (k < n); k++) {
					matrix[i][k] -= matrix[i][j] * matrix[j][k];
					inverse[i][k] -= matrix[i][j] * inverse[j][k];
				}
			}
		}
		for (int i = 0; (i < n); i++) {
			System.arraycopy(inverse[i], 0, matrix[i], 0, n);
		}
	} /* end invertGauss */

	public void setHyperstackSlice(ImagePlus imp,int channel,int slice,int frame){
		//imp.setSlice((channel-1)+(slice-1)*channels+(frame-1)*slices*channels+1);
		imp.setPosition((channel-1)+(slice-1)*channels+(frame-1)*slices*channels+1);
		//int[] pos=imp.convertIndexToPosition((channel-1)+(slice-1)*channels+(frame-1)*slices*channels+1);
		//imp.setPositionWithoutUpdate(pos[0],pos[1],pos[2]);
	}

	public ImageProcessor getImpProcessor(final ImagePlus imp,int channel,int slice,int frame){
		int index=(channel-1)+(slice-1)*channels+(frame-1)*slices*channels+1;
		return imp.getStack().getProcessor(index).convertToFloat();
	}

	public void setImpProcessor(final ImagePlus imp,ImagePlus source,int channel,int slice,int frame){
		source.getStack().deleteLastSlice();
		int index=(channel-1)+(slice-1)*channels+(frame-1)*slices*channels+1;
		switch(imp.getType()){
			case ImagePlus.GRAY8: {
				source.getProcessor().setMinAndMax(0.0,255.0);
				imp.getStack().setPixels(source.getProcessor().convertToByte(false).getPixels(),index);
				break;
			}
			case ImagePlus.GRAY16: {
				source.getProcessor().setMinAndMax(0.0,65535.0);
				imp.getStack().setPixels(source.getProcessor().convertToShort(false).getPixels(),index);
				break;
			}
			case ImagePlus.GRAY32: {
				imp.getStack().setPixels(source.getProcessor().getPixels(),index);
				break;
			}
			default: {
				ExceptionHandler.addError(Thread.currentThread(),"Unexpected image type");
			}
		}
	}

	/*------------------------------------------------------------------*/
	private boolean registerSlice (final ImagePlus target,final ImagePlus imp,final int width,final int height,
			final int transformation,final double[][] globalTransform,final double[][] anchorPoints,final int f,final ImagePlus simp) {
		//imp.setSlice(s);
		//setHyperstackSlice(imp,targetChannel,targetSlice,f);
		double[][] sourcePoints = null;
		double[][] targetPoints = null;
		double[][] localTransform = null;
		ImagePlus source=new ImagePlus("StackRegSource",getImpProcessor(imp,targetChannel,targetSlice,f));
		TurboRegJ trj=gettrj();
		switch (transformation) {
			case 0: {
				//simple translation
				trj.setTargetPoints(new double[][]{{width/2,height/2}});
				trj.setSourcePoints(new double[][]{{width/2,height/2}});
				trj.initAlignment(source, target, TurboRegJ.TRANSLATION);
				break;
			}
			case 1: {
				//rigid body
				trj.setSourcePoints(new double[][]{{width/2,height/2},{width/2,height/4},{width/2,3*height/4}});
				trj.setTargetPoints(new double[][]{{width/2,height/2},{width/2,height/4},{width/2,3*height/4}});
				trj.initAlignment(source, target, TurboRegJ.RIGID_BODY);
				break;
			}
			case 2: {
				//scaled rotation
				trj.setSourcePoints(new double[][]{{width/4,height/2},{3*width/4,height/2}});
				trj.setTargetPoints(new double[][]{{width/4,height/2},{3*width/4,height/2}});
				trj.initAlignment(source, target, TurboRegJ.SCALED_ROTATION);
				break;
			}
			case 3: {
				//affine
				trj.setSourcePoints(new double[][]{{width/2,height/4},{width/4,3*height/4},{3*width/4,3*height/4}});
				trj.setTargetPoints(new double[][]{{width/2,height/4},{width/4,3*height/4},{3*width/4,3*height/4}});
				trj.initAlignment(source, target, TurboRegJ.AFFINE);
				break;
			}
			default: {
				ExceptionHandler.addError(Thread.currentThread(),"Unexpected transformation");
				return false;
			}
		}
		target.setProcessor(null, source.getProcessor());
		sourcePoints = trj.getSourcePoints();
		targetPoints = trj.getTargetPoints();
		localTransform = getTransformationMatrix(targetPoints,sourcePoints,transformation);
		double[][] rescued = {
			{globalTransform[0][0], globalTransform[0][1], globalTransform[0][2]},
			{globalTransform[1][0], globalTransform[1][1], globalTransform[1][2]},
			{globalTransform[2][0], globalTransform[2][1], globalTransform[2][2]}
		};
		//here multiply the global transformation by the recent local transform to add all previous transformations
		for (int i = 0; (i < 3); i++) {
			for (int j = 0; (j < 3); j++) {
				globalTransform[i][j] = 0.0;
				for (int k = 0; (k < 3); k++) {
					globalTransform[i][j] += localTransform[i][k] * rescued[k][j];
				}
			}
		}
		switch (transformation) {
			case 0: {
				sourcePoints = new double[1][3];
				for (int i = 0; (i < 3); i++) {
					sourcePoints[0][i] = 0.0;
					for (int j = 0; (j < 3); j++) {
						sourcePoints[0][i] += globalTransform[i][j]* anchorPoints[0][j];
					}
				}
				for(int i=1;i<=slices;i++){
					for(int j=1;j<=channels;j++){
						trj=gettrj();
						trj.setSourcePoints(new double[][]{{sourcePoints[0][0],sourcePoints[0][1]}});
						trj.setTargetPoints(new double[][]{{width/2,height/2}});
						ImagePlus source2=new ImagePlus("StackRegSource",getImpProcessor(imp,j,i,f));
						ImagePlus transformed=trj.transformImage(source2,width,height,TurboRegJ.TRANSLATION);
						if(transformed==null) return false;
						setImpProcessor(imp,transformed,j,i,f);
					}
				}
				if(simp!=null){
					for(int i=1;i<=slices2;i++){
						for(int j=1;j<=channels2;j++){
							trj=gettrj();
							trj.setSourcePoints(new double[][]{{Math.round(sourcePoints[0][0]),Math.round(sourcePoints[0][1])}});
							trj.setTargetPoints(new double[][]{{Math.round(width/2),Math.round(height/2)}});
							ImagePlus source2=new ImagePlus("StackRegSource",getImpProcessor(simp,j,i,f));
							ImagePlus transformed=trj.transformImage(source2,width,height,TurboRegJ.TRANSLATION);
							if(transformed==null) return false;
							setImpProcessor(simp,transformed,j,i,f);
						}
					}
				}
				break;
			}
			case 1: {
				sourcePoints = new double[3][3];
				for (int i = 0; (i < 3); i++) {
					sourcePoints[0][i] = 0.0;
					sourcePoints[1][i] = 0.0;
					sourcePoints[2][i] = 0.0;
					for (int j = 0; (j < 3); j++) {
						sourcePoints[0][i] += globalTransform[i][j]* anchorPoints[0][j];
						sourcePoints[1][i] += globalTransform[i][j]* anchorPoints[1][j];
						sourcePoints[2][i] += globalTransform[i][j]* anchorPoints[2][j];
					}
				}
				
				for(int i=1;i<=slices;i++){
					for(int j=1;j<=channels;j++){
						trj=gettrj();
						trj.setSourcePoints(new double[][]{{sourcePoints[0][0],sourcePoints[0][1]},{sourcePoints[1][0],sourcePoints[1][1]},{sourcePoints[2][0],sourcePoints[2][1]}});
						trj.setTargetPoints(new double[][]{{width/2,height/2},{width/2,height/4},{width/2,3*height/4}});
						ImagePlus source2=new ImagePlus("StackRegSource",getImpProcessor(imp,j,i,f));
						ImagePlus transformed=trj.transformImage(source2,width,height,TurboRegJ.RIGID_BODY);
						if(transformed==null) return false;
						setImpProcessor(imp,transformed,j,i,f);
					}
				}
				if(simp!=null){
					for(int i=1;i<=slices2;i++){
						for(int j=1;j<=channels2;j++){
							trj=gettrj();
							trj.setSourcePoints(new double[][]{{Math.round(sourcePoints[0][0]),Math.round(sourcePoints[0][1])},{Math.round(sourcePoints[1][0]),Math.round(sourcePoints[1][1])},{Math.round(sourcePoints[2][0]),Math.round(sourcePoints[2][1])}});
							trj.setTargetPoints(new double[][]{{Math.round(width/2),Math.round(height/2)},{Math.round(width/2),Math.round(height/4)},{Math.round(width/2),Math.round(3*height/4)}});
							ImagePlus source2=new ImagePlus("StackRegSource",getImpProcessor(simp,j,i,f));
							ImagePlus transformed=trj.transformImage(source2,width,height,TurboRegJ.RIGID_BODY);
							if(transformed==null) return false;
							setImpProcessor(simp,transformed,j,i,f);
						}
					}
				}
				break;
			}
			case 2: {
				sourcePoints = new double[2][3];
				for (int i = 0; (i < 3); i++) {
					sourcePoints[0][i] = 0.0;
					sourcePoints[1][i] = 0.0;
					for (int j = 0; (j < 3); j++) {
						sourcePoints[0][i] += globalTransform[i][j]* anchorPoints[0][j];
						sourcePoints[1][i] += globalTransform[i][j]* anchorPoints[1][j];
					}
				}
				for(int i=1;i<=slices;i++){
					for(int j=1;j<=channels;j++){
						trj=gettrj();
						trj.setSourcePoints(new double[][]{{sourcePoints[0][0],sourcePoints[0][1]},{sourcePoints[1][0],sourcePoints[1][1]}});
						trj.setTargetPoints(new double[][]{{width/4,height/2},{3*width/4,height/2}});
						ImagePlus source2=new ImagePlus("StackRegSource",getImpProcessor(imp,j,i,f));
						ImagePlus transformed=trj.transformImage(source2,width,height,TurboRegJ.SCALED_ROTATION);
						if(transformed==null) return false;
						setImpProcessor(imp,transformed,j,i,f);
					}
				}
				if(simp!=null){
					for(int i=1;i<=slices2;i++){
						for(int j=1;j<=channels2;j++){
							trj=gettrj();
							trj.setSourcePoints(new double[][]{{Math.round(sourcePoints[0][0]),Math.round(sourcePoints[0][1])},{Math.round(sourcePoints[1][0]),Math.round(sourcePoints[1][1])}});
							trj.setTargetPoints(new double[][]{{Math.round(width/4),Math.round(height/2)},{Math.round(3*width/4),Math.round(height/2)}});
							ImagePlus source2=new ImagePlus("StackRegSource",getImpProcessor(simp,j,i,f));
							ImagePlus transformed=trj.transformImage(source2,width,height,TurboRegJ.SCALED_ROTATION);
							if(transformed==null) return false;
							setImpProcessor(simp,transformed,j,i,f);
						}
					}
				}
				break;
			}
			case 3: {
				sourcePoints = new double[3][3];
				for (int i = 0; (i < 3); i++) {
					sourcePoints[0][i] = 0.0;
					sourcePoints[1][i] = 0.0;
					sourcePoints[2][i] = 0.0;
					for (int j = 0; (j < 3); j++) {
						sourcePoints[0][i] += globalTransform[i][j]* anchorPoints[0][j];
						sourcePoints[1][i] += globalTransform[i][j]* anchorPoints[1][j];
						sourcePoints[2][i] += globalTransform[i][j]* anchorPoints[2][j];
					}
				}
				for(int i=1;i<=slices;i++){
					for(int j=1;j<=channels;j++){
						trj=gettrj();
						trj.setSourcePoints(new double[][]{{sourcePoints[0][0],sourcePoints[0][1]},{sourcePoints[1][0],sourcePoints[1][1]},{sourcePoints[2][0],sourcePoints[2][1]}});
						trj.setTargetPoints(new double[][]{{width/2,height/4},{width/4,3*height/4},{3*width/4,3*height/4}});
						ImagePlus source2=new ImagePlus("StackRegSource",getImpProcessor(imp,j,i,f));
						ImagePlus transformed=trj.transformImage(source2,width,height,TurboRegJ.AFFINE);
						if(transformed==null) return false;
						setImpProcessor(imp,transformed,j,i,f);
					}
				}
				if(simp!=null){
					for(int i=1;i<=slices2;i++){
						for(int j=1;j<=channels2;j++){
							trj=gettrj();
							trj.setSourcePoints(new double[][]{{Math.round(sourcePoints[0][0]),Math.round(sourcePoints[0][1])},{Math.round(sourcePoints[1][0]),Math.round(sourcePoints[1][1])},{Math.round(sourcePoints[2][0]),Math.round(sourcePoints[2][1])}});
							trj.setTargetPoints(new double[][]{{Math.round(width/2),Math.round(height/4)},{Math.round(width/4),Math.round(3*height/4)},{Math.round(3*width/4),Math.round(3*height/4)}});
							ImagePlus source2=new ImagePlus("StackRegSource",getImpProcessor(simp,j,i,f));
							ImagePlus transformed=trj.transformImage(source2,width,height,TurboRegJ.AFFINE);
							if(transformed==null) return false;
							setImpProcessor(simp,transformed,j,i,f);
						}
					}
				}
				break;
			}
		}
		return true;
	}

	public TurboRegJ gettrj(){
		try{
			Class<?> c=Class.forName(this.getClass().getPackage().getName()+"."+TurboRegMod.class.getSimpleName());
			Object tr=c.newInstance();
			return new TurboRegJ(tr);
		} catch(Throwable e){IJ.log(e.toString());}
		return null;
	}

	public static ImagePlus[] selectImages(boolean addnull,int nimages,String[] labels){
		Object[] windowlist=getImageWindowList(addnull);
		String[] titles=(String[])windowlist[1];
		int[] ids=(int[])windowlist[0];
		GenericDialog gd=new GenericDialog("Select Images");
		for(int i=0;i<nimages;i++){
			gd.addChoice(labels[i],titles,titles[0]);
		}
		gd.showDialog();
		if(gd.wasCanceled()){
			return null;
		}
		ImagePlus[] windows=new ImagePlus[nimages];
		for(int i=0;i<nimages;i++){
			int index=gd.getNextChoiceIndex();
			if(index==ids.length){
				windows[i]=null;
			}else{
				windows[i]=WindowManager.getImage(ids[index]);
			}
		}
		return windows;
	}
	public static Object[] getImageWindowList(boolean addnull){
		int[] ids=WindowManager.getIDList();
		String[] titles=null;
		if(addnull){
			titles=new String[ids.length+1];
		}else{
			titles=new String[ids.length];
		}
		for(int i=0;i<ids.length;i++){
			ImagePlus imp=WindowManager.getImage(ids[i]);
			if(imp!=null){
				titles[i]=imp.getTitle();
			}else{
				titles[i]="";
			}
		}
		if(addnull){
			titles[ids.length]="null";
		}
		Object[] retvals={ids,titles};
		return retvals;
	}
	public static ImagePlus[] selectImages(boolean addnull,int nimages){
		String[] labels=new String[nimages];
		for(int i=0;i<nimages;i++){
			labels[i]="Image"+(i+1);
		}
		return selectImages(addnull,nimages,labels);
	}

}







class TurboRegJ{
	// this is an adaptor class to provide access to TurboReg_ without scripting
	public static final int AFFINE=6;
	public static final int RIGID_BODY=3;
	public static final int SCALED_ROTATION=4;
	public static final int TRANSLATION=2;

	public Object tr;

	/*
	 * public TurboRegJ(){ tr=new TurboReg_(); }
	 */

	public TurboRegJ(Object tr){
		this.tr=tr;
	}

	public double[][] getSourcePoints(){
		// return tr.getSourcePoints();
		return (double[][])getReflectionField(tr,"sourcePoints");
	}

	public double[][] getTargetPoints(){
		// return tr.getTargetPoints();
		return (double[][])getReflectionField(tr,"targetPoints");
	}

	public void setSourcePoints(double[][] sourcePoints){
		double[][] temp=getSourcePoints();
		for(int i=0;i<sourcePoints.length;i++){
			for(int j=0;j<sourcePoints[i].length;j++){
				temp[i][j]=sourcePoints[i][j];
			}
		}
	}

	public void setTargetPoints(double[][] targetPoints){
		double[][] temp=getTargetPoints();
		for(int i=0;i<targetPoints.length;i++){
			for(int j=0;j<targetPoints[i].length;j++){
				temp[i][j]=targetPoints[i][j];
			}
		}
	}

	public ImagePlus initAlignment(final ImagePlus source,final ImagePlus target,final int transformation){
		int[] sourceCrop={0,0,source.getWidth(),source.getHeight()};
		Object[] args={source,sourceCrop,target,sourceCrop,transformation,new Boolean(false)};
		return (ImagePlus)runReflectionMethod(tr,"alignImages",args);
	}

	public ImagePlus transformImage(ImagePlus source,int width,int height,int transformation){
		Object[] args={source,width,height,transformation,new Boolean(false)};
		return (ImagePlus)runReflectionMethod(tr,"transformImage",args);
	}
	
	public static Object runReflectionMethod(Object obj,String method,Object[] args){
		// here we automatically assume that number types are primitive
		if(args==null)
			return runReflectionMethod(obj,method,null,null);
		@SuppressWarnings("rawtypes")
		Class[] argcs=new Class[args.length];
		for(int i=0;i<args.length;i++)
			argcs[i]=args[i].getClass();
		for(int i=0;i<argcs.length;i++){
			if(argcs[i]==Integer.class)
				argcs[i]=Integer.TYPE;
			if(argcs[i]==Float.class)
				argcs[i]=Float.TYPE;
			if(argcs[i]==Double.class)
				argcs[i]=Double.TYPE;
			if(argcs[i]==Short.class)
				argcs[i]=Short.TYPE;
			if(argcs[i]==Byte.class)
				argcs[i]=Byte.TYPE;
			if(argcs[i]==Boolean.class)
				argcs[i]=Boolean.TYPE;
		}
		return runReflectionMethod(obj,method,args,argcs);
	}

	@SuppressWarnings("rawtypes")
	public static Object runReflectionMethod(Object obj,String method,Object[] args,Class[] argcs){
		try{
			Class<?> temp=obj.getClass();
			Method meth=temp.getDeclaredMethod(method,argcs);
			meth.setAccessible(true);
			try{
				Object data=meth.invoke(obj,args);
				return data;
			}catch(IllegalAccessException e){
				IJ.log("illegal access exception");
			}catch(InvocationTargetException e){
				IJ.log("invocation target exception");
			}catch(ClassCastException e){
				IJ.log(e.getMessage());
			}
		}catch(NoSuchMethodException e){
			IJ.log("no such method exception");
		}
		return null;
	}
	public static Object getReflectionField(Object obj,String fieldname){
		try{
			Class<?> temp=obj.getClass();
			Field field=temp.getDeclaredField(fieldname);
			field.setAccessible(true);
			return field.get(obj);
		}catch(NoSuchFieldException e){
			IJ.log("no such field exception");
		}catch(IllegalArgumentException e){
			IJ.log("illegal argument exception");
		}catch(IllegalAccessException e){
			IJ.log("illegal access exception");
		}
		return null;
	}
}

