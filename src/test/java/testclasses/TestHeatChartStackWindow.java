package testclasses;

import java.awt.Dimension;

import ezcol.visual.visual2D.HeatChart;
import ezcol.visual.visual2D.HeatChartStackWindow;

public class TestHeatChartStackWindow {
	public TestHeatChartStackWindow(){
		double[][] data = new double[10][10];
		for(int i=0;i<data.length;i++)
			for(int j=0;j<data[i].length;j++)
				data[i][j]=i+j;
		HeatChart heatChart = new HeatChart(data);
		heatChart.setCellSize(new Dimension(50,50));
		new HeatChartStackWindow(heatChart);
	}
}
