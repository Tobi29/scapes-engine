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

import kotlinx.coroutines.experimental.internal.AtomicDesc
import kotlinx.coroutines.experimental.internal.LockFreeLinkedListNode
import kotlinx.coroutines.experimental.internal.Symbol
import kotlinx.coroutines.experimental.selects.SelectInstance
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext

// --------------- core job interfaces ---------------

/**
 * A background job. It is created with [launch] coroutine builder or with a
 * [`Job()`][Job.Key.invoke] factory function.
 * A job can be _cancelled_ at any time with [cancel] function that forces it to become _completed_ immediately.
 *
 * A job has two or three states:
 *
 * | **State**                        | [isActive] | [isCompleted] |
 * | -------------------------------- | ---------- | ------------- |
 * | _New_ (optional initial state)   | `false`    | `false`       |
 * | _Active_ (default initial state) | `true`     | `false`       |
 * | _Completed_ (final state)        | `false`    | `true`        |
 *
 * Usually, a job is created in _active_ state (it is created and started), so its only visible
 * states are _active_ and _completed_. However, coroutine builders that provide an optional `start` parameter
 * create a coroutine in _new_ state when this parameter is set to [CoroutineStart.LAZY]. Such a job can
 * be made _active_ by invoking [start] or [join].
 *
 * A job in the coroutine [context][CoroutineScope.context] represents the coroutine itself.
 * A job is active while the coroutine is working and job's cancellation aborts the coroutine when
 * the coroutine is suspended on a _cancellable_ suspension point by throwing [CancellationException]
 * or the cancellation cause inside the coroutine.
 *
 * A job can have a _parent_. A job with a parent is cancelled when its parent completes.
 *
 * All functions on this interface and on all interfaces derived from it are **thread-safe** and can
 * be safely invoked from concurrent coroutines without external synchronization.
 */
public interface Job : CoroutineContext.Element {
    /**
     * Key for [Job] instance in the coroutine context.
     */
    public companion object Key : CoroutineContext.Key<Job> {
        /**
         * Creates a new job object in _active_ state.
         * It is optionally a child of a [parent] job.
         */
        public operator fun invoke(parent: Job? = null): Job = JobImpl(parent)
    }

    /**
     * Returns `true` when this job is active.
     */
    public val isActive: Boolean

    /**
     * Returns `true` when this job has completed for any reason.
     */
    public val isCompleted: Boolean

    /**
     * Starts coroutine related to this job (if any) if it was not started yet.
     * The result `true` if this invocation actually started coroutine or `false`
     * if it was already started or completed.
     */
    public fun start(): Boolean

    /**
     * Returns the exception that signals the completion of this job -- it returns the original
     * [cancel] cause or an instance of [CancellationException] if this job had completed
     * normally or was cancelled without a cause. This function throws
     * [IllegalStateException] when invoked for an job that has not [completed][isCompleted] yet.
     *
     * The [cancellable][suspendCancellableCoroutine] suspending functions throw this exception
     * when trying to suspend in the context of this job.
     */
    fun getCompletionException(): Throwable

    /**
     * Registers handler that is **synchronously** invoked on completion of this job.
     * When job is already complete, then the handler is immediately invoked
     * with a cancellation cause or `null`. Otherwise, handler will be invoked once when this
     * job is complete. Note, that [cancellation][cancel] is also a form of completion.
     *
     * The resulting [DisposableHandle] can be used to [dispose][DisposableHandle.dispose] the
     * registration of this handler and release its memory if its invocation is no longer needed.
     * There is no need to dispose the handler after completion of this job. The reference to
     * all the handlers are released when this job completes.
     */
    public fun invokeOnCompletion(handler: CompletionHandlerI): DisposableHandle =
            invokeOnCompletion { handler(it) }

    /**
     * Registers handler that is **synchronously** invoked on completion of this job.
     * When job is already complete, then the handler is immediately invoked
     * with a cancellation cause or `null`. Otherwise, handler will be invoked once when this
     * job is complete. Note, that [cancellation][cancel] is also a form of completion.
     *
     * The resulting [DisposableHandle] can be used to [dispose][DisposableHandle.dispose] the
     * registration of this handler and release its memory if its invocation is no longer needed.
     * There is no need to dispose the handler after completion of this job. The reference to
     * all the handlers are released when this job completes.
     */
    public fun invokeOnCompletion(handler: CompletionHandler): DisposableHandle

    /**
     * Suspends coroutine until this job is complete. This invocation resumes normally (without exception)
     * when the job is complete for any reason. This function also [starts][Job.start] the corresponding coroutine
     * if the [Job] was still in _new_ state.
     *
     * This suspending function is cancellable. If the [Job] of the invoking coroutine is completed while this
     * suspending function is suspended, this function immediately resumes with [CancellationException].
     *
     * This function can be used in [select] invocation with [onJoin][SelectBuilder.onJoin] clause.
     * Use [isCompleted] to check for completion of this job without waiting.
     */
    public suspend fun join()

