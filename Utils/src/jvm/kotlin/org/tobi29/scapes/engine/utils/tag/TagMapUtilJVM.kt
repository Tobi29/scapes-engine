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
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Unit) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Unit) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Unit) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Unit) = put(key, value)

// Boolean
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Boolean) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Boolean) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Boolean) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Boolean) = put(key, value)

// Byte
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Byte) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Byte) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Byte) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Byte) = put(key, value)

// Short
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Short) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Short) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Short) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Short) = put(key, value)

// Int
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Int) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Int) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Int) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Int) = put(key, value)

// Long
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Long) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Long) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Long) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Long) = put(key, value)

// Float
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Float) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Float) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Float) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Float) = put(key, value)

// Double
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Double) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Double) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Double) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Double) = put(key, value)

// ByteArray
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: ByteArray) = put(key,
        value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: ByteArray) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: ByteArray) = put(key,
        value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: ByteArray) = put(key, value)

// String
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: String) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: String) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: String) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: String) = put(key, value)

// ReadTagMap
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: ReadTagMap) = put(key,
        value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: ReadTagMap) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: ReadTagMap) = put(key,
        value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: ReadTagMap) = put(key, value)

// ReadTagList
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: ReadTagList) = put(key,
        value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: ReadTagList) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: ReadTagList) = put(key,
        value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: ReadTagList) = put(key, value)

// Sequence<Tag>
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: Sequence<Tag>) = put(key,
        value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: Sequence<Tag>) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: Sequence<Tag>) = put(key,
        value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: Sequence<Tag>) = put(key, value)

// UUID
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: UUID) = put(key, value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: UUID) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: UUID) = put(key, value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: UUID) = put(key, value)

// TagWrite
/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: TagWrite) = put(key,
        value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: TagWrite) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: TagWrite) = put(key,
        value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: TagWrite) = put(key, value)
