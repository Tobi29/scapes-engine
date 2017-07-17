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

package org.tobi29.scapes.engine.utils.profiler

import org.tobi29.scapes.engine.utils.ThreadLocal
import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.profiler.spi.ProfilerDispatcherProvider
import java.util.*

impl object Profiler {
    internal val PROFILERS = WeakHashMap<Thread, ProfilerInstance>()
    private val INSTANCE = ThreadLocal { ProfilerInstance() }

    impl var enabled = false

    impl fun current() = INSTANCE.get()

    impl fun reset() {
        PROFILERS.values.forEach { it.resetNodes() }
    }

    fun node(thread: Thread): Node? {
        return PROFILERS[thread]?.rootNode
    }
}

impl class ProfilerInstance(thread: Thread) {
    internal val rootNode = Node({ thread.name })
    private var node = rootNode

    init {
        Profiler.PROFILERS.put(thread, this)
    }

    internal constructor() : this(Thread.currentThread())

    impl fun enterNode(name: String) {
        node = node.children.computeAbsent(name) { Node({ it }, node) }
        node.lastEnter = System.nanoTime()
        ProfilerDispatch.dispatchers.forEach { it.enterNode(name) }
    }

    impl fun exitNode(name: String) {
        val parentNode = node.parent ?: throw IllegalStateException(
                "Profiler stack popped on root node")
        assert { name == node.name() }
        node.timeNanos += System.nanoTime() - node.lastEnter
        ProfilerDispatch.dispatchers.forEach { it.exitNode(name) }
        node = parentNode
    }

    internal fun resetNodes() {
        rootNode.children.clear()
    }
}

private object ProfilerDispatch {
    val dispatchers = loadService()

    private fun loadService(): List<ProfilerDispatcher> {
        val dispatchers = ArrayList<ProfilerDispatcher>()
        for (dispatcher in ServiceLoader.load(
                ProfilerDispatcherProvider::class.java)) {
            try {
                dispatcher.dispatcher()?.let { dispatchers.add(it) }
            } catch (e: ServiceConfigurationError) {
            }
        }
        return dispatchers
    }
}
