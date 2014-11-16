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

    public DispatchQueueOrderingTest() {
        super();
        this.dispatchQueue = DispatchQueue.createDispatchQueue();
        this.baseList = new int[MAX_NUMBERS];
        this.resultList = new int[MAX_NUMBERS];
        this.nextResultListItem = 0;
        this.shouldWait = true;
        this.init();
    }

    private void init() {
        Random generator = new Random();
        for (int i = 0, limit = this.baseList.length; i < limit; ++i) {
            this.baseList[i] = generator.nextInt();;
        }
    }

    public void push(int number) {
        // no synchronization should be carried out here
        if (this.nextResultListItem < this.resultList.length) {
            this.resultList[this.nextResultListItem++] = number;
        }
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
        int i, limit = MAX_NUMBERS;

        System.out.println("\nEnqueueing tasks...");
        for (i = 0; i < limit; ++i) {
            p = new Populator(this, this.baseList[i]);
            this.dispatchQueue.dispatch(p);
        }

        System.out.println("Scheduling dismisser...");
        // dismisser
        p = new Populator(this, 0, true);
        this.dispatchQueue.dispatch(p);

        System.out.println("\nWaiting on tasks...");
        synchronized (this) {
            while (this.shouldWait) {
                try {
                    this.wait();
                } catch (InterruptedException e) {}
            }
        }

        System.out.println("\nVerifying...");
        for (i = 0; i < limit; i++) {
            assertEquals(
                "Item Comparison Error!",
                this.baseList[i],
                this.resultList[i]
            );
        }

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

        public Populator(DispatchQueueOrderingTest test, int number) {
            this(test, number, false);
        }

        @Override
        public void run() {
            if (this.dismiss) {
                this.test.dismiss();
                return;
            }
            this.test.push(this.number);
        }


    }

}
