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

package org.tobi29.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.tobi29.utils.Duration64Nanos
import org.tobi29.utils.sleepNanos
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.coroutines.CoroutineContext

actual fun CoroutineScope.newResponsiveContext(
    context: CoroutineContext
): Pair<CoroutineContext, () -> Unit> {
    val name = context[CoroutineName.Key]?.name
    val defaultThreadFactory = Executors.defaultThreadFactory()
    val threadFactory = if (name == null) {
        Executors.defaultThreadFactory()
    } else {
        ThreadFactory { target ->
            defaultThreadFactory.newThread(target).apply { setName(name) }
        }
    }
    return Executors.newSingleThreadScheduledExecutor(threadFactory)
        .asCoroutineDispatcher().let { (context + it) to { it.close() } }
}

actual class ResponsiveCoroutineScope actual constructor(
    private val delegate: CoroutineScope
) : CoroutineScope by delegate {
    actual suspend inline fun delayResponsiveNanos(time: Duration64Nanos) {
        sleepNanos(time)
        yield()
    }
}

// TODO: Remove after 0.0.14

@Deprecated("Use launchResponsive or similar")
fun CoroutineScope.launchThread(
    name: String,
    block: suspend CoroutineScope.() -> Unit
): ThreadJob {
    val context = newSingleThreadContext(name)
    val thread = runBlocking(context) { Thread.currentThread() }
    val job = launch(context, block = block)
    job.invokeOnCompletion { context.close() }
    return object : ThreadJob,
        Job by job {
        override val thread = thread
    }
}

@Deprecated("Use launchResponsive or similar")
fun <E> CoroutineScope.actorThread(
    name: String,
    capacity: Int = 0,
    block: suspend ActorScope<E>.() -> Unit
): ActorThreadJob<E> {
    val channel = Channel<E>(capacity)
    val job = launchThread(name) {
        val scope = object : ActorScope<E>,
            CoroutineScope by this,
            ReceiveChannel<E> by channel {
            override val channel get() = channel
        }
        block(scope)
    }
    return object : ActorThreadJob<E>,
        ThreadJob by job,
        SendChannel<E> by channel {
    }
}

@Deprecated("Use launchResponsive or similar")
interface ThreadJob : Job {
    val thread: Thread
}

@Deprecated("Use launchResponsive or similar")
interface ActorThreadJob<in E> : SendChannel<E>, ThreadJob

@Deprecated("Use with CoroutineScope")
fun launchThread(
    name: String,
    block: suspend CoroutineScope.() -> Unit
): ThreadJob {
    val context = newSingleThreadContext(name)
    val thread = runBlocking(context) { Thread.currentThread() }
    val job = GlobalScope.launch(context, block = block)
    job.invokeOnCompletion { context.close() }
    return object : ThreadJob,
        Job by job {
        override val thread = thread
    }
}

@Deprecated("Use with CoroutineScope")
fun <E> actorThread(
    name: String,
    capacity: Int = 0,
    block: suspend ActorScope<E>.() -> Unit
): ActorThreadJob<E> {
    val channel = Channel<E>(capacity)
    val job = launchThread(name) {
        val scope = object : ActorScope<E>,
            CoroutineScope by this,
            ReceiveChannel<E> by channel {
            override val channel get() = channel
        }
        block(scope)
    }
    return object : ActorThreadJob<E>,
        ThreadJob by job,
        SendChannel<E> by channel {
    }
}
