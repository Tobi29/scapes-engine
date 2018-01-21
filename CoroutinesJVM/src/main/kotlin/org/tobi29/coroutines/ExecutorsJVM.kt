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

package org.tobi29.coroutines

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import org.tobi29.stdex.atomic.AtomicInt
import org.tobi29.stdex.checkPermission
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

private val commonExecutor by lazy {
    (tryGetForkJoinPoolCommon() ?:
            run {
                val parallelism = defaultParallelism()
                tryCreateForkJoinPool(parallelism)
                        ?: createScheduledExecutor("Background-Executor",
                        parallelism) { isDaemon = true }
            }).asCoroutineDispatcher()
}

val defaultBackgroundExecutor
    get() = run {
        checkPermission("scapesengine.defaultbackgroundexecutor")
        commonExecutor
    }

fun ExecutorService.convertToCoroutineDispatcher(parent: Job? = null) =
        asCoroutineDispatcher().let { dispatcher ->
            val job = Job(parent)
            job.invokeOnCompletion { shutdown() }
            dispatcher + job
        }

fun createScheduledExecutor(name: String,
                            parallelism: Int,
                            threadInit: Thread.() -> Unit): ScheduledExecutorService {
    val i = AtomicInt(1)
    return createScheduledExecutor(parallelism) {
        Thread(it, "$name-${i.incrementAndGet()}").apply { threadInit() }
    }
}

fun createScheduledExecutor(parallelism: Int,
                            threadFactory: (Runnable) -> Thread): ScheduledExecutorService =
        Executors.newScheduledThreadPool(parallelism) { threadFactory(it) }

private fun tryGetForkJoinPoolCommon() =
        try {
            val fjp = Class.forName("java.util.concurrent.ForkJoinPool")
            val fjpCommonPool = fjp.getMethod("commonPool")
            fjpCommonPool.invoke(null) as? ExecutorService
        } catch (e: Throwable) {
            null
        }

private fun tryCreateForkJoinPool(parallelism: Int) =
        try {
            val fjp = Class.forName("java.util.concurrent.ForkJoinPool")
            val fjpNew = fjp.getConstructor(Int::class.java)
            fjpNew.newInstance(parallelism) as? ExecutorService
        } catch (e: Throwable) {
            null
        }

private fun defaultParallelism() =
        try {
            val fjp = Class.forName("java.util.concurrent.ForkJoinPool")
            val fjpParallelism = fjp.getMethod("getCommonPoolParallelism")
            fjpParallelism.invoke(null) as? Int
        } catch (e: Throwable) {
            null
        } ?: (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)
