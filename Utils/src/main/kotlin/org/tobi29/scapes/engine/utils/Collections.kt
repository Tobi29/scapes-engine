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

interface Queue<E> : MutableCollection<E> {
    fun offer(element: E): Boolean
    fun remove(): E
    fun poll(): E?
    fun element(): E
    fun peek(): E
}

interface Deque<E> : Queue<E> {
    fun addFirst(element: E)
    fun addLast(element: E)
    fun offerFirst(element: E): Boolean
    fun offerLast(element: E): Boolean
    fun removeFirst(): E
    fun removeLast(): E
    fun pollFirst(): E?
    fun pollLast(): E?
    fun getFirst(): E
    fun getLast(): E
    fun peekFirst(): E
    fun peekLast(): E
    fun removeFirstOccurrence(element: E): Boolean
    fun removeLastOccurrence(element: E): Boolean
    fun push(element: E)
    fun pop(): E
    fun descendingIterator(): Iterator<E>
}

// TODO: Make header
interface ConcurrentMap<K, V> : MutableMap<K, V>
