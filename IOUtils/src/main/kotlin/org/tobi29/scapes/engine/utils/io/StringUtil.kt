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

package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.Readable
import kotlin.math.min

/**
 * Reads a complete line and returns it dropping [ln] in the process
 * @param ln The newline string to check for
 * @receiver The source to read from
 * @returns The line
 * @throws IOException Read implementation may throw on errors
 * @throws EndOfStreamException When stream ends prematurely
 */
fun Readable.readLine(ln: String = "\n"): String =
        readLineTry(ln) ?: throw EndOfStreamException()

/**
 * Reads a complete line and writes it to [output] dropping [ln] in
 * the process
 *
 * **Note:** If the stream ends before the line ends it will write everything up
 * to the end into [output] and throw [EndOfStreamException]
 * @param output Output to write to
 * @param ln The newline string to check for
 * @receiver The source to read from
 * @throws IOException Read implementation may throw on errors
 * @throws EndOfStreamException When stream ends prematurely
 */
fun Readable.readLine(output: Appendable,
                      ln: String = "\n") {
    if (!readLineTry(output, ln)) throw EndOfStreamException()
}

/**
 * Reads a complete line and returns it dropping [ln] in the process
 * @param ln The newline string to check for
 * @receiver The source to read from
 * @returns The line or `null` if the stream ended
 * @throws IOException Read implementation may throw on errors
 */
fun Readable.readLineTry(ln: String = "\n"): String? {
    val output = StringBuilder()
    if (!readLineTry(output, ln)) return null
    return output.toString()
}

/**
 * Reads a complete line and writes it to [output] dropping [ln] in
 * the process
 *
 * **Note:** If the stream ends before the line ends it will write everything up
 * to the end into [output] and return `false`
 * @param output Output to write to
 * @param ln The newline string to check for
 * @receiver The source to read from
 * @returns `true` on success or `false` if the stream ended before reaching [ln]
 * @throws IOException Read implementation may throw on errors
 */
fun Readable.readLineTry(output: Appendable,
                         ln: String = "\n"): Boolean {
    if (ln.isEmpty())
        throw IllegalArgumentException("Newline string cannot be empty")
    val lnchar = ln.last()
    if (ln.length == 1) return readLineTry(output, lnchar)
    val lnbuf = CharArray(ln.length - 1)
    var read = 0
    var lnpos = 0
    try {
        loop@ while (true) {
            val char = readTry().let {
                if (it < 0) return false
                it.toChar()
            }
            read++
            if (char == lnchar && read >= ln.length) {
                var lnback = lnpos
                var i = ln.length - 2
                while (true) {
                    if (lnbuf[lnback] != ln[i]) break
                    if (i-- <= 0) {
                        read = 0
                        break@loop
                    }
                    lnback = (lnback + ln.length) % (ln.length - 1)
                }
            }
            lnpos = (lnpos + 1) % (ln.length - 1)
            output.append(lnbuf[lnpos])
            lnbuf[lnpos] = char
        }
    } finally {
        for (i in 0 until min(read, ln.length - 1)) {
            lnpos = (lnpos + 1) % (ln.length - 1)
            output.append(lnbuf[lnpos])
        }
    }
    return true
}

private fun Readable.readLineTry(output: Appendable,
                                 ln: Char): Boolean {
    while (true) {
        val char = readTry().let {
            if (it < 0) return false
            it.toChar()
        }
        if (char == ln) break
        output.append(char)
    }
    return true
}
