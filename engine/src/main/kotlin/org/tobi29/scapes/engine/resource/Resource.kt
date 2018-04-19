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

package org.tobi29.scapes.engine.resource

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Resource<out T : Any> {
    fun get(): Deferred<T>

    fun tryGet(): T?

    fun onLoaded(block: (T) -> Unit)

    suspend fun getAsync(): T
}

internal class ImmediateResource<out T : Any>(private val loaded: T) : Resource<T> {
    override fun get(): Deferred<T> = CompletableDeferred(loaded)

    override fun tryGet(): T? = loaded

    override fun onLoaded(block: (T) -> Unit) = block(loaded)

    override suspend fun getAsync(): T = loaded
}

class DeferredResource<out T : Any>(private val loaded: Deferred<T>) : Resource<T> {
    override fun get(): Deferred<T> = loaded

    override fun tryGet(): T? =
            if (loaded.isCompleted) loaded.getCompleted() else null

    override fun onLoaded(block: (T) -> Unit) {
        loaded.invokeOnCompletion { block(loaded.getCompleted()) }
    }

    override suspend fun getAsync(): T = loaded.await()
}

fun <T : Any> Resource(resource: T): Resource<T> = ImmediateResource(resource)

fun <T : Any> Resource<T>.asProperty(fail: () -> Throwable) = object : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?,
                          property: KProperty<*>): T = tryGet() ?: throw fail()
}
