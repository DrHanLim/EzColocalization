package ezcol.metric;

import java.util.Random;

import ezcol.cell.CellData;
import ezcol.cell.DataSorter;
import ezcol.main.PluginConstants;
import ij.gui.Plot;
import ij.process.ImageProcessor;

public class CostesThreshold {
	
	public CostesThreshold(){};
	
	public CostesThreshold(ImageProcessor ip1,ImageProcessor ip2){CostesAutoThr(ip1, ip2);}
	
	public double[] CostesAutoThr(CellData c1, CellData c2){
		return null;
	}
	
	public double[] CostesAutoThr(int[] c1, int[] c2){
		return null;
	}
	
	public void test()
	{
		int length=10;
		int[] A=new int[length];
		int[] B=new int[length];
		Random random = new Random(System.nanoTime());
		double sumA2=0,sumB2=0;
		for(int i=0;i<A.length;i++){
			A[i]=random.nextInt(10000)+1;
			B[i]=random.nextInt(10000)+1;
			sumA2+=A[i]*A[i];
			sumB2+=B[i]*B[i];
		}
		System.out.println("sum(A^2): "+sumA2+", sum(B^2): "+sumB2);
		
		
		 //First Step: define line equation
	    double[] tmp=linreg(A, B, 0, 0);
	    double a=tmp[0];
	    double b=tmp[1];
	    double PCC1,PCC2;
	    long nanoTime;
	    
	    nanoTime=System.nanoTime();
	    PCC1=getPCCcostes(A,B,a,b);
	    System.out.println("y = "+a+" * x + "+b);
	    System.out.println("sorted way time: "+(System.nanoTime()-nanoTime));
	    System.out.println("sorted PCC value: "+PCC1);
	    
	    /*nanoTime=System.nanoTime();
	    PCC2=autoCostes(A,B,a,b);
	    System.out.println("sorted way: "+(System.nanoTime()-nanoTime));
	    System.out.println("enumerate PCC: "+PCC2);*/
	    
	    System.out.println("GET PCC value: "+getPCC(A,B));
	    
	    
	    
	    float[] fA=new float[A.length];
	    float[] fB=new float[B.length];
	    for(int i=0;i<A.length;i++){
			fA[i]=(float)A[i];
			fB[i]=(float)B[i];
			
		}
	    Plot myPlot = new Plot("A vs B","A","B");
	    myPlot.addPoints(fA, fB, Plot.toShape("circle"));
	    double[] limits = new double[]{0,10000};
	    //System.out.println("limits: "+limits[0]+" "+limits[1]+" "+limits[2]+" "+limits[3]);
	    myPlot.add("line", new double[]{limits[0],limits[1]}, new double[]{a*limits[0]+b,a*limits[1]+b});
	    
	    myPlot.show();
	    
	    CellData cellC1 = new CellData(fA);
	    CellData cellC2 = new CellData(fB);
	    
	    MetricCalculator mc = new MetricCalculator(PluginConstants.DO_PCC);
	    mc.calMetrics(new CellData[][]{new CellData[]{cellC1},new CellData[]{cellC2}}, 1);
	    /*double PCC = mc.getMetrics(MetricCalculator.SHOW_PCC,0);
	    if(mc.isMetrics(MetricCalculator.SHOW_PCC))
	    	System.out.println("all PCC value: "+PCC);
	    else
	    	System.out.println("Error in PCC");*/

	}
	
	private double[] linreg(float[] Aarray, float[] Barray){
	    double[] coeff=new double[2];
	    int count=0;
	    double sumA,sumB,sumAB,sumsqrA;
	    
	    sumA=0;
	    sumB=0;
	    sumAB=0;
	    sumsqrA=0;
	    
	    for (int m=0; m<Aarray.length; m++){
	           sumA+=Aarray[m];
	           sumB+=Barray[m];
	           sumAB+=Aarray[m]*Barray[m];
	           sumsqrA+=Aarray[m]*Aarray[m];
	           count++;
	    }
	   //0:a, 1:b
	   coeff[0]=(count*sumAB-sumA*sumB)/(count*sumsqrA-sumA*sumA);
	   coeff[1]=(sumsqrA*sumB-sumA*sumAB)/(count*sumsqrA-sumA*sumA);
	   return coeff;
	}
	
