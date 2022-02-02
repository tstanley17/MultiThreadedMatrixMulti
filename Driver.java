import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Driver { 

	static int[][] C = null;
	static boolean noMoreTasksP = false; // Used by consumer to make sure all tasks are loaded into buffer
	static boolean noMoreP = false; // Used by driver to check if producer has completed all functions

	static ArrayList<WorkItem> done = new ArrayList<WorkItem>(); // List of all completed tasks
	static ArrayList<Integer> sleepTimeC = new ArrayList<Integer>(); // List of time the consumers were sleeping
	static ArrayList<Integer> sleepTimeP = new ArrayList<Integer>(); // List of time the producer were sleeping
	static int[] numWorkItemsC = null; // List to determine how many work items each consumer completed
	static int numWorkItems = 0; // Total number of work items (incremented by producer)
	static int bufferFull = 0; // Number of times the buffer was full
	static int bufferEmpty = 0; // Number of times the buffer was empty
	static int M = 0,N = 0,P = 0,SplitSize = 0,NumConsumer = 0,MaxProducerSleepTime = 0,MaxConsumerSleepTime = 0,MaxBufferSize = 0; // Parameters from input file

	public static void printMatrix(int[][] matrix) {
		
	    Arrays.stream(matrix).forEach((row) -> {
	        System.out.print("\t[");
	        Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
	        System.out.println("]");
	      });
	
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws FileNotFoundException {
				
		// Load parameters from input file into variables
		File file = new File("input.txt");
		Scanner sc = new Scanner(file);
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] key_value = line.split("=");
			String key = key_value[0].replace("<", "").replace(">", "").trim();
			String value = key_value[1].replace("<", "").replace(">", "").trim();
			
			switch(key) {
			case "M":
				M = Integer.parseInt(value);
				break;
			case "N":
				N = Integer.parseInt(value);
				break;
			case "P":
				P = Integer.parseInt(value);
				break;
			case "MaxBufferSize":
				MaxBufferSize = Integer.parseInt(value);
				break;
			case "SplitSize":
				SplitSize = Integer.parseInt(value);
				break;
			case "NumConsumer":
				NumConsumer = Integer.parseInt(value);
				break;
			case "MaxProducerSleepTime":
				MaxProducerSleepTime = Integer.parseInt(value);
				break;
			case"MaxConsumerSleepTime":
				MaxConsumerSleepTime = Integer.parseInt(value);
				break;
			}
		}
		
		numWorkItemsC = new int[NumConsumer]; // Initialize array for keeping track of consumer work items
		
		long startTime = System.currentTimeMillis(); // After parameters are loaded, simulation has started

		//Create Matrices
		int[][] A = new int[M][N]; // Initialize first matrix
		int[][] B = new int[N][P]; // Initialize second matrix
		
		// Load first matrix with random numbers (0-9)
		for(int i = 0; i < M; i++) {
			for(int j = 0; j < N; j++) {
				A[i][j] = new Random().nextInt(10); // Random number (0-9)
			}
		}
		// Load second matrix with random numbers (0-9)
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < P; j++) {
				B[i][j] = new Random().nextInt(10); // Random number (0-9)
			}
		}
		System.out.println("First Matrix: ");
		printMatrix(A);
		System.out.println("Second Matrix: ");
		printMatrix(B);
		
		C = new int[M][P]; // Initialize matrix for multiplication results from producer-consumer method

		SharedBuffer<WorkItem> bb = new SharedBuffer<WorkItem>(MaxBufferSize); // Create new bounded buffer for producers and consumers

		Producer p = new Producer(bb, A, B, SplitSize, MaxProducerSleepTime); // Create producer object
		Thread producer = new Thread(p); // Create producer thread for creating work items
		producer.start(); // Start Producer thread
		
		Thread[] consumers = new Thread[NumConsumer]; // Create array to hold consumer threads
		for(int i = 0; i < NumConsumer; i++) {
			Thread consumer = new Thread(new Consumer(bb, MaxConsumerSleepTime,i)); // Create new consumer threads with consumer objects
			consumers[i] = consumer; // Load into array
			consumer.start(); // Start consumer thread
		}
		
		while(producer.isAlive()); // Wait to print final results until producer has finished compiling the completed work items
		
		long endTime = System.currentTimeMillis(); // Mark end of simulation
		
		System.out.println("Final result of parallel matrix multiplication: "); // Print the matrix multiplication results from producer-consumer method
		printMatrix(C);
		System.out.println("Verified result of sequential matrix multiplication: "); // Print the matrix results from the sequential multiplication method
		
		int[][] seqMethod = new int[M][P]; // Result matrix for checking if producer-consumer 
		for(int i = 0; i < A.length; i++) {  // Multiply the two generated matrices
			for(int j = 0; j < B[0].length; j++) {
				for(int k = 0; k < B.length; k++) {
					seqMethod[i][j]+=A[i][k]*B[k][j];
				}
			}
		}
		printMatrix(seqMethod); // Print the result matrix from sequential method
		
		
		System.out.println();
		int sumSleepC = 0, sumSleepP = 0;
		long cc = 0, cp = 0;
		for(int i: sleepTimeC) { // Calculate the total time consumers were asleep
			cc++;
			sumSleepC += i;
		}
		for(int i: sleepTimeP) { // Calculate the total time producers were asleep
			cp++;
			sumSleepP += i;
		}
		System.out.println("PRODUCER / CONSUMER SIMULATION RESULT");							// Show the system statistics
		System.out.println("Simulation Time: \t\t\t\t" + ((endTime-startTime)*1.0) + "ms");		// Simulation time
		System.out.println("Consumer average sleep time: \t\t\t" + (1.0*(sumSleepC/cc)) + "ms");// Ave sleep time consumer
		System.out.println("Producer average sleep time: \t\t\t" + (1.0*(sumSleepP/cp)) + "ms");// Ave sleep time producer
		System.out.println("Number of producer threads: \t\t\t" + "1");							// number of producer threads
		System.out.println("Number of consumers threads: \t\t\t" + NumConsumer);				// number of consumer threads
		System.out.println("Buffer size: \t\t\t\t\t" + MaxBufferSize);							// Size of buffer
		System.out.println("Number of work items produced from producers: ");					// Work items produced from producer
		System.out.println("   Producer work items produced:\t\t " + numWorkItems);				// 
		System.out.println("Number of work items completed from consumers: ");					// Num work items each consumer completed
		for(int i = 0; i < consumers.length; i++) {
			System.out.println("   Consumer " + i + " work items completed:\t\t " + numWorkItemsC[i]);
		}
		System.out.println("Number of items remaining in buffer: \t\t" + bb.bufferSize());		// Num items left in buffer
		System.out.println("Number of times buffer was full: \t\t" + bufferFull);				// Num times buffer was full
		System.out.println("Number of time buffer was empty: \t\t" + bufferEmpty);				// Num times buffer was empty

		try { Thread.sleep(1000); } catch (Exception e) {}	// Sleep to make sure producer has completed
		for(int i = 0; i < consumers.length; i++) { // Double check if every thread is done
			if(consumers[i].isAlive()) {
				consumers[i].stop();
			}
		}
		if(producer.isAlive()) 
			producer.stop();
	}

}
