/*
 * Copyright 2016-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.coroutines.experimental

import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

private val KEEP_ALIVE = java.lang.Long.getLong(
        "kotlinx.coroutines.ScheduledExecutor.keepAlive", 1000L)

@Volatile
private var _scheduledExecutor: ScheduledExecutorService? = null

internal val scheduledExecutor: ScheduledExecutorService get() =
_scheduledExecutor ?: getOrCreateScheduledExecutorSync()

@Synchronized
private fun getOrCreateScheduledExecutorSync(): ScheduledExecutorService =
        _scheduledExecutor ?: ScheduledThreadPoolExecutor(1) { r ->
            Thread(r,
                    "kotlinx.coroutines.ScheduledExecutor").apply { isDaemon = true }
        }.apply {
            setKeepAliveTime(KEEP_ALIVE, TimeUnit.MILLISECONDS)
            allowCoreThreadTimeOut(true)
            executeExistingDelayedTasksAfterShutdownPolicy = false
            // "setRemoveOnCancelPolicy" is available only since JDK7, so try it via reflection
            try {
                val m = this::class.java.getMethod("setRemoveOnCancelPolicy",
                        Boolean::class.javaPrimitiveType)
                m.invoke(this, true)
            } catch (ex: Throwable) {
                /* ignore */
            }
            _scheduledExecutor = this
        }

@Synchronized
impl internal fun scheduledExecutorShutdownNow() {
    _scheduledExecutor?.shutdownNow()
}

@Synchronized
impl internal fun scheduledExecutorShutdownNowAndRelease() {
    _scheduledExecutor?.apply {
        shutdownNow()
        _scheduledExecutor = null
    }
}

impl internal fun scheduleDisposable(runnable: Runnable,
                                     time: Long,
                                     unit: TimeUnit): DisposableHandle =
        DisposableFutureHandle(scheduleFuture(runnable, time, unit))

internal fun scheduleFuture(runnable: Runnable,
                            time: Long,
                            unit: TimeUnit): Future<*> =
        scheduledExecutor.schedule(runnable, time, unit)