	private double getPCC(int[] A,int[] B){
		if(A==null||B==null||A.length<=0||B.length<=0||A.length!=B.length)
			return Double.NaN;
		double Asum=0.0,Bsum=0.0,ABsum=0.0,A2sum=0.0,B2sum=0.0;
		for(int i=0;i<A.length;i++){
			Asum+=A[i];
			Bsum+=B[i];
			ABsum+=A[i]*B[i];
			A2sum+=A[i]*A[i];
			B2sum+=B[i]*B[i];
		}
		int n=A.length;
		double PCC=(n*ABsum-Asum*Bsum)/Math.sqrt((n*A2sum-Asum*Asum)*(n*B2sum-Bsum*Bsum));
		return PCC;
	}
	
	/**
	 * Run Costes Algorithm to find costes thresholds
	 * @param Aarray input float values of channel 1
	 * @param Barray input float values of channel 2
	 * @return a double array with two elements
	 * [0]: Threshold of Channel 1
	 * [1]: Threshold of Channel 2
	 */
	public double[] getCostesThrd(float[] Aarray, float[] Barray){
		double[] tmp=linreg(Aarray, Barray);
	    return getCostesThrd(Aarray,Barray,tmp[0],tmp[1]);
	}
	
	private double[] getCostesThrd(float[] Aarray, float[] Barray,double slope, double intercept){
		if(Aarray==null||Barray==null)
			return new double[]{Double.NaN,Double.NaN};
		int len = Aarray.length<Barray.length?Aarray.length:Barray.length;
		double Asum=0.0,Bsum=0.0,ABsum=0.0,A2sum=0.0,B2sum=0.0;
		
		double PCC=1.0;
		int n=0;
		double squarePCC,covariance;
		double temp;
		double[] projBarray = new double[len];
		int[] sortedIdxes;
		
		//Determine the rank of data points of pixels above Costes thresholds in sortedIdxes
		//The data points should include CostesIdx and everything below it
		//because sortedIdxes is the indexes in descending order.
		int CostesIdx=-1;
		
		for(int i=0;i<len;i++){
			temp = slope*Aarray[i]+intercept;
			projBarray[i] = temp<Barray[i]? temp : Barray[i];
		}
		
		//sort the data array to get the order of elimination of pixels
		//The order of elimination is determined by the projected value (to the linear regression line) of data point in Channel B
		DataSorter mySort = new DataSorter();
		mySort.sort(projBarray);
		sortedIdxes = mySort.getSortIdx();
		
		
		
		//avoid using Math.sqrt for better performance
		
		for(int i=len-1;i>=0;){
			
			//remove all the following data points on the threshold line
			while(i>=0){
				Asum+=Aarray[sortedIdxes[i]];
				Bsum+=Barray[sortedIdxes[i]];
				ABsum+=Aarray[sortedIdxes[i]]*Barray[sortedIdxes[i]];
				A2sum+=Aarray[sortedIdxes[i]]*Aarray[sortedIdxes[i]];
				B2sum+=Barray[sortedIdxes[i]]*Barray[sortedIdxes[i]];
				n++;
				i--;
				if(i>=0 && Aarray[sortedIdxes[i+1]]!=Aarray[sortedIdxes[i]] && Barray[sortedIdxes[i+1]]!=Barray[sortedIdxes[i]])
					break;
			}
			covariance=n*ABsum-Asum*Bsum;
			squarePCC=covariance*covariance/((n*A2sum-Asum*Asum)*(n*B2sum-Bsum*Bsum));
			//This line is commented out so that the minimum absolute value of PCC will be selected
			//squarePCC=covariance>0 ? squarePCC : (-squarePCC);
			if(squarePCC<PCC){
				CostesIdx=i;
				PCC=squarePCC;
			}
		}
		//PCC = PCC>0 ? Math.sqrt(PCC) : -Math.sqrt(-PCC);
		return new double[]{Aarray[sortedIdxes[CostesIdx+1]],projBarray[sortedIdxes[CostesIdx+1]]};
	}
	
	
	private double getPCCcostes(int[] Aarray, int[] Barray,double slope, double intercept){
		
		if(Aarray==null||Barray==null)
			return Double.NaN;
		int len = Aarray.length<Barray.length?Aarray.length:Barray.length;
		double Asum=0.0,Bsum=0.0,ABsum=0.0,A2sum=0.0,B2sum=0.0;
		
		double PCC=1.0;
		int n=0;
		double squarePCC,covariance;
		double temp;
		double[] projBarray = new double[len];
		int[] sortedIdxes;
		
		//Determine the rank of data points of pixels above Costes thresholds in sortedIdxes
		//The data points should include CostesIdx and everything below it
		//because sortedIdxes is the indexes in descending order.
		double CostesIdx;
		
		for(int i=0;i<len;i++){
			temp = slope*Aarray[i]+intercept;
			projBarray[i] = temp<Barray[i]? temp : Barray[i];
		}
		
		//sort the data array to get the order of elimination of pixels
		//The order of elimination is determined by the projected value (to the linear regression line) of data point in Channel B
		DataSorter mySort = new DataSorter();
		mySort.sort(projBarray);
		sortedIdxes = mySort.getSortIdx();
		
		//avoid using Math.sqrt for better performance
		
		for(int i=len-1;i>=0;){
			
			//remove all the following data points on the threshold line
			while(i>=0){
				Asum+=Aarray[sortedIdxes[i]];
				Bsum+=Barray[sortedIdxes[i]];
				ABsum+=Aarray[sortedIdxes[i]]*Barray[sortedIdxes[i]];
				A2sum+=Aarray[sortedIdxes[i]]*Aarray[sortedIdxes[i]];
				B2sum+=Barray[sortedIdxes[i]]*Barray[sortedIdxes[i]];
				n++;
				i--;
				if(i>=0 && Aarray[sortedIdxes[i+1]]!=Aarray[sortedIdxes[i]] && Barray[sortedIdxes[i+1]]!=Barray[sortedIdxes[i]])
					break;
			}
			covariance=n*ABsum-Asum*Bsum;
			squarePCC=covariance*covariance/((n*A2sum-Asum*Asum)*(n*B2sum-Bsum*Bsum));
			squarePCC=covariance>0 ? squarePCC : (-squarePCC);
			if(squarePCC<PCC){
				CostesIdx=i;
				PCC=squarePCC;
			}
			}
		PCC = PCC>0 ? Math.sqrt(PCC) : -Math.sqrt(-PCC);
		return PCC;
	}
	
