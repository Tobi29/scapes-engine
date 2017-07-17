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

import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KClass

class EventDispatcher internal constructor(
        private val parent: EventDispatcher? = null) {
    private val root = findRoot()
    private val children = ConcurrentHashSet<EventDispatcher>()
    private val activeListeners = ConcurrentHashMap<KClass<*>, MutableSet<Listener<*>>>()
    internal val listeners = ConcurrentHashMap<KClass<*>, MutableSet<Listener<*>>>()
    private val enabled = AtomicBoolean(parent == null)

    @Synchronized
    fun enable() {
        val parent = parent ?: return
        if (!enabled.getAndSet(true)) {
            parent.children.add(this)
            var toRoot: EventDispatcher? = parent
            while (toRoot != null) {
                if (!toRoot.enabled.get()) {
                    return
                }
                toRoot = toRoot.parent
            }
            activate()
        }
    }

    @Synchronized
    fun disable() {
        val parent = parent ?: return
        if (enabled.getAndSet(false)) {
            parent.children.remove(this)
            deactivate()
        }
    }

    private fun activate() {
        for ((clazz, set) in listeners) {
            val activeList = root.activeListeners.computeAbsent(
                    clazz) { ConcurrentSkipListSet<Listener<*>>() }
            activeList.addAll(set)
        }
        children.forEach { it.activate() }
    }

    private fun deactivate() {
        for ((clazz, set) in listeners) {
            root.activeListeners[clazz]?.removeAll(set)
        }
        children.forEach { it.deactivate() }
    }

    fun <E : Any> fire(event: E) {
        root.activeListeners[event::class]?.let {
            @Suppress("UNCHECKED_CAST")
            (it as Iterable<Listener<E>>).forEach {
                if (it.accepts(event)) {
                    it(event)
                }
            }
        }
    }

    private fun findRoot(): EventDispatcher {
        var root = this
        var parent = root.parent
        while (parent != null) {
            root = parent
            parent = root.parent
        }
        return root
    }
}

fun EventDispatcher() = EventDispatcher(null)

fun EventDispatcher(parent: EventDispatcher,
                    init: ListenerRegistrar.() -> Unit): EventDispatcher {
    val listener = EventDispatcher(parent)
    init(ListenerRegistrar(listener))
    return listener
}

class ListenerRegistrar internal constructor(val events: EventDispatcher) {
    fun <E : Any> listen(clazz: KClass<E>,
                         priority: Int,
                         accepts: (E) -> Boolean,
                         listener: (E) -> Unit) {
        val list = events.listeners.computeAbsent(clazz) {
            ConcurrentSkipListSet<Listener<*>>()
        }
        val reference = Listener(priority, accepts, listener)
        list.add(reference)
    }

    inline fun <reified E : Any> listen(
            noinline listener: (E) -> Unit) {
        listen(0, listener)
    }

    inline fun <reified E : Any> listen(
            priority: Int,
            noinline listener: (E) -> Unit) {
        listen(priority, { true }, listener)
    }

    inline fun <reified E : Any> listen(
            noinline accepts: (E) -> Boolean,
            noinline listener: (E) -> Unit) {
        listen(0, accepts, listener)
    }

    inline fun <reified E : Any> listen(
            priority: Int,
            noinline accepts: (E) -> Boolean,
            noinline listener: (E) -> Unit) {
        listen(E::class, priority, accepts, listener)
    }
}

internal data class Listener<in E : Any>(
        internal val priority: Int,
        internal val accepts: (E) -> Boolean,
        internal val listener: (E) -> Unit) : Comparable<Listener<*>> {
    private val uid = UID_COUNTER.andIncrement

    @Suppress("KDocMissingDocumentation")
    override fun compareTo(other: Listener<*>): Int {
        if (priority > other.priority) {
            return -1
        }
        if (priority < other.priority) {
            return 1
        }
        if (uid > other.uid) {
            return 1
        }
        if (uid < other.uid) {
            return -1
        }
        return 0
    }

    internal operator fun invoke(event: E) {
        listener(event)
    }

    companion object {
        private val UID_COUNTER = AtomicLong(Long.MIN_VALUE)
    }
}
