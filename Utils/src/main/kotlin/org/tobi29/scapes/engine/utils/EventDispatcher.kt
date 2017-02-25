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

import org.tobi29.scapes.engine.utils.math.max
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicLong

/**
 * An event dispatcher based on class types to identify events
 * Each listener is referenced by a [ListenerOwner] and will be garbage
 * collected once this owner goes unused
 * @param parent An optional parent dispatcher to forward events to
 */
class EventDispatcher(private val parent: EventDispatcher? = null) {
    private val listeners = ConcurrentHashMap<Class<*>, ConcurrentSkipListSet<Listener<*>>>()

    /**
     * Adds a listener for the specified type to this dispatcher
     * @param owner The owner for the listener
     * @param listener Code to be executed when event is fired
     * @param E Type of the event
     */
    inline fun <reified E : Any> listener(owner: ListenerOwner,
                                          noinline listener: (E) -> Unit) {
        listener(owner, 0, listener)
    }

    /**
     * Adds a listener for the specified type to this dispatcher
     * @param owner The owner for the listener
     * @param priority Priority of the listener, higher means earlier execution
     * @param listener Code to be executed when event is fired
     * @param E Type of the event
     */
    inline fun <reified E : Any> listener(owner: ListenerOwner,
                                          priority: Int,
                                          noinline listener: (E) -> Unit) {
        listener(E::class.java, owner, priority, listener)
    }

    /**
     * Adds a listener for the specified type to this dispatcher
     * @param clazz The class of the event type to listen to
     * @param owner The owner for the listener
     * @param priority Priority of the listener, higher means earlier execution
     * @param listener Code to be executed when event is fired
     * @param E Type of the event
     */
    fun <E : Any> listener(clazz: Class<E>,
                           owner: ListenerOwner,
                           priority: Int,
                           listener: (E) -> Unit) {
        val list = listeners.computeAbsent(clazz) {
            ConcurrentSkipListSet<Listener<*>>()
        }
        val ownerHandle = owner.listenerOwner
        val reference = { event: E ->
            if (ownerHandle.isValid) {
                listener(event)
            }
        }
        // Avoid referencing many dead listeners when no events get fired
        if (list.size > 16) {
            cleanListeners(list)
        }
        list.add(Listener(priority, WeakReference(reference)))
        ownerHandle.add(reference)
    }

    /**
     * Adds a listener for the specified type to this dispatcher and its parents
     * @param owner The owner for the listener
     * @param listener Code to be executed when event is fired
     * @param E Type of the event
     */
    inline fun <reified E : Any> listenerGlobal(owner: ListenerOwner,
                                                noinline listener: (E) -> Unit) {
        listenerGlobal(owner, 0, listener)
    }

    /**
     * Adds a listener for the specified type to this dispatcher and its parents
     * @param owner The owner for the listener
     * @param priority Priority of the listener, higher means earlier execution
     * @param listener Code to be executed when event is fired
     * @param E Type of the event
     */
    inline fun <reified E : Any> listenerGlobal(owner: ListenerOwner,
                                                priority: Int,
                                                noinline listener: (E) -> Unit) {
        listenerGlobal(E::class.java, owner, priority, listener)
    }

    /**
     * Adds a listener for the specified type to this dispatcher and its parents
     * @param clazz The class of the event type to listen to
     * @param owner The owner for the listener
     * @param priority Priority of the listener, higher means earlier execution
     * @param listener Code to be executed when event is fired
     * @param E Type of the event
     */
    fun <E : Any> listenerGlobal(clazz: Class<E>,
                                 owner: ListenerOwner,
                                 priority: Int,
                                 listener: (E) -> Unit) {
        var dispatcher: EventDispatcher? = this
        while (dispatcher != null) {
            dispatcher.listener(clazz, owner, priority, listener)
            dispatcher = dispatcher.parent
        }
    }

    /**
     * Fires the event to this dispatcher and all parents
     * @param event The event to fire
     * @param E The type of the event
     */
    fun <E : Any> fire(event: E) {
        if (parent != null) {
            val listeners = ArrayList<MutableSet<Listener<*>>>()
            var dispatcher: EventDispatcher? = this
            while (dispatcher != null) {
                dispatcher.listeners[event::class.java]?.and {
                    it.isNotEmpty()
                }?.let { listeners.add(it) }
                dispatcher = dispatcher.parent
            }
            if (listeners.size == 1) {
                listeners[0].let { cascadeEventFast(event, it) }
            } else if (listeners.size != 0) {
                cascadeEvent(event, listeners)
            }
        } else {
            fireLocal(event)
        }
    }

    /**
     * Fires the event to this dispatcher
     * @param event The event to fire
     * @param E The type of the event
     */
    fun <E : Any> fireLocal(event: E) {
        listeners[event::class.java]?.let { cascadeEventFast(event, it) }
    }

    private fun cleanListeners(listeners: MutableSet<Listener<*>>) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val listener = iterator.next()
            if (listener.isDead) {
                iterator.remove()
            }
        }
    }

    private fun <E : Any> cascadeEventFast(event: E,
                                           listeners: MutableSet<Listener<*>>) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val listener = iterator.next()
            @Suppress("UNCHECKED_CAST")
            if (!(listener as Listener<E>)(event)) {
                iterator.remove()
            }
        }
    }

    private fun <E : Any> cascadeEvent(event: E,
                                       listeners: List<MutableSet<Listener<*>>>) {
        class IteratorState<E : Any>(private val iterator: MutableIterator<E>) {
            internal var current: E? = null

            internal fun next() {
                if (iterator.hasNext()) {
                    current = iterator.next()
                } else {
                    current = null
                }
            }

            internal fun remove() {
                iterator.remove()
            }
        }

        val iterators = Array(listeners.size) {
            val iterator = IteratorState(listeners[it].iterator())
            iterator.next()
            iterator
        }
        var priority = Int.MAX_VALUE
        while (true) {
            var currentPriority = Int.MIN_VALUE
            var active = false
            for (i in iterators.indices) {
                val listener = iterators[i].current ?: continue
                active = true
                if (listener.priority == priority) {
                    @Suppress("UNCHECKED_CAST")
                    if (!(listener as Listener<E>)(event)) {
                        iterators[i].remove()
                    }
                    iterators[i].next()
                }
                currentPriority = max(currentPriority, listener.priority)
            }
            if (!active) {
                break
            }
            assert(currentPriority <= priority)
            priority = currentPriority
        }
    }

    private data class Listener<E : Any>(internal val priority: Int,
                                         internal val listener: WeakReference<(E) -> Unit>) : Comparable<Listener<*>> {
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

        internal operator fun invoke(event: E): Boolean {
            val listener = listener.get() ?: run {
                return false
            }
            listener(event)
            return true
        }

        internal val isDead: Boolean
            get() {
                return listener.get() == null
            }

        companion object {
            private val UID_COUNTER = AtomicLong(Long.MIN_VALUE)
        }
    }
}