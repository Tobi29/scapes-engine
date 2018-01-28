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

package org.tobi29.utils

/**
 * Find the first element of correct type and casts it
 * @param T The type to cast to
 * @receiver The elements to search in
 * @return The first casted element or `null`
 */
inline fun <reified T : Any> Array<*>.findMap(): T? {
    return find { it is T } as? T
}

/**
 * Generates a lazy iterator containing all permutations of the given elements
 * in lists of the same size as [size]
 *
 * @param size The size of each list
 * @receiver The elements to use
 */
fun <T> Array<T>.permutations(size: Int): Iterator<List<T>> =
    Array(size) { this }.permutations()

/**
 * Generates a lazy iterator containing all permutations of the given elements
 * in lists of the same size as the given array
 *
 * @receiver The elements for each index in the resulting lists to use
 */
fun <T> Array<Array<T>>.permutations(): Iterator<List<T>> {
    val elements: Array<Array<T>> = this@permutations
    return object : Iterator<List<T>> {
        private val indices = IntArray(elements.size)
            .also { if (it.isNotEmpty()) it[it.lastIndex] = -1 }

        override fun hasNext(): Boolean {
            if (indices.isEmpty() || elements.last().isEmpty()) return false
            return indices.withIndex()
                .any { (i, current) -> current < elements[i].lastIndex }
        }

        override fun next(): List<T> {
            var i = indices.lastIndex
            while (true) {
                indices[i]++
                if (indices[i] < elements.size) break
                indices[i] = 0
                i--
                if (i < 0) {
                    for (j in 0 until indices.size) {
                        indices[j] = elements.lastIndex
                    }
                    throw NoSuchElementException(
                        "Iterator has no more elements"
                    )
                }
            }
            return (0 until indices.size).map { elements[it][indices[it]] }
        }
    }
}
