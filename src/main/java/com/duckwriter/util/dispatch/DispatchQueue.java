package com.duckwriter.util.dispatch;

public final class DispatchQueue extends Object implements Runnable {

    /*
     * Private Instance Variables
     */

    private final Object semaphore;
    private Task next;
    private boolean isEnabled;
    private boolean isRunning;

    /*
     * Constructors
     */

    public DispatchQueue() {
        super();
        this.semaphore = new Object();
        this.next = null;
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

    private void dispatchLoop() {

        boolean shouldRun;
        Task task;

        do {

            // request synchronized access
            synchronized (this.semaphore) {
                while (this.isEnabled && this.next == null) {
                    try {
                        this.semaphore.wait();
                    } catch (InterruptedException e) {}
                }
                shouldRun = this.isEnabled;
                task = shouldRun ? this.next : null;
                if (task != null) {
                    this.next = task.next;
                    task.next = null;
                }
            }

            // execute runnable outside of any critical section
            if (task != null) {
                task.runnable.run();
            }

        } while (shouldRun);

    }

    /*
     * Public Methods
     */

    public void dispatch(Runnable runnable) {

        Task task, newTask;

        if (runnable != null) {

            // build new task
            newTask = new Task(runnable);

            // request synchronized access
            synchronized (this.semaphore) {
                if (this.next == null) {
                    this.next = newTask;
                } else {
                    task = this.next;
                    while (task.next != null) {
                        task = task.next;
                    }
                    task.next = newTask;
                }
                this.semaphore.notify();
            }

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
