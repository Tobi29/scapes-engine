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
import org.tobi29.scapes.engine.utils.IOException
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.task.Joiner
import java.nio.channels.ClosedSelectorException
import java.nio.channels.Selector


class SelectorJoinable(val selector: Selector) : Joiner.Joinable {
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

    companion object : KLogging()
}