	private double autoCostes(int[] Aarray, int[] Barray,double slope, double intercept){
		
		if(Aarray==null||Barray==null)
			return Double.NaN;
		int len = Aarray.length<Barray.length?Aarray.length:Barray.length;
		double Asum=0.0,Bsum=0.0,ABsum=0.0,A2sum=0.0,B2sum=0.0;
		double PCC=Double.NEGATIVE_INFINITY;
		double squarePCC;
		double temp;
		
		 int Amin=Integer.MAX_VALUE,Bmin=Integer.MAX_VALUE,Amax=0,Bmax=0;
		 for (int i=0; i<len; i++){
             
                 if(Amin>Aarray[i])
                	 Amin=Aarray[i];
                 if(Bmin>Barray[i])
                	 Bmin=Barray[i];
                 if(Amax<Aarray[i])
                	 Amax=Aarray[i];
                 if(Bmax<Barray[i])
                	 Bmax=Barray[i];
                
         }
		int LoopMin= (int) Math.max(Amin, (Bmin-intercept)/slope);
	    int LoopMax= (int) Math.min(Amax, (Bmax-intercept)/slope);
		
		
		double n;
		double BThrd;
		PCC=Double.NEGATIVE_INFINITY;
		for (int loop=LoopMax;loop>=LoopMin;loop--){
			BThrd=slope*loop+intercept;
			Asum=0.0;Bsum=0.0;ABsum=0.0;A2sum=0.0;B2sum=0.0;
			n=0;
			for(int i=len-1;i>=0;i--){
					if(Aarray[i]<loop||Barray[i]<BThrd){
						Asum+=Aarray[i];
						Bsum+=Barray[i];
						ABsum+=Aarray[i]*Barray[i];
						A2sum+=Aarray[i]*Aarray[i];
						B2sum+=Barray[i]*Barray[i];
						n++;
					}
			}
			temp=n*ABsum-Asum*Bsum;
			squarePCC=temp*temp/((n*A2sum-Asum*Asum)*(n*B2sum-Bsum*Bsum));
			squarePCC=temp>0 ? squarePCC : (-squarePCC);
			if(squarePCC<PCC)
				PCC=squarePCC;
		}
		
		return Math.sqrt(PCC);
	}


	
	/**The following functions are adapted from JaCoP with minor changes
	 * The authors of this plugin are not responsible for these functions
	 * Copyright (C) 2006 Susanne Bolte & Fabrice P. Cordelieres
	 * License:
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License as published by
	 * the Free Software Foundation; either version 3 of the License, or
	 * (at your option) any later version.
	 */

		
		private CellData[] CostesAutoThr(ImageProcessor ip1, ImageProcessor ip2) {
			
			if(ip1==null||ip2==null)
				return null;
			int width=ip1.getWidth(),height=ip1.getHeight();
			if(ip2.getWidth()!=width||ip2.getHeight()!=height)
				return null;
			
			CellData[] cellData=new CellData[2];
			int numOfPixel=0;
			
			int Amin=Integer.MAX_VALUE,Bmin=Integer.MAX_VALUE,Amax=0,Bmax=0;
			int length=width*height;
			int[] A=new int[length],B=new int[length];
			int index=0;
			 for (int y=0; y<height; y++){
	             for (int x=0; x<width; x++){
	                 A[index]=ip1.getPixel(x,y);
	                 B[index]=ip2.getPixel(x,y);
	                 if(Amin>A[index])
	                	 Amin=A[index];
	                 if(Bmin>B[index])
	                	 Bmin=B[index];
	                 if(Amax<A[index])
	                	 Amax=A[index];
	                 if(Bmax<B[index])
	                	 Bmax=B[index];
	                 index++;
	                
	             }
	         }
		    double CostesPearson=1;
		    double [] rx= new double[Amax-Amin+1];
		    double [] ry= new double[Amax-Amin+1];
		    double rmax=0;
		    double rmin=1;
		    int count=0;
		    
		    //First Step: define line equation
		    double[] tmp=linreg(A, B, 0, 0);
		    double a=tmp[0];
		    double b=tmp[1];
		    
		    int CostesThrA=Amax;
		    int CostesThrB=Bmax;
		    /*double CoeffCorr=tmp[2];
		    double CostesSumAThr=0;
		    double CostesSumA=0;
		    double CostesSumBThr=0;
		    double CostesSumB=0;*/
		    
		    int LoopMin= (int) Math.max(Amin, (Bmin-b)/a);
		    int LoopMax= (int) Math.min(Amax, (Bmax-b)/a);
		    
		    
		    //Minimize r of points below (thrA,a*thrA+b)
		    for (int i=LoopMax;i>=LoopMin;i--){
		    	
		        CostesPearson=linregCostes(A, B, i, (int) (a*i+b))[2];
		        rx[count]=i;
		        ry[count]=CostesPearson;

		        if (((Double) CostesPearson).isNaN()){
		            if (count!=LoopMax&&count>0){
		            		ry[count]=ry[count-1];
		            }else{
		                ry[count]=1;
		            }
		        }
		        
		        if (CostesPearson<=rmin && i!=LoopMax){
		            CostesThrA=i;
		            CostesThrB=(int)(a*i+b);
		            //i=Amin-1;
		        }
		        
		        rmax=Math.max(rmax,ry[count]);
		        rmin=Math.min(rmin,ry[count]);
		        count++;
		        
		    }
		    
		    for(int i=0; i<length; i++){
		    	if(A[i]>CostesThrA&&B[i]>CostesThrB) 
		    		numOfPixel++;
		    }
		    
		    cellData[0]=new CellData(numOfPixel);
		    cellData[1]=new CellData(numOfPixel);
		    
		    index=0;
			    for(int i=0; i<length; i++){
			    	if(A[i]>CostesThrA&B[i]>CostesThrB)
			    	{
			    		cellData[0].setData(A[i], i%width, i/width, index);
			    		cellData[1].setData(B[i], i%width, i/width, index);
			    		index++;
			    	}
			    }
		    
		    /*for (int i=0; i<length; i++){
		        CostesSumA+=A[i];
		        if (A[i]>CostesThrA) CostesSumAThr+=A[i];
		        CostesSumB+=B[i];
		        if (B[i]>CostesThrB) CostesSumBThr+=B[i];
		    }*/
		    
		    //Draw the zero line
		    /*double[] xline={CostesThrA, CostesThrA};
		    double[] yline={rmin, rmax};
		    
		    ImagePlus CostesMask=NewImage.createRGBImage("Costes' mask",width,height,nbSlices,0);
		    CostesMask.getProcessor().setValue(Math.pow(2, depth));
		    for (int k=1; k<=nbSlices; k++){
		        CostesMask.setSlice(k);
		        for (int j=0; j<height; j++){
		            for (int i=0; i<width; i++){
		                int position=offset(i,j,k);
		                int [] color=new int[3];
		                color[0]=A[position];
		                color[1]=B[position];
		                color[2]=0;
		                if (color[0]>CostesThrA && color[1]>CostesThrB){
		                    //CostesMask.getProcessor().setValue(((A[position]-CostesThrA)/(LoopMax-CostesThrA))*Math.pow(2, depthA));
		                    //CostesMask.getProcessor().drawPixel(i,j);
		                    for (int l=0; l<=2; l++) color[l]=255;
		                }
		                CostesMask.getProcessor().putPixel(i,j,color);
		            }
		        }
		    }
		    CostesMask.setCalibration(cal);
		    CostesMask.setSlice(1);
		    CostesMask.show();*/
		    
		    //IJ.log("\nCostes' automatic threshold set to "+CostesThrA+" for imgA & "+CostesThrB+" for imgB");
		    //IJ.log("Pearson's Coefficient:\nr="+round(linreg(A, B,CostesThrA,CostesThrB)[2],3)+" ("+round(CostesPearson,3)+" below thresholds)");
		    //IJ.log("M1="+round(CostesSumAThr/CostesSumA,3)+" & M2="+round(CostesSumBThr/CostesSumB,3));
		    
		    return cellData;
		}
		
