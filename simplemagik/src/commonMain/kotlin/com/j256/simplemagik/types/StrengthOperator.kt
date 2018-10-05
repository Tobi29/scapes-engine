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

enum class StrengthOperator(
    private val prefixChar: Char,
    internal val id: Int
) {
    PLUS('+', 0),
    MINUS('-', 1),
    TIMES('*', 2),
    DIV('/', 3);

    companion object {
        internal fun of(char: Char): StrengthOperator? {
            for (operator in values()) {
                if (operator.prefixChar == char) {
                    return operator
                }
            }
            return null
        }

        internal fun of(id: Int): StrengthOperator? = when (id) {
            0 -> PLUS
            1 -> MINUS
            2 -> TIMES
            3 -> DIV
            else -> null
        }
    }
}

operator fun Pair<StrengthOperator, Int>?.invoke(value: Int): Int =
    if (this == null) value else first(value, second)

operator fun StrengthOperator.invoke(a: Int, b: Int): Int = when (this) {
    StrengthOperator.PLUS -> a + b
    StrengthOperator.MINUS -> a - b
    StrengthOperator.TIMES -> a * b
    StrengthOperator.DIV -> a / b
}
