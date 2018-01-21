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

package org.tobi29.coroutines

import org.tobi29.utils.Option
import org.tobi29.utils.OptionSome
import org.tobi29.utils.nil

/**
 * A queue of tasks or [nil] indicating the end of a batch.
 */
typealias TaskChannel<T> = UnboundChannel<Option<T>>

inline fun <T> TaskChannel(): TaskChannel<T> = LinkedListChannel()

/**
 * Adds an element to the end of the queue.
 * @param block The element to add
 * @receiver The queue to add to
 */
inline fun <T> TaskChannel<T>.offer(block: T) {
    offer(OptionSome(block))
}

/**
 * Calls all elements and removes them in the queue, even if added during
 * execution.
 * @receiver The queue to iterate through
 */
inline fun <T : () -> Any?> TaskChannel<T>.processDrain() =
        processDrain { it() }

/**
 * Calls [execute] on all element and removes them in the queue, even if added
 * during execution.
 * @param execute Called on each element removed from the queue
 * @receiver The queue to iterate through
 */
inline fun <T> TaskChannel<T>.processDrain(execute: (T) -> Unit) {
    while (!isEmpty) {
        poll().let { it as? OptionSome }?.let { execute(it.value) }
    }
}

/**
 * Calls all elements and removes them in the queue before starting to iterate.
 * @receiver The queue to iterate through
 */
inline fun <T : () -> Any?> TaskChannel<T>.processCurrent() =
        processCurrent { it() }

/**
 * Calls [execute] on all element and removes them in the queue before starting
 * to iterate.
 * @param execute Called on each element removed from the queue
 * @receiver The queue to iterate through
 */
inline fun <T> TaskChannel<T>.processCurrent(execute: (T) -> Unit) {
    if (!isEmpty) {
        offer(nil)
        current@ while (!isEmpty) {
            val element = poll()
            when (element) {
                is OptionSome -> execute(element.value)
                else -> break@current
            }
        }
    }
}
