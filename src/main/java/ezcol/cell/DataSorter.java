package ezcol.cell;

import java.util.Vector;

import ezcol.debug.ExceptionHandler;

/*
 * This is a class used to sort arrays so that the index of each number in the sorted array
 * is accessible for calculating threshold overlap score
 * the sorting is performed by quick sort algorithm
 * the sorting is always in descending order
 */
public class DataSorter {

	public static final int INT=1,FLOAT=2,DOUBLE=3;
	private int[] sortIdx;
	private int[] inputInt;
	private float[] inputFloat;
	private double [] inputDouble;
	private int inputlength;
	private int marker;
    Object Output;
    
    public void reset()
    {
    	this.sortIdx=null;
    	this.inputlength=0;
    	this.inputDouble=null;
    	this.inputFloat=null;
    	this.inputInt=null;
    }
    
	/**
	 * sortedIdx can be array.length+index if the corresponding data point is NaN
	 * @return
	 */
	public int[] getSortIdx()
	{return sortIdx.clone();}
	
	/*
	 * Change in version 1.3
	 * now if sortIdx is array.length+index, rank will be set to Double.NaN
	 * Change in version 1.4
	 * it returns fraction rank (tie rank) now
	 */
	public double[] getRank()
	{
		if(sortIdx==null)
			return null;
		double[] rank=new double[sortIdx.length];
		Vector<Integer> ties = new Vector<Integer>();
		for(int i=1;i<=rank.length;i++){
			if(sortIdx[i-1]>=sortIdx.length){
				rank[sortIdx[i-1]-sortIdx.length]= Double.NaN ;
			}
			else if(getData(i-1).equals(getData(i))){
				ties.add(i);
			}
			else if(ties.isEmpty()){
				rank[sortIdx[i-1]] = i;
			}
			else {
				ties.add(i);
				double avg = 0.0;
				for(Integer j:ties){
					avg+=j;
				}
				avg/=ties.size();
				for(Integer j:ties){
					rank[sortIdx[j-1]]=avg;
				}
				ties.clear();
			}
		}
		return rank;
	}
	
	public Number getData(int index){
		if(index<0||index>=inputlength)
			return Double.NaN;
		else{
			switch(marker){
				case INT: 
					return inputInt[index];
				case FLOAT: 
					return inputFloat[index];
				case DOUBLE: 
					return inputDouble[index];
			}
		}
		return Double.NaN;
	}
    
    public void sort(int[] inputArr) 
    {
        if (inputArr == null || inputArr.length == 0) {
        	return;
        }
        marker = INT;
        inputInt = inputArr.clone();
        inputlength = inputArr.length;
        sortIdx=new int[inputlength];
        for (int idx=0;idx<inputlength;idx++){
        	  this.sortIdx[idx] = idx;
        }
        quickSortInt(0, inputlength - 1);
        Output=(Object) inputInt;
    }
    
    public void sort(int[] inputArr,int size) 
    {
	    
        if (inputArr == null || inputArr.length < size || size == 0) {
        	return;
        }
        marker = INT;
        inputInt = inputArr.clone();
        inputlength = size;
        for (int i=0;i<size;i++)
        	inputInt[i]=inputArr[i];
        sortIdx=new int[inputlength];
        for (int idx=0;idx<inputlength;idx++){
        	  this.sortIdx[idx] = idx;
        }
        quickSortInt(0, inputlength - 1);
        Output=(Object) inputInt;
    }
    
    public void sort(float[] inputArr) {
	       
        if (inputArr == null || inputArr.length == 0) {
            return;
        }
        marker = FLOAT;
        inputFloat = inputArr.clone();
        inputlength = inputArr.length;
        sortIdx=new int[inputlength];
        
        for (int idx=0;idx<inputlength;idx++){
        	if(Float.isNaN(inputArr[idx]))
              this.sortIdx[idx] = idx+sortIdx.length;
        	else
        	  this.sortIdx[idx] = idx;
        }
        quickSortFloat(0, inputlength - 1);
        Output=(Object) inputFloat;
    }
    public void sort(float[] inputArr,int size) {
	       
        if (inputArr == null || inputArr.length < size || size == 0) {
            return;
        }
        marker = FLOAT;
        inputFloat = inputArr.clone();
        inputlength = size;
        sortIdx=new int[inputlength];
        for (int idx=0;idx<inputlength;idx++){
        	if(Float.isNaN(inputArr[idx]))
              this.sortIdx[idx] = idx+sortIdx.length;
        	else
        	  this.sortIdx[idx] = idx;
        }
        quickSortFloat(0, inputlength - 1);
        
        Output=(Object) inputFloat;
    }
    
	
	public void sort(double[] inputArr) {
	       
        if (inputArr == null || inputArr.length == 0) {
        	return;
        }
        marker = DOUBLE;
        inputDouble = inputArr.clone();
        inputlength = inputArr.length;
        sortIdx=new int[inputlength];
        for (int idx=0;idx<inputlength;idx++){
        	if(Double.isNaN(inputArr[idx]))
              this.sortIdx[idx] = idx + sortIdx.length;
        	else
        	  this.sortIdx[idx] = idx;
        }
        quickSortDouble(0, inputlength - 1);
        Output=(Object) inputDouble;
    }
	
