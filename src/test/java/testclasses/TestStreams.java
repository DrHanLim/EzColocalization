package testclasses;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Stream;

public class TestStreams {
	public TestStreams(){
		//test();
	}
	
	public void test(){
		/*Set<Integer> servers;
		Integer[] a={1,2,3,4,5};
		
	    servers.parallelStream().forEach((server) -> {
	        serverData.put(server.getIdentifier(), server.fetchData());
	    });*/
		int LEN=100000;
		double[] array = new double[LEN];
		long timer = System.currentTimeMillis();
		
		for (int j = 0; j < array.length; j++) {
		    array[j] = Math.pow(Math.log(j),Math.log(j));
		}
		System.out.println("seq for loop:¡¡"+(System.currentTimeMillis()-timer));
		timer=System.currentTimeMillis();
		
		int NTHREADS =10;
		
		ForkJoinPool pool = new ForkJoinPool(NTHREADS); 
		// blocks until completion
		pool.invoke(new ForEach(array, 0, array.length)); 
		System.out.println("forkjoin: "+(System.currentTimeMillis()-timer));
		timer=System.currentTimeMillis();
	}
	
	public void parallelStreams(){
		
	}
	
	public void test(int i){
		
	}
	
	
}


class ForEach extends RecursiveAction {

	private double[] array;
	private int from;
	private int to;

	// you can fine-tune this,
	// should be sth between 100 and 10000
	public final static int TASK_LEN = 5000;

	public ForEach(double[] array, int from, int to) {
		this.array = array;
		this.from = from;
		this.to = to;
	}

	@Override
	protected void compute() {
		int len = to - from;
		if (len < TASK_LEN) {
			work(array, from, to);
		} else {
			// split work in half, execute sub-tasks asynchronously
			int mid = (from + to) >>> 1;
			new ForEach(array, from, mid).fork();
			new ForEach(array, mid, to).fork();
		}
	}
	
	void work(double[] array, int from, int to) {
	    for (int j = from; j < to; j++) {
	    	array[j] = Math.pow(Math.log(j),Math.log(j));
	    }
	}
}
