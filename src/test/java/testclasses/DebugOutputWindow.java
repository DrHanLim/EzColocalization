package testclasses;

import java.util.Random;

import ezcol.visual.visual2D.OutputWindow;

public class DebugOutputWindow extends OutputWindow{

	
	public DebugOutputWindow(){
		
		addImage("Channel 1", null);
		addImage("Channel 2", null);
		addImage("Channel 3", null);
		Random rnd = new Random();
		for(int t=0;t<5;t++){
			double [] data = new double[rnd.nextInt(100)+1];
			for(int i=0;i<data.length;i++)
				data[i] = rnd.nextDouble();
			addMetric(data);
		}
		double [] data = new double[rnd.nextInt(100)+1];
		for(int i=0;i<data.length;i++)
			data[i] = rnd.nextDouble();
		addMetric(data);
		showLogWindow();
		
	}
}