		private double[] linregCostes(int[] Aarray, int[] Barray, int TA, int TB){
		    double num=0;
		    double den1=0;
		    double den2=0;
		    double[] coeff=new double[3];
		    int count=0;
		    double sumA,sumB,sumAB,sumsqrA,Aarraymean,Barraymean;
		    sumA=0;
		    sumB=0;
		    sumAB=0;
		    sumsqrA=0;
		    Aarraymean=0;
		    Barraymean=0;
		    
		    for (int m=0; m<Aarray.length; m++){
		       if (Aarray[m]<TA && Barray[m]<TB){
		           sumA+=Aarray[m];
		           sumB+=Barray[m];
		           sumAB+=Aarray[m]*Barray[m];
		           sumsqrA+=Aarray[m]*Aarray[m];
		           count++;
		       }
		   }
		    	
		        Aarraymean=sumA/count;
		        Barraymean=sumB/count;
		             
		    
		    for (int m=0; m<Aarray.length; m++){
		       if (Aarray[m]<TA && Barray[m]<TB){
		           num+=(Aarray[m]-Aarraymean)*(Barray[m]-Barraymean);
		           den1+=(Aarray[m]-Aarraymean)*(Aarray[m]-Aarraymean);
		           den2+=(Barray[m]-Barraymean)*(Barray[m]-Barraymean);
		       }
		    }
		   
		   coeff[0]=(count*sumAB-sumA*sumB)/(count*sumsqrA-sumA*sumA);
		   coeff[1]=(sumsqrA*sumB-sumA*sumAB)/(count*sumsqrA-sumA*sumA);
		   coeff[2]=num/(Math.sqrt(den1*den2));
		   
		   return coeff;
		}
		
