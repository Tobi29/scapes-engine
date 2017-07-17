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

import kotlin.coroutines.experimental.Continuation

impl internal class DispatchedContinuation<in T>(
        impl @JvmField val dispatcher: CoroutineDispatcher,
        impl @JvmField val continuation: Continuation<T>
) : Continuation<T> by continuation {
    impl override fun resume(value: T) {
        val context = continuation.context
        if (dispatcher.isDispatchNeeded(context))
            dispatcher.dispatch(context,
                    DispatchTask(this, value, exception = false,
                            cancellable = false))
        else
            resumeUndispatched(value)
    }

    impl override fun resumeWithException(exception: Throwable) {
        val context = continuation.context
        if (dispatcher.isDispatchNeeded(context))
            dispatcher.dispatch(context,
                    DispatchTask(this, exception, exception = true,
                            cancellable = false))
        else
            resumeUndispatchedWithException(exception)
    }

    @Suppress(
            "NOTHING_TO_INLINE") // we need it inline to save us an entry on the stack
    impl inline fun resumeCancellable(value: T) {
        val context = continuation.context
        if (dispatcher.isDispatchNeeded(context))
            dispatcher.dispatch(context,
                    DispatchTask(this, value, exception = false,
                            cancellable = true))
        else
            resumeUndispatched(value)
    }

    @Suppress(
            "NOTHING_TO_INLINE") // we need it inline to save us an entry on the stack
    impl inline fun resumeCancellableWithException(exception: Throwable) {
        val context = continuation.context
        if (dispatcher.isDispatchNeeded(context))
            dispatcher.dispatch(context,
                    DispatchTask(this, exception, exception = true,
                            cancellable = true))
        else
            resumeUndispatchedWithException(exception)
    }

    @Suppress(
            "NOTHING_TO_INLINE") // we need it inline to save us an entry on the stack
    impl inline fun resumeUndispatched(value: T) {
        withCoroutineContext(context) {
            continuation.resume(value)
        }
    }

    @Suppress(
            "NOTHING_TO_INLINE") // we need it inline to save us an entry on the stack
    impl inline fun resumeUndispatchedWithException(exception: Throwable) {
        withCoroutineContext(context) {
            continuation.resumeWithException(exception)
        }
    }

    // used by "yield" implementation
    impl internal fun dispatchYield(job: Job?,
                               value: T) {
        val context = continuation.context
        dispatcher.dispatch(context, object : Runnable {
            override fun run() {
                withCoroutineContext(context) {
                    if (job != null && job.isCompleted)
                        continuation.resumeWithException(
                                job.getCompletionException())
                    else
                        continuation.resume(value)
                }
            }
        })
    }

    override fun toString(): String = "DispatchedContinuation[$dispatcher, $continuation]"
}
