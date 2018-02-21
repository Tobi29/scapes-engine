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

package org.tobi29.logging

/**
 * Pure Kotlin Logger to allow logging on both JVM and JS
 * (and Native in the future)
 */
abstract class KLogger {
    abstract val isTraceEnabled: Boolean
    abstract val isDebugEnabled: Boolean
    abstract val isInfoEnabled: Boolean
    abstract val isWarnEnabled: Boolean
    abstract val isErrorEnabled: Boolean

    /**
     * Log a message at the TRACE level.
     * @param msg the message string to be logged
     */
    abstract fun trace(msg: String)

    /**
     * Log a message at the DEBUG level.
     * @param msg the message string to be logged
     */
    abstract fun debug(msg: String)

    /**
     * Log a message at the INFO level.
     * @param msg the message string to be logged
     */
    abstract fun info(msg: String)

    /**
     * Log a message at the WARN level.
     * @param msg the message string to be logged
     */
    abstract fun warn(msg: String)

    /**
     * Log a message at the ERROR level.
     * @param msg the message string to be logged
     */
    abstract fun error(msg: String)

    /**
     * Log a message at the TRACE level.
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    open fun trace(msg: String, t: Throwable) =
        trace { "$msg: ${t.message}" }

    /**
     * Log a message at the DEBUG level.
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    open fun debug(msg: String, t: Throwable) =
        debug { "$msg: ${t.message}" }

    /**
     * Log a message at the INFO level.
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    open fun info(msg: String, t: Throwable) =
        info { "$msg: ${t.message}" }

    /**
     * Log a message at the WARN level.
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    open fun warn(msg: String, t: Throwable) =
        warn { "$msg: ${t.message}" }

    /**
     * Log a message at the ERROR level.
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    open fun error(msg: String, t: Throwable) =
        error { "$msg: ${t.message}" }

    /**
     * Log a message at the TRACE level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param msg the message string to be logged
     */
    inline fun trace(msg: () -> String) {
        if (isTraceEnabled) trace(msg.invoke())
    }

    /**
     * Log a message at the DEBUG level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param msg the message string to be logged
     */
    inline fun debug(msg: () -> String) {
        if (isDebugEnabled) debug(msg.invoke())
    }

    /**
     * Log a message at the INFO level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param msg the message string to be logged
     */
    inline fun info(msg: () -> String) {
        if (isInfoEnabled) info(msg.invoke())
    }

    /**
     * Log a message at the WARN level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param msg the message string to be logged
     */
    inline fun warn(msg: () -> String) {
        if (isWarnEnabled) warn(msg.invoke())
    }

    /**
     * Log a message at the ERROR level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param msg the message string to be logged
     */
    inline fun error(msg: () -> String) {
        if (isErrorEnabled) error(msg.invoke())
    }

    /**
     * Log a message at the TRACE level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    inline fun trace(
        t: Throwable,
        msg: () -> String
    ) {
        if (isTraceEnabled) trace(msg.invoke(), t)
    }

    /**
     * Log a message at the DEBUG level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    inline fun debug(
        t: Throwable,
        msg: () -> String
    ) {
        if (isDebugEnabled) debug(msg.invoke(), t)
    }

    /**
     * Log a message at the INFO level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    inline fun info(
        t: Throwable,
        msg: () -> String
    ) {
        if (isInfoEnabled) info(msg.invoke(), t)
    }

    /**
     * Log a message at the WARN level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    inline fun warn(
        t: Throwable,
        msg: () -> String
    ) {
        if (isWarnEnabled) warn(msg.invoke(), t)
    }

    /**
     * Log a message at the ERROR level.
     *
     * **Note:** The lambda will only get evaluated when log level is enabled
     * @param t the exception (throwable) to log
     * @param msg the message string to be logged
     */
    inline fun error(
        t: Throwable,
        msg: () -> String
    ) {
        if (isErrorEnabled) error(msg.invoke(), t)
    }
}

/**
 * Enum representing log levels
 */
enum class KLogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}
