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

package org.tobi29.scapes.engine.utils

fun newEventDispatcher() = EventDispatcher(null)

fun EventDispatcher(parent: EventDispatcher,
                    init: ListenerRegistrar.() -> Unit): EventDispatcher {
    val listener = EventDispatcher(parent)
    init(ListenerRegistrar(listener))
    return listener
}

interface EventMuteable {
    var muted: Boolean
}

inline fun <reified E : EventMuteable> ListenerRegistrar.listenAlive(
        noinline listener: (E) -> Unit) =
        listenAlive(0, listener)

inline fun <reified E : EventMuteable> ListenerRegistrar.listenAlive(
        priority: Int,
        noinline listener: (E) -> Unit) =
        listenAlive(0, { true }, listener)

inline fun <reified E : EventMuteable> ListenerRegistrar.listenAlive(
        noinline accepts: (E) -> Boolean,
        noinline listener: (E) -> Unit) =
        listenAlive(0, accepts, listener)

inline fun <reified E : EventMuteable> ListenerRegistrar.listenAlive(
        priority: Int,
        noinline accepts: (E) -> Boolean,
        noinline listener: (E) -> Unit) =
        listen(priority, { !it.muted && accepts(it) }, listener)
