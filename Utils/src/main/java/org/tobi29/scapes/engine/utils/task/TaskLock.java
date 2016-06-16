package org.tobi29.scapes.engine.utils.task;

import java.util.concurrent.atomic.AtomicInteger;

public class TaskLock {
    private final AtomicInteger count = new AtomicInteger();

    public void increment() {
        count.incrementAndGet();
    }

    @SuppressWarnings("NakedNotify")
    public void decrement() {
        if (count.decrementAndGet() < 0) {
            throw new IllegalStateException("Negative task count");
        }
        synchronized (count) {
            count.notifyAll();
        }
    }

    public void lock() {
        synchronized (count) {
            while (count.get() > 0) {
                try {
                    count.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
