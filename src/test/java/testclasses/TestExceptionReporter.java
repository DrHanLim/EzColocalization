package testclasses;

import java.util.Random;


public class TestExceptionReporter {

	public TestExceptionReporter(){
		test();
	}
	
	private int n=10;
	private void test(){
		Random rnd = new Random();
		for(int i=0;i<n;i++){
			if(i==rnd.nextInt(n)){
				stackTrace(Thread.currentThread());
				break;
			}
		}
		
	}
	
	public void stackTrace(Thread thread){
		StackTraceElement[] sts = thread.getStackTrace();
		for(StackTraceElement st: sts)
			System.out.println(st);
		System.out.println("*******************************");
		System.out.println(sts.toString());
	}
}
