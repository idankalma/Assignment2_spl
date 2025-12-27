import org.junit.jupiter.api.Test;
import scheduling.TiredThread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TiredThreadTest {

    /* Testing Basic functionality */

    @Test
    public void testExecutesSingleTask() throws Exception {
        // Verifies that a worker can execute a single task successfully

        CountDownLatch latch = new CountDownLatch(1);

        TiredThread worker = new TiredThread(0, 1.0);
        worker.start();

        // Task simply signals completion
        worker.newTask(latch::countDown);

        // Ensure the task actually ran
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        worker.shutdown();
        worker.join();
    }

    /* Testing Busy and idle state transitions */

    @Test
    public void testBusyTrueDuringTaskAndFalseAfter() throws Exception {
        // Ensures that the busy flag is:
        // - true while a task is running
        // - false after the task finishes

        CountDownLatch started = new CountDownLatch(1);

        TiredThread worker = new TiredThread(1, 1.0);
        worker.start();

        worker.newTask(() -> {
            started.countDown();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        });

        started.await();

        // Worker should be marked busy during execution
        assertTrue(worker.isBusy());

        // Wait for task to finish
        Thread.sleep(150);

        // Worker should no longer be busy
        assertFalse(worker.isBusy());

        worker.shutdown();
        worker.join();
    }

    @Test
    public void testIdleTimeIncreasesWhenIdle() throws Exception {
        // Verifies that idle time is accumulated when the worker is not executing tasks

        TiredThread worker = new TiredThread(2, 1.0);
        worker.start();

        long idleBefore = worker.getTimeIdle();

        Thread.sleep(50);

        long idleAfter = worker.getTimeIdle();

        // Idle time should monotonically increase
        assertTrue(idleAfter >= idleBefore);

        worker.shutdown();
        worker.join();
    }

    /* Testing newTask contract */

    @Test
    public void testNewTaskWhileBusyThrows() throws Exception {
        // A worker must reject new tasks while it is already busy

        TiredThread worker = new TiredThread(3, 1.0);
        worker.start();

        worker.newTask(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}
        });

        Thread.sleep(50); // ensure task started

        assertThrows(IllegalStateException.class,
                () -> worker.newTask(() -> {}));

        worker.shutdown();
        worker.join();
    }

    @Test
    public void testNewTaskAfterShutdownThrows() throws Exception {
        // After shutdown, the worker must reject all new tasks

        TiredThread worker = new TiredThread(4, 1.0);
        worker.start();

        worker.shutdown();
        worker.join();

        assertThrows(IllegalStateException.class,
                () -> worker.newTask(() -> {}));
    }

    /* Testing Shutdown behavior */

    @Test
    public void testShutdownWhileIdleTerminatesThread() throws Exception {
        // If shutdown is called while idle, the thread should exit cleanly

        TiredThread worker = new TiredThread(5, 1.0);
        worker.start();

        worker.shutdown();
        worker.join(500);

        assertFalse(worker.isAlive());
    }

    @Test
    public void testShutdownDuringTaskTerminatesAfterTask() throws Exception {
        // If shutdown is called during execution, the worker should:
        // - finish the current task
        // - then terminate

        CountDownLatch started = new CountDownLatch(1);

        TiredThread worker = new TiredThread(6, 1.0);
        worker.start();

        worker.newTask(() -> {
            started.countDown();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        });

        started.await();

        worker.shutdown();
        worker.join(500);

        assertFalse(worker.isAlive());
    }

    /* Testing Single-task execution guarantee */

    @Test
    public void testOnlyOneTaskRunsAtATime() throws Exception {
        // Verifies that a worker never executes more than one task concurrently

        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        TiredThread worker = new TiredThread(7, 1.0);
        worker.start();

        worker.newTask(() -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            latch.countDown();
        });

        latch.await();

        // Only one execution should have occurred
        assertEquals(1, counter.get());

        worker.shutdown();
        worker.join();
    }

    /* Testing Fatigue and comparison */

    @Test
    public void testFatigueIncreasesAfterWork() throws Exception {
        // Fatigue must increase after performing work

        TiredThread worker = new TiredThread(8, 1.0);
        worker.start();

        double fatigueBefore = worker.getFatigue();

        worker.newTask(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        });

        Thread.sleep(100);

        double fatigueAfter = worker.getFatigue();

        assertTrue(fatigueAfter > fatigueBefore);

        worker.shutdown();
        worker.join();
    }

    @Test
    public void testCompareToReflectsWorkload() throws Exception {
        // A worker that performed more work should be considered "more fatigued"

        TiredThread w1 = new TiredThread(9, 1.0);
        TiredThread w2 = new TiredThread(10, 1.0);

        w1.start();
        w2.start();

        w1.newTask(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        });

        Thread.sleep(150);

        assertTrue(w1.compareTo(w2) > 0);

        w1.shutdown();
        w2.shutdown();
        w1.join();
        w2.join();
    }

    /* Testing Worker ID */
    @Test
    public void testGetWorkerId() {
        // Verifies that getWorkerId() returns the correct ID assigned at construction
        TiredThread worker = new TiredThread(42, 1.0);
        assertEquals(42, worker.getWorkerId());
    }

    /* Testing Time used */
    @Test
    public void testGetTimeUsedIncreasesAfterTask() throws Exception {
        // Verifies that getTimeUsed() increases after executing a task
        TiredThread worker = new TiredThread(0, 1.0);
        worker.start();

        long timeBefore = worker.getTimeUsed();

        worker.newTask(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
        });

        // Wait for the task to finish
        Thread.sleep(100);

        long timeAfter = worker.getTimeUsed();

        assertTrue(timeAfter > timeBefore);

        worker.shutdown();
        worker.join();
    }
}