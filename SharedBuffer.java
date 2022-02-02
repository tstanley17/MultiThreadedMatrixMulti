class SharedBuffer<WorkItem> extends Driver {

    private final WorkItem[] buffer; // Bounded buffer
    private int putpos, takepos, count; 

    /**
     * Constructor: SharedBuffer
     * @param bound
     */
    public SharedBuffer(int bound) {
        buffer = (WorkItem[]) new Object[bound];
    }
    
    /**
     * Method: bufferSize()
     * Function: return current size of buffer (length)
     * @return
     */
    public int bufferSize() {
    	return count;
    }

    /**
     * Method: put()
     * Function: put new work item into buffer
     * Restraint: if buffer is full, wait()
     * @param object
     */
    public synchronized void put(WorkItem object) {
        try {
            while (isFull()) {
            	System.out.println("The pool is full " + Thread.currentThread().getName() + " is waiting, size="+count);
            	bufferFull++;
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        doPut(object);
        notifyAll();
    }

    /**
     * Method: take()
     * Function: get a work item from the buffer
     * Restraint: if buffer is empty, wait()
     * @return
     */
    public synchronized WorkItem take() {
        try {
            while (isEmpty()) {
            	System.out.println("The pool is empty " + Thread.currentThread().getName() + " is waiting.");
            	bufferEmpty++;
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WorkItem element = doTake();
        notifyAll();
        return element;
    }

    /**
     * Method: isFul()
     * Function: return true if num work items equals buffer length
     * @return
     */
    public synchronized boolean isFull() {
        return count == buffer.length;
    }

    /**
     * Method: isEmpty()
     * Function: return true if num of work items equals zero
     * @return
     */
    public synchronized boolean isEmpty() {
        return count == 0;
    }

    /**
     * Method: doPut()
     * Function: put work item into buffer at given index
     * @param object
     */
    protected synchronized void doPut(WorkItem object) {
        buffer[putpos] = object;
        if (++putpos == buffer.length) {
            putpos = 0;
        }
        ++count;
    }

    /**
     * Method: doTake()
     * Function: get work item into buffer at given index
     * @param object
     */
    protected synchronized WorkItem doTake() {
    	WorkItem element = (WorkItem) buffer[takepos];
        if (++takepos == buffer.length) {
            takepos = 0;
        }
        --count;
        return element;
    }
}
