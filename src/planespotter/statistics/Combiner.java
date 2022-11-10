package planespotter.statistics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.controller.Controller;
import planespotter.model.ExceptionHandler;
import planespotter.throwables.NoMatchException;

import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Combiner<T> {

    protected final Vector<T> elements;

    protected final AtomicReference<T> result;

    @SafeVarargs
    protected Combiner(T... initElements) {
        this.elements = new Vector<>(List.of(initElements));
        this.result = new AtomicReference<>();
    }

    @NotNull
    public abstract Combiner<T> combineAll();

    public void add(@NotNull T element) {
        elements.add(element);
    }

    public int elements() {
        return elements.size();
    }

    public T getResult() {
        return result.get();
    }


    @Deprecated
    public static class FixedExecutor {

        private final Vector<Thread> threads;

        private final LinkedBlockingQueue<Runnable> workQueue;

        private boolean terminated;

        public FixedExecutor(int nThreads, @Nullable ExceptionHandler exceptionHandler) {
            this.threads = new Vector<>(nThreads);
            this.workQueue = new LinkedBlockingQueue<>();
            this.terminated = false;
            Thread thr;
            for (int i = 0; i < nThreads; i++) {
                thr = new Thread(worker());
                if (exceptionHandler != null) {
                    thr.setUncaughtExceptionHandler((t, e) -> exceptionHandler.handleException(e));
                }
                threads.add(thr);
                thr.start();
            }
        }

        public void invoke(@NotNull Runnable task) {
            workQueue.add(task);
        }

        public void invokeAll(@NotNull Collection<? extends Runnable> tasks) {
            workQueue.addAll(tasks);
        }

        public boolean isWorking() {
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    return true;
                }
            }
            return false;
        }

        public synchronized boolean shutdown() {
            workQueue.clear();
            for (Thread task : threads) {
                if (task != null && !task.isInterrupted()) {
                    task.interrupt();
                }
            }
            return terminated = true;
        }

        private Runnable worker() {
            return () -> {
                Runnable poll = null;
                while (!terminated) {
                    nextTask(poll);
                }
            };
        }

        private void nextTask(@Nullable Runnable pollRef) {
            if (workQueue.isEmpty()) {
                Thread.onSpinWait();
            } else {
                pollRef = workQueue.poll();
                if (pollRef != null) {
                    pollRef.run();
                }
            }
        }


    }
}
