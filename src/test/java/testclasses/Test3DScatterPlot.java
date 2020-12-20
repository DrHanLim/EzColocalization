package testclasses;
import java.awt.Color;
import java.awt.Image;
import java.lang.reflect.Array;
import java.util.Random;

import ij.ImagePlus;
//import intra.jRenderer3D.*;

public class Test3DScatterPlot {
	public static void test(){
		
		/*JRenderer3D jRenderer3D = new JRenderer3D(125, 125, 125);
		jRenderer3D.setBufferSize(512,512);
		Random rnd = new Random();
		for(int i=0;i<10;i++){
			jRenderer3D.addPoint3D(rnd.nextInt(250), rnd.nextInt(250),  rnd.nextInt(250), 5, Color.WHITE,JRenderer3D.POINT_SPHERE);
		}
		
		//jRenderer3D.setPoints3DDrawMode(JRenderer3D.POINT_SPHERE);
		jRenderer3D.setBackgroundColor(0xFF000050); // dark blue background<br />
		//jRenderer3D.setZAspectRatio(4); 					// set the z-aspect ratio to 4<br />
		jRenderer3D.setTransformScale(1.5); 		// scale factor<br />
		jRenderer3D.setTransformRotationXYZ(80, 0, 160); // rotation angles (in degrees)
		jRenderer3D.doRendering();
		ImagePlus impNew = new ImagePlus("rendered image", jRenderer3D.getImage());
		impNew.show();*/
		//new New3DSurfacePlot().run("");
	}
	
	public static void testGeneric(){
		
		Integer[][][] cellCs = new Integer[2][3][4];
		//Integer[][] testA = (Integer[][])append2D(cellCs,cellCs[0][0].getClass());
		Integer[][] testB = (Integer[][])append2D(cellCs,Integer.class);
		
		
	}
	
	
	private static <T> T[] append2D(T[][] data, Class<? extends T> type) {
		if (data == null)
			return null;

		int length = 0;
		for (int i = 0; i < data.length; i++) {
			if (data[i] != null)
				length += data[i].length;
		}
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(type, length);

		length = 0;

		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) {
				for (int j = 0; j < data[i].length; j++)
					result[length + j] = data[i][j];
				length += data[i].length;
			}
		}

		return result;
	}
}
