/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.utils

/**
 * Creates a hash from the given [String]
 * @param value String to create the hash from
 * @param h Base value for creating the hash
 * @return A 64-bit hash
 */
fun hash(value: String,
         h: Long = 0L): Long {
    var h = h
    val length = value.length
    for (i in 0 until length) {
        h = 31 * h + value[i].toLong()
    }
    return h
}

/**
 * Converts a wildcard expression into a [Regex]
 * @param exp [String] containing wildcard expression
 * @return A [Regex] matching like the wildcard expression
 */
fun wildcard(exp: String) = Regex.escape(exp).replace("?", "\\E.?\\Q").replace(
        "*", "\\E.*\\Q").toRegex()

/**
 * Assembles a sequence of string replace operations into a function
 * @receiver List of string patterns and replacements
 * @return Callable function applying all the replacement on a given string
 */
fun Collection<Pair<String, String>>.toReplace(): (String) -> String = { str ->
    fold(str) { str, (pattern, replace) ->
        str.replace(pattern, replace)
    }
}

/**
 * Assembles a sequence of regex replace operations into a function
 * @receiver Sequence of regex patterns and replacements
 * @return Callable function applying all the replacement on a given string
 */
fun Sequence<Pair<String, String>>.toRegexReplace() = map { (pattern, replace) ->
    pattern.toRegex() to replace
}.toList().toRegexReplace()

/**
 * Assembles a sequence of regex replace operations into a function
 * @receiver List of regex patterns and replacements
 * @return Callable function applying all the replacement on a given string
 */
fun Collection<Pair<Regex, String>>.toRegexReplace(): (String) -> String = { str ->
    fold(str) { str, (regex, replace) ->
        str.replace(regex, replace)
    }
}

/**
 * Copies the characters of the given array into a new string
 * @param offset First index to read in the array
 * @param length Number of characters to read
 * @receiver Array to read from
 * @return A new string containing the copied characters
 */
fun CharArray.copyToString(offset: Int = 0,
                           length: Int = size - offset): String =
        copyToStringImpl(offset, length)

/**
 * Copies the characters of the given string into an array
 * @param destination Array to write to
 * @param offset First index to write into in the array
 * @param startIndex First index to copy from (inclusive)
 * @param endIndex Last index to copy from (exclusive)
 * @receiver String to read from
 * @return A new string containing the copied characters
 */
fun String.copyToArray(destination: CharArray = CharArray(length),
                       offset: Int = 0,
                       startIndex: Int = 0,
                       endIndex: Int = length): CharArray =
        copyToArrayImpl(destination, offset, startIndex, endIndex)

/**
 * Ensures a certain length of the string by prefixing or throwing an exception
 * @param char Character to fill with
 * @param minLength Minimum length of resulting string
 * @param maxLength Maximum length of resulting string
 * @receiver String to use
 * @throws IllegalArgumentException If initial string was already too long
 * @return String with length between minLength and maxLength (inclusive)
 */
fun String.prefixToLength(char: Char,
                          minLength: Int,
                          maxLength: Int = Int.MAX_VALUE): String {
    if (length > maxLength) throw IllegalArgumentException(
            "String already too long")
    if (length >= minLength) return this
    val output = StringBuilder(minLength)
    repeat(minLength - length) {
        output.append(char)
    }
    output.append(this)
    return output.toString()
}

/**
 * Ensures a certain length of the string by suffixing or throwing an exception
 * @param char Character to fill with
 * @param minLength Minimum length of resulting string
 * @param maxLength Maximum length of resulting string
 * @receiver String to use
 * @throws IllegalArgumentException If initial string was already too long
 * @return String with length between minLength and maxLength (inclusive)
 */
fun String.suffixToLength(char: Char,
                          minLength: Int,
                          maxLength: Int = Int.MAX_VALUE): String {
    if (length > maxLength) throw IllegalArgumentException(
            "String already too long")
    if (length >= minLength) return this
    val output = StringBuilder(minLength)
    output.append(this)
    repeat(minLength - length) {
        output.append(char)
    }
    return output.toString()
}

/**
 * Converts the given string to a mutable one
 * @receiver String to copy from
 * @return A mutable copy of the string
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toMutableString(): MutableString = MutableString(this)
