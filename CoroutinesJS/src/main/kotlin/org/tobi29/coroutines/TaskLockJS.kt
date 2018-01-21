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

package org.tobi29.coroutines

import org.tobi29.stdex.toIntClamped

actual class TaskLock {
    private var count = 0L
    private var onDone = ArrayList<() -> Unit>()

    actual fun increment() {
        count++
    }

    actual fun decrement() {
        val newCount = --count
        if (newCount == 0L) {
            val done = onDone
            onDone = ArrayList()
            done.forEach { it() }
        } else if (newCount < 0) {
            throw IllegalStateException("Negative task count")
        }
    }

    actual fun onDone(block: () -> Unit) {
        if (isDone()) block() else onDone.add(block)
    }

    actual val activeTasks get() = count.toIntClamped()

    actual fun isDone() = count == 0L
}