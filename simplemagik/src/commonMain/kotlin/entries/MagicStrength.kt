/*
 * Copyright 2012-2019 Tobi29
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

@Constant
internal inline val MULT
    get() = 10

internal fun MagicEntryBuilder.computeStrength(): Int {
    var value = 2 * MULT
    when (matcher) {
        is ByteType ->
            value += matcher.comparison?.second.updateStrength(1 * MULT)
        is ShortType ->
            value += matcher.comparison?.second.updateStrength(2 * MULT)
        is IntType ->
            value += matcher.comparison?.second.updateStrength(4 * MULT)
        is FloatType ->
            value += matcher.comparison?.second.updateStrength(4 * MULT)
        is DateType ->
            value += matcher.comparison?.second.updateStrength(4 * MULT)
        is Id3LengthType ->
            value += matcher.comparison?.second.updateStrength(4 * MULT)
        is LongType ->
            value += matcher.comparison?.second.updateStrength(8 * MULT)
        is DoubleType ->
            value += matcher.comparison?.second.updateStrength(8 * MULT)
        is LongDateType ->
            value += matcher.comparison?.second.updateStrength(8 * MULT)
        is StringType ->
            value = matcher.comparison.updateStrength(value)
        is PStringType ->
            value = matcher.comparison.updateStrength(value)
        is BigEndianString16Type ->
            value = matcher.comparison.updateStrength(value)
        is LittleEndianString16Type ->
            value = matcher.comparison.updateStrength(value)
        is SearchType ->
            value += matcher.comparison.pattern.size.let {
                it * max(MULT / it, 1)
            }
        is RegexType ->
            value += matcher.pattern.pattern.length.let {
                it * max(MULT / it, 1)
            }
        is IndirectType, is NameType, is UseType -> {
        }
        is DefaultType -> return 0
    }
    return strength(value)
}

private fun TestOperator?.updateStrength(strength: Int): Int {
    var value = strength
    if (this == null) value = 0
    else {
        when (this) {
            TestOperator.NOT_EQUALS -> value = 0
            TestOperator.EQUALS -> value += MULT
            TestOperator.LESS_THAN,
            TestOperator.GREATER_THAN -> value -= 2 * MULT
            TestOperator.AND_ALL_SET,
            TestOperator.AND_ALL_CLEARED -> value -= MULT
            TestOperator.NEGATE -> {
            }
        }
    }
    return value
}

private fun StringComparison?.updateStrength(strength: Int): Int {
    var value = strength
    if (this == null) value = 0
    else {
        value += pattern.size * MULT
        when (operator) {
            StringOperator.NOT_EQUALS -> value = 0
            StringOperator.EQUALS -> value += MULT
            StringOperator.LESS_THAN,
            StringOperator.GREATER_THAN -> value -= 2 * MULT
        }
    }
    return value
}

private fun StringComparison16?.updateStrength(strength: Int): Int {
    var value = strength
    if (this == null) value = 0
    else {
        value += pattern.length * MULT
        when (operator) {
            StringOperator.NOT_EQUALS -> value = 0
            StringOperator.EQUALS -> value += MULT
            StringOperator.LESS_THAN,
            StringOperator.GREATER_THAN -> value -= 2 * MULT
        }
    }
    return value
}
