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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

/**
 * A queue of tasks or [Option.None] indicating the end of a batch.
 */
typealias TaskQueue<T> = ConcurrentLinkedQueue<Option<T>>

/**
 * Adds an element to the end of the queue.
 * @param block The element to add
 * @receiver The queue to add to
 */
inline fun <T> TaskQueue<T>.add(block: T) {
    add(Option.Some(block))
}

/**
 * Removes an element once from the queue.
 * @param block The element to remove
 * @receiver The queue to remove from
 */
inline fun <T> TaskQueue<T>.remove(block: T): Boolean =
        remove(Option.Some(block))

/**
 * Calls all elements and removes them in the queue, even if added during
 * execution.
 * @receiver The queue to iterate through
 */
inline fun <T : () -> Any?> TaskQueue<T>.processDrain() =
        processDrain { it() }

/**
 * Calls [execute] on all element and removes them in the queue, even if added
 * during execution.
 * @param execute Called on each element removed from the queue
 * @receiver The queue to iterate through
 */
inline fun <T> TaskQueue<T>.processDrain(execute: (T) -> Unit) {
    while (isNotEmpty()) {
        poll().let { it as? Option.Some }?.let { execute(it.value) }
    }
}

/**
 * Calls all elements and removes them in the queue before starting to iterate.
 * @receiver The queue to iterate through
 */
inline fun <T : () -> Any?> TaskQueue<T>.processCurrent() =
        processCurrent { it() }

/**
 * Calls [execute] on all element and removes them in the queue before starting
 * to iterate.
 * @param execute Called on each element removed from the queue
 * @receiver The queue to iterate through
 */
inline fun <T> TaskQueue<T>.processCurrent(execute: (T) -> Unit) {
    if (isNotEmpty()) {
        add(Option.None)
        current@ while (isNotEmpty()) {
            val element = poll()
            when (element) {
                is Option.Some -> execute(element.value)
                else -> break@current
            }
        }
    }
}
