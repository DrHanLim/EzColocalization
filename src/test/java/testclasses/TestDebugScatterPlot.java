package testclasses;

import java.util.Random;

import ezcol.visual.visual2D.PlotStackWindow;
import ezcol.visual.visual2D.ScatterPlotGenerator;


public class TestDebugScatterPlot {

	public TestDebugScatterPlot()
	{test();}
	
	public static void test(){
		Random rand = new Random();
		
		float[][] x=new float[1][1000];
		float[][] y=new float[1][1000];
		for(int i=0;i<x.length;i++)
			for(int j=0;j<x[0].length;j++)
			{
				x[i][j]=rand.nextInt(65535) + 1;
				y[i][j]=rand.nextInt(65535) + 1;
			}
	
		
		ScatterPlotGenerator spg=new ScatterPlotGenerator();
		for(int i=0;i<x.length;i++)
			spg.addPlot(x[i], y[i]);
		
		spg.showPlot(true);
		
	}
	
	public static void test2(){
		Random rand = new Random();
		
		float[][] x=new float[1][1000];
		float[][] y=new float[1][1000];
		for(int i=0;i<x.length;i++)
			for(int j=0;j<x[0].length;j++)
			{
				x[i][j]=rand.nextInt(65535) + 1;
				y[i][j]=rand.nextInt(65535) + 1;
			}
	
		PlotStackWindow psw = new ScatterPlotGenerator("test2","Channel 1","Channel 2",
	            x,y,null).show();
		
	}
	
	
}
