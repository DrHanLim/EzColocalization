package testclasses;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ImageProcessor;

public class TestMaxMin {

	public TestMaxMin(){
		test3();
	}
	
	public void test2(){
		ImagePlus imp = WindowManager.getCurrentImage();
		if(imp!=null){
			System.out.println("Before: "+imp.getPixel(1, 1)[0]+", processor: "+imp.getProcessor().get(1, 1));
			imp.getProcessor().subtract(5000);
			System.out.println("After: "+imp.getPixel(1, 1)[0]+", processor: "+imp.getProcessor().get(1, 1));
			ImageStack impStack=new ImageStack(imp.getWidth(),imp.getHeight(),imp.getStackSize());
			for(int i=1;i<=impStack.getSize();i++){
				impStack.setProcessor(imp.getStack().getProcessor(i).duplicate(), i);
				System.out.println("Before stack["+i+"]: "+impStack.getProcessor(i).get(1, 1));
				impStack.getProcessor(i).subtract(5000);
				System.out.println("After: stack["+i+"]: "+impStack.getProcessor(i).get(1, 1));
			}
			//new ImagePlus("Result",impStack).show();
		}
	}
	private int iTest;
	public void test(){
		ImagePlus imp = WindowManager.getCurrentImage();
		if(imp!=null)
		{
			ImageStack impStack=new ImageStack(imp.getWidth(),imp.getHeight(),imp.getStackSize());
			for(iTest=1;iTest<=impStack.getSize();iTest++){
				/*new Thread(new Runnable(){
					public void run(){
						impStack.setProcessor(imp.getStack().getProcessor(iTest).duplicate(), iTest);
						System.out.println(iTest+" stack, max: "+impStack.getProcessor(iTest).getStatistics().max+", min: "+impStack.getProcessor(iTest).getStatistics().min);
						System.out.println(iTest+" DUP stack, max: "+impStack.getProcessor(iTest).duplicate().getStatistics().max+", min: "+impStack.getProcessor(iTest).duplicate().getStatistics().min);
						
					}
				}).start();*/
				ImageProcessor oldip=imp.getStack().getProcessor(iTest).duplicate();
				impStack.setProcessor(oldip.duplicate(), iTest);
				System.out.println(iTest+" stack, max: "+impStack.getProcessor(iTest).getMax()+", min: "+impStack.getProcessor(iTest).getMin());
				System.out.println(iTest+" oldip, max: "+oldip.getMax()+", min: "+oldip.getMin());
				System.out.println(iTest+" oldip DUP, max: "+oldip.duplicate().getMax()+", min: "+oldip.duplicate().getMin());
				
			}
		}
		else{
			System.out.println("no imp");
		}
	}
	
	public void test3(){
		ImagePlus imp = WindowManager.getCurrentImage();
		if(imp!=null){
			ImageProcessor oldip=imp.getStack().getProcessor(1).duplicate();
			oldip.resetMinAndMax();
			System.out.println(iTest+" imp.getStack().getProcessor(1), max: "+imp.getStack().getProcessor(1).getMax()+", min: "+imp.getStack().getProcessor(1).getMin());
			System.out.println(iTest+" oldip, max: "+oldip.getMax()+", min: "+oldip.getMin());
		}
	}
}
