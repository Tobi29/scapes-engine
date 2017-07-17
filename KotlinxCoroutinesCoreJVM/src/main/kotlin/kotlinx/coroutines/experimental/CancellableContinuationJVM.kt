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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED

@PublishedApi
impl internal open class CancellableContinuationImpl<in T>(
        @JvmField
        protected val delegate: Continuation<T>,
        impl override val defaultResumeMode: Int,
        active: Boolean
) : AbstractCoroutine<T>(active), CancellableContinuation<T> {
    @Volatile
    private var decision = UNDECIDED

    impl override val parentContext: CoroutineContext
        get() = delegate.context

    protected companion object {
        @JvmField
        val DECISION: AtomicIntegerFieldUpdater<CancellableContinuationImpl<*>> =
                AtomicIntegerFieldUpdater.newUpdater(
                        CancellableContinuationImpl::class.java, "decision")

        const val UNDECIDED = 0
        const val SUSPENDED = 1
        const val RESUMED = 2

        @Suppress("UNCHECKED_CAST")
        fun <T> getSuccessfulResult(state: Any?): T = if (state is CompletedIdempotentResult) state.result as T else state as T
    }

    impl override fun initCancellability() {
        initParentJob(parentContext[Job])
    }

    @PublishedApi
    impl internal fun getResult(): Any? {
        val decision = this.decision // volatile read
        if (decision == UNDECIDED && DECISION.compareAndSet(this, UNDECIDED,
                SUSPENDED)) return COROUTINE_SUSPENDED
        // otherwise, afterCompletion was already invoked, and the result is in the state
        val state = this.state
        if (state is CompletedExceptionally) throw state.exception
        return getSuccessfulResult(state)
    }

    impl override val isCancelled: Boolean get() = state is Cancelled

    impl override fun tryResume(value: T,
                                idempotent: Any?): Any? {
        while (true) {
            // lock-free loop on state
            val state = this.state // atomic read
            when (state) {
                is Incomplete -> {
                    val idempotentStart = state.idempotentStart
                    val update: Any? = if (idempotent == null && idempotentStart == null) value else
                        CompletedIdempotentResult(idempotentStart, idempotent,
                                value, state)
                    if (tryUpdateState(state, update)) return state
                }
                is CompletedIdempotentResult -> {
                    if (state.idempotentResume === idempotent) {
                        check(state.result === value) { "Non-idempotent resume" }
                        return state.token
                    } else
                        return null
                }
                else -> return null // cannot resume -- not active anymore
            }
        }
    }

    impl override fun tryResumeWithException(exception: Throwable): Any? {
        while (true) {
            // lock-free loop on state
            val state = this.state // atomic read
            when (state) {
                is Incomplete -> {
                    if (tryUpdateState(state,
                            CompletedExceptionally(state.idempotentStart,
                                    exception))) return state
                }
                else -> return null // cannot resume -- not active anymore
            }
        }
    }

    impl override fun completeResume(token: Any) {
        completeUpdateState(token, state, defaultResumeMode)
    }

    impl override fun afterCompletion(state: Any?,
                                      mode: Int) {
        val decision = this.decision // volatile read
        if (decision == UNDECIDED && DECISION.compareAndSet(this, UNDECIDED,
                RESUMED)) return // will get result in getResult
        // otherwise, getResult has already commenced, i.e. it was resumed later or in other thread
        if (state is CompletedExceptionally) {
            val exception = state.exception
            when (mode) {
                MODE_ATOMIC_DEFAULT -> delegate.resumeWithException(exception)
                MODE_CANCELLABLE -> delegate.resumeCancellableWithException(
                        exception)
                MODE_DIRECT -> delegate.resumeDirectWithException(exception)
                MODE_UNDISPATCHED -> (delegate as DispatchedContinuation).resumeUndispatchedWithException(
                        exception)
                else -> error("Invalid mode $mode")
            }
        } else {
            val value = getSuccessfulResult<T>(state)
            when (mode) {
                MODE_ATOMIC_DEFAULT -> delegate.resume(value)
                MODE_CANCELLABLE -> delegate.resumeCancellable(value)
                MODE_DIRECT -> delegate.resumeDirect(value)
                MODE_UNDISPATCHED -> (delegate as DispatchedContinuation).resumeUndispatched(
                        value)
                else -> error("Invalid mode $mode")
            }
        }
    }

    impl override fun CoroutineDispatcher.resumeUndispatched(value: T) {
        val dc = delegate as? DispatchedContinuation ?: throw IllegalArgumentException(
                "Must be used with DispatchedContinuation")
        check(dc.dispatcher === this) { "Must be invoked from the context CoroutineDispatcher" }
        resume(value, MODE_UNDISPATCHED)
    }

    impl override fun CoroutineDispatcher.resumeUndispatchedWithException(exception: Throwable) {
        val dc = delegate as? DispatchedContinuation ?: throw IllegalArgumentException(
                "Must be used with DispatchedContinuation")
        check(dc.dispatcher === this) { "Must be invoked from the context CoroutineDispatcher" }
        resumeWithException(exception, MODE_UNDISPATCHED)
    }

    private class CompletedIdempotentResult(
            idempotentStart: Any?,
            @JvmField val idempotentResume: Any?,
            @JvmField val result: Any?,
            @JvmField val token: Incomplete
    ) : CompletedIdempotentStart(idempotentStart) {
        override fun toString(): String = "CompletedIdempotentResult[$result]"
    }
}