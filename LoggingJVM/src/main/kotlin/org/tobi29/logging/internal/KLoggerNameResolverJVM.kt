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

import org.tobi29.logging.KLoggable
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

// Based on https://github.com/MicroUtils/kotlin-logging
@Suppress("NOTHING_TO_INLINE")
actual internal object KLoggerNameResolver {
    inline actual fun name(loggable: KLoggable): String =
            unwrapCompanionClass(
                    loggable::class).java.simpleName ?: "???"

    inline private fun <T : Any> unwrapCompanionClass(clazz: KClass<T>): KClass<*> {
        if (clazz.java.enclosingClass != null) {
            try {
                val field = clazz.java.enclosingClass.getField(
                        clazz.java.simpleName)
                if (Modifier.isStatic(field.modifiers)
                        && field.type == clazz.java) {
                    return clazz.java.enclosingClass.kotlin
                }
            } catch(e: Exception) {
            }
        }
        return clazz
    }
}
