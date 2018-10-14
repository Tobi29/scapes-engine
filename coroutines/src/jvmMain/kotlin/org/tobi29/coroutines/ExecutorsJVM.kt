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

import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import org.tobi29.stdex.atomic.AtomicInt
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

// TODO: Remove after 0.0.14

@Deprecated(
    "Use Dispatchers.Default",
    ReplaceWith(
        "Dispatchers.Default",
        "kotlinx.coroutines.experimental.Dispatchers"
    )
)
val defaultBackgroundExecutor
    get() = Dispatchers.Default

@Deprecated("Use asCoroutineDispatcher manually")
fun ExecutorService.convertToCoroutineDispatcher(parent: Job? = null) =
    asCoroutineDispatcher().let { dispatcher ->
        val job = Job(parent)
        job.invokeOnCompletion { shutdown() }
        dispatcher + job
    }

@Deprecated("Removed without replacement")
fun createScheduledExecutor(
    name: String,
    parallelism: Int,
    threadInit: Thread.() -> Unit
): ScheduledExecutorService {
    val i = AtomicInt(1)
    return createScheduledExecutor(parallelism) {
        Thread(it, "$name-${i.incrementAndGet()}").apply { threadInit() }
    }
}

@Deprecated("Removed without replacement")
fun createScheduledExecutor(
    parallelism: Int,
    threadFactory: (Runnable) -> Thread
): ScheduledExecutorService =
    Executors.newScheduledThreadPool(parallelism) { threadFactory(it) }
