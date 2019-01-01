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

package org.tobi29.stdex

import kotlin.reflect.KClass

// Common annotations

/**
 * Function might throw any of [exceptionClasses] in valid execution of the
 * program
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR
)
expect annotation class Throws(
    vararg val exceptionClasses: KClass<out Throwable>
)

/**
 * Field should be accessible from multiple threads without further
 * synchronization by providing sufficient memory barriers for accesses
 */
@Target(
    AnnotationTarget.FIELD
)
expect annotation class Volatile()

/**
 * Small inlined function, which should never be referenced directly after
 * compilation by the binary
 *
 * **Note:** Should have `@Suppress("NOTHING_TO_INLINE")` as needed
 *
 * **Note:** If possible might imply something comparable to
 * `kotlin.internal.InlineOnly` in the future
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.SOURCE)
annotation class InlineUtility

/**
 * Similar to [InlineUtility] but implies that the bulk of the implementation
 * is provided by the platform
 *
 * **Note:** Should have `@Suppress("NOTHING_TO_INLINE")` as needed
 *
 * **Note:** If possible might imply something comparable to
 * `kotlin.internal.InlineOnly` in the future
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.SOURCE)
annotation class PlatformProvidedImplementation

/**
 * Constant property, consider using `const` modifier in the future depending
 * in its specification
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.SOURCE)
annotation class Constant

// JVM specific annotations

/**
 * Cross-platform version of `kotlin.jvm.JvmOverloads`
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR
)
expect annotation class JvmOverloads()

/**
 * Cross-platform version of `kotlin.jvm.JvmStatic`
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
expect annotation class JvmStatic()

/**
 * Cross-platform version of `kotlin.jvm.JvmName`
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FILE
)
expect annotation class JvmName(val name: String)

/**
 * Cross-platform version of `kotlin.jvm.JvmMultifileClass`
 */
@Target(
    AnnotationTarget.FILE
)
expect annotation class JvmMultifileClass()

/**
 * Cross-platform version of `kotlin.jvm.JvmField`
 */
@Target(
    AnnotationTarget.FIELD
)
expect annotation class JvmField()

// JS specific annotations

/**
 * Cross-platform version of `kotlin.js.JsField`
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
expect annotation class JsName(val name: String)
