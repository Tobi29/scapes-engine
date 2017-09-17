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

package java.util

import org.tobi29.scapes.engine.utils.UInt128
import org.tobi29.scapes.engine.utils.toString

impl class UUID internal constructor(private val value: UInt128) {
    impl constructor(mostSignificantBits: Long,
                     leastSignificantBits: Long) : this(
            UInt128(mostSignificantBits, leastSignificantBits))

    impl open fun getMostSignificantBits() = value.high
    impl open fun getLeastSignificantBits() = value.low

    override fun toString(): String =
            "${(value.high ushr 32 and 0xFFFFFFFFL).toString(16, 8)}-${
            (value.high ushr 16 and 0xFFFFL).toString(16, 4)}-${
            (value.high ushr 0 and 0xFFFFL).toString(16, 4)}-${
            (value.low ushr 48 and 0xFFFFL).toString(16, 4)}-${
            (value.low ushr 0 and 0xFFFFFFFFFFFFL).toString(16, 12)}"
}
