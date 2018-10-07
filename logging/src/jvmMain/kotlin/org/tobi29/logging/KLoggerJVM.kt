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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.tobi29.stdex.PlatformProvidedImplementation

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun KLogger(name: String): KLogger =
    KLogger(LoggerFactory.getLogger(name))

actual class KLogger @PublishedApi internal constructor(
    @PublishedApi
    internal val logger: Logger
) {
    actual inline val isTraceEnabled: Boolean
        get() = logger.isTraceEnabled
    actual inline val isDebugEnabled: Boolean
        get() = logger.isDebugEnabled
    actual inline val isInfoEnabled: Boolean
        get() = logger.isInfoEnabled
    actual inline val isWarnEnabled: Boolean
        get() = logger.isWarnEnabled
    actual inline val isErrorEnabled: Boolean
        get() = logger.isErrorEnabled

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun trace(message: String) {
        logger.trace(message)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun debug(message: String) {
        logger.debug(message)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun info(message: String) {
        logger.info(message)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun warn(message: String) {
        logger.warn(message)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun error(message: String) {
        logger.error(message)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun trace(message: String, throwable: Throwable) {
        logger.trace(message, throwable)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun debug(message: String, throwable: Throwable) {
        logger.debug(message, throwable)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun info(message: String, throwable: Throwable) {
        logger.info(message, throwable)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun warn(message: String, throwable: Throwable) {
        logger.warn(message, throwable)
    }

    @PlatformProvidedImplementation
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun error(message: String, throwable: Throwable) {
        logger.error(message, throwable)
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
