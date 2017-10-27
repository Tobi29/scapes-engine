package org.tobi29.scapes.engine.utils.logging.internal

import org.tobi29.scapes.engine.utils.logging.KLoggable
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

// Based on https://github.com/MicroUtils/kotlin-logging
@Suppress("NOTHING_TO_INLINE")
actual internal object KLoggerNameResolver {
    inline actual fun name(loggable: KLoggable): String =
            unwrapCompanionClass(loggable::class).java.simpleName ?: "???"

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
