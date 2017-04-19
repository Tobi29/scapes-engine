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

import org.tobi29.scapes.engine.utils.AtomicInteger

class TaskLock {
    private val count = AtomicInteger()

    fun increment() {
        count.incrementAndGet()
    }

    fun decrement() {
        if (count.decrementAndGet() < 0) {
            throw IllegalStateException("Negative task count")
        }
        synchronized(count) {
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            (count as Object).notifyAll()
        }
    }

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
