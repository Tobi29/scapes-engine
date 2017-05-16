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

package org.tobi29.scapes.engine.utils.tag

import org.tobi29.scapes.engine.utils.UUID

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
