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

package org.tobi29.scapes.engine.utils.io.filesystem

import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.tobi29.scapes.engine.utils.io.WritableByteStream
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets

private val SYSTEM_PROPERTIES = arrayOf("os.name", "os.version", "os.arch",
        "java.runtime.name", "java.version", "java.class.version",
        "java.vendor")
private val FORMATTER = DateTimeFormatter.ISO_DATE_TIME
private val FILE_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd_HH-mm-ss")
private val LN = "\n"

fun file(path: FilePath,
         time: OffsetDateTime = OffsetDateTime.now()): FilePath {
    return path.resolve(
            "CrashReport-" + time.format(FILE_FORMATTER) + ".txt")
}

fun writeCrashReport(e: Throwable,
                     path: FilePath,
                     name: String,
                     debugValues: Map<String, String>,
                     time: OffsetDateTime = OffsetDateTime.now()) {
    write(path, { stream ->
        ln(stream, name + " has crashed: " + e)
        ln(stream)
        ln(stream,
                "Please give this file to someone who has an idea of what to do with it (developer!)")
        ln(stream)
        ln(stream, "-----------Stacktrace:-----------")
        StringWriter().use { writer ->
            e.printStackTrace(PrintWriter(writer))
            ln(stream, writer.toString())
        }
        ln(stream)
        ln(stream, "---------Active Threads:---------")
        for ((key1, value1) in Thread.getAllStackTraces()) {
            ln(stream, "Thread:" + key1)
            for (element in value1) {
                ln(stream, "\tat " + element)
            }
            ln(stream)
        }
        ln(stream, "--------System properties:-------")
        for (property in SYSTEM_PROPERTIES) {
            ln(stream, property + " = " + System.getProperty(property))
        }
        ln(stream)
        ln(stream, "----------Debug values:----------")
        for ((key, value) in debugValues) {
            ln(stream, key + " = " + value)
        }
        ln(stream)
        ln(stream, "--------------Time:--------------")
        ln(stream, LocalDateTime.from(time).format(FORMATTER))
    })
}

private fun ln(stream: WritableByteStream) {
    print(stream, LN)
}

private fun ln(stream: WritableByteStream,
               line: String) {
    print(stream, line + LN)
}

private fun print(stream: WritableByteStream,
                  str: String) {
    val array = str.toByteArray(StandardCharsets.UTF_8)
    stream.put(array)
}