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

package org.tobi29.scapes.engine.server

import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.tobi29.scapes.engine.utils.AtomicInteger
import org.tobi29.scapes.engine.utils.Queue
import org.tobi29.scapes.engine.utils.logging.KLogging

class Joiner(private val joinables: Array<SelectorJoinable>) {
    constructor(thread: SelectorJoinable) : this(arrayOf(thread))

    constructor(joiners: Collection<Joiner>) : this(*joiners.toTypedArray())

    constructor(vararg joiners: Joiner) : this(
            joiners.flatMap { it.joinables.asIterable() }.toTypedArray())

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
            onJoin { cont.resume(Unit) }
        }
    }

    fun onJoin(block: () -> Unit) {
        val counter = AtomicInteger(joinables.size)
        for (thread in joinables) {
            if (!thread.onCompletion {
                if (counter.decrementAndGet() == 0) {
                    block()
                }
            }) {
                if (counter.decrementAndGet() == 0) {
                    block()
                }
            }
        }
    }

    companion object : KLogging() {
        private fun <E> collectQueue(queue: Queue<E>): ArrayList<E> {
            val list = ArrayList<E>()
            while (queue.isNotEmpty()) {
                list.add(queue.poll()!!)
            }
            return list
        }
    }
}

inline fun SelectorJoinable.wake(block: () -> Unit) {
    wake()
    block()
}
