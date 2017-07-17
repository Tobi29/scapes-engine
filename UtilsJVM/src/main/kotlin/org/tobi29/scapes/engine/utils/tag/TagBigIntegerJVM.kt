@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils.tag

import java.math.BigInteger

/**
 * Tag for storing a [BigInteger]
 */
class TagBigInteger
/**
 * Get the tag for the given value
 */
(override val value: BigInteger) : TagInteger()

/**
 * Get the tag for the given value
 */
inline fun BigInteger.toTag() = TagBigInteger(this)

/** Returns a new read-only [TagList] of given element */
inline fun tagListOf(vararg elements: BigInteger) = TagList {
    elements.forEach { add(it.toTag()) }
}

/**
 * Delegates a property to an entry at [key] in the given tag map
 * @receiver The map to store the property in
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @return An object for property delegation
 */
fun MutableTagMap.tagBigInteger(key: String,
                                default: BigInteger) =
        tagBigInteger(key, default) { this }

/**
 * Delegates a property to an entry at [key] in the tag map return from [access]
 * @param key The key of the entry
 * @param default The default value in case the map does not contain the key
 * @param access On each invocation it fetches the backing tag map from this
 * @return An object for property delegation
 */
inline fun tagBigInteger(key: String,
                         default: BigInteger,
                         crossinline access: () -> MutableTagMap) =
        tag(key, { it?.toBigInteger() ?: default }, BigInteger::toTag, access)

inline fun MutableTag.toBigInteger() = toNumber()?.let {
    BigInteger(it.toString())
}
