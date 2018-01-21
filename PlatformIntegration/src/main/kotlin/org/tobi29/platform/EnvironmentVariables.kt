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

package org.tobi29.platform

import kotlin.reflect.KProperty

/**
 * Object for accessing environment variables
 *
 * One can use this object for delegated properties as follows:
 * ```Kotlin
 * val PATH by EnvironmentVariable
 * ```
 */
object EnvironmentVariable {
    /**
     * Returns the environment variable set on the given [key]
     * @param key Environment variable to look for
     * @returns The environment variable value or `null` of not set
     */
    operator fun get(key: String): String? = environmentVariableImpl(key)

    /**
     * Integration for delegated properties
     */
    operator fun getValue(thisRef: Any?,
                          property: KProperty<*>): String? = get(property.name)
}
