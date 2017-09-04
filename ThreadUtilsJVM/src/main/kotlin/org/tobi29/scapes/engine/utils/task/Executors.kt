package org.tobi29.scapes.engine.utils.task

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import org.tobi29.scapes.engine.utils.AtomicInteger
import org.tobi29.scapes.engine.utils.checkPermission
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
    val i = AtomicInteger(1)
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
