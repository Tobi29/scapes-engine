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

import org.tobi29.logging.internal.createDefaultLogger
import org.tobi29.logging.internal.name

/**
 * Returns a logger named after the given [loggable] or [name]
 * @param loggable Class to fetch the name from
 * @param name Allows overriding the name
 */
fun KLogger(
    loggable: KLoggable,
    name: String? = null
): KLogger = KLogger(name ?: loggable.name)

/**
 * Returns a logger named using [name]
 * @param name The name for the logger
 */
fun KLogger(name: String): KLogger = createDefaultLogger(name)
