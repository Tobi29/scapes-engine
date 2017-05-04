@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils.tag

import java.math.BigDecimal

/**
 * Tag for storing a [BigDecimal]
 */
class TagBigDecimal
/**
 * Get the tag for the given value
 */
(override val value: BigDecimal) : TagDecimal()

/**
 * Get the tag for the given value
 */
inline fun BigDecimal.toTag() = TagBigDecimal(this)

/**
 * Adds the specified element to the collection.
 *
 * The [element] gets converted to a tag before inserting it
 *
 * @return `true` if the element has been added, `false` if the collection does not support duplicates
 * and the element is already contained in the collection.
 */
inline fun ReadWriteTagMutableList.add(element: BigDecimal) = add(
        element.toTag())

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: BigDecimal) = TagList {
    elements.forEach { add(it.toTag()) }
}

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMutableMap.put(key: String,
                                      value: BigDecimal) = put(key,
        value.toTag())

/**
 * Associates the specified [value] with the specified [key] in the map.
 *
 * The [value] gets converted to a tag before inserting it
 *
 * @return the previous value associated with the key, or `null` if the key was not present in the map.
 */
inline fun ReadWriteTagMap.put(key: String,
                               value: BigDecimal) = put(key, value.toTag())

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMutableMap.set(key: String,
                                               value: BigDecimal) = put(key,
        value)

/**
 * Allows to use the index operator for storing values in a mutable map.
 */
inline operator fun ReadWriteTagMap.set(key: String,
                                        value: BigDecimal) = put(key, value)

/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagBigDecimal(key: String,
                                default: BigDecimal) =
        tagBigDecimal(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagBigDecimal(key: String,
                         default: BigDecimal,
                         crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toBigDecimal() ?: default }, BigDecimal::toTag, access)

inline fun MutableTag.toBigDecimal() = toNumber()?.let {
    BigDecimal(it.toString())
}
