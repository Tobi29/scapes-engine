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

import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.computeAbsent
import org.tobi29.scapes.engine.utils.steadyClock

actual class Profiler {
    actual val root = Node("JS Runtime")

    private val handle = ProfilerHandle(root)

    actual fun current() = handle
}

actual class ProfilerHandle actual internal constructor(private var node: Node) {
    actual fun enterNode(name: String) {
        node = node.children.computeAbsent(name) { Node(it, node) }
        node.lastEnter = steadyClock.timeSteadyNanos()
        dispatchers.forEach { it.enterNode(name) }
    }

    actual fun exitNode(name: String) {
        val parentNode = node.parent ?: throw IllegalStateException(
                "Profiler stack popped on root node")
        assert { name == node.name() }
        node.timeNanos += steadyClock.timeSteadyNanos() - node.lastEnter
        dispatchers.forEach { it.exitNode(name) }
        node = parentNode
    }
}

actual internal val dispatchers: List<ProfilerDispatcher> = emptyList()
