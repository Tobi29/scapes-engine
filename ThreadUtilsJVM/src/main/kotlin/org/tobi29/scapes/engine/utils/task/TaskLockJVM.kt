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

import org.tobi29.scapes.engine.utils.*
import java.util.concurrent.ConcurrentLinkedQueue

actual class TaskLock {
    private val count = AtomicLong(0L)
    private val onDone = ConcurrentLinkedQueue<Option<() -> Unit>>()

    actual fun increment() {
        count.incrementAndGet()
    }

    actual fun decrement() {
        val newCount = count.decrementAndGet()
        if (newCount == 0L) {
            synchronized(count) {
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (count as Object).notifyAll()
            }
            onDone.add(nil)
            process@ while (onDone.isNotEmpty()) {
                val it = onDone.poll() ?: break
                when (it) {
                    is OptionSome -> it.get()()
                    else -> break@process
                }
            }
        } else if (newCount < 0) {
            throw IllegalStateException("Negative task count")
        }
    }

    actual fun onDone(block: () -> Unit) {
        if (isDone()) {
            block()
        } else {
            val blockOption = OptionSome(block)
            onDone.add(blockOption)
            if (isDone() && onDone.remove(blockOption)) block()
        }
    }

    actual val activeTasks get() = count.get().toIntClamped()

    actual fun isDone() = count.get() == 0L

    fun lock() {
        synchronized(count) {
            while (count.get() > 0) {
                try {
                    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                    (count as Object).wait()
                } catch (e: InterruptedException) {
                }
            }
        }
    }
}
