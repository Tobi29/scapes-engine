/*
 * Copyright 2017, Gray Watson
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.j256.simplemagik.types

import org.tobi29.stdex.maskAll
import org.tobi29.stdex.maskAny
import kotlin.experimental.and
import kotlin.experimental.inv

/**
 * Operators for tests. If no operator character then equals is assumed.
 */
enum class TestOperator(private val prefixChar: Char) {
    EQUALS('='),
    NOT_EQUALS('!'),
    GREATER_THAN('>'),
    LESS_THAN('<'),
    AND_ALL_SET('&'),
    AND_ALL_CLEARED('^'),
    NEGATE('~');

    companion object {
        inline val DEFAULT_OPERATOR get() = EQUALS

        internal fun fromTest(testStr: String): TestOperator? {
            if (testStr.isEmpty()) return null

            return of(testStr[0])
        }

        internal fun of(char: Char): TestOperator? {
            for (operator in values()) {
                if (operator.prefixChar == char) {
                    return operator
                }
            }
            return null
        }
    }
}

fun TestOperator.compare(x: Byte, y: Byte): Boolean = when (this) {
    TestOperator.EQUALS -> x == y
    TestOperator.NOT_EQUALS -> x != y
    TestOperator.GREATER_THAN -> x > y
    TestOperator.LESS_THAN -> x < y
    TestOperator.AND_ALL_SET -> x.maskAll(y)
    TestOperator.AND_ALL_CLEARED -> !x.maskAny(y)
    TestOperator.NEGATE -> x.inv() == y
}

fun TestOperator.compare(
    x: Byte, y: Byte,
    unsignedType: Boolean
): Boolean = if (
    unsignedType && (
            this == TestOperator.GREATER_THAN
                    || this == TestOperator.LESS_THAN
            )) compare(
    x.toShort() and (-1).toShort(),
    y.toShort() and (-1).toShort()
) else compare(x, y)

fun TestOperator.compare(x: Short, y: Short): Boolean = when (this) {
    TestOperator.EQUALS -> x == y
    TestOperator.NOT_EQUALS -> x != y
    TestOperator.GREATER_THAN -> x > y
    TestOperator.LESS_THAN -> x < y
    TestOperator.AND_ALL_SET -> x.maskAll(y)
    TestOperator.AND_ALL_CLEARED -> !x.maskAny(y)
    TestOperator.NEGATE -> x.inv() == y
}

fun TestOperator.compare(
    x: Short, y: Short,
    unsignedType: Boolean
): Boolean = if (
    unsignedType && (
            this == TestOperator.GREATER_THAN
                    || this == TestOperator.LESS_THAN
            )) compare(
    x.toInt() and -1,
    y.toInt() and -1
) else compare(x, y)

fun TestOperator.compare(x: Int, y: Int): Boolean = when (this) {
    TestOperator.EQUALS -> x == y
    TestOperator.NOT_EQUALS -> x != y
    TestOperator.GREATER_THAN -> x > y
    TestOperator.LESS_THAN -> x < y
    TestOperator.AND_ALL_SET -> x.maskAll(y)
    TestOperator.AND_ALL_CLEARED -> !x.maskAny(y)
    TestOperator.NEGATE -> x.inv() == y
}

fun TestOperator.compare(
    x: Int, y: Int,
    unsignedType: Boolean
): Boolean = if (
    unsignedType && (
            this == TestOperator.GREATER_THAN
                    || this == TestOperator.LESS_THAN
            )) compare(
    x.toLong() and -1L,
    y.toLong() and -1L
) else compare(x, y)

fun TestOperator.compare(x: Long, y: Long): Boolean = when (this) {
    TestOperator.EQUALS -> x == y
    TestOperator.NOT_EQUALS -> x != y
    TestOperator.GREATER_THAN -> x > y
    TestOperator.LESS_THAN -> x < y
    TestOperator.AND_ALL_SET -> x.maskAll(y)
    TestOperator.AND_ALL_CLEARED -> !x.maskAny(y)
    TestOperator.NEGATE -> x.inv() == y
}

fun TestOperator.compare(
    x: Long, y: Long,
    unsignedType: Boolean
): Boolean = if (
    unsignedType && (
            this == TestOperator.GREATER_THAN
                    || this == TestOperator.LESS_THAN
            )) {
    val xLarge = x < 0
    val yLarge = y < 0
    when (this) {
        TestOperator.GREATER_THAN ->
            if (xLarge && !yLarge) true
            else if (!xLarge && yLarge) false
            else if (!xLarge && !yLarge) x > y
            else x < y
        TestOperator.LESS_THAN ->
            if (xLarge && !yLarge) false
            else if (!xLarge && yLarge) true
            else if (!xLarge && !yLarge) x < y
            else x > y
        else -> error("Impossible")
    }
} else compare(x, y)

fun TestOperator.compare(x: Float, y: Float): Boolean = when (this) {
    TestOperator.EQUALS -> x == y
    TestOperator.NOT_EQUALS -> x != y
    TestOperator.GREATER_THAN -> x > y
    TestOperator.LESS_THAN -> x < y
    TestOperator.AND_ALL_SET -> x.toLong().maskAll(y.toLong())
    TestOperator.AND_ALL_CLEARED -> !x.toLong().maskAny(y.toLong())
    TestOperator.NEGATE -> x.toLong().inv() == y.toLong()
}

fun TestOperator.compare(x: Double, y: Double): Boolean = when (this) {
    TestOperator.EQUALS -> x == y
    TestOperator.NOT_EQUALS -> x != y
    TestOperator.GREATER_THAN -> x > y
    TestOperator.LESS_THAN -> x < y
    TestOperator.AND_ALL_SET -> x.toLong().maskAll(y.toLong())
    TestOperator.AND_ALL_CLEARED -> !x.toLong().maskAny(y.toLong())
    TestOperator.NEGATE -> x.toLong().inv() == y.toLong()
}