    /**
     * Registers [onJoin][SelectBuilder.onJoin] select clause.
     * @suppress **This is unstable API and it is subject to change.**
     */
    public fun <R> registerSelectJoin(select: SelectInstance<R>,
                                      block: suspend () -> R)

    /**
     * Cancel this activity with an optional cancellation [cause]. The result is `true` if this job was
     * cancelled as a result of this invocation and `false` otherwise
     * (if it was already _completed_ or if it is [NonCancellable]).
     * Repeated invocations of this function have no effect and always produce `false`.
     *
     * When cancellation has a clear reason in the code, an instance of [CancellationException] should be created
     * at the corresponding original cancellation site and passed into this method to aid in debugging by providing
     * both the context of cancellation and text description of the reason.
     */
    public fun cancel(cause: Throwable? = null): Boolean

    /**
     * @suppress **Error**: Operator '+' on two Job objects is meaningless.
     * Job is a coroutine context element and `+` is a set-sum operator for coroutine contexts.
     * The job to the right of `+` just replaces the job the left of `+`.
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(message = "Operator '+' on two Job objects is meaningless. " +
            "Job is a coroutine context element and `+` is a set-sum operator for coroutine contexts. " +
            "The job to the right of `+` just replaces the job the left of `+`.",
            level = DeprecationLevel.ERROR)
    public operator fun plus(other: Job) = other

    /**
     * Registration object for [invokeOnCompletion]. It can be used to [unregister] if needed.
     * There is no need to unregister after completion.
     * @suppress **Deprecated**: Replace with `DisposableHandle`
     */
    @Deprecated(message = "Replace with `DisposableHandle`",
            replaceWith = ReplaceWith("DisposableHandle"))
    public interface Registration {
        /**
         * Unregisters completion handler.
         * @suppress **Deprecated**: Replace with `dispose`
         */
        @Deprecated(message = "Replace with `dispose`",
                replaceWith = ReplaceWith("dispose()"))
        public fun unregister()
    }
}

/**
 * A handle to an allocated object that can be disposed to make it eligible for garbage collection.
 */
@Suppress("DEPRECATION") // todo: remove when Job.Registration is removed
public interface DisposableHandle : Job.Registration {
    /**
     * Disposes the corresponding object, making it eligible for garbage collection.
     * Repeated invocation of this function has no effect.
     */
    public fun dispose()

    /**
     * Unregisters completion handler.
     * @suppress **Deprecated**: Replace with `dispose`
     */
    @Deprecated(message = "Replace with `dispose`",
            replaceWith = ReplaceWith("dispose()"))
    public override fun unregister() = dispose()
}

/**
 * Handler for [Job.invokeOnCompletion].
 */
public typealias CompletionHandler = (Throwable?) -> Unit
public interface CompletionHandlerI {
    operator fun invoke(reason: Throwable?)
}

/**
 * Thrown by cancellable suspending functions if the [Job] of the coroutine is cancelled while it is suspending.
 */
header open class CancellationException : IllegalStateException {
    constructor()

    constructor(message: String)
}

/**
 * Unregisters a specified [registration] when this job is complete.
 *
 * This is a shortcut for the following code with slightly more efficient implementation (one fewer object created).
 * ```
 * invokeOnCompletion { registration.unregister() }
 * ```
 * @suppress: **Deprecated**: Renamed to `disposeOnCompletion`.
 */
@Deprecated(message = "Renamed to `disposeOnCompletion`",
        replaceWith = ReplaceWith("disposeOnCompletion(registration)"))
public fun Job.unregisterOnCompletion(registration: DisposableHandle): DisposableHandle =
        invokeOnCompletion(DisposeOnCompletion(this, registration))

/**
 * Disposes a specified [handle] when this job is complete.
 *
 * This is a shortcut for the following code with slightly more efficient implementation (one fewer object created).
 * ```
 * invokeOnCompletion { handle.dispose() }
 * ```
 */
public fun Job.disposeOnCompletion(handle: DisposableHandle): DisposableHandle =
        invokeOnCompletion(DisposeOnCompletion(this, handle))

/**
 * @suppress **Deprecated**: `join` is now a member function of `Job`.
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "DeprecatedCallableAddReplaceWith")
@Deprecated(message = "`join` is now a member function of `Job`")
public suspend fun Job.join() = this.join()

/**
 * No-op implementation of [Job.Registration].
 */
@Deprecated(message = "Replace with `NonDisposableHandle`",
        replaceWith = ReplaceWith("NonDisposableHandle"))
typealias EmptyRegistration = NonDisposableHandle

/**
 * No-op implementation of [DisposableHandle].
 */
public object NonDisposableHandle : DisposableHandle {
    /** Does not do anything. */
    override fun dispose() {}

    /** Returns "NonDisposableHandle" string. */
    override fun toString(): String = "NonDisposableHandle"
}

// --------------- utility classes to simplify job implementation

