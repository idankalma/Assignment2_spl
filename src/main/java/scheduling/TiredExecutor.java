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
        // TODO
        TiredThread worker;
        synchronized (this) {
            while (idleMinHeap.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            worker = idleMinHeap.poll();
            inFlight.incrementAndGet();
        }
        Runnable wrappedTask = () -> {
            try{
                task.run();
            }
            finally {
                synchronized (TiredExecutor.this){
                    idleMinHeap.add(worker);
                    inFlight.decrementAndGet();
                    TiredExecutor.this.notifyAll();
                }
            }
        };
        worker.newTask(wrappedTask);
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        for (Runnable r : tasks){
            submit(r);
        }
        synchronized (this) {
            while (inFlight.get() > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
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
        StringBuilder sb = new StringBuilder();
        for (TiredThread w : workers) {
            sb.append("Worker ").append(w.getWorkerId())
                    .append(": busy=").append(w.isBusy())
                    .append(", timeUsed=").append(w.getTimeUsed())
                    .append(", timeIdle=").append(w.getTimeIdle())
                    .append(", fatigue=").append(String.format("%.2f", w.getFatigue()))
                    .append("\n");
        }
        return sb.toString();
    }
}
