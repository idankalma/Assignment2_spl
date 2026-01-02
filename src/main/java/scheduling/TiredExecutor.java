package scheduling;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        workers = new TiredThread[numThreads];

        Random rnd = new Random();

        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = 0.5 + rnd.nextDouble(); // [0.5,1.5)
            TiredThread worker = new TiredThread(i, fatigueFactor);
            workers[i] = worker;
            idleMinHeap.add(worker);
            worker.start();
        }
    }

    public void submit(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("task cannot be null");
        }

        // Take least-fatigued IDLE worker (blocks if none are idle).
        final TiredThread worker;
        try {
            worker = idleMinHeap.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for an idle worker", e);
        }

        inFlight.incrementAndGet();

        Runnable wrapped = () -> {
            try {
                task.run();
            } finally {
                // return worker to idle heap (fatigue order recomputed dynamically)
                idleMinHeap.add(worker);

                int left = inFlight.decrementAndGet();
                if (left == 0) {
                    synchronized (TiredExecutor.this) {
                        TiredExecutor.this.notifyAll();
                    }
                }
            }
        };

        try {
            worker.newTask(wrapped);
        } catch (IllegalStateException ex) {
            // Roll back: we didn't actually schedule the task
            idleMinHeap.add(worker);

            int left = inFlight.decrementAndGet();
            if (left == 0) {
                synchronized (this) {
                    notifyAll();
                }
            }
            throw ex;
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        for (Runnable r : tasks){
            submit(r);
        }
        synchronized (TiredExecutor.this) {
            while (inFlight.get() > 0) {
                try {
                    TiredExecutor.this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        // TODO
        for(TiredThread worker : workers){
            worker.shutdown();
        }

        for(TiredThread worker : workers){
            worker.join();
        }
    }


    public synchronized String getWorkerReport() {
        StringBuilder ret = new StringBuilder();

        for (TiredThread worker : workers) {

            double fatigueFactor = worker.getFatigue();

            double fatigue = fatigueFactor * worker.getTimeUsed();

            ret.append(String.format(
                    "Worker %d: Time Used = %d ns, Time Idle = %d ns, Fatigue = %.2f (Fatigue = %.5f × %d)\n",
                    worker.getWorkerId(),
                    worker.getTimeUsed(),
                    worker.getTimeIdle(),
                    fatigue,
                    fatigueFactor,
                    worker.getTimeUsed()
            ));
        }

        return ret.toString();
    }
}
