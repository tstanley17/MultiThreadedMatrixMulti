import java.util.Random;

public class Consumer extends Driver implements Runnable {

	private SharedBuffer<WorkItem> bb;
	private int MaxConsumerSleepTime;
	private int id;
	
	public Consumer(SharedBuffer<WorkItem> bb, int st, int id) {
		this.id = id;
		this.bb = bb;
		this.MaxConsumerSleepTime = st;
	}

	public void multSubs(WorkItem wi) {
		int[][] A = wi.getSubA(); // Get sub matrices
		int[][] B = wi.getSubB();
		int[][] tempC = new int[A.length][B[0].length]; // create temp matrix for sub matrix multiplication results

		for(int i = 0; i < A.length; i++) {			// Multiply
	    	for(int j = 0; j < B[0].length; j++) {
	    		for(int k = 0; k < B.length; k++) {
	    				tempC[i][j] += A[i][k] * B[k][j];
	    		}
	    	}
	    }
		System.out.println("Consumer " + Thread.currentThread().getName() + " finished calculating: ");

		printMatrix(A); // Print sub matrix A x sub matrix B
		System.out.print("    X");
		printMatrix(B);
		System.out.print("    =>");
		printMatrix(tempC); // Print result
		wi.setSubC(tempC);
		wi.setDone(true); // Mark work item as completed
		numWorkItemsC[this.id]++; // Increment total work items this consumer has completed
		Driver.done.add(wi);
	}

	@Override
	public void run() {
		//System.out.println("Consumer " + Thread.currentThread().getName().replace("Thread-", "") + " has started.");

		while(true) {
			WorkItem wi = bb.take();
			//System.out.println("Consumer " + Thread.currentThread().getName().replace("Thread-", "") + " got new task.");

			if(bb.isEmpty() && noMoreTasksP == true)
				break;
			multSubs(wi);
			//System.out.println("Consumer " + Thread.currentThread().getName().replace("Thread-", "") + " has completed task.");
			
			if(Driver.noMoreTasksP == true && bb.isEmpty()) break; // If producer is done and the buffer is empty, consumer is complete

			try {
				int sleep = new Random().nextInt(MaxConsumerSleepTime + 1); // Sleep random time (given max)
				sleepTimeC.add(sleep); 
				Thread.sleep(sleep);
			}
			catch(Exception e) { }
		}
	}
}
