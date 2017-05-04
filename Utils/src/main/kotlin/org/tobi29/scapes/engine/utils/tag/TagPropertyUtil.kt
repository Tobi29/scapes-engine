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

package org.tobi29.scapes.engine.utils.tag

// Boolean
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagBoolean(key: String,
                             default: Boolean) =
        tagBoolean(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagBoolean(key: String,
                      default: Boolean,
                      crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toBoolean() ?: default }, Boolean::toTag, access)

// Byte
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagByte(key: String,
                          default: Byte) =
        tagByte(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagByte(key: String,
                   default: Byte,
                   crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toByte() ?: default }, Byte::toTag, access)

// Short
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagShort(key: String,
                           default: Short) =
        tagShort(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagShort(key: String,
                    default: Short,
                    crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toShort() ?: default }, Short::toTag, access)

// Int
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagInt(key: String,
                         default: Int) =
        tagInt(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagInt(key: String,
                  default: Int,
                  crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toInt() ?: default }, Int::toTag, access)

// Long
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagLong(key: String,
                          default: Long) =
        tagLong(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagLong(key: String,
                   default: Long,
                   crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toLong() ?: default }, Long::toTag, access)

// Float
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagFloat(key: String,
                           default: Float) =
        tagFloat(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagFloat(key: String,
                    default: Float,
                    crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toFloat() ?: default }, Float::toTag, access)

// Double
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagDouble(key: String,
                            default: Double) =
        tagDouble(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagDouble(key: String,
                     default: Double,
                     crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toDouble() ?: default }, Double::toTag, access)

// ByteArray
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagByteArray(key: String,
                               default: ByteArray) =
        tagByteArray(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagByteArray(key: String,
                        default: ByteArray,
                        crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toByteArray() ?: default }, ByteArray::toTag, access)

// String
/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagString(key: String,
                            default: String) =
        tagString(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagString(key: String,
                     default: String,
                     crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toString() ?: default }, String::toTag, access)
