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

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.ActorScope
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel

/**
 * Launch a new single thread context and execute [block] on it
 *
 * Upon completion of [block] the thread dispatcher is closed
 *
 * This aims at being a coroutine compatible alternative to creating a
 * [Thread] manually, in case e.g. precise timings are required or blocking
 * cannot be avoided
 * @param name Name for thread context
 * @param parent Parent job for thread context
 * @param block Code to execute on thread context
 * @return ThreadJob for the running [block]
 */
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

/**
 * Launch a new single thread context and execute the actor [block] on it
 *
 * Upon completion of [block] the thread context is cancelled
 * @param name Name for thread context
 * @param capacity Capacity for created [Channel]
 * @param block Code to execute on thread context
 * @return ActorThreadJob for the running [block]
 */
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

interface ThreadJob : Job {
    val thread: Thread
}

interface ActorThreadJob<in E> : SendChannel<E>,
    ThreadJob

// TODO: Remove after 0.0.14

@Deprecated("Use with CoroutineScope")
fun launchThread(
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
