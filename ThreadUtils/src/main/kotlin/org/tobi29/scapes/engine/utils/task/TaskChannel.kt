package org.tobi29.scapes.engine.utils.task

import kotlinx.coroutines.experimental.channels.LinkedListChannel
import org.tobi29.scapes.engine.utils.Option
import org.tobi29.scapes.engine.utils.OptionSome
import org.tobi29.scapes.engine.utils.nil

/**
 * A queue of tasks or [nil] indicating the end of a batch.
 */
typealias TaskChannel<T> = LinkedListChannel<Option<T>>

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
