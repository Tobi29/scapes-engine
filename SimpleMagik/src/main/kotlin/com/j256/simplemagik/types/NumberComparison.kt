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

/**
 * Internal class that compares a number from the bytes with the value from the magic rule.
 */
class NumberComparison
/**
 * Pre-process the test string into an operator and a value.
 */
    (private val numberType: NumberType, testStr: String) {
    private val operator: TestOperator
    val value: Number

    init {
        var op = TestOperator.fromTest(testStr)
        val valueStr: String
        if (op == null) {
            op = TestOperator.DEFAULT_OPERATOR
            valueStr = testStr
        } else {
            valueStr = testStr.substring(1).trim { it <= ' ' }
        }
        this.operator = op
        this.value = numberType.decodeValueString(valueStr)
    }

    fun isMatch(
        andValue: Long?,
        unsignedType: Boolean,
        extractedValue: Number
    ): Boolean {
        var extractedValue = extractedValue
        if (andValue != null) {
            extractedValue = extractedValue.toLong() and andValue
        }
        return operator.doTest(unsignedType, extractedValue, value, numberType)
    }

    override fun toString(): String {
        return operator.toString() + ", value " + value
    }
}
