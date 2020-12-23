package testclasses;


import ij.IJ;
import ij.plugin.PlugIn;
 
public class TestOutputWindow{
	
	private static final String[] METRIC_STRINGS={"PCC","SRC","ICQ","TOSh","TOSmax","TOSmin", "M1 and M2"};
	private static final String[] INTERP_STRINGS={"Interpretation: -1 represents complete anti-colocalization\n0 represents non-colocalization\n1 represents complete colocalization",
		"Interpretation: -1 represents complete anti-colocalization\n0 represents non-colocalization\n1 represents complete colocalization",
		"Interpretation: 0 represents complete anti-colocalization\n0.5 represents non-colocalization\n1 represents complete colocalization",
		"Interpretation: -1 represents complete anti-colocalization\n0represents non-colocalization\n1 represents complete colocalization"
				+ " \n \nThe distance of TOSh between 0 and 1 or -1 represents the distance between \nthe null hypothesis and colocalization or anti-colocalization."
				+ "\nFor example: 0.5 is halfway between the null distribution and complete overlap \nof selected fractions."
				,"Interpretation: -1 represents complete anti-colocalization\n0 represents non-colocalization\n1 represents complete colocalization"
				+ " \n \nThe value of TOSmax represents the maximum TOS value at any combination of \nselected fractions used in mTOS."
				+ "\nThe value can show whether colocalization occurs when observing any selected \nfractions used."
				,"Interpretation: -1 represents complete anti-colocalization\n0 represents non-colocalization\n1 represents complete colocalization"
				+ " \n \nThe value of TOSmin represents the minimum TOS value at any combination of \nselected fractions used in mTOS."
				+ "\nThe value can show whether anti-colocalization occurs when observing any \nselected fractions used.",
				"Interpretation: 0 represents complete anti-colocalization\n0.5 represents non-colocalization\n1 represents complete colocalization"};
	private static final String SEPARATOR_LINE=("********************************************");
	private static final String BLANK_LINE=(" ");
	private static final String[] STAT_VALUE={"mean = ","median = ","standard deviation = "};
	
	
	public void showWindow(String arg) {
		IJ.log("Results Summary:");
		//add in spacing line
		IJ.log(BLANK_LINE);
		//add in image names obtained from plugin (also status of aligned/unaligned) and number of cells, 
		//this could be tricky with all the input combinations
		IJ.log("Channel 1 image(s): XX (Aligned/Unaligned)");
		IJ.log("Channel 2 image(s): XX (Aligned/Unaligned)");
		IJ.log("Phase-contrast image(s): XX (Aligned/Unaligned)");
		//add in spacing lines and then need to get number of cells identified from plugin
		IJ.log(BLANK_LINE);
		IJ.log("Number of cells analyzed = XX");
		IJ.log(BLANK_LINE);
		
		
		//add in spacer, and then warnings and if statements triggering differ
		IJ.log("******************Warnings******************");
		
		//add in if statements to trigger different warnings and a spacer afterwards
		IJ.log("Cells containing less than 30 pixels have been analyzed.");
		IJ.log("No cells have been identified for analysis. Try broadening filter ranges.");
		IJ.log("A very high number of cells have been identified. This may slow down the plugin.");
		IJ.log("Cells containing greater than 1000 pixels have been analyzed.");
		IJ.log("Costes¡¯ threshold method has selected almost all pixels for M1 and M2 analyses.");
		IJ.log("Costes¡¯ threshold method has not selected any pixels for M1 and M2 analyses.");
		IJ.log(BLANK_LINE);
		
		//this for loop needs to change to get the "i" values from GUI, as in which outputs should be generated
		for (int i = 0; i < 6; i++){ 
		
		//add in if statement for PCC analysis here and if statements for statistics
		IJ.log(SEPARATOR_LINE);
		IJ.log(METRIC_STRINGS[i]+" analysis of cell population");
		//needs an if statement for if mean is checked in GUI, also need to calculate mean with function from below
		IJ.log(STAT_VALUE[0]+"XX");
		//needs an if statement for if median is checked in GUI, also need to calculate median with function from below
		IJ.log(STAT_VALUE[1]+"XX");
		//needs an if statement for if standard deviation is checked in GUI, also need to calculate standard dev with function from below
		IJ.log(INTERP_STRINGS[i]);
		IJ.log(BLANK_LINE);
		}
		
		//add in if statement for M1 and M2 analysis here and if statements for statistics 
		//(this was seperated out because the strings are a little different)
		IJ.log(SEPARATOR_LINE);
		IJ.log(METRIC_STRINGS[6]+" analysis of cell population");
		//needs an if statement for if mean is checked in GUI, also need to calculate mean with function from below
		IJ.log("mean M1 = XX");
		//needs an if statement for if median is checked in GUI, also need to calculate median with function from below
		IJ.log("median M1 = XX");
		//needs an if statement for if standard deviation is checked in GUI, also need to calculate standard dev with function from below
		IJ.log("standard deviation M1 = XX");
		//needs an if statement for if mean is checked in GUI, also need to calculate mean with function from below
		IJ.log("mean M2 = XX");
		//needs an if statement for if median is checked in GUI, also need to calculate median with function from below
		IJ.log("median M2 = XX");
		//needs an if statement for if standard deviation is checked in GUI, also need to calculate standard dev with function from below
		IJ.log("standard deviation M2 = XX");
		IJ.log(INTERP_STRINGS[6]);
		IJ.log(BLANK_LINE);
		
		//citation printed out at bottom
		IJ.log(SEPARATOR_LINE);
		IJ.log("Citation:");
		IJ.log("Sheng, Huanjie, Weston Stauffer, and Han N. Lim.\nSystematic and general method for quantifying localization in microscopy images.");
		
	}
	//function to calculate mean of array m, will need to get an array from plugin to perform calculation
	public static double mean(double[] m) {
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    return sum / m.length;
	}
	//function to calculate median of array m, will need to get an array from plugin to perform calculation
	public static double median(double[] m) {
	    int middle = m.length/2;
	    if (m.length%2 == 1) {
	        return m[middle];
	    } else {
	        return (m[middle-1] + m[middle]) / 2.0;
	    }
	}
	//function to calculate standard deviation of array m, will need to get an array from plugin to perform calculation
	public static double stdDeviation(double[] m) {
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    double sigma = 0;
	    for (int i = 0; i < m.length; i++) {
	        sigma += (m[i]-(sum / m.length))*(m[i]-(sum / m.length));
	    }
	    return Math.sqrt((1/m.length)*sigma);
	    
	}
	
	
}