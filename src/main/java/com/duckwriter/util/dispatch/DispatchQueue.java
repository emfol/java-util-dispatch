package com.duckwriter.util.dispatch;

public final class DispatchQueue extends Object implements Runnable {

    /*
     * Private Instance Variables
     */

    private final Object semaphore;
    private final DispatchQueueItem dispatchQueue;
    private boolean isEnabled;
    private boolean isRunning;

    /*
     * Constructors
     */

    public DispatchQueue() {
        super();
        this.semaphore = new Object();
        this.dispatchQueue = new DispatchQueueItem(null);
        this.isEnabled = true;
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

    // this method assumes semaphore is locked
    private void enqueue(DispatchQueueItem newItem) {
        DispatchQueueItem item = this.dispatchQueue;
        while (item.next != null) {
            item = item.next;
        }
        item.next = newItem;
    }

    // this method assumes semaphore is locked
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
        boolean shouldRun;

        do {
            synchronized (this.semaphore) {
                while ((shouldRun = this.isEnabled)
                    && (nextItem = this.dequeue()) == null) {
                    try {
                        this.semaphore.wait();
                    } catch (InterruptedException e) {}
                }
            }
            if (shouldRun && nextItem != null) {
                nextItem.task.run();
            }
        } while (shouldRun);

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
        synchronized (this.semaphore) {
            this.enqueue(newItem);
            this.semaphore.notify();
        }

    }

    public void stop() {
        synchronized (this.semaphore) {
            this.isEnabled = false;
            this.semaphore.notify();
        }
    }

    @Override
    public void run() {

        boolean shouldRun;

        synchronized (this.semaphore) {
            shouldRun = this.isEnabled && !this.isRunning;
            if (shouldRun) {
                this.isRunning = true;
            }
        }

        if (shouldRun) {
            // now it's ok to start running...
            this.dispatchLoop();
            // this point is only reached when stop method has been called...
            synchronized (this.semaphore) {
                this.isEnabled = true;
                this.isRunning = false;
            }
        }

    }

}
