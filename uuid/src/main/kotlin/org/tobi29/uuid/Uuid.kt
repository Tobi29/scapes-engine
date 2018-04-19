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

package org.tobi29.uuid

import org.tobi29.io.tag.*

expect fun String.toUuid(): Uuid?

expect class Uuid(
    mostSignificantBits: Long,
    leastSignificantBits: Long
) {
    open fun getMostSignificantBits(): Long
    open fun getLeastSignificantBits(): Long
}

fun Uuid.toTag() = TagMap {
    this["Most"] = getMostSignificantBits().toTag()
    this["Least"] = getLeastSignificantBits().toTag()
}

fun MutableTag.toUuid(): Uuid? {
    toUuidNumber()?.let { return it }
    return toUuidString()
}

private fun MutableTag.toUuidString(): Uuid? = when (this) {
    is TagString -> value.toUuid()
    else -> null
}

private fun MutableTag.toUuidNumber(): Uuid? {
    val map = toMap() ?: return null
    val most = map["Most"]?.toLong() ?: return null
    val least = map["Least"]?.toLong() ?: return null
    return Uuid(most, least)
}
