package org.tobi29.scapes.engine.utils.logging.internal

import java.lang.reflect.Modifier
import kotlin.reflect.KClass

// Based on https://github.com/MicroUtils/kotlin-logging
@Suppress("NOTHING_TO_INLINE")
internal object KLoggerNameResolver {
    inline internal fun <T : Any> name(forClass: KClass<T>): String =
            KLoggerNameResolver.unwrapCompanionClass(
                    forClass).java.simpleName ?: "???"

    inline private fun <T : Any> unwrapCompanionClass(clazz: KClass<T>): KClass<*> {
        if (clazz.java.enclosingClass != null) {
            try {
                val field = clazz.java.enclosingClass.getField(
                        clazz.java.simpleName)
                if (Modifier.isStatic(field.modifiers) && field.type == clazz) {
                    return clazz.java.enclosingClass.kotlin
                }
            } catch(e: Exception) {
            }
        }
        return clazz
    }
}
