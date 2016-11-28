/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils

import org.tobi29.scapes.engine.utils.spi.CPUReaderProvider
import java.util.*

/**
 * Object to read the current cpu usage of the program
 */
object CPUUtil {
    private val PROVIDER = loadService()

    private fun loadService(): CPUReaderProvider? {
        for (provider in ServiceLoader.load(CPUReaderProvider::class.java)) {
            try {
                if (provider.available()) {
                    return provider
                }
            } catch (e: ServiceConfigurationError) {
            }
        }
        return null
    }

    /**
     * Returns a new [Reader] if available on the current platform
     * @return A new [Reader] instance or null
     */
    fun reader(): Reader? {
        return PROVIDER?.reader()
    }

    /**
     * Allows reading the current cpu usage for a set of threads or the entire
     * program
     */
    interface Reader {
        /**
         * Returns the cpu usage of the entire program as an average between
         * invocations
         * @return The cpu usage of the entire program
         */
        fun totalCPU(): Double

        /**
         * Returns the cpu usage of the specified threads program as an average
         * between invocations
         * @param threads Array of thread ids
         * @return The cpu usage of the specified threads
         */
        fun totalCPU(threads: LongArray): Double
    }
}
