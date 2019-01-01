/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.utils

@PublishedApi
internal fun eventDispatcher(
    parent: EventDispatcher? = null
): Pair<EventDispatcher, ListenerRegistrar> =
    EventDispatcher(parent).let { it to ListenerRegistrar(it) }

inline fun EventDispatcher(
    parent: EventDispatcher,
    init: ListenerRegistrar.() -> Unit
): EventDispatcher {
    val (listener, registrar) = eventDispatcher(parent)
    init(registrar)
    return listener
}

interface EventMuteable {
    var muted: Boolean
}

inline fun <reified E : EventMuteable> ListenerRegistrar.listenAlive(
    noinline listener: (E) -> Unit
) = listenAlive(0, listener)

inline fun <reified E : EventMuteable> ListenerRegistrar.listenAlive(
    priority: Int,
    noinline listener: (E) -> Unit
) = listenAlive(0, { true }, listener)

inline fun <reified E : EventMuteable> ListenerRegistrar.listenAlive(
    noinline accepts: (E) -> Boolean,
    noinline listener: (E) -> Unit
) = listenAlive(0, accepts, listener)

inline fun <reified E : EventMuteable> ListenerRegistrar.listenAlive(
    priority: Int,
    noinline accepts: (E) -> Boolean,
    noinline listener: (E) -> Unit
) = listen(priority, { !it.muted && accepts(it) }, listener)

expect class EventDispatcher internal constructor(
    parent: EventDispatcher?
) {
    constructor()

    fun enable()

    fun disable()

    fun <E : Any> fire(event: E)
}

expect class ListenerRegistrar internal constructor(events: EventDispatcher) {
    val events: EventDispatcher

    inline fun <reified E : Any> listen(
        noinline listener: (E) -> Unit
    )

    inline fun <reified E : Any> listen(
        priority: Int,
        noinline listener: (E) -> Unit
    )

    inline fun <reified E : Any> listen(
        noinline accepts: (E) -> Boolean,
        noinline listener: (E) -> Unit
    )

    inline fun <reified E : Any> listen(
        priority: Int,
        noinline accepts: (E) -> Boolean,
        noinline listener: (E) -> Unit
    )
}
