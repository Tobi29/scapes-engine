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

package org.tobi29.io

import java.io.PrintWriter
import java.io.StringWriter

private val SYSTEM_PROPERTIES = arrayOf("os.name", "os.version", "os.arch",
        "java.runtime.name", "java.version", "java.class.version",
        "java.vendor")

fun crashReportSectionStacktrace(e: Throwable): Pair<String, Appendable.() -> Unit> =
        Pair<String, Appendable.() -> Unit>("Stacktrace", {
            StringWriter().use { writer ->
                e.printStackTrace(PrintWriter(writer))
                println(writer.toString())
            }
        })

fun crashReportSectionActiveThreads(): Pair<String, Appendable.() -> Unit> =
        Pair<String, Appendable.() -> Unit>("Active Threads", {
            for ((key1, value1) in Thread.getAllStackTraces()) {
                println("Thread:" + key1)
                for (element in value1) {
                    println("\tat " + element)
                }
                println()
            }
        })

fun crashReportSectionSystemProperties(): Pair<String, Appendable.() -> Unit> =
        "System properties" to crashReportSectionProperties(
                SYSTEM_PROPERTIES.asSequence().map {
                    it to System.getProperty(it)
                })
