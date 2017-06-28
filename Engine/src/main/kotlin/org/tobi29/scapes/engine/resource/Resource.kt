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

package org.tobi29.scapes.engine.resource

import kotlinx.coroutines.experimental.runBlocking
import org.tobi29.scapes.engine.utils.AtomicReference
import org.tobi29.scapes.engine.utils.ConcurrentLinkedQueue
import org.tobi29.scapes.engine.utils.Result
import org.tobi29.scapes.engine.utils.task.Joiner

interface Resource<out T : Any> {
    fun tryGet(): T?

    fun get(): T

    fun onLoaded(block: () -> Unit)

    suspend fun getAsync(): T
}

internal class ThreadedResource<out T : Any>(
        private var reference: ResourceReference<T>) : Resource<T> {

    override fun tryGet(): T? = reference.value?.get()

    override fun get(): T {
        tryGet()?.let { return it }
        while (true) {
            reference.joiner.joiner.join()
            tryGet()?.let { return it }
        }
    }

    override fun onLoaded(block: () -> Unit) {
        reference.joiner.joiner.onJoin(block)
    }

    override suspend fun getAsync(): T {
        tryGet()?.let { return it }
        reference.joiner.joiner.joinAsync()
        tryGet()?.let { return it }
        throw IllegalStateException("No value after completion")
    }
}

internal class ImmediateResource<out T : Any>(val loaded: T) : Resource<T> {
    override fun tryGet(): T? = get()

    override fun get(): T = loaded

    override fun onLoaded(block: () -> Unit) = block()

    override suspend fun getAsync(): T = get()
}

class LazyResource<out T : Any>(load: suspend () -> T) : Resource<T> {
    private val completionTasks = ConcurrentLinkedQueue<() -> Unit>()
    private val load = AtomicReference<(suspend () -> T)?>(load)
    private val loaded by lazy {
        val result = this.load.get()!!.let { runBlocking { it() } }
        synchronized(this) {
            this.load.set(null)
            while (completionTasks.isNotEmpty()) {
                completionTasks.poll()()
            }
        }
        result
    }

    override fun tryGet(): T? = get()

    override fun get(): T = loaded

    override fun onLoaded(block: () -> Unit) {
        if (load.get() != null) {
            block()
            return
        }
        synchronized(this) {
            if (load.get() != null) {
                block()
            } else {
                completionTasks.add(block)
            }
        }
    }

    override suspend fun getAsync(): T = get()
}

class ResourceReference<T : Any>(value: T? = null) {
    var value: Result<T, Throwable>? = value?.let { Result.Ok(it) }
        set(value) {
            field = value
            value?.let { joiner.join() }
        }

    val resource: Resource<T> by lazy { ThreadedResource(this) }
    internal val joiner = Joiner.BasicJoinable()
}

fun <T : Any> Resource(resource: T): Resource<T> = ImmediateResource(resource)
