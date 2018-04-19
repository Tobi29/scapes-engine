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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.io.tag

// Unit
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Unit) = TagList {
    elements.forEach { add(it.toTag()) }
}

// Boolean
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Boolean) = TagList {
    elements.forEach { add(it.toTag()) }
}

// Byte
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Byte) = TagList {
    elements.forEach { add(it.toTag()) }
}

// Short
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Short) = TagList {
    elements.forEach { add(it.toTag()) }
}

// Int
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Int) = TagList {
    elements.forEach { add(it.toTag()) }
}

// Long
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Long) = TagList {
    elements.forEach { add(it.toTag()) }
}

// Float
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Float) = TagList {
    elements.forEach { add(it.toTag()) }
}

// Double
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Double) = TagList {
    elements.forEach { add(it.toTag()) }
}

// ByteArray
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: ByteArray) = TagList {
    elements.forEach { add(it.toTag()) }
}

// String
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: String) = TagList {
    elements.forEach { add(it.toTag()) }
}

// ReadTagMap
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: ReadTagMap) = TagList {
    elements.forEach { add(it.toTag()) }
}

// ReadTagList
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: ReadTagList) = TagList {
    elements.forEach { add(it.toTag()) }
}

// Sequence<Tag>
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Sequence<Tag>) = TagList {
    elements.forEach { add(it.toTag()) }
}

// TagWrite
/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: TagWrite) = TagList {
    elements.forEach { add(it.toTag()) }
}
