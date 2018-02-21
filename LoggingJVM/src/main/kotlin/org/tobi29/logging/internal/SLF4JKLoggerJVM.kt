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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.tobi29.logging.KLogger

internal actual fun createDefaultLogger(name: String): KLogger =
    SLF4JKLogger(LoggerFactory.getLogger(name))

internal class SLF4JKLogger(private val logger: Logger) : KLogger() {
    override val isTraceEnabled get() = logger.isTraceEnabled
    override val isDebugEnabled get() = logger.isDebugEnabled
    override val isInfoEnabled get() = logger.isInfoEnabled
    override val isWarnEnabled get() = logger.isWarnEnabled
    override val isErrorEnabled get() = logger.isErrorEnabled

    override fun trace(msg: String) = logger.trace(msg)
    override fun debug(msg: String) = logger.debug(msg)
    override fun info(msg: String) = logger.info(msg)
    override fun warn(msg: String) = logger.warn(msg)
    override fun error(msg: String) = logger.error(msg)

    override fun trace(msg: String, t: Throwable) = logger.trace(msg, t)
    override fun debug(msg: String, t: Throwable) = logger.debug(msg, t)
    override fun info(msg: String, t: Throwable) = logger.info(msg, t)
    override fun warn(msg: String, t: Throwable) = logger.warn(msg, t)
    override fun error(msg: String, t: Throwable) = logger.error(msg, t)
}
