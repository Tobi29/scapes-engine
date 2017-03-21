/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.utils.task

import mu.KLogging
import org.tobi29.scapes.engine.utils.Crashable
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.min
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class TaskExecutor {
    private val tasks = ConcurrentSkipListSet<TaskWorker>()
    private val taskLock = TaskLock()
    private val taskPool: ThreadPoolExecutor
    private val crashHandler: Crashable
    private val name: String
    private val root: Boolean
    private var eventLoop: Joiner?

    constructor(parent: TaskExecutor,
                name: String,
                wakeup: Joiner? = null) {
        crashHandler = parent.crashHandler
        this.name = parent.name + name + '-'
        this.eventLoop = wakeup
        root = false
        taskPool = parent.taskPool
    }

    constructor(crashHandler: Crashable,
                name: String,
                wakeup: Joiner? = null) {
        this.crashHandler = crashHandler
        this.name = name + '-'
        this.eventLoop = wakeup
        root = true
        val processors = Runtime.getRuntime().availableProcessors()
        val taskPoolSize = max(processors, 1)
        taskPool = ThreadPoolExecutor(taskPoolSize, taskPoolSize, 60L,
                TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>(),
                PriorityThreadFactory(Thread.NORM_PRIORITY))
    }

    fun eventLoop(joiner: Joiner) {
        eventLoop = joiner
    }

    fun tick(): Long {
        val time = System.currentTimeMillis()
        var earliestTask = Long.MAX_VALUE
        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            if (time >= task.delay) {
                try {
                    if (task.async) {
                        runTask({
                            val delay = task.task()
                            if (delay < 0) {
                                task.stopped = true
                            } else {
                                // TODO: Use task.delay instead of time
                                // FIXME: Warn about slow tasks
                                task.delay = time + delay
                                eventLoop?.wake()
                            }
                        }, task.name)
                    } else {
                        val delay = profilerSection(task.name) {
                            task.task()
                        }
                        if (delay < 0) {
                            task.stopped = true
                        } else {
                            // TODO: Use task.delay instead of time
                            // FIXME: Warn about slow tasks
                            task.delay = time + delay
                            earliestTask = min(earliestTask, delay)
                        }
                    }
                } catch (e: Throwable) {
                    task.stopped = true
                    crashHandler.crash(e)
                }
            } else {
                earliestTask = min(earliestTask, task.delay)
            }
            if (task.stopped) {
                iterator.remove()
            }
        }
        return max(earliestTask - System.currentTimeMillis(), 1)
    }

    fun runThread(task: (Joiner.ThreadJoinable) -> Unit,
                  name: String,
                  priority: Priority = Priority.LOW): ThreadJoiner {
        val joiner = Joiner.ThreadJoinable()
        val wrapper = ThreadWrapper(joiner) { task(joiner) }
        val thread = Thread(wrapper)
        thread.name = this.name + name
        thread.priority = priority.priority
        joiner.thread = thread
        thread.start()
        return joiner.joiner
    }

    fun <J : Joiner.Joinable> runThread(task: (J) -> Unit,
                                        name: String,
                                        priority: Priority = Priority.LOW,
                                        joiner: J): Joiner {
        val wrapper = ThreadWrapper(joiner) { task(joiner) }
        val thread = Thread(wrapper)
        thread.name = this.name + name
        thread.priority = priority.priority
        thread.start()
        return joiner.joiner
    }

    fun runTask(task: () -> Unit,
                name: String) {
        taskLock.increment()
        taskPool.execute {
            var time = System.nanoTime()
            try {
                profilerSection(name) {
                    task()
                }
            } finally {
                time = System.nanoTime() - time
                if (time > 10000000000L) {
                    logger.warn { "Task took ${time / 1000000000} seconds to complete: $name" }
                }
                taskLock.decrement()
            }
        }
    }

    fun runTask(task: () -> Unit,
                taskLock: TaskLock,
                name: String) {
        taskLock.increment()
        runTask({
            try {
                task()
            } finally {
                taskLock.decrement()
            }
        }, name)
    }

    fun addTaskOnce(task: () -> Unit,
                    name: String,
                    delay: Long = 0,
                    async: Boolean = false) {
        addTask({
            task()
            -1
        }, name, delay, async)
    }

    fun addTask(task: () -> Long,
                name: String,
                delay: Long = 0,
                async: Boolean = false) {
        val time = delay + System.currentTimeMillis()
        tasks.add(TaskWorker(task, name, time, async))
        eventLoop?.wake()
    }

    fun shutdown() {
        eventLoop?.join()
        if (root) {
            taskLock.lock()
            taskPool.shutdown()
            try {
                if (!taskPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    Thread.getAllStackTraces().keys.forEach(::println)
                }
            } catch (e: InterruptedException) {
            }
        }
    }

    enum class Priority(val priority: Int) {
        HIGH(Thread.MAX_PRIORITY),
        MEDIUM(Thread.NORM_PRIORITY),
        LOW(Thread.MIN_PRIORITY)
    }

    private class TaskWorker(val task: () -> Long,
                             val name: String,
                             var delay: Long,
                             val async: Boolean) : Comparable<TaskWorker> {
        var stopped = false
        private val uid = UID_COUNTER.andIncrement

        override fun compareTo(other: TaskWorker): Int {
            if (uid > other.uid) {
                return 1
            }
            if (uid < other.uid) {
                return -1
            }
            return 0
        }
    }

    private inner class ThreadWrapper(val joinable: Joiner.Joinable,
                                      val task: () -> Unit) : Runnable {
        init {
            taskLock.increment()
        }

        override fun run() {
            try {
                task()
            } catch (e: Throwable) {
                // Yes this catches ThreadDeath, so don't use it
                crashHandler.crash(e)
            } finally {
                joinable.join()
                taskLock.decrement()
            }
        }
    }

    private inner class PriorityThreadFactory(val priority: Int) : ThreadFactory {
        val id = AtomicInteger(1)
        val group: ThreadGroup

        init {
            group = Thread.currentThread().threadGroup
        }

        override fun newThread(r: Runnable): Thread {
            val thread = Thread(group, r, name + id.andIncrement, 0)
            if (thread.isDaemon) {
                thread.isDaemon = false
            }
            thread.priority = priority
            return thread
        }
    }

    companion object : KLogging() {
        private val UID_COUNTER = AtomicLong(Long.MIN_VALUE)
    }
}

inline fun TaskExecutor.start(priority: TaskExecutor.Priority = TaskExecutor.Priority.MEDIUM,
                              joiner: Joiner.Joinable = Joiner.BasicJoinable()) {
    eventLoop(runThread({ joiner ->
        while (!joiner.marked) {
            joiner.sleep(tick())
        }
    }, "Tasks", priority, joiner))
}

inline fun TaskExecutor.start(crossinline closeHook: () -> Unit) {
    start(closeHook, TaskExecutor.Priority.MEDIUM)
}

inline fun TaskExecutor.start(crossinline closeHook: () -> Unit,
                              priority: TaskExecutor.Priority = TaskExecutor.Priority.MEDIUM,
                              joiner: Joiner.Joinable = Joiner.BasicJoinable()) {
    eventLoop(runThread({ joiner ->
        while (!joiner.marked) {
            joiner.sleep(tick())
        }
        closeHook()
    }, "Tasks", priority, joiner))
}
