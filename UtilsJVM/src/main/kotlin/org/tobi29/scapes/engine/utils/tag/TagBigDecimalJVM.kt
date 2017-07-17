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

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: BigDecimal) = TagList {
    elements.forEach { add(it.toTag()) }
}

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
