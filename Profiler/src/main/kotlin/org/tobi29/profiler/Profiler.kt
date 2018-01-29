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

package org.tobi29.profiler

import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.assert
import org.tobi29.stdex.atomic.AtomicReference
import org.tobi29.stdex.computeAbsent
import org.tobi29.utils.steadyClock

expect class Profiler() {
    val root: Node

    fun current(): ProfilerHandle
}

class ProfilerHandle internal constructor(private var node: Node) {
    fun enterNode(name: String) {
        node = node.children.computeAbsent(name) { Node(it, node) }
        node.lastEnter = steadyClock.timeSteadyNanos()
        dispatchers.forEach { it.enterNode(name) }
    }

    fun exitNode(name: String) {
        val parentNode = node.parent
                ?: throw IllegalStateException("Profiler stack popped on root node")
        assert { name == node.name() }
        node.timeNanos += steadyClock.timeSteadyNanos() - node.lastEnter
        dispatchers.forEach { it.exitNode(name) }
        node = parentNode
    }
}

internal expect val dispatchers: List<ProfilerDispatcher>

private val profiler: AtomicReference<Profiler?> = AtomicReference(null)

val PROFILER: Profiler? get() = profiler.get()
val PROFILER_CURRENT: ProfilerHandle? get() = PROFILER?.current()

inline val PROFILER_ENABLED: Boolean get() = PROFILER != null

fun profilerEnable() {
    profiler.compareAndSet(null, Profiler())
}

fun profilerDisable() {
    profiler.set(null)
}

fun profilerReset() {
    profiler.getAndSet(null)?.let { profilerEnable() }
}

class Node(val name: () -> String, val parent: Node? = null) {
    constructor(
        name: String,
        parent: Node? = null
    ) : this({ name }, parent)

    val children = ConcurrentHashMap<String, Node>()
    var lastEnter = 0L
    var timeNanos = 0L

    val time get() = timeNanos
}

inline fun <R> profilerSection(name: String, receiver: () -> R): R =
    PROFILER_CURRENT.let { handle ->
        handle?.enterNode(name)
        try {
            receiver()
        } finally {
            handle?.exitNode(name)
        }
    }

interface ProfilerDispatcher {
    fun enterNode(name: String)

    fun exitNode(name: String)
}
