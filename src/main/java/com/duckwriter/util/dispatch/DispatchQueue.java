package com.duckwriter.util.dispatch;

public final class DispatchQueue extends Object implements Runnable {

    /*
     * Private Instance Variables
     */

    private final DispatchQueueItem dispatchQueue;
    private boolean shouldRun;
    private boolean isRunning;

    /*
     * Constructors
     */

    public DispatchQueue() {
        super();
        this.dispatchQueue = new DispatchQueueItem(null);
        this.shouldRun = true;
        this.isRunning = false;
    }

    /*
     * Public Class Methods
     */

    public static DispatchQueue createDispatchQueue() {

        DispatchQueue dq;
        Thread dt;

        dq = new DispatchQueue();
        dt = new Thread(dq);
        dt.setPriority(Thread.MAX_PRIORITY);
        dt.start();

        // try to release cpu for new thread
        Thread.yield();

        return dq;

    }

    /*
     * Private Methods
     */

    private void enqueue(DispatchQueueItem newItem) {
        DispatchQueueItem item = this.dispatchQueue;
        while (item.next != null) {
            item = item.next;
        }
        item.next = newItem;
    }

    private DispatchQueueItem dequeue() {
        DispatchQueueItem item, queue = this.dispatchQueue;
        item = queue.next;
        if (item != null) {
            queue.next = item.next;
            item.next = null;
        }
        return item;
    }

    private void dispatchLoop() {

        DispatchQueueItem nextItem = null;
        boolean shouldRun = true;

        while (shouldRun) {
            synchronized (this) {
                while ((shouldRun = this.shouldRun)
                    && (nextItem = this.dequeue()) == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {}
                }
            }
            if (shouldRun && nextItem != null) {
                nextItem.task.run();
            }
        }

    }

    /*
     * Public Methods
     */

    public void dispatch(Runnable task) {

        DispatchQueueItem newItem;

        if (task == null) {
            throw new NullPointerException("Null Pointer for Task");
        }

        newItem = new DispatchQueueItem(task);
        synchronized (this) {
            this.enqueue(newItem);
            this.notifyAll();
        }

    }

    public void stop() {
        synchronized (this) {
            this.shouldRun = false;
            this.notifyAll();
        }
    }

    public void run() {

        boolean isNotRunning;

        synchronized (this) {
            isNotRunning = !this.isRunning;
            if (isNotRunning) {
                this.isRunning = true;
            }
        }

        if (isNotRunning) {
            // now it's ok to start running...
            this.dispatchLoop();
            // this point is only reached when stop method has been called...
            synchronized (this) {
                this.shouldRun = true;
                this.isRunning = false;
            }
        }

    }

}
