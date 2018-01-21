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

package org.tobi29.logging.internal

import org.tobi29.logging.KLogLevel
import org.tobi29.logging.KLogger

actual internal fun createDefaultLogger(name: String): KLogger =
        ConsoleKLogger(name)

internal class ConsoleKLogger(val name: String) : KLogger() {
    override val isTraceEnabled get() = logLevel >= KLogLevel.TRACE
    override val isDebugEnabled get() = logLevel >= KLogLevel.DEBUG
    override val isInfoEnabled get() = logLevel >= KLogLevel.INFO
    override val isWarnEnabled get() = logLevel >= KLogLevel.WARN
    override val isErrorEnabled get() = logLevel >= KLogLevel.ERROR

    override fun trace(msg: String) {
        if (isTraceEnabled) console.log("[TRACE] $name: $msg")
    }

    override fun debug(msg: String) {
        if (isDebugEnabled) console.log("[DEBUG] $name: $msg")
    }

    override fun info(msg: String) {
        if (isInfoEnabled) console.log("[INFO] $name: $msg")
    }

    override fun warn(msg: String) {
        if (isWarnEnabled) console.warn("[WARN] $name: $msg")
    }

    override fun error(msg: String) {
        if (isErrorEnabled) console.error("[ERROR] $name: $msg")
    }
}

internal var logLevel = KLogLevel.INFO
