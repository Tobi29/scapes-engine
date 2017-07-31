package org.tobi29.scapes.engine.utils.io

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