/**
 * A concrete implementation of [Job]. It is optionally a child to a parent job.
 * This job is cancelled when the parent is complete, but not vise-versa.
 *
 * This is an open class designed for extension by more specific classes that might augment the
 * state and mare store addition state information for completed jobs, like their result values.
 *
 * @param active when `true` the job is created in _active_ state, when `false` in _new_ state. See [Job] for details.
 * @suppress **This is unstable API and it is subject to change.**
 */
header public open class JobSupport(active: Boolean) : AbstractCoroutineContextElement(
        Job), Job {
    header protected companion object {
        fun stateToString(state: Any?): String
    }

    /**
     * Initializes parent job.
     * It shall be invoked at most once after construction after all other initialization.
     */
    public fun initParentJob(parent: Job?)

    /**
     * Invoked at most once on parent completion.
     * @suppress **This is unstable API and it is subject to change.**
     */
    protected open fun onParentCompletion(cause: Throwable?)

    /**
     * Returns current state of this job.
     */
    protected val state: Any?

    /**
     * Updates current [state] of this job.
     */
    protected fun updateState(expect: Any,
                              update: Any?,
                              mode: Int): Boolean

    /**
     * Tries to initiate update of the current [state] of this job.
     */
    protected fun tryUpdateState(expect: Any,
                                 update: Any?): Boolean

    /**
     * Completes update of the current [state] of this job.
     */
    protected fun completeUpdateState(expect: Any,
                                      update: Any?,
                                      mode: Int)

    public final override val isActive: Boolean

    public final override val isCompleted: Boolean

    // this is for `select` operator. `isSelected` state means "not new" (== was started or already completed)
    public val isSelected: Boolean

    public final override fun start(): Boolean

    // return: 0 -> false (not new), 1 -> true (started), -1 -> retry
    internal fun startInternal(state: Any?): Int

    // it is just like start(), but support idempotent start
    public fun trySelect(idempotent: Any?): Boolean

    public fun performAtomicTrySelect(desc: AtomicDesc): Any?

    public fun performAtomicIfNotSelected(desc: AtomicDesc): Any?

    /**
     * Override to provide the actual [start] action.
     */
    protected open fun onStart()

    final override fun getCompletionException(): Throwable

    final override fun invokeOnCompletion(handler: CompletionHandler): DisposableHandle

    final override suspend fun join()

    override fun <R> registerSelectJoin(select: SelectInstance<R>,
                                        block: suspend () -> R)

    internal fun removeNode(node: JobNode<*>)

    final override fun cancel(cause: Throwable?): Boolean

    /**
     * Override to process any exceptions that were encountered while invoking [invokeOnCompletion] handlers.
     */
    protected open fun handleCompletionException(closeException: Throwable)

    /**
     * Override for post-completion actions that need to do something with the state.
     * @param mode completion mode.
     */
    protected open fun afterCompletion(state: Any?,
                                       mode: Int)

    // for nicer debugging
    /* TODO: override fun toString(): String = "${this::class.java.simpleName}{${stateToString(state)}}@${Integer.toHexString(System.identityHashCode(this))}" */

    /**
     * Interface for incomplete [state] of a job.
     */
    header public interface Incomplete {
        val isActive: Boolean
        val idempotentStart: Any? // != null if this state is a descendant of trySelect(idempotent)
    }

    header public open class CompletedIdempotentStart(
            idempotentStart: Any?
    ) {
        val idempotentStart: Any?
    }

    /**
     * Class for a [state] of a job that had completed exceptionally, including cancellation.
     *
     * @param cause the exceptional completion cause. If `cause` is null, then a [CancellationException]
     *        if created on first get from [exception] property.
     */
    header public open class CompletedExceptionally(
            idempotentStart: Any?,
            cause: Throwable?
    ) : CompletedIdempotentStart(idempotentStart) {
        val cause: Throwable?
        public val exception: Throwable
    }

    /**
     * A specific subclass of [CompletedExceptionally] for cancelled jobs.
     */
    header public class Cancelled(
            idempotentStart: Any?,
            cause: Throwable?
    ) : CompletedExceptionally(idempotentStart, cause)
}

internal val ALREADY_SELECTED: Any = Symbol("ALREADY_SELECTED")

header internal abstract class JobNode<out J : Job>(
        job: J
) : LockFreeLinkedListNode(), DisposableHandle, CompletionHandlerI, JobSupport.Incomplete {
    val job: J
    final override val isActive: Boolean
    final override val idempotentStart: Any?
    // if unregister is called on this instance, then Job was an instance of JobSupport that added this node it itself
    // directly without wrapping
    final override fun dispose()

    override abstract fun invoke(reason: Throwable?)
}

internal class DisposeOnCompletion(
        job: Job,
        private val handle: DisposableHandle
) : JobNode<Job>(job) {
    override fun invoke(reason: Throwable?) = handle.dispose()
    override fun toString(): String = "DisposeOnCompletion[$handle]"
}

private class JobImpl(parent: Job? = null) : JobSupport(true) {
    init {
        initParentJob(parent)
    }
}