	public void sort(double[] inputArr,int size) {
	       
        if (inputArr == null || inputArr.length < size || size == 0) {
        	return;
        }
        marker = DOUBLE;
        inputDouble = inputArr.clone();
        inputlength = size;
        for (int i=0;i<size;i++)
        	inputDouble[i]=inputArr[i];
        sortIdx=new int[inputlength];
        for (int idx=0;idx<inputlength;idx++){
        	if(Double.isNaN(inputArr[idx]))
              this.sortIdx[idx] = idx+sortIdx.length;
        	else
        	  this.sortIdx[idx] = idx;
        }
        quickSortDouble(0, inputlength - 1);
        Output=(Object) inputDouble;
    }
 
    private void quickSortInt(int lowerIndex, int higherIndex) {
       
        int i = lowerIndex;
        int j = higherIndex;
        int temp;
        int tempIdx;
        // calculate pivot number, I am taking pivot as middle index number
        int pivot = inputInt[lowerIndex+(higherIndex-lowerIndex)/2];
        // Divide into two inputInt
        while (i <= j) {
            /**
            * In each iteration, we will identify a number from left side which
            * is greater then the pivot value, and also we will identify a number
            * from right side which is less then the pivot value. Once the search
            * is done, then we exchange both numbers.
            */
            while (inputInt[i] > pivot) {
                i++;
            }
            while (inputInt[j] < pivot) {
                j--;
            }
            if (i <= j) {
            	temp = inputInt[i];
                inputInt[i] = inputInt[j];
                inputInt[j] = temp;
                tempIdx = sortIdx[i];
                sortIdx[i] = sortIdx[j];
                sortIdx[j] = tempIdx;
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSortInt(lowerIndex, j);
        if (i < higherIndex)
            quickSortInt(i, higherIndex);
    }
    private void quickSortFloat(int lowerIndex, int higherIndex) {
        
        int i = lowerIndex;
        int j = higherIndex;
        float temp;
        int tempIdx;
        // calculate pivot number, I am taking pivot as middle index number
        float pivot=0;
        pivot = inputFloat[lowerIndex+(higherIndex-lowerIndex)/2];
        
        // Divide into two inputFloat
        while (i <= j) {
            /**
            * In each iteration, we will identify a number from left side which
            * is greater then the pivot value, and also we will identify a number
            * from right side which is less then the pivot value. Once the search
            * is done, then we exchange both numbers.
            */
            while (inputFloat[i] > pivot || 
            	(Float.isNaN(pivot)&&(!Float.isNaN(inputFloat[i])))) {
                i++;
            }
            while (inputFloat[j] < pivot ||
            	(!Float.isNaN(pivot)&&Float.isNaN(inputFloat[j]))) {
                j--;
            }
            if (i <= j) {
            	temp = inputFloat[i];
            	inputFloat[i] = inputFloat[j];
            	inputFloat[j] = temp;
                tempIdx = sortIdx[i];
                sortIdx[i] = sortIdx[j];
                sortIdx[j] = tempIdx;
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSortFloat(lowerIndex, j);
        if (i < higherIndex)
            quickSortFloat(i, higherIndex);
    }
    private void quickSortDouble(int lowerIndex, int higherIndex) {
        
        int i = lowerIndex;
        int j = higherIndex;
        double temp;
        int tempIdx;
        // calculate pivot number, I am taking pivot as middle index number
        double pivot = inputDouble[lowerIndex+(higherIndex-lowerIndex)/2];
        // Divide into two inputDouble
        while (i <= j) {
            /**
            * In each iteration, we will identify a number from left side which
            * is greater then the pivot value, and also we will identify a number
            * from right side which is less then the pivot value. Once the search
            * is done, then we exchange both numbers.
            */
            while (inputDouble[i] > pivot ||
            	(Double.isNaN(pivot)&&!Double.isNaN(inputDouble[i]))) {
                i++;
            }
            while (inputDouble[j] < pivot ||
            	(!Double.isNaN(pivot)&&Double.isNaN(inputDouble[j]))) {
                j--;
            }
            if (i <= j) {
            	temp = inputDouble[i];
                inputDouble[i] = inputDouble[j];
                inputDouble[j] = temp;
                tempIdx = sortIdx[i];
                sortIdx[i] = sortIdx[j];
                sortIdx[j] = tempIdx;
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSortDouble(lowerIndex, j);
        if (i < higherIndex)
            quickSortDouble(i, higherIndex);
    }
    
    public Object getResult(){
    	return Output;
    }
    
}
