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

package org.tobi29.stdex


actual inline fun Int.toString(radix: Int): String {
    if (radix !in 1..36)
        throw IllegalArgumentException("Invalid radix: $radix")
    @Suppress("UnsafeCastFromDynamic")
    return asDynamic().toString(radix)
}

actual inline fun Long.toString(radix: Int): String {
    if (radix !in 1L..36L)
        throw IllegalArgumentException("Invalid radix: $radix")
    @Suppress("UnsafeCastFromDynamic")
    return asDynamic().toString(radix)
}

// Dirty hack, but should be reasonably stable

@Suppress("UnsafeCastFromDynamic")
actual inline fun <R> Long.splitToInts(output: (Int, Int) -> R): R {
    val l: Kotlin.Long = asDynamic()
    return output(l.getHighBits(), l.getLowBits())
}

@Suppress("UnsafeCastFromDynamic")
actual inline fun combineToLong(i1: Int, i0: Int): Long =
    Kotlin.Long(i0, i1).asDynamic()

@PublishedApi
internal external object Kotlin {
    class Long(low: Int, high: Int) {
        fun getHighBits(): Int
        fun getLowBits(): Int
    }
}
