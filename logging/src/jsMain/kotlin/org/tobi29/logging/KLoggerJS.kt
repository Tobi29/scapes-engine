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

package org.tobi29.logging

import org.tobi29.stdex.PlatformProvidedImplementation

var logLevel: KLogLevel = KLogLevel.INFO
    set(value) {
        check(logLevel <= KLogLevel.INFO) { "INFO or higher cannot be disabled" }
        field = value
    }

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun KLogger(name: String): KLogger =
    KLogger("$name: ", null)

actual class KLogger @PublishedApi internal constructor(
    @PublishedApi
    internal val prefix: String, dummy: Nothing?
) {
    actual inline val isTraceEnabled: Boolean
        get() = logLevel <= KLogLevel.TRACE

    actual inline val isDebugEnabled: Boolean
        get() = logLevel <= KLogLevel.DEBUG

    actual inline val isInfoEnabled: Boolean
        get() = true

    actual inline val isWarnEnabled: Boolean
        get() = true

    actual inline val isErrorEnabled: Boolean
        get() = true

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun trace(message: String) {
        if (isTraceEnabled) console.log("[TRACE] $prefix$message")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun debug(message: String) {
        if (isDebugEnabled) console.log("[DEBUG] $prefix$message")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun info(message: String) {
        if (isInfoEnabled) console.info("$prefix$message")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun warn(message: String) {
        if (isWarnEnabled) console.warn("$prefix$message")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun error(message: String) {
        if (isErrorEnabled) console.error("$prefix$message")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun trace(message: String, throwable: Throwable) {
        trace("$message: ${throwable.message}")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun debug(message: String, throwable: Throwable) {
        debug("$message: ${throwable.message}")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun info(message: String, throwable: Throwable) {
        info("$message: ${throwable.message}")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun warn(message: String, throwable: Throwable) {
        warn("$message: ${throwable.message}")
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun error(message: String, throwable: Throwable) {
        error("$message: ${throwable.message}")
    }

    actual inline fun trace(message: () -> String) {
        if (isTraceEnabled) trace(message.invoke())
    }

    actual inline fun debug(message: () -> String) {
        if (isDebugEnabled) debug(message.invoke())
    }

    actual inline fun info(message: () -> String) {
        if (isInfoEnabled) info(message.invoke())
    }

    actual inline fun warn(message: () -> String) {
        if (isWarnEnabled) warn(message.invoke())
    }

    actual inline fun error(message: () -> String) {
        if (isErrorEnabled) error(message.invoke())
    }

    actual inline fun trace(throwable: Throwable, message: () -> String) {
        if (isTraceEnabled) trace(message.invoke(), throwable)
    }

    actual inline fun debug(throwable: Throwable, message: () -> String) {
        if (isDebugEnabled) debug(message.invoke(), throwable)
    }

    actual inline fun info(throwable: Throwable, message: () -> String) {
        if (isInfoEnabled) info(message.invoke(), throwable)
    }

    actual inline fun warn(throwable: Throwable, message: () -> String) {
        if (isWarnEnabled) warn(message.invoke(), throwable)
    }

    actual inline fun error(throwable: Throwable, message: () -> String) {
        if (isErrorEnabled) error(message.invoke(), throwable)
    }
}
