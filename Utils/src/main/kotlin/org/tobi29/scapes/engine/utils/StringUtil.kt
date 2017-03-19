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
 * Assembles a list of replace operations
 * @param array Matcher and replacement strings Requires 2 arguments per pattern
 * @return A function that runs the replaces on a string
 */
fun replace(vararg array: String): (String) -> String {
    if (array.size % 2 != 0) {
        throw IllegalArgumentException(
                "Amount of arguments has to be a multiple of 2")
    }
    val patterns = ArrayList<(String) -> String>(array.size shr 1)
    var i = 0
    while (i < array.size) {
        val pattern = array[i].toRegex()
        val replace = array[i + 1]
        patterns.add({ it.replace(pattern, replace) })
        i += 2
    }
    return { str ->
        var output = str
        for (pattern in patterns) {
            output = pattern(output)
        }
        output
    }
}
