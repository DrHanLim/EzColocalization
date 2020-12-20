package testclasses;

import ezcol.cell.DataSorter;

public class TestTieRank {
	public static void test(){
		double[] data = {1,2,2,3,Double.NaN,Double.NaN,Double.NaN,5,5,5};
		DataSorter ds = new DataSorter();
		ds.sort(data);
		double[] rank = ds.getRank();
		System.out.println("original data");
		for(double d:data){
			System.out.print(d+" ");
		}
		System.out.println();
		
		System.out.println("rank");
		for(double d:rank){
			System.out.print(d+" ");
		}
		System.out.println();
		
		int[] sortedIdx = ds.getSortIdx();
		System.out.println("sortedIdx");
		for(int d:sortedIdx){
			System.out.print(d+" ");
		}
		System.out.println();
		
	}
	
	public static void compare(){
		double nan = Double.NaN;
		System.out.println(0>nan);
		System.out.println(0<nan);
		System.out.println(1>nan);
		System.out.println(1<nan);
		System.out.println(!false&&false);
	}
}
