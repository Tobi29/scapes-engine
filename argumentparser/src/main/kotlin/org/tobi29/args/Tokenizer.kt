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

package org.tobi29.args

/**
 * Splits the given string into tokens using space, whilst taking single and
 * double quotes into account
 * @receiver The string to split
 * @return The list of tokens
 */
fun String.tokenize(): List<String> {
    if (isEmpty()) {
        return emptyList()
    }

    val output = ArrayList<String>()

    var inSingle = false
    var inDouble = false
    var builder = StringBuilder()
    for (char in this) {
        when (char) {
            '\'' -> {
                if (inDouble) {
                    builder.append(char)
                } else {
                    inSingle = !inSingle
                }
            }
            '"' -> {
                if (inSingle) {
                    builder.append(char)
                } else {
                    inDouble = !inDouble
                }
            }
            ' ' -> {
                if (inSingle || inDouble) {
                    builder.append(char)
                } else {
                    output.add(builder.toString())
                    builder = StringBuilder()
                }
            }
            else -> builder.append(char)
        }
    }
    if (!inSingle && !inDouble) {
        output.add(builder.toString())
    }

    return output
}
