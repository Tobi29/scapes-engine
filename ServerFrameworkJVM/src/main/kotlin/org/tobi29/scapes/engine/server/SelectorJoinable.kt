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

import org.tobi29.scapes.engine.utils.AtomicBoolean
import org.tobi29.scapes.engine.utils.ConcurrentLinkedQueue
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.logging.KLogging
import java.nio.channels.ClosedSelectorException
import java.nio.channels.Selector

class SelectorJoinable(private val selector: Selector) {
    val joiner = Joiner(this)
    private val woken = AtomicBoolean()
    private val joinedMut = AtomicBoolean()
    private val markedMut = AtomicBoolean()
    private val completionTasks = ConcurrentLinkedQueue<() -> Unit>()
    val joined: Boolean get() = joinedMut.get()
    val marked: Boolean get() = markedMut.get()

    fun mark() {
        markedMut.set(true)
    }

    fun joinWait(time: Long) {
        synchronized(this) {
            try {
                @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
                (this as Object).wait(time)
            } catch (e: InterruptedException) {
            }
        }
    }

    fun join() {
        synchronized(this) {
            joinedMut.set(true)
            while (completionTasks.isNotEmpty()) {
                completionTasks.poll()?.invoke()
            }
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            (this as Object).notifyAll()
        }
    }

    fun wake() {
        woken.set(true)
        try {
            selector.wakeup()
        } catch (e: ClosedSelectorException) {
        }
    }

    fun sleep(time: Long) {
        if (!woken.getAndSet(false)) {
            try {
                selector.select(time)
                selector.selectedKeys().clear()
            } catch (e: IOException) {
                logger.warn { "Error when waiting with selector: $e" }
            } catch (e: ClosedSelectorException) {
            }
        }
    }

    fun onCompletion(runnable: () -> Unit): Boolean {
        if (joined) {
            return false
        }
        return synchronized(this) {
            if (joined) {
                return@synchronized false
            }
            completionTasks.add(runnable)
            true
        }
    }

    companion object : KLogging()
}
