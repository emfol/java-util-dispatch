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

        try {

            System.out.println("\n# TEST A: Initiated...\n");

            System.out.println("# TEST A: Dispatching Task B (Aragorn)...");
            this.dispatchQueue.dispatch(new TaskB("Aragorn"));

            System.out.println("# TEST A: Dispatching Task A (Gandalf)...");
            this.dispatchQueue.dispatch(new TaskA("Gandalf", dispatchQueue));

            System.out.println("# TEST A: Dispatching Task A (Legolas)...");
            this.dispatchQueue.dispatch(new TaskA("Legolas", null));

            System.out.println("# TEST A: Dispatching Task B (Frodo)...");
            this.dispatchQueue.dispatch(new TaskB("Frodo"));

            System.out.println("# TEST A: Waiting for 20 seconds...");
            Thread.sleep(20000);

            System.out.println("# TEST A: Dispatching Task A (Bilbo)...");
            this.dispatchQueue.dispatch(new TaskA("Bilbo", dispatchQueue));

            System.out.println("# TEST A: Waiting for 20 seconds again...");
            Thread.sleep(20000);
            System.out.println("# TEST A: Done...\n\n");

        } catch (InterruptedException e) {}

    }

    @Test
    public void dispatchTasksStopAndResumeQueue() {

        try {

            System.out.println("\n# TEST B: Initiated...\n");

            System.out.println("# TEST B: Dispatching Task B (Aragorn)...");
            this.dispatchQueue.dispatch(new TaskB("Aragorn"));

            Thread.yield();

            System.out.println("# TEST B: Dispatching Task B (Frodo)...");
            this.dispatchQueue.dispatch(new TaskB("Frodo"));

            System.out.println("# TEST B: Stopping dispatch queue...");
            this.dispatchQueue.stop();

            System.out.println("# TEST B: Waiting for 15 seconds...");
            Thread.sleep(15000);

            System.out.println("# TEST B: Dispatching Task A (Legolas)...");
            this.dispatchQueue.dispatch(new TaskA("Legolas", null));

            System.out.println("# TEST B: Restarting dispatch queue...");
            (new Thread(this.dispatchQueue)).start();

            System.out.println("# TEST B: Waiting for 10 seconds...");
            Thread.sleep(10000);

            System.out.println("# TEST B: Done...\n\n");

        } catch (InterruptedException e) {}

    }

    @Test
    public void dispatchQueuesShouldBeThreadSafe() {

        Thread thread;
        long ti, tf;

        try {

            System.out.println("\n# TEST C: Initiated...\n");

            System.out.println("# TEST C: Trying to re-run dispatch queue...");
            this.dispatchQueue.run();
            System.out.println("# TEST C: OK...");

            System.out.println("# TEST C: Creating a second thread for dispatch queue...");
            thread = new Thread(this.dispatchQueue);
            ti = System.nanoTime();
            thread.start();
            System.out.println("# TEST C: Waiting for second thread to die...");
            thread.join();
            tf = System.nanoTime();
            System.out.printf("# TEST C: Second thread is dead within %d ns...\n", tf - ti);
            thread = null;

            System.out.println("# TEST C: Dispatching Task A (Legolas)...");
            this.dispatchQueue.dispatch(new TaskA("Legolas", null));

            System.out.println("# TEST C: Waiting for 15 seconds...");
            Thread.sleep(15000);
            System.out.println("# TEST C: Done...\n\n");

        } catch (InterruptedException e) {}


    }

    /*
     * Task A
     */

    static class TaskA extends Object implements Runnable {

        private final String name;
        private final DispatchQueue dispatchQueue;

        public TaskA(String name, DispatchQueue dispatchQueue) {
            super();
            this.name = name;
            this.dispatchQueue = dispatchQueue;
        }

        public void run() {

            int delay;

            try {

                System.out.printf("TASK A (%s): Initiated...\n", this.name);

                delay = random.nextInt(5000);
                System.out.printf("TASK A (%s): Single delay of %d ms...\n", this.name, delay);
                Thread.sleep(delay);

                if (this.dispatchQueue != null) {
                    System.out.printf("TASK A (%s): Dispatching a new Task B...\n", this.name);
                    this.dispatchQueue.dispatch(new TaskB(
                            String.format("Child of Task A (%s)", this.name)
                        )
                    );
                }

                System.out.printf("TASK A (%s): Done...\n\n", this.name);

            } catch (InterruptedException e) {}

        }

    }

    /*
     * Task B
     */

    static class TaskB extends Object implements Runnable {

        private final String name;

        public TaskB(String name) {
            super();
            this.name = name;
        }

        public void run() {

            int delay;

            try {

                System.out.printf("TASK B (%s): Initiated...\n", this.name);

                delay = random.nextInt(3000);
                System.out.printf("TASK B (%s): First delay of %d ms...\n", this.name, delay);
                Thread.sleep(delay);

                delay = random.nextInt(2000);
                System.out.printf("TASK B (%s): Second delay of %d ms...\n", this.name, delay);
                Thread.sleep(delay);

                System.out.printf("TASK B (%s): Done...\n\n", this.name);

            } catch (InterruptedException e) {}


        }
    }

}

