package com.duckwriter.util;

import java.util.Random;

import com.duckwriter.util.dispatch.DispatchQueue;
import org.junit.Test;

public class DispatchQueueTest extends Object {

    public static final Random random;

    static {
        random = new Random();
    }

    private final DispatchQueue dispatchQueue;

    public DispatchQueueTest() {
        super();
        this.dispatchQueue = new DispatchQueue();
        (new Thread(dispatchQueue)).start();
    }

    @Override
    public void finalize() {
        this.dispatchQueue.stop();
    }

    @Test
    public void createAndDispatchTasks() {

        System.out.println("# TESTER: Initiated...\n");

        this.dispatchQueue.dispatch(new TaskB("TESTER"));
        this.dispatchQueue.dispatch(new TaskA(null));
        this.dispatchQueue.dispatch(new TaskA(dispatchQueue));
        this.dispatchQueue.dispatch(new TaskB("TESTER"));

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {}

        System.out.println("# TESTER: Done...\n");

    }

    static class TaskA extends Object implements Runnable {

        private final DispatchQueue dispatchQueue;

        public TaskA(DispatchQueue dispatchQueue) {
            super();
            this.dispatchQueue = dispatchQueue;
        }

        public void run() {

            int delay;

            System.out.println("TASK A: Initiated...");

            delay = random.nextInt(5000);
            System.out.printf("TASK A: Starting delay that will take about %d ms to complete...\n", delay);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {}

            if (this.dispatchQueue != null) {
                System.out.println("TASK A: Dispatching a Task B...");
                this.dispatchQueue.dispatch(new TaskB("TASK A"));
            }

            System.out.println("TASK A: Done...\n");

        }

    }

    static class TaskB extends Object implements Runnable {

        private final String initiator;

        public TaskB(String initiator) {
            super();
            this.initiator = initiator;
        }

        public void run() {

            int delay;

            System.out.printf("TASK B: Initiated (%s)...\n", this.initiator);

            delay = random.nextInt(3000);
            System.out.printf("TASK B: This first part will take about %d ms to complete...\n", delay);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {}

            delay = random.nextInt(2000);
            System.out.printf("TASK B: This second part will take about %d ms to complete...\n", delay);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {}

            System.out.println("TASK B: Done...\n");

        }
    }

}