		private double[] linreg(int[] Aarray, int[] Barray, int TA, int TB){
		    double num=0;
		    double den1=0;
		    double den2=0;
		    double[] coeff=new double[6];
		    int count=0;
		    double sumA,sumB,sumAB,sumsqrA,Aarraymean,Barraymean;
		    
		    sumA=0;
		    sumB=0;
		    sumAB=0;
		    sumsqrA=0;
		    Aarraymean=0;
		    Barraymean=0;
		    
		    for (int m=0; m<Aarray.length; m++){
		       if (Aarray[m]>=TA && Barray[m]>=TB){
		           sumA+=Aarray[m];
		           sumB+=Barray[m];
		           sumAB+=Aarray[m]*Barray[m];
		           sumsqrA+=Math.pow(Aarray[m],2);
		           count++;
		       }
		    }
		
		    Aarraymean=sumA/count;
		    Barraymean=sumB/count;
		    
		    for (int m=0; m<Aarray.length; m++){
		       if (Aarray[m]>=TA && Barray[m]>=TB){
		           num+=(Aarray[m]-Aarraymean)*(Barray[m]-Barraymean);
		           den1+=Math.pow((Aarray[m]-Aarraymean), 2);
		           den2+=Math.pow((Barray[m]-Barraymean), 2);
		       }
		    }
		   
		   //0:a, 1:b, 2:corr coeff, 3: num, 4: den1, 5: den2
		   coeff[0]=(count*sumAB-sumA*sumB)/(count*sumsqrA-Math.pow(sumA,2));
		   coeff[1]=(sumsqrA*sumB-sumA*sumAB)/(count*sumsqrA-Math.pow(sumA,2));
		   coeff[2]=num/(Math.sqrt(den1*den2));
		   coeff[3]=num;
		   coeff[4]=den1;
		   coeff[5]=den2;
		   return coeff;
		}
}


