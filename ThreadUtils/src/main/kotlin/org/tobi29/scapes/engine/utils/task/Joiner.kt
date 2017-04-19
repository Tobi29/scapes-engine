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

import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.tobi29.scapes.engine.utils.AtomicBoolean
import org.tobi29.scapes.engine.utils.AtomicInteger
import org.tobi29.scapes.engine.utils.ConcurrentLinkedQueue
import org.tobi29.scapes.engine.utils.Queue
import org.tobi29.scapes.engine.utils.logging.KLogging

open class Joiner {
    private val joinables: Array<Joinable>

    constructor(thread: Joinable) {
        joinables = arrayOf(thread)
    }

    constructor(joiners: Queue<Joiner>) : this(collectQueue(joiners))

    constructor(joiners: Collection<Joiner>) : this(*joiners.toTypedArray())

    constructor(vararg joiners: Joiner) {
        val list = ArrayList<Joinable>(joiners.size)
        for (joiner in joiners) {
            list.addAll(joiner.joinables)
        }
        joinables = list.toTypedArray()
    }

    fun wake() {
        joinables.forEach { it.wake() }
    }

    fun join() {
        for (thread in joinables) {
            thread.mark()
        }
        for (thread in joinables) {
            while (!thread.joined) {
                thread.wake {
                    thread.joinWait(100)
                }
            }
        }
    }

    fun join(supplier: () -> Boolean) {
        for (thread in joinables) {
            thread.mark()
        }
        for (thread in joinables) {
            while (!thread.joined) {
                thread.wake {
                    if (!supplier()) {
                        return
                    }
                    thread.joinWait(100)
                }
            }
        }
    }

    suspend fun joinAsync() {
        for (thread in joinables) {
            thread.mark()
        }
        return suspendCancellableCoroutine { cont ->
            val counter = AtomicInteger(joinables.size)
            for (thread in joinables) {
                if (!thread.onCompletion {
                    if (counter.decrementAndGet() == 0) {
                        cont.resume(Unit)
                    }
                }) {
                    if (counter.decrementAndGet() == 0) {
                        cont.resume(Unit)
                    }
                }
            }
        }
    }

    interface Joinable {
        val joiner: Joiner
        val joined: Boolean
        val marked: Boolean

        fun mark()
        fun join()
        fun wake()
        fun joinWait(time: Long = 0)
        fun sleep(time: Long = 0)
        fun onCompletion(runnable: () -> Unit): Boolean
    }

    open class BasicJoinable : Joinable {
        override val joiner = Joiner(this)
        private val woken = AtomicBoolean()
        private val joinedMut = AtomicBoolean()
        private val markedMut = AtomicBoolean()
        private val completionTasks = ConcurrentLinkedQueue<() -> Unit>()
        override val joined: Boolean
            get() = joinedMut.get()
        override val marked: Boolean
            get() = markedMut.get()

        override fun mark() {
            markedMut.set(true)
        }

        override fun joinWait(time: Long) {
            synchronized(this) {
                try {
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (this as Object).wait(time)
                } catch (e: InterruptedException) {
                }
            }
        }

        override fun join() {
            synchronized(this) {
                joinedMut.set(true)
                while (completionTasks.isNotEmpty()) {
                    completionTasks.poll()()
                }
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (this as Object).notifyAll()
            }
        }

        override fun wake() {
            woken.set(true)
            synchronized(this) {
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (this as Object).notifyAll()
            }
        }

        override fun sleep(time: Long) {
            if (!woken.getAndSet(false)) {
                synchronized(this) {
                    try {
                        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                        (this as Object).wait(time)
                    } catch (e: InterruptedException) {
                    }
                }
            }
        }

        override fun onCompletion(runnable: () -> Unit): Boolean {
            if (joined) {
                return false
            }
            synchronized(this) {
                if (joined) {
                    return false
                }
                completionTasks.add(runnable)
            }
            return true
        }
    }

    class ThreadJoinable : BasicJoinable() {
        lateinit var thread: Thread
            internal set
        override val joiner = ThreadJoiner(this)
    }

    companion object : KLogging() {
        private fun <E> collectQueue(queue: Queue<E>): ArrayList<E> {
            val list = ArrayList<E>()
            while (queue.isNotEmpty()) {
                list.add(queue.poll())
            }
            return list
        }
    }
}

class ThreadJoiner(private val joinable: ThreadJoinable) : Joiner(joinable) {
    val thread: Thread
        get() = joinable.thread
}

inline fun Joiner.Joinable.wake(block: () -> Unit) {
    wake()
    block()
}
