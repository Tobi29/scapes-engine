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

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import org.tobi29.scapes.engine.utils.Crashable
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.sleep
import kotlin.coroutines.experimental.CoroutineContext

impl class TaskExecutor {
    private val parent: TaskExecutor?
    private val taskLock = TaskLock()
    private val taskPool: CoroutineContext
    private val taskPoolJob: Job?
    private val crashHandler: Crashable
    private val name: String
    private var eventLoop: Joiner?

    impl constructor(parent: TaskExecutor,
                     name: String,
                     wakeup: Joiner? = null) {
        this.parent = parent
        crashHandler = parent.crashHandler
        this.name = parent.name + name + '-'
        this.eventLoop = wakeup
        taskPool = parent.taskPool
        taskPoolJob = null
    }

    impl constructor(crashHandler: Crashable,
                     name: String,
                     wakeup: Joiner? = null) {
        parent = null
        this.crashHandler = crashHandler
        this.name = name + '-'
        this.eventLoop = wakeup
        val processors = Runtime.getRuntime().availableProcessors()
        val taskPoolSize = max(processors, 1)
        taskPoolJob = Job()
        taskPool = newFixedThreadPoolContext(taskPoolSize, name, taskPoolJob)
    }

    fun runThread(task: (ThreadJoinable) -> Unit,
                  name: String,
                  priority: Priority = Priority.LOW): ThreadJoiner {
        val joiner = ThreadJoinable()
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

    impl fun runTask(task: suspend () -> Unit,
                     name: String): Job {
        thisAndParents { taskLock.increment() }
        return launch(taskPool) {
            try {
                task()
            } catch (e: Throwable) {
                crashHandler.crash(e)
            } finally {
                thisAndParents { taskLock.decrement() }
            }
        }
    }

    impl fun runTask(task: () -> Unit,
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

    impl fun shutdown() {
        eventLoop?.join()
        taskLock.lock()
        taskPoolJob?.cancel()
    }

    private inline fun thisAndParents(block: TaskExecutor.() -> Unit) {
        var current: TaskExecutor? = this
        while (current != null) {
            block(current)
            current = current.parent
        }
    }

    impl enum class Priority(val priority: Int) {
        HIGH(Thread.MAX_PRIORITY),
        MEDIUM(Thread.NORM_PRIORITY),
        LOW(Thread.MIN_PRIORITY)
    }

    private inner class ThreadWrapper(val joinable: Joiner.Joinable,
                                      val task: () -> Unit) : Runnable {
        init {
            thisAndParents { taskLock.increment() }
        }

        override fun run() {
            try {
                task()
            } catch (e: Throwable) {
                // Yes this catches ThreadDeath, so don't use it
                crashHandler.crash(e)
            } finally {
                joinable.join()
                thisAndParents { taskLock.decrement() }
            }
        }
    }

    companion object : KLogging()
}
