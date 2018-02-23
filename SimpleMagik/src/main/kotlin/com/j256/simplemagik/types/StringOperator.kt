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
 * Internal class that provides information about a particular test.
 */
enum class StringOperator private constructor(private val prefixChar: Char) {

    EQUALS('=') {
        override fun doTest(
            extractedChar: Char,
            testChar: Char,
            lastChar: Boolean
        ): Boolean {
            return extractedChar == testChar
        }
    },
    NOT_EQUALS('!') {
        override fun doTest(
            extractedChar: Char,
            testChar: Char,
            lastChar: Boolean
        ): Boolean {
            return extractedChar != testChar
        }
    },
    GREATER_THAN('>') {
        override fun doTest(
            extractedChar: Char,
            testChar: Char,
            lastChar: Boolean
        ): Boolean {
            return if (lastChar) {
                extractedChar > testChar
            } else {
                extractedChar >= testChar
            }
        }
    },
    LESS_THAN('<') {
        override fun doTest(
            extractedChar: Char,
            testChar: Char,
            lastChar: Boolean
        ): Boolean {
            return if (lastChar) {
                extractedChar < testChar
            } else {
                extractedChar <= testChar
            }
        }
    };

    /**
     * Test 2 characters. If this is the last character then the operator might want to be more strict in its testing.
     * For example, "dogs" > "dog" but 'd', 'o', and 'g' should be tested as >=.
     */
    abstract fun doTest(
        extractedChar: Char,
        testChar: Char,
        lastChar: Boolean
    ): Boolean

    companion object {

        /**
         * Default operator which should be used if [.fromTest] returns null;
         */
        val DEFAULT_OPERATOR = EQUALS

        /**
         * Returns the operator if the first character is an operator. Otherwise this returns null and you should use the
         * [.DEFAULT_OPERATOR].
         *
         *
         *
         * **NOTE:** We _don't_ return the default operator here because the caller needs to know if the prefix was
         * supplied or not.
         *
         */
        fun fromTest(testStr: String): StringOperator? {
            if (testStr.length == 0) {
                return null
            }
            val first = testStr[0]
            for (operator in values()) {
                if (operator.prefixChar == first) {
                    return operator
                }
            }
            return null
        }
    }
} // end
