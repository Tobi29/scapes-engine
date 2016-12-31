/*
 * Copyright 2012-2016 Tobi29
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
import java.io.IOException
import java.nio.channels.ClosedSelectorException
import java.nio.channels.Selector
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class Joiner {
    private val joinables: Array<Joinable>

    private constructor(thread: Joinable) {
        joinables = arrayOf(thread)
    }

    constructor(joiners: Queue<Joiner>) : this(collectQueue(joiners))

    constructor(joiners: Collection<Joiner>) : this(*joiners.toTypedArray())

    constructor(vararg joiners: Joiner) {
        val list = ArrayList<Joinable>(joiners.size)
        for (joiner in joiners) {
            Collections.addAll(list, *joiner.joinables)
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

    interface Joinable {
        val joiner: Joiner
        val joined: Boolean
        val marked: Boolean

        fun mark()
        fun join()
        fun wake()
        fun joinWait(time: Long = 0)
        fun sleep(time: Long = 0)
    }

    class BasicJoinable : Joinable {
        override val joiner: Joiner
        private val woken = AtomicBoolean()
        override @Volatile var joined = false
            private set
        override @Volatile var marked = false
            private set

        init {
            joiner = Joiner(this)
        }

        override fun mark() {
            marked = true
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
            joined = true
            synchronized(this) {
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
    }

    class SelectorJoinable(val selector: Selector) : Joinable {
        override val joiner: Joiner
        private val woken = AtomicBoolean()
        override @Volatile var joined = false
            private set
        override @Volatile var marked = false
            private set

        init {
            joiner = Joiner(this)
        }

        override fun mark() {
            marked = true
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
            joined = true
            synchronized(this) {
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (this as Object).notifyAll()
            }
        }

        override fun wake() {
            woken.set(true)
            try {
                selector.wakeup()
            } catch(e: ClosedSelectorException) {
            }
        }

        override fun sleep(time: Long) {
            if (!woken.getAndSet(false)) {
                try {
                    selector.select(time)
                    selector.selectedKeys().clear()
                } catch (e: IOException) {
                    logger.warn { "Error when waiting with selector: $e" }
                } catch(e: ClosedSelectorException) {
                }
            }
        }

        companion object : KLogging()
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

inline fun Joiner.Joinable.wake(block: () -> Unit) {
    wake()
    block()
}
