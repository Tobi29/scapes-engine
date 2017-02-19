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

package org.tobi29.scapes.engine.utils.io.tag

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

// Unit
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Unit) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Unit) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Unit) = TagList {
    elements.forEach { add(it) }
}

// Boolean
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Boolean) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Boolean) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Boolean) = TagList {
    elements.forEach { add(it) }
}

// Byte
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Byte) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Byte) = TagList {
    elements.forEach { add(it) }
}

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Byte) = add(element.toTag())

// Short
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Short) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Short) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Short) = TagList {
    elements.forEach { add(it) }
}

// Int
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Int) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Int) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Int) = TagList {
    elements.forEach { add(it) }
}

// Long
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Long) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Long) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Long) = TagList {
    elements.forEach { add(it) }
}

// BigInteger
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: BigInteger) = add(
        element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: BigInteger) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: BigInteger) = TagList {
    elements.forEach { add(it) }
}

// Float
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Float) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Float) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Float) = TagList {
    elements.forEach { add(it) }
}

// Double
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Double) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Double) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Double) = TagList {
    elements.forEach { add(it) }
}

// BigDecimal
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: BigDecimal) = add(
        element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: BigDecimal) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: BigDecimal) = TagList {
    elements.forEach { add(it) }
}

// ByteArray
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: ByteArray) = add(
        element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: ByteArray) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: ByteArray) = TagList {
    elements.forEach { add(it) }
}

// String
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: String) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: String) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: String) = TagList {
    elements.forEach { add(it) }
}

// ReadTagMap
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: ReadTagMap) = add(
        element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: ReadTagMap) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: ReadTagMap) = TagList {
    elements.forEach { add(it) }
}

// ReadTagList
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: ReadTagList) = add(
        element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: ReadTagList) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: ReadTagList) = TagList {
    elements.forEach { add(it) }
}

// Sequence<Tag>
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: Sequence<Tag>) = add(
        element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: Sequence<Tag>) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: Sequence<Tag>) = TagList {
    elements.forEach { add(it) }
}

// UUID
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: UUID) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: UUID) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: UUID) = TagList {
    elements.forEach { add(it) }
}

// TagWrite
/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
@JvmName("addMut")
inline fun ReadWriteTagMutableList.add(element: TagWrite) = add(element.toTag())

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagList.add(element: TagWrite) = add(element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: TagWrite) = TagList {
    elements.forEach { add(it) }
}
