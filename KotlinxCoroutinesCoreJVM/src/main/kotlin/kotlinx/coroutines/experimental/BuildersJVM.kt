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

import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.experimental.*
import kotlin.coroutines.experimental.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn

impl internal fun launchImpl(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
): Job {
    val newContext = newCoroutineContext(context)
    val coroutine = if (start.isLazy)
        LazyStandaloneCoroutine(newContext, block) else
        StandaloneCoroutine(newContext, active = true)
    coroutine.initParentJob(context[Job])
    start(block, coroutine, coroutine)
    return coroutine
}

impl internal suspend fun <T> runImpl(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend () -> T
): T = suspendCoroutineOrReturn sc@ { cont ->
    val oldContext = cont.context
    // fast path #1 if there is no change in the actual context:
    if (context === oldContext || context is CoroutineContext.Element && oldContext[context.key] === context)
        return@sc block.startCoroutineUninterceptedOrReturn(cont)
    // compute new context
    val newContext = oldContext + context
    // fast path #2 if the result is actually the same
    if (newContext === oldContext)
        return@sc block.startCoroutineUninterceptedOrReturn(cont)
    // fast path #3 if the new dispatcher is the same as the old one.
    // `equals` is used by design (see equals implementation is wrapper context like ExecutorCoroutineDispatcher)
    if (newContext[ContinuationInterceptor] == oldContext[ContinuationInterceptor]) {
        val newContinuation = RunContinuationDirect(newContext, cont)
        return@sc block.startCoroutineUninterceptedOrReturn(newContinuation)
    }
    // slowest path otherwise -- use new interceptor, sync to its result via a
    // full-blown instance of CancellableContinuation
    require(!start.isLazy) { "$start start is not supported" }
    val newContinuation = RunContinuationCoroutine(
            parentContext = newContext,
            resumeMode = if (start == CoroutineStart.ATOMIC) MODE_ATOMIC_DEFAULT else MODE_CANCELLABLE,
            continuation = cont)
    newContinuation.initCancellability() // attach to parent job
    start(block, newContinuation)
    newContinuation.getResult()
}

impl internal fun <T> runBlockingImpl(context: CoroutineContext,
                                      block: suspend CoroutineScope.() -> T): T {
    val currentThread = Thread.currentThread()
    val eventLoop = if (context[ContinuationInterceptor] == null) EventLoopImpl(
            currentThread) else null
    val newContext = newCoroutineContext(
            context + (eventLoop ?: EmptyCoroutineContext))
    val coroutine = BlockingCoroutine<T>(newContext, currentThread,
            privateEventLoop = eventLoop != null)
    coroutine.initParentJob(context[Job])
    eventLoop?.initParentJob(coroutine)
    block.startCoroutine(coroutine, coroutine)
    return coroutine.joinBlocking()
}

private open class StandaloneCoroutine(
        override val parentContext: CoroutineContext,
        active: Boolean
) : AbstractCoroutine<Unit>(active) {
    override fun afterCompletion(state: Any?,
                                 mode: Int) {
        // note the use of the parent's job context below!
        if (state is CompletedExceptionally) handleCoroutineException(
                parentContext, state.exception)
    }
}

private class LazyStandaloneCoroutine(
        parentContext: CoroutineContext,
        private val block: suspend CoroutineScope.() -> Unit
) : StandaloneCoroutine(parentContext, active = false) {
    override fun onStart() {
        block.startCoroutineCancellable(this, this)
    }
}

private class RunContinuationDirect<in T>(
        override val context: CoroutineContext,
        continuation: Continuation<T>
) : Continuation<T> by continuation

private class RunContinuationCoroutine<in T>(
        override val parentContext: CoroutineContext,
        resumeMode: Int,
        continuation: Continuation<T>
) : CancellableContinuationImpl<T>(continuation, defaultResumeMode = resumeMode,
        active = true)

private class BlockingCoroutine<T>(
        override val parentContext: CoroutineContext,
        private val blockedThread: Thread,
        private val privateEventLoop: Boolean
) : AbstractCoroutine<T>(active = true) {
    val eventLoop: EventLoop? = parentContext[ContinuationInterceptor] as? EventLoop

    init {
        if (privateEventLoop) require(eventLoop is EventLoopImpl)
    }

    override fun afterCompletion(state: Any?,
                                 mode: Int) {
        if (Thread.currentThread() != blockedThread)
            LockSupport.unpark(blockedThread)
    }

    @Suppress("UNCHECKED_CAST")
    fun joinBlocking(): T {
        while (true) {
            if (Thread.interrupted()) throw InterruptedException().also {
                cancel(it)
            }
            val parkNanos = eventLoop?.processNextEvent() ?: Long.MAX_VALUE
            // note: process next even may look unpark flag, so check !isActive before parking
            if (!isActive) break
            LockSupport.parkNanos(this, parkNanos)
        }
        // process queued events (that could have been added after last processNextEvent and before cancel
        if (privateEventLoop) (eventLoop as EventLoopImpl).shutdown()
        // now return result
        val state = this.state
        (state as? CompletedExceptionally)?.let { throw it.exception }
        return state as T
    }
}