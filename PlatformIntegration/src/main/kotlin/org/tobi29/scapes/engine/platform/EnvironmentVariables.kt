package org.tobi29.scapes.engine.platform

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
