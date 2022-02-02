
public class WorkItem {
	private int[][] subA;
	private int[][] subB;
	private int[][] subC;
	private int lowA, highA, lowB, highB;
	private int rowIndex;
	private int columnIndex;
	private boolean done;
	
	/**
	 * Constructor: WorkItem
	 * @param subA
	 * @param subB
	 * @param lowA
	 * @param highA
	 * @param lowB
	 * @param highB
	 * @param done
	 */
	public WorkItem(int[][] subA, int[][] subB, int lowA, int highA, int lowB, int highB, boolean done) {
		this.subA = subA;
		this.subB = subB;
		this.subC = new int[subA.length][subB[0].length];
		this.lowA = lowA;
		this.highA = highA;
		this.lowB = lowB;
		this.highB = highB;
		this.done = done;
	}
	
	// getter and setters
	public int[][] getSubA() {
		return this.subA;
	}
	public int[][] getSubB() {
		return this.subB;
	}
	public int[][] getSubC() {
		return this.subC;
	}
	public void setSubC(int[][] C) {
		this.subC = C;
	}
	public int getRowIndex() {
		return this.rowIndex;
	}
	public int getColumnIndex() {
		return this.columnIndex;
	}
	public void setDone(boolean b) {
		this.done = b;
	}
	public int getLowA() {
		return this.lowA;
	}
	public int getLowB() {
		return this.lowB;
	}
	public int getHighA() {
		return this.highA;
	}
	public int getHighB() {
		return this.highB;
	}
}
