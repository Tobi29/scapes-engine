/*
 * Copyright 2012-2018 Tobi29
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
import org.tobi29.stdex.readOnly
import org.tobi29.utils.steadyClock

expect class Profiler() {
    val root: Node
    val roots: Map<String, Node>
    val threadRoot: Node

    @PublishedApi
    internal fun current(): ProfilerHandle
}

@PublishedApi
internal class ProfilerHandle internal constructor(private var node: Node) {
    fun enterNode(name: String) {
        node = node._children.computeAbsent(name) { Node(it, node) }
        node.lastEnter = steadyClock.timeSteadyNanos()
        for (dispatcher in dispatchers) {
            dispatcher.enterNode(name)
        }
    }

    fun exitNode(name: String) {
        val parentNode = node.parent
                ?: throw IllegalStateException("Profiler stack popped on root node")
        assert { name == node.name }
        node.time += steadyClock.timeSteadyNanos() - node.lastEnter
        for (dispatcher in dispatchers) {
            dispatcher.exitNode(name)
        }
        node = parentNode
    }
}

internal expect val dispatchers: List<ProfilerDispatcher>

private val _profiler: AtomicReference<Profiler?> = AtomicReference(null)

val profiler: Profiler? get() = _profiler.get()
inline val profilerEnabled: Boolean get() = profiler != null

fun profilerEnable() {
    _profiler.compareAndSet(null, Profiler())
}

fun profilerDisable() {
    _profiler.set(null)
}

fun profilerReset() {
    _profiler.getAndSet(null)?.let { profilerEnable() }
}

class Node(name: Lazy<String>, val parent: Node? = null) {
    constructor(
        name: String,
        parent: Node? = null
    ) : this(lazyOf(name), parent)

    val name by name
    internal val _children = ConcurrentHashMap<String, Node>()
    val children = _children.readOnly()
    internal var lastEnter = 0L
    var time = 0L
        internal set
}

@PublishedApi
internal val profilerHandle: ProfilerHandle?
    get() = profiler?.current()

inline fun <R> profilerSection(
    name: String, crossinline receiver: () -> R
): R = profilerHandle.let { handle ->
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
