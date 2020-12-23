package testclasses;

import ezcol.cell.ParticleAnalyzerMT;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class TestMaskMeasure {

	public static void test(){
		ImagePlus imp = WindowManager.getCurrentImage();
		if(imp==null)
			return;
		ImageProcessor ip = imp.getProcessor();
		ResultsTable tempResultTable = new ResultsTable();
		
		Analyzer aSys = new Analyzer(imp, Measurements.MEAN + Measurements.MEDIAN + Measurements.AREA, tempResultTable); // System
					
		RoiManager roiManager = new RoiManager();
		
		ParticleAnalyzer analyzer = new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER, 0, null, 0.0, Double.POSITIVE_INFINITY);
		ParticleAnalyzer.setRoiManager(roiManager);
		analyzer.analyze(imp);
		Roi[] rois = roiManager.getRoisAsArray();
		
		ip.resetRoi();
		ShapeRoi shapeRoi = new ShapeRoi(ip.getRoi());
		for(Roi roi: rois){
			shapeRoi.xor(new ShapeRoi(roi));
		}
		imp.setRoi(shapeRoi);
		// Possible problem
		aSys.measure();
		
		roiManager.addRoi(imp.getRoi());
		
		imp.restoreRoi();

		tempResultTable.show("windowTitle");
	}
}
