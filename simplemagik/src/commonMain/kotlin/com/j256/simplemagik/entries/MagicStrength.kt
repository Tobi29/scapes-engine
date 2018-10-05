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

package com.j256.simplemagik.entries

import com.j256.simplemagik.types.*
import org.tobi29.stdex.Constant
import kotlin.math.max

// TODO: Strength line

@Constant
internal inline val MULT
    get() = 10

internal fun MagicEntryBuilder.computeStrength(): Int {
    var strength = strength
    when (matcher) {
        is ByteType ->
            strength += 1 * MULT
        is ShortType ->
            strength += 2 * MULT
        is IntType, is FloatType, is DateType, is Id3LengthType ->
            strength += 4 * MULT
        is LongType, is DoubleType, is LongDateType ->
            strength += 8 * MULT
        is StringType ->
            strength += (matcher.comparison?.pattern?.size ?: 0) * MULT
        is PStringType ->
            strength += (matcher.comparison?.pattern?.size ?: 0) * MULT
        is BigEndianString16Type ->
            strength += (matcher.comparison?.pattern?.length ?: 0) * MULT
        is LittleEndianString16Type ->
            strength += (matcher.comparison?.pattern?.length ?: 0) * MULT
        is SearchType ->
            strength += matcher.comparison.pattern.size.let {
                it * max(MULT / it, 1)
            }
        is RegexType ->
            strength += matcher.pattern.pattern.length.let {
                it * max(MULT / it, 1)
            }
        is IndirectType, is NameType, is UseType -> {
        }
        is DefaultType -> return 0
    }
    // TODO: Operator strengths
    return strength
}
