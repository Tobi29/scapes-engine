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
import kotlinx.coroutines.experimental.sync.Mutex
import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.atomic.AtomicReference
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

class JobHandle(private val scope: CoroutineScope) {
    private val _job = AtomicReference<Job?>(null)

    val job: Job? get() = _job.get()

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        onCompletion: CompletionHandler? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Job? = launchImpl(block, {
        scope.launch(context, start, onCompletion, it)
    })?.let { (job, mutex) ->
        mutex.unlock()
        job
    }

    fun launchLater(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        onCompletion: CompletionHandler? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<Job, () -> Unit>? = launchImpl(block, {
        scope.launch(context, start, onCompletion, it)
    })?.let { (job, mutex) -> job to { mutex.unlock() } }

    private inline fun launchImpl(
        noinline block: suspend CoroutineScope.() -> Unit,
        launch: (suspend CoroutineScope.() -> Unit) -> Job
    ): Pair<Job, Mutex>? {
        val mutex = Mutex(true)
        val newJob = launch {
            mutex.lock()
            block()
        }
        cleanInactive()
        return if (_job.compareAndSet(null, newJob)) {
            newJob to mutex
        } else {
            newJob.cancel()
            mutex.unlock()
            null
        }
    }

    private fun cleanInactive() {
        if (_job.get()?.isActive != true) _job.set(null)
    }
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun JobHandle.launchOrStop(
    state: Boolean,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline onCompletion: CompletionHandler? = null,
    noinline block: suspend CoroutineScope.() -> Unit
): Job? {
    job?.cancel()
    return if (state) launch(
        context, start, onCompletion, block
    ) else null
}
