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

package org.tobi29.utils

/**
 * Creates a hash from the given [String]
 * @receiver String to create the hash from
 * @param start Base value for creating the hash
 * @return A 64-bit hash
 */
fun String.longHashCode(start: Long = 0L): Long {
    var h = start
    val length = length
    for (i in 0 until length) {
        h = 31 * h + this[i].toLong()
    }
    return h
}

/**
 * Converts a wildcard expression into a [Regex]
 * @return A [Regex] matching like the wildcard expression
 */
fun wildcard(expression: String) =
    Regex.escape(expression)
        .replace("?", "\\E.?\\Q")
        .replace("*", "\\E.*\\Q")
        .toRegex()

/**
 * Assembles a sequence of string replace operations into a function
 * @return Callable function applying all the replacements on a given string
 */
fun Iterable<Pair<String, String>>.toReplace(): (String) -> String =
    { initialStr ->
        fold(initialStr) { str, (pattern, replace) ->
            str.replace(pattern, replace)
        }
    }

/**
 * Assembles a sequence of regex replace operations into a function
 * @return Callable function applying all the replacements on a given string
 */
fun Iterable<Pair<String, String>>.toRegexReplace() =
    map { (pattern, replace) ->
        pattern.toRegex() to replace
    }.toRegexReplace()

/**
 * Assembles a sequence of regex replace operations into a function
 * @return Callable function applying all the replacements on a given string
 */
fun Iterable<Pair<Regex, String>>.toRegexReplace(): (String) -> String =
    { initialStr ->
        fold(initialStr) { str, (regex, replace) ->
            str.replace(regex, replace)
        }
    }
