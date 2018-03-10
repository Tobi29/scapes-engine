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
 * Operators for tests. If no operator character then equals is assumed.
 */
enum class TestOperator(private val prefixChar: Char) {

    EQUALS('=') {
        override fun doTest(
            unsignedType: Boolean,
            extractedValue: Number,
            testValue: Number,
            numberType: NumberType
        ): Boolean {
            return numberType.compare(
                unsignedType,
                extractedValue,
                testValue
            ) == 0
        }
    },
    NOT_EQUALS('!') {
        override fun doTest(
            unsignedType: Boolean,
            extractedValue: Number,
            testValue: Number,
            numberType: NumberType
        ): Boolean {
            return numberType.compare(
                unsignedType,
                extractedValue,
                testValue
            ) != 0
        }
    },
    GREATER_THAN('>') {
        override fun doTest(
            unsignedType: Boolean,
            extractedValue: Number,
            testValue: Number,
            numberType: NumberType
        ): Boolean {
            return numberType.compare(
                unsignedType,
                extractedValue,
                testValue
            ) > 0
        }
    },
    LESS_THAN('<') {
        override fun doTest(
            unsignedType: Boolean,
            extractedValue: Number,
            testValue: Number,
            numberType: NumberType
        ): Boolean {
            return numberType.compare(
                unsignedType,
                extractedValue,
                testValue
            ) < 0
        }
    },
    AND_ALL_SET('&') {
        override fun doTest(
            unsignedType: Boolean,
            extractedValue: Number,
            testValue: Number,
            numberType: NumberType
        ): Boolean {
            // NOTE: we assume that we are dealing with decimal numbers here
            val testValueLong = testValue.toLong()
            return extractedValue.toLong() and testValueLong == testValueLong
        }
    },
    AND_ALL_CLEARED('^') {
        override fun doTest(
            unsignedType: Boolean,
            extractedValue: Number,
            testValue: Number,
            numberType: NumberType
        ): Boolean {
            // NOTE: we assume that we are dealing with decimal numbers here
            return extractedValue.toLong() and testValue.toLong() == 0L
        }
    },
    NEGATE('~') {
        override fun doTest(
            unsignedType: Boolean,
            extractedValue: Number,
            testValue: Number,
            numberType: NumberType
        ): Boolean {
            // we need the mask because we are using bit negation but testing only a portion of the long
            // NOTE: we assume that we are dealing with decimal numbers here
            val negatedValue = numberType.maskValue(testValue.toLong().inv())
            return extractedValue.toLong() == negatedValue
        }
    };

    /**
     * Perform the test using the operator.
     */
    abstract fun doTest(
        unsignedType: Boolean, extractedValue: Number, testValue: Number,
        numberType: NumberType
    ): Boolean

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
