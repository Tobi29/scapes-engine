/*
 * Copyright 2012-2019 Tobi29
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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.resource

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.tobi29.coroutines.tryGet
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : Any> Deferred<T>.asProperty(fail: () -> Throwable) =
    object : ReadOnlyProperty<Any?, T> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>
        ): T = tryGet() ?: throw fail()
    }

/**
 * Calls [handler] with the completed value as soon as it is available
 *
 * **Note:** Just like with [Deferred.invokeOnCompletion] [handler] should be
 * lock-free and fast and not throw any exceptions
 * @param handler Handler to be called
 * @receiver Deferred to await
 */
inline fun <T> Deferred<T>.onLoaded(crossinline handler: (T) -> Unit) {
    invokeOnCompletion { handler(getCompleted()) }
}

// TODO: Remove after 0.0.14

@Deprecated(
    "Use Deferred",
    ReplaceWith("Deferred", "kotlinx.coroutines.Deferred")
)
typealias Resource<T> = Deferred<T>

@Deprecated("Use Deferred", ReplaceWith("this"))
inline fun <T> Resource<T>.get(): Deferred<T> = this

@Deprecated("Use Deferred", ReplaceWith("await()"))
suspend inline fun <T> Resource<T>.getAsync(): T = await()

@Deprecated(
    "Use Deferred",
    ReplaceWith(
        "CompletableDeferred(resource)",
        "kotlinx.coroutines.CompletableDeferred"
    )
)
inline fun <T : Any> Resource(resource: T): Resource<T> =
    CompletableDeferred(resource)

@Deprecated(
    "Use Deferred",
    ReplaceWith("Deferred", "kotlinx.coroutines.Deferred")
)
typealias DeferredResource<T> = Deferred<T>

@Deprecated("Use Deferred", ReplaceWith("loaded"))
inline fun <T> DeferredResource(loaded: Deferred<T>): DeferredResource<T> =
    loaded
