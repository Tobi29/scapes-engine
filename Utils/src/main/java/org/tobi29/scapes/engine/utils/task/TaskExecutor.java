/*
 * Copyright 2012-2015 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tobi29.scapes.engine.utils.task;

import java8.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.Crashable;
import org.tobi29.scapes.engine.utils.Streams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskExecutor {
    private final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);
    private final List<TaskWorker> tasks = new ArrayList<>();
    private final AtomicInteger threadCount = new AtomicInteger();
    private final ThreadPoolExecutor taskPool;
    private final Crashable crashHandler;
    private final String name;
    private final boolean root;

    public TaskExecutor(TaskExecutor parent, String name) {
        crashHandler = parent.crashHandler;
        this.name = parent.name + name + '-';
        root = false;
        taskPool = parent.taskPool;
    }

    public TaskExecutor(Crashable crashHandler, String name) {
        this.crashHandler = crashHandler;
        this.name = name + '-';
        root = true;
        int taskPoolSize = Runtime.getRuntime().availableProcessors();
        taskPool = new ThreadPoolExecutor(taskPoolSize, taskPoolSize, 60L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new PriorityThreadFactory(Thread.MIN_PRIORITY));
    }

    public void tick() {
        long time = System.currentTimeMillis();
        int i = 0;
        while (i < tasks.size()) {
            TaskWorker task;
            synchronized (tasks) {
                if (i >= tasks.size()) {
                    break;
                }
                task = tasks.get(i);
            }
            if (time >= task.delay) {
                try {
                    if (task.async) {
                        runTask(() -> {
                            long delay = task.task.getAsLong();
                            if (delay < 0) {
                                task.stopped = true;
                            } else {
                                task.delay = time + delay;
                            }
                        }, task.name);
                    } else {
                        long delay = task.task.getAsLong();
                        if (delay < 0) {
                            task.stopped = true;
                        } else {
                            task.delay = time + delay;
                        }
                    }
                } catch (Throwable e) {
                    task.stopped = true;
                    crashHandler.crash(e);
                }
            }
            if (task.stopped) {
                synchronized (tasks) {
                    tasks.remove(i);
                }
            } else {
                i++;
            }
        }
    }

    public Joiner runTask(ASyncTask task, String name) {
        return runTask(task, name, Priority.LOW);
    }

    public Joiner runTask(ASyncTask task, String name, Priority priority) {
        threadCount.incrementAndGet();
        ThreadWrapper wrapper = new ThreadWrapper(task);
        Thread thread = new Thread(wrapper);
        thread.setName(this.name + name);
        thread.setPriority(priority.priority);
        thread.start();
        return wrapper.joinable.joiner();
    }

    public void runTask(Runnable task, String name) {
        taskPool.execute(() -> {
            long time = System.nanoTime();
            task.run();
            time = System.nanoTime() - time;
            if (time > 10000000000L) {
                LOGGER.warn("Task took {} seconds to complete: {}",
                        time / 1000000000, name);
            }
        });
    }

    public void addTask(Runnable task, String name) {
        addTask(task, name, 0);
    }

    public void addTask(Runnable task, String name, long delay) {
        addTask(task, name, delay, false);
    }

    public void addTask(Runnable task, String name, long delay, boolean async) {
        addTask(() -> {
            task.run();
            return -1;
        }, name, delay, async);
    }

    public void addTask(LongSupplier task, String name) {
        addTask(task, name, 0);
    }

    public void addTask(LongSupplier task, String name, long delay) {
        addTask(task, name, delay, false);
    }

    public void addTask(LongSupplier task, String name, long delay,
            boolean async) {
        delay += System.currentTimeMillis();
        synchronized (tasks) {
            tasks.add(new TaskWorker(task, name, delay, async));
        }
    }

    public void shutdown() {
        if (root) {
            taskPool.shutdown();
            try {
                if (!taskPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    Streams.forEach(Thread.getAllStackTraces().keySet(),
                            System.out::println);
                }
            } catch (InterruptedException e) {
            }
            synchronized (threadCount) {
                while (threadCount.get() > 0) {
                    try {
                        threadCount.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public enum Priority {
        HIGH(Thread.MAX_PRIORITY),
        MEDIUM(Thread.NORM_PRIORITY),
        LOW(Thread.MIN_PRIORITY);
        private final int priority;

        Priority(int priority) {
            this.priority = priority;
        }
    }

    public interface ASyncTask {
        void run(Joiner.Joinable joiner) throws Exception;
    }

    private static final class TaskWorker {
        private final LongSupplier task;
        private final String name;
        private final boolean async;
        private long delay;
        private boolean stopped;

        private TaskWorker(LongSupplier task, String name, long delay,
                boolean async) {
            this.task = task;
            this.name = name;
            this.delay = delay;
            this.async = async;
        }
    }

    private class ThreadWrapper implements Runnable {
        private final ASyncTask task;
        private final Joiner.Joinable joinable;

        protected ThreadWrapper(ASyncTask task) {
            this.task = task;
            joinable = new Joiner.Joinable();
        }

        @SuppressWarnings("OverlyBroadCatchBlock")
        @Override
        public void run() {
            try {
                task.run(joinable);
            } catch (Throwable e) { // Yes this catches ThreadDeath, so don't use it
                crashHandler.crash(e);
            } finally {
                joinable.join();
                synchronized (threadCount) {
                    threadCount.decrementAndGet();
                    threadCount.notifyAll();
                }
            }
        }
    }

    private class PriorityThreadFactory implements ThreadFactory {
        private final AtomicInteger id = new AtomicInteger(1);
        private final int priority;
        private final ThreadGroup group;

        protected PriorityThreadFactory(int priority) {
            this.priority = priority;
            group = Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread =
                    new Thread(group, r, name + id.getAndIncrement(), 0);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            thread.setPriority(priority);
            return thread;
        }
    }
}
