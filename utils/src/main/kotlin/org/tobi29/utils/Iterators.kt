/*
 * Copyright 2012-2018 Tobi29
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

/**
 * Returns an iterator containing the results of applying the given [transform]
 * function to each element in the original iterator.
 */
inline fun <T, R> Iterator<T>.map(
    crossinline transform: (T) -> R
): Iterator<R> =
    object : Iterator<R> {
        override fun hasNext() = this@map.hasNext()

        override fun next() = transform(this@map.next())
    }

/**
 * Constructs an infinite iterator starting with all the elements in the given
 * one and filling the rest with `null`.
 */
fun <T> Iterator<T>.andNull(): Iterator<T?> =
    object : Iterator<T?> {
        private var finishedFirst = false

        override fun hasNext() = true

        override fun next(): T? {
            if (finishedFirst) return null
            if (!this@andNull.hasNext()) {
                finishedFirst = true
                return null
            }
            return this@andNull.next()
        }
    }

/**
 * Constructs an iterator starting with all the elements in the given
 * one and then the next iterator.
 */
operator fun <T> Iterator<T>.plus(other: Iterator<T>): Iterator<T> =
    object : Iterator<T> {
        private var finishedFirst = false

        override fun hasNext() =
            (!finishedFirst && this@plus.hasNext()) || other.hasNext()

        override fun next(): T {
            if (finishedFirst) return other.next()
            if (!this@plus.hasNext()) {
                finishedFirst = true
                return other.next()
            }
            return this@plus.next()
        }
    }

/**
 * Returns a normal iterator that goes backwards through the list iterator
 * @receiver A list iterator
 * @return A normal iterator that goes backwards
 */
fun <E> ListIterator<E>.descendingIterator(): Iterator<E> =
    object : Iterator<E> {
        override fun hasNext(): Boolean =
            this@descendingIterator.hasPrevious()

        override fun next(): E =
            this@descendingIterator.previous()
    }

/**
 * Returns a normal iterator that goes backwards through the list iterator
 * @receiver A list iterator
 * @return A normal iterator that goes backwards
 */
fun <E> MutableListIterator<E>.descendingMutableIterator(): MutableIterator<E> =
    object : MutableIterator<E> {
        override fun hasNext(): Boolean =
            this@descendingMutableIterator.hasPrevious()

        override fun next(): E =
            this@descendingMutableIterator.previous()

        override fun remove() =
            this@descendingMutableIterator.remove()
    }

/**
 * Constructs an iterator from the given [supplier], stopping at the first
 * `null` returned
 * @param supplier Supplier of values for the iterator
 * @return An iterator returning the values from [supplier]
 */
inline fun <T> Iterator(crossinline supplier: () -> T?): Iterator<T> =
    object : Iterator<T> {
        private var next: T? = null
        private var init = false

        // We have this in a function to only inline once, but still have a
        // non-virtual call
        private fun iterate() {
            next = supplier()
        }

        // Used to avoid having to call invoke() before actually evaluating
        // Makes this properly lazy
        private fun touch() {
            if (!init) {
                init = true
                iterate()
            }
        }

        override fun hasNext(): Boolean {
            touch()
            return next != null
        }

        override fun next(): T {
            touch()
            val element = next ?: throw NoSuchElementException(
                "No more elements in iterator"
            )
            iterate()
            return element
        }
    }
