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
import org.tobi29.stdex.atomic.AtomicReference

val PROFILER: AtomicReference<Profiler?> = AtomicReference(null)

inline val PROFILER_ENABLED: Boolean get() = PROFILER.get() != null

fun profilerEnable() {
    PROFILER.compareAndSet(null, Profiler())
}

fun profilerDisable() {
    PROFILER.set(null)
}

fun profilerReset() {
    PROFILER.getAndSet(null)?.let {
        profilerEnable()
    }
}

class Node(val name: () -> String,
           val parent: Node? = null) {
    constructor(name: String,
                parent: Node? = null) : this({ name }, parent)

    val children = ConcurrentHashMap<String, Node>()
    var lastEnter = 0L
    var timeNanos = 0L

    val time get() = timeNanos
}

inline fun <R> profilerSection(name: String,
                               receiver: () -> R): R {
    val instance = PROFILER.get()?.current()
    instance?.enterNode(name)
    return try {
        receiver()
    } finally {
        instance?.exitNode(name)
    }
}

interface ProfilerDispatcher {
    fun enterNode(name: String)

    fun exitNode(name: String)
}
