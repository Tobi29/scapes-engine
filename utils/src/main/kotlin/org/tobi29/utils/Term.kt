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

import org.tobi29.arrays.ElementsRO2

/**
 * Prints a table with left aligned columns
 *
 * It will always end with a newline so one should not add it a second time
 * @param appendable Output to print to
 * @param delimiter String between columns
 * @param prefix String before every line
 * @param suffix String after every line
 * @param newline String to terminate every line
 */
fun ElementsRO2<String>.formatTable(
    appendable: Appendable,
    delimiter: String = " ",
    prefix: String = "",
    suffix: String = "",
    newline: String = "\n"
) {
    val lineEnd = "$suffix$newline"
    val maxWidths = IntArray(width) { x ->
        var max = 0
        for (y in 0 until height) {
            max = max.coerceAtLeast(this[x, y].length)
        }
        max
    }
    for (y in 0 until height) {
        appendable.append(prefix)
        for (x in 0 until width) {
            appendable.append(this[x, y])
            if (x != width - 1) {
                appendable.append(delimiter)
                repeat(maxWidths[x] - this[x, y].length) {
                    appendable.append(' ')
                }
            }
        }
        appendable.append(lineEnd)
    }
}
