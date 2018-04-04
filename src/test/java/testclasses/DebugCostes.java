package testclasses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import ezcol.cell.CellData;
import ezcol.debug.Debugger;
import ezcol.main.PluginConstants;
import ezcol.metric.CostesThreshold;
import ezcol.metric.MetricCalculator;
import ezcol.visual.visual2D.ScatterPlotGenerator;
import ij.gui.Plot;

public class DebugCostes {
	public static void testPCC(){
		float[] Aarray = new float[]{1, 4 ,5, 6,7 ,2 ,10, 2};
		float[] Barray = new float[]{2, 3, 5,10, 2, 0, 3, 3};
		CostesThreshold costes = new CostesThreshold();
		Debugger.print(costes.linregCostes(Aarray, Barray, -1, -1, true));
		Debugger.print(costes.linregCostes(Aarray, Barray, 11, 11, false));
	}
	
	public static void testCostes(){
		System.out.println("_____________________________");
		CostesThreshold costes = new CostesThreshold();
		//Positive test
		float[] Aarray = new float[]{1, 4 ,5, 6,7 ,2 ,10, 2};
		float[] Barray = new float[]{2, 3, 5,10, 2, 0, 3, 3};
		
		//Debugger.printCurrentLine("Positive slope dynamic: ");
		//Debugger.print(costes.getCostesThrd(Aarray, Barray));
		
		//Debugger.printCurrentLine("Positive slope slow: ");
		//Debugger.print(costes.getCostesThrdSlow(Aarray, Barray));
		
		
		//Negative test
		float[] A2array = new float[]{1, 3, 5, 7,10, 2 ,3, 9, 1};
		float[] B2array = new float[]{10,8,11, 3, 4, 5, 2, 5, 8};
		
		//Debugger.printCurrentLine("Negative slope dynamic: ");
		//Debugger.print(costes.getCostesThrd(A2array, B2array));
		
		//Debugger.printCurrentLine("Negative slope slow: ");
		//Debugger.print(costes.getCostesThrdSlow(A2array, B2array));
		
		
		int length = 100;
		float[] A3array = new float[length];
		float[] B3array = new float[length];
		//long seed = 860563119486512L;//System.nanoTime();
		//long seed = 871613141857114L;//System.nanoTime();
		//long seed = 874058088730499L;//System.nanoTime();
		long seed = System.nanoTime();
		Debugger.printCurrentLine("seed: " + seed);
		Random random = new Random(seed);
		for (int i = 0; i < length; i++) {
			A3array[i] = random.nextInt(10000) + 1;
			B3array[i] = random.nextInt(10000) + 1;
		}
		
		//Debugger.print(costes.linreg(A3array, B3array));
		Debugger.print(costes.linregCostes(A3array, B3array, -10, -10, true));
		
		
		double[] tmpTholds = costes.getCostesThrd(A3array, B3array);
		Debugger.printCurrentLine("Random dynamic: ");
		Debugger.print(tmpTholds);
		
		/*Set<Integer> dataSet = new HashSet<Integer>(costes.dataSet);

		if(dataSet.size() < costes.dataSet.size()){
		   // There are duplicates
			Debugger.printCurrentLine("DUPLICATES!!!!!!!!!");
		}
		
		float[] Alist = new float[costes.dataSet.size()];
		float[] Blist = new float[costes.dataSet.size()];
		
		for(int i =0; i< costes.dataSet.size(); i++){
			Alist[i] = A3array[costes.dataSet.get(i)];
			Blist[i] = B3array[costes.dataSet.get(i)];
		}*/
		
		//double PCC1 = costes.linregCostes(Alist, Blist, -10, -10, true)[2];
		//Debugger.printCurrentLine("dynamic PCC list: " + (PCC1 * PCC1));
		
		
		double PCC = costes.linregCostes(A3array, B3array, (float)tmpTholds[0], (float)tmpTholds[1], false)[2];
		Debugger.printCurrentLine("dynamic PCC using linregCostes: " + (PCC * PCC));
		
		double[] slowTholds = costes.getCostesThrdSlow(A3array, B3array);
		Debugger.printCurrentLine("Random slow: ");
		Debugger.print(slowTholds);
		
		ScatterPlotGenerator spg = new ScatterPlotGenerator(
				DebugCostes.class.getSimpleName(), "Channel 1", "Channel 2", 
				new float[][]{A3array}, new float[][]{B3array},
				new String[]{""});
		//Debugger.print(tmps);
		//Debugger.printCurrentLine("(0, "+(int)tmps[1]+") to ("+(int)(- tmps[1] / tmps[0])+", 0)");
		//spg.addRegressionLine();
		
		//spg.addDashLines((float)tmpTholds[0], (float)tmpTholds[1], 0);
		//spg.addSolidLines((float)slowTholds[0], (float)slowTholds[1], 0);
		//spg.addCostes();
		spg.show();
	}
}
