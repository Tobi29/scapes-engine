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

import org.tobi29.stdex.InlineUtility
import kotlin.reflect.KClass

/**
 * Returns a logger named using [name]
 */
expect fun KLogger(name: String): KLogger

/**
 * Returns a logger named after the given [clazz]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun KLogger(clazz: KClass<*>): KLogger = KLogger(clazz.loggerName)

/**
 * Returns a logger named after the class of the receiver
 */
fun Any.KLogger(): KLogger = KLogger(this::class)

/**
 * Multiplatform Kotlin Logger
 */
expect class KLogger {
    val isTraceEnabled: Boolean
    val isDebugEnabled: Boolean
    val isInfoEnabled: Boolean
    val isWarnEnabled: Boolean
    val isErrorEnabled: Boolean

    /**
     * Logs a message at the TRACE level.
     */
    fun trace(message: String)

    /**
     * Logs a message at the DEBUG level.
     */
    fun debug(message: String)

    /**
     * Logs a message at the INFO level.
     */
    fun info(message: String)

    /**
     * Logs a message at the WARN level.
     */
    fun warn(message: String)

    /**
     * Logs a message at the ERROR level.
     */
    fun error(message: String)

    /**
     * Logs a message at the TRACE level.
     */
    fun trace(message: String, throwable: Throwable)

    /**
     * Logs a message at the DEBUG level.
     */
    fun debug(message: String, throwable: Throwable)

    /**
     * Logs a message at the INFO level.
     */
    fun info(message: String, throwable: Throwable)

    /**
     * Logs a message at the WARN level.
     */
    fun warn(message: String, throwable: Throwable)

    /**
     * Logs a message at the ERROR level.
     */
    fun error(message: String, throwable: Throwable)

    /**
     * Logs a message at the TRACE level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun trace(message: () -> String)

    /**
     * Logs a message at the DEBUG level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun debug(message: () -> String)

    /**
     * Logs a message at the INFO level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun info(message: () -> String)

    /**
     * Logs a message at the WARN level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun warn(message: () -> String)

    /**
     * Logs a message at the ERROR level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun error(message: () -> String)

    /**
     * Logs a message at the TRACE level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun trace(throwable: Throwable, message: () -> String)

    /**
     * Logs a message at the DEBUG level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun debug(throwable: Throwable, message: () -> String)

    /**
     * Logs a message at the INFO level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun info(throwable: Throwable, message: () -> String)

    /**
     * Logs a message at the WARN level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun warn(throwable: Throwable, message: () -> String)

    /**
     * Logs a message at the ERROR level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     */
    fun error(throwable: Throwable, message: () -> String)
}

/**
 * Enum representing log levels
 */
enum class KLogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}
