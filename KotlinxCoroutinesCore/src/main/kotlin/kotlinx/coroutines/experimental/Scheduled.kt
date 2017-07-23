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

import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn

// used for tests
header internal fun scheduledExecutorShutdownNow()

header internal fun scheduledExecutorShutdownNowAndRelease()

header internal fun scheduleDisposable(runnable: Runnable,
                                       time: Long,
                                       unit: TimeUnit): DisposableHandle

/**
 * Runs a given suspending [block] of code inside a coroutine with a specified timeout and throws
 * [CancellationException] if timeout was exceeded.
 *
 * The code that is executing inside the [block] is cancelled on timeout and the active or next invocation of
 * cancellable suspending function inside the block throws [CancellationException], so normally that exception,
 * if uncaught, also gets thrown by `withTimeout` as a result.
 * However, the code in the block can suppress [CancellationException].
 *
 * The sibling function that does not throw exception on timeout is [withTimeoutOrNull].
 * Note, that timeout action can be specified for [select] invocation with [onTimeout][SelectBuilder.onTimeout] clause.
 *
 * This function delegates to [Delay.invokeOnTimeout] if the context [CoroutineDispatcher]
 * implements [Delay] interface, otherwise it tracks time using a built-in single-threaded scheduled executor service.
 *
 * @param time timeout time
 * @param unit timeout unit (milliseconds by default)
 */
public suspend fun <T> withTimeout(time: Long,
                                   unit: TimeUnit = TimeUnit.MILLISECONDS,
                                   block: suspend () -> T): T {
    require(time >= 0) { "Timeout time $time cannot be negative" }
    if (time <= 0L) throw CancellationException("Timed out immediately")
    return suspendCoroutineOrReturn { cont: Continuation<T> ->
        val context = cont.context
        val coroutine = TimeoutExceptionCoroutine(time, unit, cont)
        val delay = context[ContinuationInterceptor] as? Delay
        // schedule cancellation of this coroutine on time
        if (delay != null)
            coroutine.disposeOnCompletion(
                    delay.invokeOnTimeout(time, unit, coroutine)) else
            coroutine.disposeOnCompletion(
                    scheduleDisposable(coroutine, time, unit))
        coroutine.initParentJob(context[Job])
        // restart block using new coroutine with new job,
        // however start it as undispatched coroutine, because we are already in the proper context
        block.startCoroutineUninterceptedOrReturn(coroutine)
    }
}

private class TimeoutExceptionCoroutine<in T>(
        private val time: Long,
        private val unit: TimeUnit,
        private val cont: Continuation<T>
) : JobSupport(true), Runnable, Continuation<T> {
    override val context: CoroutineContext = cont.context + this // mix in this Job into the context
    override fun run() {
        cancel(TimeoutException(time, unit, this))
    }

    override fun resume(value: T) {
        cont.resumeDirect(value)
    }

    override fun resumeWithException(exception: Throwable) {
        cont.resumeDirectWithException(exception)
    }
}

/**
 * Runs a given suspending block of code inside a coroutine with a specified timeout and returns
 * `null` if this timeout was exceeded.
 *
 * The code that is executing inside the [block] is cancelled on timeout and the active or next invocation of
 * cancellable suspending function inside the block throws [CancellationException]. Normally that exception,
 * if uncaught by the block, gets converted into the `null` result of `withTimeoutOrNull`.
 * However, the code in the block can suppress [CancellationException].
 *
 * The sibling function that throws exception on timeout is [withTimeout].
 * Note, that timeout action can be specified for [select] invocation with [onTimeout][SelectBuilder.onTimeout] clause.
 *
 * This function delegates to [Delay.invokeOnTimeout] if the context [CoroutineDispatcher]
 * implements [Delay] interface, otherwise it tracks time using a built-in single-threaded scheduled executor service.
 *
 * @param time timeout time
 * @param unit timeout unit (milliseconds by default)
 */
public suspend fun <T> withTimeoutOrNull(time: Long,
                                         unit: TimeUnit = TimeUnit.MILLISECONDS,
                                         block: suspend () -> T): T? {
    require(time >= 0) { "Timeout time $time cannot be negative" }
    if (time <= 0L) return null
    return suspendCoroutineOrReturn { cont: Continuation<T?> ->
        val context = cont.context
        val coroutine = TimeoutNullCoroutine(time, unit, cont)
        val delay = context[ContinuationInterceptor] as? Delay
        // schedule cancellation of this coroutine on time
        if (delay != null)
            coroutine.disposeOnCompletion(
                    delay.invokeOnTimeout(time, unit, coroutine)) else
            coroutine.disposeOnCompletion(
                    scheduleDisposable(coroutine, time, unit))
        coroutine.initParentJob(context[Job])
        // restart block using new coroutine with new job,
        // however start it as undispatched coroutine, because we are already in the proper context
        try {
            block.startCoroutineUninterceptedOrReturn(coroutine)
        } catch (e: TimeoutException) {
            null // replace inner timeout exception with null result
        }
    }
}


internal class TimeoutNullCoroutine<in T>(
        private val time: Long,
        private val unit: TimeUnit,
        private val cont: Continuation<T?>
) : JobSupport(true), Runnable, Continuation<T> {
    override val context: CoroutineContext = cont.context + this // mix in this Job into the context
    override fun run() {
        cancel(TimeoutException(time, unit, this))
    }

    override fun resume(value: T) {
        cont.resumeDirect(value)
    }

    override fun resumeWithException(exception: Throwable) {
        // suppress inner timeout exception and replace it with null
        if (exception is TimeoutException && exception.coroutine === this)
            cont.resumeDirect(null) else
            cont.resumeDirectWithException(exception)
    }
}

internal class TimeoutException(
        time: Long,
        unit: TimeUnit,
        val coroutine: Job
) : CancellationException("Timed out waiting for $time $unit")
