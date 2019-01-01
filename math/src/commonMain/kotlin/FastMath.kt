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

package org.tobi29.math

// TODO: Remove after 0.0.14

@Deprecated("Use top-level functions")
object FastMath {
    @Deprecated(
        "Use extension function",
        ReplaceWith("a.toHalfFloatShort()", "org.tobi29.math.toHalfFloatShort")
    )
    inline fun convertFloatToHalf(a: Float): Short = a.toHalfFloatShort()

    @Deprecated(
        "Use top-level function",
        ReplaceWith("diff(value1, value2, modulus)", "org.tobi29.math.diff")
    )
    inline fun diff(value1: Double, value2: Double, modulus: Double): Double =
        org.tobi29.math.diff(value1, value2, modulus)

    @Deprecated(
        "Use top-level function",
        ReplaceWith("nextPowerOfTwo(value)", "org.tobi29.math.nextPowerOfTwo")
    )
    inline fun nextPowerOfTwo(value: Int): Int =
        org.tobi29.math.nextPowerOfTwo(value)
}
