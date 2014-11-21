package com.duckwriter.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import com.duckwriter.util.dispatch.DispatchQueue;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DispatchQueueGeneralTest extends Object {

    /*
     * Static Fields
     */

    private static final Random generator;
    private static final String[] taskNames;

    static {
        generator = new Random();
        taskNames = new String[] {
            "Aragorn",
            "Gandalf",
            "Legolas",
            "Frodo",
            "Bilbo",
            "Faramir",
            "Olwë",
            "Thorin",
            "Ghân-buri-Ghân"
        };
    }

    /*
     * Instance Members
     */

    private final List<String> executedTasks;
    private final Object semaphore;
    private boolean condition;

    /*
     * Constructors
     */

    public DispatchQueueGeneralTest() {
        super();
        this.executedTasks = new ArrayList<String>();
        this.semaphore = new Object();
        this.condition = false;
    }

    /*
     * Priavte Methods
     */

    private void waitOnCondition() {
        synchronized (this.semaphore) {
            this.condition = false;
            while (!this.condition) {
                try {
                    this.semaphore.wait();
                } catch (InterruptedException e) { }
            }
        }
    }

    private String formatDelay(long nsDelay) {
        return String.format("%.2f ms", nsDelay / 1000000.0);
    }

    /*
     * Public Methods
     */

    public void addToExecuted(final String taskName) {
        this.executedTasks.add(taskName);
    }

    public void signalCondition() {
        synchronized (this.semaphore) {
            this.condition = true;
            this.semaphore.notifyAll();
        }
    }

    @Test
    public void GeneralTest() {

        final DispatchQueue queue = DispatchQueue.createDispatchQueue();
        Thread dumbThread;
        long t0, t1;
        int i, executedCount;

        System.out.println("\nStarting...");

        // fisrt task...
        System.out.println("Dispatching first task and waiting...");
        t0 = System.nanoTime();
        queue.dispatch(new TaskA(taskNames[0], true));
        this.waitOnCondition();
        t1 = System.nanoTime();
        System.out.printf("OK (%s)...\n", this.formatDelay(t1 - t0));

        // test of secondary thread
        System.out.print("Trying to run dispatch queue on a secondary thread... ");
        t0 = System.nanoTime();
        dumbThread = new Thread(queue);
        dumbThread.start();
        try {
            dumbThread.join();
        } catch (InterruptedException e) {}
        // test of same thread run
        queue.run();
        t1 = System.nanoTime();
        System.out.println(this.formatDelay(t1 - t0));

        // dispatch remaining tasks and wait...
        System.out.println("Dispatching remaining tasks and waiting...");
        t0 = System.nanoTime();

        queue.dispatch(new TaskA(taskNames[1], false));
        queue.dispatch(new TaskB(taskNames[2], taskNames[3], queue));
        this.waitOnCondition();
        queue.dispatch(new TaskA(taskNames[4], false));
        queue.dispatch(new TaskB(taskNames[5], taskNames[6], queue));
        this.waitOnCondition();
        queue.dispatch(new TaskA(taskNames[7], false));
        queue.dispatch(new TaskA(taskNames[8], true));
        this.waitOnCondition();

        t1 = System.nanoTime();
        System.out.printf("OK (%s)...\n", this.formatDelay(t1 - t0));

        // verify...
        System.out.print("Verifying... ");
        t0 = System.nanoTime();
        executedCount = this.executedTasks.size();
        assertEquals(
            "List Size Inconsistency",
            taskNames.length,
            executedCount
        );
        for (i = 0; i < executedCount; ++i) {
            assertEquals(
                "Execution Order Error",
                taskNames[i],
                this.executedTasks.get(i)
            );
        }
        t1 = System.nanoTime();
        System.out.println(this.formatDelay(t1 - t0));

        // stopping queue...
        queue.stop();

        // Done!
        System.out.println("Done...\n");

    }

    /*
     * Static Classes
     */

    class TaskA extends Object implements Runnable {

        private final String name;
        private final boolean signal;

        TaskA(String name, boolean signal) {
            super();
            this.name = name;
            this.signal = signal;
        }

        @Override
        public void run() {

            int delay = 1000 + generator.nextInt(1000);

            // presentation...
            System.out.printf(
                "$ Task A # %s [%c] (%d ms)...\n",
                this.name,
                this.signal ? '+' : '-',
                delay
            );

            // from outer instance
            addToExecuted(this.name);

            // delay
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {}

            // signal
            if (this.signal) {
                // from outer class instance
                signalCondition();
            }

        }

    }

    class TaskB extends Object implements Runnable {

        private final String name;
        private final String childName;
        private final DispatchQueue queue;

        TaskB(String name, String childName, DispatchQueue queue) {
            super();
            this.name = name;
            this.childName = childName;
            this.queue = queue;
        }

        @Override
        public void run() {

            int delay = 1000 + generator.nextInt(2000);

            // presentation...
            System.out.printf("$ Task B # %s > %s (%d ms)...\n", this.name, this.childName, delay);

            // this code is being run by dispatch queue task... is it reentrant?
            this.queue.run();

            // if execution reaches this point it is reentrant!
            this.queue.dispatch(new TaskA(this.childName, false));

            // from outer instance
            addToExecuted(this.name);

            // delay
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {}

            // from outer class instance
            signalCondition();

        }

    }

}
