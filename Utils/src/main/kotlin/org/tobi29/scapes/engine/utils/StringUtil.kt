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
 * @param h     Base value for creating the hash
 * @return A 64-bit hash
 */
fun hash(value: String,
         h: Long = 0L): Long {
    var h = h
    val length = value.length
    for (i in 0..length - 1) {
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

/*
header fun ByteArray.strUTF8(): String

header fun String.bytesUTF8(): ByteArray
*/
