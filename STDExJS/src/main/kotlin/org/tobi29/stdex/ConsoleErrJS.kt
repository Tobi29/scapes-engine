/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Based on println implementation for Kotlin/JS

package org.tobi29.stdex

private abstract class BaseOutputErr {
    open fun println() {
        print("\n")
    }

    open fun println(message: Any?) {
        print(message)
        println()
    }

    abstract fun print(message: Any?)

    open fun flush() {}
}

private class NodeJsOutputErr(val outputStream: dynamic) : BaseOutputErr() {
    override fun print(message: Any?): dynamic =
            outputStream.write(String(message))
}

private open class BufferedOutputErr : BaseOutputErr() {
    var buffer = ""

    override fun print(message: Any?) {
        buffer += String(message)
    }

    override fun flush() {
        buffer = ""
    }
}

private class BufferedOutputToConsoleErr : BufferedOutputErr() {
    override fun print(message: Any?) {
        var s = String(message)
        val i = s.lastIndexOf('\n')
        if (i >= 0) {
            buffer += s.substring(0, i)
            flush()
            s = s.substring(i + 1)
        }
        buffer += s
    }

    override fun flush() {
        console.error(buffer)
        buffer = ""
    }
}

private var outputErr = run {
    val isNode: Boolean = js(
            "typeof process !== 'undefined' && process.versions && !!process.versions.node")
    if (isNode) NodeJsOutputErr(
            js("process.stderr")) else BufferedOutputToConsoleErr()
}

private inline fun String(value: Any?): String = js("String")(value)

actual fun printerr(message: Any?) {
    outputErr.print(message)
}

actual fun printerrln(message: Any?) {
    outputErr.println(message)
}

actual fun printerrln() {
    outputErr.println()
}
