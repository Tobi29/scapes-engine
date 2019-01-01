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

import com.j256.simplemagik.entries.decodeLong

fun decodeComparison(testStr: String?): Pair<Long, TestOperator>? {
    if (testStr == null) return null
    var op = TestOperator.fromTest(testStr)
    val valueStr: String
    if (op == null) {
        op = TestOperator.DEFAULT_OPERATOR
        valueStr = testStr
    } else {
        valueStr = testStr.substring(1).trim { it <= ' ' }
    }
    return decodeLong(valueStr) to op
}

fun decodeComparisonDecimal(testStr: String?): Pair<Double, TestOperator>? {
    if (testStr == null) return null
    var op = TestOperator.fromTest(testStr)
    val valueStr: String
    if (op == null) {
        op = TestOperator.DEFAULT_OPERATOR
        valueStr = testStr
    } else {
        valueStr = testStr.substring(1).trim { it <= ' ' }
    }
    return valueStr.toDouble() to op
}
