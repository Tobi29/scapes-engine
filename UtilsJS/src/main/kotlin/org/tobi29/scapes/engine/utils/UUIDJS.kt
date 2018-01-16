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

// TODO: Apply overflow semantics from Java 9 (which make more sense)
actual fun String.toUUID(): UUID? {
    val split = split('-')
    if (split.size != 5) return null
    val g0 = split[0].toLongOrNull(16) ?: return null
    val g1 = split[1].toLongOrNull(16) ?: return null
    val g2 = split[2].toLongOrNull(16) ?: return null
    val g3 = split[3].toLongOrNull(16) ?: return null
    val g4 = split[4].toLongOrNull(16) ?: return null
    return UUID((g0 shl 32) or (g1 shl 16) or (g2), (g3 shl 48) or (g4))
}

actual class UUID actual constructor(private val mostSignificantBits: Long,
                                     private val leastSignificantBits: Long) {
    actual open fun getMostSignificantBits() = mostSignificantBits
    actual open fun getLeastSignificantBits() = leastSignificantBits

    override fun toString(): String =
            "${(mostSignificantBits ushr 32 and 0xFFFFFFFFL).toString(16, 8)}-${
            (mostSignificantBits ushr 16 and 0xFFFFL).toString(16, 4)}-${
            (mostSignificantBits ushr 0 and 0xFFFFL).toString(16, 4)}-${
            (leastSignificantBits ushr 48 and 0xFFFFL).toString(16, 4)}-${
            (leastSignificantBits ushr 0 and 0xFFFFFFFFFFFFL).toString(16, 12)}"
}
