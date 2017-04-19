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

import org.tobi29.scapes.engine.utils.AtomicLong
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.math.max
import org.tobi29.scapes.engine.utils.math.min
import org.tobi29.scapes.engine.utils.profiler.profilerSection
import java.util.concurrent.ConcurrentSkipListSet

class UpdateLoop(val executor: TaskExecutor,
                 private val wakeup: Joiner? = null) {
    private val tasks = ConcurrentSkipListSet<TaskWorker>()

    fun tick(): Long {
        val time = System.currentTimeMillis()
        var earliestTask = Long.MAX_VALUE
        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            if (time >= task.delay) {
                try {
                    if (task.async) {
                        executor.runTask({
                            val delay = task.task()
                            if (delay < 0) {
                                task.stopped = true
                            } else {
                                // TODO: Use task.delay instead of time
                                // FIXME: Warn about slow tasks
                                task.delay = time + delay
                                wakeup?.wake()
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
        wakeup?.wake()
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

    companion object : KLogging() {
        private val UID_COUNTER = AtomicLong(Long.MIN_VALUE)
    }
}
