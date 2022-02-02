
public class LowHigh {
	
	private int low;
	private int high;
	
	/**
	 * Constructor: LowHigh
	 * Function: store low and high indexes for producer and consumer
	 * @param l
	 * @param h
	 */
	public LowHigh(int l, int h) {
		this.low = l;
		this.high = h;
	}
	
	public int getLow() { return this.low; }
	public int getHigh() { return this.high; }
	
}
