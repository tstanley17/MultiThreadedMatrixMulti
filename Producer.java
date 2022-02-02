import java.util.ArrayList;
import java.util.Random;

public class Producer extends Driver implements Runnable {

	private int[][] A = null; // local matrix A
	private int[][] B = null; // local matrix B
	private SharedBuffer<WorkItem> bb = null; // shared buffer
	private int splitSize = 0; // number of rows/columns to put into work items
	private int MaxProducerSleepTime = 0; // sleep time

	private int lowA = 0; // Low index of rows for matrix A
	private int highA = 0; // High index of rows from matrix A
	private int lowB = 0; // Low index of columns from matrix B
	private int highB = 0; // High index of columns from matrix B
	private boolean isDone = false; // Boolean to check if all possible work items have been completed

	static boolean start = true; // Boolean to check if current work item is the first work item
	static int indexA = 0,indexB=0;	// Current index of matrix A (rows) and index of matrix B (columns) - used for indexing LowHigh Arrays
	static ArrayList<LowHigh> lowhighA = new ArrayList<LowHigh>(); // Stores the Low indexes and high indexes for Matrix A
	static ArrayList<LowHigh> lowhighB = new ArrayList<LowHigh>(); // Stores the Low indexes and high indexes for Matrix B

	/**
	 * Constructor: Producer()
	 * @param bb
	 * @param A
	 * @param B
	 * @param splitSize
	 * @param MaxProducerSleepTime
	 */
	public Producer(SharedBuffer<WorkItem> bb, int[][] A, int[][] B, int splitSize, int MaxProducerSleepTime) { // Producer constructor
		this.bb = bb;
		this.A = A;
		this.B = B;
		this.splitSize = splitSize;
		this.MaxProducerSleepTime = MaxProducerSleepTime;
	}
	
	/**
	 * Method setLowHigh()
	 * Function: make and stored Low High object for producer to create work items
	 */
	public void setLowHigh() {
		int low=0,high=-1,c=0;
		while(true) { // Create LowHighs for matrix A
			low=high+1; // increment low
			high=low+splitSize-1; // increment high
			if(high == M-1) {
				lowhighA.add(c, new LowHigh(low,M-1)); // last low high available
				break;
			} else if(high >= M) {
				lowhighA.add(c, new LowHigh(low,M-1)); // last low high available
				break;
			}
			else {
				lowhighA.add(c, new LowHigh(low,high)); // low high available
			}
			c++;
		}
		low=0;
		high=-1;
		c=0;
		while(true) { // Create LowHighs for matrix B
			low=high+1; // increment low
			high=low+splitSize-1; // Increment high
			if(high == P-1) {
				lowhighB.add(c, new LowHigh(low,P-1));  // last low high available
				break;
			} else if(high >= P) {
				lowhighB.add(c, new LowHigh(low,P-1));  // last low high available
				break;
			}
			else {
				lowhighB.add(c, new LowHigh(low,high));  // low high available
			}
			c++;
		}
	}

	//
	public void SetLH() {
		if(indexB == lowhighB.size() && indexA == lowhighA.size()-1) { // If now more availible LowHigh's, producer is done making tasks
			isDone = true;
		}else if(start) { // Set first Low and high indexes for Matrices for first work item
			LowHigh temp = lowhighB.get(indexB);
			lowB = temp.getLow();
			highB = temp.getHigh();
			indexB++;
			LowHigh temp2 = lowhighA.get(indexA);
			lowA=temp2.getLow();
			highA=temp2.getHigh();
		}else if(indexB == lowhighB.size() && indexA < lowhighA.size()-1) { // If we have matched all columns of B but now completed all rows of A
			indexB=0;
			LowHigh temp = lowhighB.get(indexB);
			lowB = temp.getLow();
			highB = temp.getHigh();
			indexB++;
			indexA++;
			LowHigh temp2 = lowhighA.get(indexA);
			lowA=temp2.getLow();
			highA=temp2.getHigh();
		} else if(indexB < lowhighB.size() && indexA < lowhighA.size()) { // If both A and be have more rows/columns left 
			LowHigh temp = lowhighB.get(indexB);
			lowB = temp.getLow();
			highB = temp.getHigh();	
			indexB++;
		}
		else if(indexB < lowhighB.size() && indexA == lowhighA.size()) { // If only B has more columns left
			LowHigh temp = lowhighB.get(indexB);
			lowB = temp.getLow();
			highB = temp.getHigh();	
			indexB++;
		}
	}

	/**
	 * Method: createTask()
	 * Function: Create a work item task for consumer
	 * @return
	 */
	public WorkItem createTask() {

		int[][] subA = new int[splitSize][A[0].length]; // Sub matrix of matrix A
		int[][] subB = new int[B.length][splitSize]; // Sub matrix of matrix B
		
		int row = 0, column = 0;			// Load sub Matrix A given low index and high index
		for(int i = lowA; i <= highA; i++) {
			for(int j = 0; j < A[0].length; j++) {
					subA[row][j] = A[i][j];
			}
			row++;
		}									
		row=0;
		column=0;
		for(int i = 0; i < B.length; i++) {	// laod sub matrix b given low index and high index
			for(int j = lowB; j <= highB; j++) {
					subB[row][column] = B[i][j];
					column++;
			}
			column=0;
			row++;
		}

		WorkItem wi = new WorkItem(subA,subB,lowA,highA,lowB,highB,false); // create new work item from sub matrices
		return wi;
	}

	@Override
	public void run() {	

		System.out.println("Producer " + Thread.currentThread().getName().replace("Thread-", "") + " has started."); // Producer has started
		setLowHigh(); // Create LowHigh objects
		while(true) {
			SetLH(); // Get LowHigh from low high objects
			System.out.println("Producer " + Thread.currentThread().getName()+ " put rows "+ lowA+"-"+highA+" of Matrix A and columns "+lowB+"-"+highB+" of Matrix B to buffer.");
			bb.put(createTask()); // Create new WorkItem
			start = false; // no longer first work item
			if(isDone) break; // If is done, producer create no more tasks
			
			numWorkItems++; // Increment number of work item produced

			try {
				int sleep = new Random().nextInt(MaxProducerSleepTime + 1); // Sleep for random time (max is given)
				sleepTimeP.add(sleep); // Add current sleep time to system storage
				Thread.sleep(sleep); // Sleep
			}
			catch(Exception e) { }

		}
		
		noMoreTasksP = true;

		// Wait to load result matrix until all tasks are complete from the consumers
		while(!bb.isEmpty()) { 
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Load the result matrix from multiplication using producer-consumer model
		for(WorkItem wi: done) {
			int[][] subC = wi.getSubC();
//			System.out.println(subC.length + "-" + subC[0].length);
			int la = wi.getLowA();
			int lb = wi.getLowB();

			for(int i = 0; i < subC.length; i++) {
				for(int j = 0; j < subC[0].length; j++) {
					if(lb > wi.getHighB()) continue;
					if(la > wi.getHighA()) continue; 
						C[la][lb] = subC[i][j];
						lb++;
				}
				lb=wi.getLowB();
				la++;
			}	
		}
		System.out.println("\nProducer successfully assembled all the results from the consumer threads.");
		System.out.println("---------------------------------------------------------------");

		noMoreP = true;
		//		Thread.currentThread().stop();
	}// end run()
}
