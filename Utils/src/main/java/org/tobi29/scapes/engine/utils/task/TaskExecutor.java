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
import org.tobi29.scapes.engine.utils.Crashable;
import org.tobi29.scapes.engine.utils.Streams;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskExecutor {
    private final List<TaskWorker> tasks = new ArrayList<>();
    private final Map<Priority, ThreadPoolExecutor> threadPools;
    private final Crashable crashHandler;
    private final String name;
    private final boolean root;

    public TaskExecutor(TaskExecutor parent, String name) {
        crashHandler = parent.crashHandler;
        this.name = parent.name + name + '-';
        root = false;
        threadPools = parent.threadPools;
    }

    public TaskExecutor(Crashable crashHandler, String name) {
        this.crashHandler = crashHandler;
        this.name = name + '-';
        root = true;
        threadPools = new EnumMap<>(Priority.class);
        Streams.of(Priority.values()).forEach(priority -> threadPools
                .put(priority, new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
                        TimeUnit.SECONDS, new SynchronousQueue<>(),
                        new PriorityThreadFactory(priority.priority))));
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
        ThreadWrapper thread = new ThreadWrapper(task, this.name + name);
        threadPools.get(priority).execute(thread);
        return thread.joinable.joiner();
    }

    public void runTask(Runnable task, String name) {
        runTask(task, name, Priority.LOW);
    }

    public void runTask(Runnable task, String name, Priority priority) {
        threadPools.get(priority).execute(() -> {
            Thread thread = Thread.currentThread();
            thread.setName(name);
            task.run();
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
            Streams.of(threadPools.values())
                    .forEach(ThreadPoolExecutor::shutdown);
            Streams.of(threadPools.values()).forEach(threadPool -> {
                try {
                    if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                        Streams.of(Thread.getAllStackTraces().keySet())
                                .forEach(System.out::println);
                    }
                } catch (InterruptedException e) {
                }
            });
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

    private static class TaskWorker {
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
        private final String name;
        private final Joiner.Joinable joinable;

        private ThreadWrapper(ASyncTask task, String name) {
            this.task = task;
            this.name = name;
            joinable = new Joiner.Joinable();
        }

        @SuppressWarnings("OverlyBroadCatchBlock")
        @Override
        public void run() {
            try {
                Thread thread = Thread.currentThread();
                thread.setName(name);
                task.run(joinable);
            } catch (Throwable e) { // Yes this catches ThreadDeath, so don't use it
                crashHandler.crash(e);
            } finally {
                joinable.join();
            }
        }
    }

    private class PriorityThreadFactory implements ThreadFactory {
        private final AtomicInteger id = new AtomicInteger(1);
        private final int priority;
        private final ThreadGroup group;

        private PriorityThreadFactory(int priority) {
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
