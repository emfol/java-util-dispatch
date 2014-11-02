package com.duckwriter.util;

import java.util.Random;

import com.duckwriter.util.dispatch.DispatchQueue;
import org.junit.Test;

public class DispatchQueueTest extends Object {

    public static final Random random;

    static {
        random = new Random();
    }

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

        System.out.println("TESTER: Initiated...");

        dispatchQueue.dispatch(new TaskB("TESTER"));
        dispatchQueue.dispatch(new TaskA(null));
        dispatchQueue.dispatch(new TaskA(dispatchQueue));
        dispatchQueue.dispatch(new TaskB("TESTER"));

        System.out.println("TESTER: Done...");

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
            Thread.sleep(delay);

            if (this.dispatchQueue != null) {
                System.out.println("TASK A: Dispatching a Task B...\n");
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
            Thread.sleep(delay);

            delay = random.nextInt(2000);
            System.out.printf("TASK B: This second part will take about %d ms to complete...\n", delay);
            Thread.sleep(delay);

            System.out.println("TASK B: Done...\n");

        }
    }

}

