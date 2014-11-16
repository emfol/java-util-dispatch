package com.duckwriter.util;

import java.util.Random;

import com.duckwriter.util.dispatch.DispatchQueue;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DispatchQueueOrderingTest extends Object {

    private static final int MAX_NUMBERS = 4096;

    private final DispatchQueue dispatchQueue;
    private final int[] baseList;
    private final int[] resultList;
    private int nextResultListItem;
    private boolean shouldWait;

    /*
     * Constructors
     */

    public DispatchQueueOrderingTest() {
        super();
        this.dispatchQueue = DispatchQueue.createDispatchQueue();
        this.baseList = new int[MAX_NUMBERS];
        this.resultList = new int[MAX_NUMBERS];
        this.nextResultListItem = 0;
        this.shouldWait = true;
        this.init();
    }

    /*
     * Private Methods
     */

    private void init() {
        Random generator = new Random();
        for (int i = 0, limit = this.baseList.length; i < limit; ++i) {
            this.baseList[i] = generator.nextInt();
        }
    }

    private String formatDelay(long nsDelay) {
        return String.format("%.2f ms", nsDelay / 1000000.0);
    }

    /*
     * Public Methods
     */

    public void push(int number) {
        // no synchronization should be done here
        // also, no array boundary checking...
        this.resultList[this.nextResultListItem++] = number;
    }

    public void dismiss() {
        synchronized (this) {
            this.shouldWait = false;
            this.notifyAll();
        }
    }

    @Test
    public void shouldPopulateArrayInCorrectOrder() {

        Populator p;
        long t0, t1;
        int i, last = this.baseList.length - 1;

        System.out.print("\nEnqueueing tasks... ");
        t0 = System.nanoTime();
        for (i = 0; i <= last; ++i) {
            p = new Populator(
                this,
                this.baseList[i],
                i == last
            );
            this.dispatchQueue.dispatch(p);
        }
        t1 = System.nanoTime();
        System.out.println(this.formatDelay(t1 - t0));

        System.out.print("Waiting for tasks to finish... ");
        t0 = System.nanoTime();
        synchronized (this) {
            while (this.shouldWait) {
                try {
                    this.wait();
                } catch (InterruptedException e) {}
            }
        }
        t1 = System.nanoTime();
        System.out.println(this.formatDelay(t1 - t0));

        System.out.print("Verifying... ");
        t0 = System.nanoTime();
        for (i = 0; i <= last; i++) {
            assertEquals(
                "Item Comparison Error!",
                this.baseList[i],
                this.resultList[i]
            );
        }
        t1 = System.nanoTime();
        System.out.println(this.formatDelay(t1 - t0));

        // Done!
        System.out.println("Done!");

    }

    /*
     * Static Classes
     */

    static class Populator extends Object implements Runnable {

        private final DispatchQueueOrderingTest test;
        private final int number;
        private final boolean dismiss;

        public Populator(DispatchQueueOrderingTest test, int number, boolean dismiss) {
            super();
            this.test = test;
            this.number = number;
            this.dismiss = dismiss;
        }

        @Override
        public void run() {
            if (this.test != null) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {}
                this.test.push(this.number);
                if (this.dismiss) {
                    this.test.dismiss();
                }
            }
        }


    }

}
