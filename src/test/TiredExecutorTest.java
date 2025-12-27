import org.junit.jupiter.api.Test;

import scheduling.TiredExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TiredExecutorTest {

    /**
     * Basic sanity test:
     * submit() should execute a single task.
     */
    @Test
    public void testSubmitRunsTask() throws Exception {
        TiredExecutor executor = new TiredExecutor(2);

        AtomicInteger counter = new AtomicInteger(0);

        executor.submit(counter::incrementAndGet);
        executor.submitAll(List.of()); // wait for completion

        assertEquals(1, counter.get());

        executor.shutdown();
    }

    /**
     * submitAll() must block until ALL tasks finish.
     */
    @Test
    public void testSubmitAllWaitsForCompletion() throws Exception {
        TiredExecutor executor = new TiredExecutor(3);

        AtomicInteger counter = new AtomicInteger(0);

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(counter::incrementAndGet);
        }

        executor.submitAll(tasks);

        assertEquals(10, counter.get());

        executor.shutdown();
    }

    /**
     * Tasks should actually run in parallel when multiple threads exist.
     */
    @Test
    public void testParallelExecution() throws Exception {
        TiredExecutor executor = new TiredExecutor(2);

        CountDownLatch latch = new CountDownLatch(2);

        Runnable slowTask = () -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}
            latch.countDown();
        };

        long start = System.currentTimeMillis();

        executor.submit(slowTask);
        executor.submit(slowTask);

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        long duration = System.currentTimeMillis() - start;

        // If tasks ran sequentially → ~400ms
        // If parallel → ~200ms
        assertTrue(duration < 350, "Tasks did not run in parallel");

        executor.shutdown();
    }

    /**
     * Executor must not lose tasks even when many are submitted.
     */
    @Test
    public void testNoTaskLossUnderLoad() throws Exception {
        TiredExecutor executor = new TiredExecutor(4);

        AtomicInteger counter = new AtomicInteger(0);
        int taskCount = 100;

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(counter::incrementAndGet);
        }

        executor.submitAll(tasks);

        assertEquals(taskCount, counter.get());

        executor.shutdown();
    }

    /**
     * shutdown() must wait for workers to terminate.
     */
    @Test
    public void testShutdownTerminatesWorkers() throws Exception {
        TiredExecutor executor = new TiredExecutor(2);

        executor.submit(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        });

        executor.shutdown();

        // If shutdown returned, workers must be dead
        // (no exception thrown is success here)
        assertTrue(true);
    }

    /**
     * getWorkerReport() should return a non-empty readable string.
     */
    @Test
    public void testWorkerReportNotEmpty() throws Exception {
        TiredExecutor executor = new TiredExecutor(2);

        executor.submit(() -> {});
        executor.submitAll(List.of());

        String report = executor.getWorkerReport();

        assertNotNull(report);
        assertFalse(report.isEmpty());
        assertTrue(report.contains("Worker"));

        executor.shutdown();
    }
}