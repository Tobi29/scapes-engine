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

package org.tobi29.codec

import org.tobi29.stdex.math.clamp
import kotlin.math.roundToInt

inline fun toInt16(sample: Float): Short {
    val pcm = (sample * Short.MAX_VALUE).roundToInt()
    return clamp(pcm, Short.MIN_VALUE.toInt(),
            Short.MAX_VALUE.toInt()).toShort()
}
