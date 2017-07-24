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

class Node(val name: () -> String,
           val parent: Node? = null) {
    val children: MutableMap<String, Node> = HashMap()
    var lastEnter = 0L
    var timeNanos = 0L

    val time get() = timeNanos
}

inline fun <R> profilerSection(name: String,
                               receiver: () -> R): R {
    val instance = if (Profiler.enabled) Profiler.current() else null
    instance?.enterNode(name)
    try {
        return receiver()
    } finally {
        instance?.exitNode(name)
    }
}
