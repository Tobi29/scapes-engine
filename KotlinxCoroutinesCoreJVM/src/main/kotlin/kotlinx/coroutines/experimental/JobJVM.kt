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

import kotlinx.coroutines.experimental.internal.*
import kotlinx.coroutines.experimental.intrinsics.startCoroutineUndispatched
import kotlinx.coroutines.experimental.selects.SelectInstance
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation

/**
 * Cancels a specified [future] when this job is complete.
 *
 * This is a shortcut for the following code with slightly more efficient implementation (one fewer object created).
 * ```
 * invokeOnCompletion { future.cancel(false) }
 * ```
 */
public fun Job.cancelFutureOnCompletion(future: Future<*>): DisposableHandle =
        invokeOnCompletion(CancelFutureOnCompletion(this, future))

impl public typealias CancellationException = java.util.concurrent.CancellationException

private class CancelFutureOnCompletion(
        job: Job,
        private val future: Future<*>
) : JobNode<Job>(job) {
    override fun invoke(reason: Throwable?) {
        // Don't interrupt when cancelling future on completion, because no one is going to reset this
        // interruption flag and it will cause spurious failures elsewhere
        future.cancel(false)
    }

    override fun toString() = "CancelFutureOnCompletion[$future]"
}

impl public open class JobSupport(active: Boolean) : AbstractCoroutineContextElement(
        Job), Job {
    /*
       === Internal states ===

       name       state class    public state  description
       ------     ------------   ------------  -----------
       EMPTY_N    EmptyNew     : New           no completion listeners
       EMPTY_A    EmptyActive  : Active        no completion listeners
       SINGLE     JobNode      : Active        a single completion listener
       SINGLE+    JobNode      : Active        a single completion listener + NodeList added as its next
       LIST_N     NodeList     : New           a list of listeners (promoted once, does not got back to EmptyNew)
       LIST_A     NodeList     : Active        a list of listeners (promoted once, does not got back to JobNode/EmptyActive)
       FINAL_C    Cancelled    : Completed     cancelled (final state)
       FINAL_F    Failed       : Completed     failed for other reason (final state)
       FINAL_R    <any>        : Completed     produced some result

       === Transitions ===

           New states      Active states     Inactive states
          +---------+       +---------+       +----------+
          | EMPTY_N | --+-> | EMPTY_A | --+-> |  FINAL_* |
          +---------+   |   +---------+   |   +----------+
               |        |     |     ^     |
               |        |     V     |     |
               |        |   +---------+   |
               |        |   | SINGLE  | --+
               |        |   +---------+   |
               |        |        |        |
               |        |        V        |
               |        |   +---------+   |
               |        +-- | SINGLE+ | --+
               |            +---------+   |
               |                 |        |
               V                 V        |
          +---------+       +---------+   |
          | LIST_N  | ----> | LIST_A  | --+
          +---------+       +---------+

       This state machine and its transition matrix are optimized for the common case when job is created in active
       state (EMPTY_A) and at most one completion listener is added to it during its life-time.

       Note, that the actual `_state` variable can also be a reference to atomic operation descriptor `OpDescriptor`
     */

    @Volatile
    private var _state: Any? = if (active) EmptyActive else EmptyNew // shared objects while we have no listeners

    @Volatile
    private var parentHandle: DisposableHandle? = null

    impl protected companion object {
        private val STATE: AtomicReferenceFieldUpdater<JobSupport, Any?> =
                AtomicReferenceFieldUpdater.newUpdater(JobSupport::class.java,
                        Any::class.java, "_state")

        impl fun stateToString(state: Any?): String =
                if (state is Incomplete)
                    if (state.isActive) "Active" else "New"
                else "Completed"
    }

    /**
     * Initializes parent job.
     * It shall be invoked at most once after construction after all other initialization.
     */
    impl public fun initParentJob(parent: Job?) {
        check(parentHandle == null)
        if (parent == null) {
            parentHandle = NonDisposableHandle
            return
        }
        // directly pass HandlerNode to parent scope to optimize one closure object (see makeNode)
        val newRegistration = parent.invokeOnCompletion(
                ParentOnCompletion(parent, this))
        parentHandle = newRegistration
        // now check our state _after_ registering (see updateState order of actions)
        if (isCompleted) newRegistration.dispose()
    }

    /**
     * Invoked at most once on parent completion.
     * @suppress **This is unstable API and it is subject to change.**
     */
    impl protected open fun onParentCompletion(cause: Throwable?) {
        // if parent was completed with CancellationException then use it as the cause of our cancellation, too.
        // however, we shall not use application specific exceptions here. So if parent crashes due to IOException,
        // we cannot and should not cancel the child with IOException
        cancel(cause as? java.util.concurrent.CancellationException)
    }

    /**
     * Returns current state of this job.
     */
    impl protected val state: Any? get() {
        while (true) {
            // lock-free helping loop
            val state = _state
            if (state !is OpDescriptor) return state
            state.perform(this)
        }
    }

    /**
     * Updates current [state] of this job.
     */
    impl protected fun updateState(expect: Any,
                                   update: Any?,
                                   mode: Int): Boolean {
        if (!tryUpdateState(expect, update)) return false
        completeUpdateState(expect, update, mode)
        return true
    }

    /**
     * Tries to initiate update of the current [state] of this job.
     */
    impl protected fun tryUpdateState(expect: Any,
                                      update: Any?): Boolean {
        require(expect is Incomplete && update !is Incomplete) // only incomplete -> completed transition is allowed
        if (!STATE.compareAndSet(this, expect, update)) return false
        // Unregister from parent job
        parentHandle?.dispose() // volatile read parentHandle _after_ state was updated
        return true // continues in completeUpdateState
    }

    /**
     * Completes update of the current [state] of this job.
     */
    impl protected fun completeUpdateState(expect: Any,
                                           update: Any?,
                                           mode: Int) {
        // Invoke completion handlers
        val cause = (update as? CompletedExceptionally)?.cause
        var completionException: Throwable? = null
        when (expect) {
        // SINGLE/SINGLE+ state -- one completion handler (common case)
            is JobNode<*> -> try {
                expect.invoke(cause)
            } catch (ex: Throwable) {
                completionException = ex
            }
        // LIST state -- a list of completion handlers
            is NodeList -> expect.forEach<JobNode<*>> { node ->
                try {
                    node.invoke(cause)
                } catch (ex: Throwable) {
                    completionException?.apply {
                        // TODO: addSuppressed(ex)
                    } ?: run { completionException = ex }
                }

            }
        // otherwise -- do nothing (it was Empty*)
            else -> check(expect is Empty)
        }
        // handle invokeOnCompletion exceptions
        completionException?.let { handleCompletionException(it) }
        // Do other (overridable) processing after completion handlers
        afterCompletion(update, mode)
    }

    impl public final override val isActive: Boolean get() {
        val state = this.state
        return state is Incomplete && state.isActive
    }

    impl public final override val isCompleted: Boolean get() = state !is Incomplete

    // this is for `select` operator. `isSelected` state means "not new" (== was started or already completed)
    impl public val isSelected: Boolean get() {
        val state = this.state
        return state !is Incomplete || state.isActive
    }

    impl public final override fun start(): Boolean {
        while (true) {
            // lock-free loop on state
            when (startInternal(state)) {
                0 -> return false
                1 -> return true
            }
        }
    }

    // return: 0 -> false (not new), 1 -> true (started), -1 -> retry
    impl internal fun startInternal(state: Any?): Int {
        when {
            state === EmptyNew -> {
                // EMPTY_NEW state -- no completion handlers, new
                if (!STATE.compareAndSet(this, state, EmptyActive)) return -1
                onStart()
                return 1
            }
            state is NodeList -> {
                // LIST -- a list of completion handlers (either new or active)
                if (state.isActive) return 0
                if (!NodeList.ACTIVE.compareAndSet(state, null,
                        NodeList.ACTIVE_STATE)) return -1
                onStart()
                return 1
            }
            else -> return 0 // not a new state
        }
    }

    // it is just like start(), but support idempotent start
    impl public fun trySelect(idempotent: Any?): Boolean {
        if (idempotent == null) return start() // non idempotent -- use plain start
        check(idempotent !is OpDescriptor) { "cannot use OpDescriptor as idempotent marker" }
        while (true) {
            // lock-free loop on state
            val state = this.state
            when {
                state === EmptyNew -> {
                    // EMPTY_NEW state -- no completion handlers, new
                    // try to promote it to list in new state
                    STATE.compareAndSet(this, state, NodeList(active = false))
                }
                state is NodeList -> {
                    // LIST -- a list of completion handlers (either new or active)
                    val active = state.active
                    if (active === idempotent) return true // was activated with the same marker --> true
                    if (active != null) return false
                    if (NodeList.ACTIVE.compareAndSet(state, null,
                            idempotent)) {
                        onStart()
                        return true
                    }
                }
                state is CompletedIdempotentStart -> {
                    // remembers idempotent start token
                    return state.idempotentStart === idempotent
                }
                else -> return false
            }
        }
    }

    impl public fun performAtomicTrySelect(desc: AtomicDesc): Any? = AtomicSelectOp(
            desc, true).perform(null)

    impl public fun performAtomicIfNotSelected(desc: AtomicDesc): Any? = AtomicSelectOp(
            desc, false).perform(null)

    private inner class AtomicSelectOp(
            @JvmField val desc: AtomicDesc,
            @JvmField val activate: Boolean
    ) : AtomicOp() {
        override fun prepare(): Any? = prepareIfNotSelected() ?: desc.prepare(
                this)

        override fun complete(affected: Any?,
                              failure: Any?) {
            completeSelect(failure)
            desc.complete(this, failure)
        }

        fun prepareIfNotSelected(): Any? {
            while (true) {
                // lock-free loop on state
                val state = _state
                when {
                    state === this@AtomicSelectOp -> return null // already in progress
                    state is OpDescriptor -> state.perform(
                            this@JobSupport) // help
                    state === EmptyNew -> {
                        // EMPTY_NEW state -- no completion handlers, new
                        if (STATE.compareAndSet(this@JobSupport, state,
                                this@AtomicSelectOp)) return null // success
                    }
                    state is NodeList -> {
                        // LIST -- a list of completion handlers (either new or active)
                        val active = state._active
                        when {
                            active == null -> {
                                if (NodeList.ACTIVE.compareAndSet(state, null,
                                        this@AtomicSelectOp)) return null // success
                            }
                            active === this@AtomicSelectOp -> return null // already in progress
                            active is OpDescriptor -> active.perform(
                                    state) // help
                            else -> return ALREADY_SELECTED // active state
                        }
                    }
                    else -> return ALREADY_SELECTED // not a new state
                }
            }
        }

        private fun completeSelect(failure: Any?) {
            val success = failure == null
            val state = _state
            when {
                state === this -> {
                    val update = if (success && activate) EmptyActive else EmptyNew
                    if (STATE.compareAndSet(this@JobSupport, this, update)) {
                        if (success) onStart()
                    }
                }
                state is NodeList -> {
                    // LIST -- a list of completion handlers (either new or active)
                    if (state._active === this) {
                        val update = if (success && activate) NodeList.ACTIVE_STATE else null
                        if (NodeList.ACTIVE.compareAndSet(state, this,
                                update)) {
                            if (success) onStart()
                        }
                    }
                }
            }

        }
    }

    /**
     * Override to provide the actual [start] action.
     */
    impl protected open fun onStart() {}

    impl final override fun getCompletionException(): Throwable {
        val state = this.state
        return when (state) {
            is Incomplete -> throw IllegalStateException(
                    "Job has not completed yet")
            is CompletedExceptionally -> state.exception
            else -> java.util.concurrent.CancellationException(
                    "Job has completed normally")
        }
    }

    impl final override fun invokeOnCompletion(handler: CompletionHandler): DisposableHandle {
        var nodeCache: JobNode<*>? = null
        while (true) {
            // lock-free loop on state
            val state = this.state
            when {
                state === EmptyActive -> {
                    // EMPTY_ACTIVE state -- no completion handlers, active
                    // try move to SINGLE state
                    val node = nodeCache ?: makeNode(
                            handler).also { nodeCache = it }
                    if (STATE.compareAndSet(this, state, node)) return node
                }
                state === EmptyNew -> {
                    // EMPTY_NEW state -- no completion handlers, new
                    // try to promote it to list in new state
                    STATE.compareAndSet(this, state, NodeList(active = false))
                }
                state is JobNode<*> -> {
                    // SINGLE/SINGLE+ state -- one completion handler
                    // try to promote it to list (SINGLE+ state)
                    state.addOneIfEmpty(NodeList(active = true))
                    // it must be in SINGLE+ state or state has changed (node could have need removed from state)
                    val list = state.next // either NodeList or somebody else won the race, updated state
                    // just attempt converting it to list if state is still the same, then continue lock-free loop
                    STATE.compareAndSet(this, state, list)
                }
                state is NodeList -> {
                    // LIST -- a list of completion handlers (either new or active)
                    val node = nodeCache ?: makeNode(
                            handler).also { nodeCache = it }
                    if (state.addLastIf(
                            node) { this.state === state }) return node
                }
                else -> {
                    // is inactive
                    handler((state as? CompletedExceptionally)?.exception)
                    return NonDisposableHandle
                }
            }
        }
    }

    impl final override suspend fun join() {
        while (true) {
            // lock-free loop on state
            val state = this.state as? Incomplete ?: return // fast-path - no need to wait
            if (startInternal(state) >= 0) break // break unless needs to retry
        }
        return joinSuspend() // slow-path
    }

    private suspend fun joinSuspend() = suspendCancellableCoroutine<Unit> { cont ->
        cont.disposeOnCompletion(
                invokeOnCompletion(ResumeOnCompletion(this, cont)))
    }

    impl override fun <R> registerSelectJoin(select: SelectInstance<R>,
                                             block: suspend () -> R) {
        // fast-path -- check state and select/return if needed
        while (true) {
            if (select.isSelected) return
            val state = this.state
            if (state !is Incomplete) {
                // already complete -- select result
                if (select.trySelect(idempotent = null))
                    block.startCoroutineUndispatched(select.completion)
                return
            }
            if (startInternal(state) == 0) {
                // slow-path -- register waiter for completion
                select.disposeOnSelect(invokeOnCompletion(
                        SelectJoinOnCompletion(this, select, block)))
                return
            }
        }
    }

    impl internal fun removeNode(node: JobNode<*>) {
        // remove logic depends on the state of the job
        while (true) {
            // lock-free loop on job state
            val state = this.state
            when (state) {
            // SINGE/SINGLE+ state -- one completion handler
                is JobNode<*> -> {
                    if (state !== node) return // a different job node --> we were already removed
                    // try remove and revert back to empty state
                    if (STATE.compareAndSet(this, state, EmptyActive)) return
                }
            // LIST -- a list of completion handlers
                is NodeList -> {
                    // remove node from the list
                    node.remove()
                    return
                }
            // it is inactive or Empty* (does not have any completion handlers)
                else -> return
            }
        }
    }

    impl final override fun cancel(cause: Throwable?): Boolean {
        while (true) {
            // lock-free loop on state
            val state = this.state as? Incomplete ?: return false // quit if already complete
            // we are dispatching coroutine to process its cancellation exception, so there is no need for
            // an extra check for Job status in MODE_CANCELLABLE
            if (updateState(state, Cancelled(state.idempotentStart, cause),
                    mode = MODE_ATOMIC_DEFAULT)) return true
        }
    }

    /**
     * Override to process any exceptions that were encountered while invoking [invokeOnCompletion] handlers.
     */
    impl protected open fun handleCompletionException(closeException: Throwable) {
        throw closeException
    }

    /**
     * Override for post-completion actions that need to do something with the state.
     * @param mode completion mode.
     */
    impl protected open fun afterCompletion(state: Any?,
                                            mode: Int) {
    }

    private fun makeNode(handler: CompletionHandler): JobNode<*> =
            (handler as? JobNode<*>)?.also { require(it.job === this) }
                    ?: InvokeOnCompletion(this, handler)

    // for nicer debugging
    override fun toString(): String = "${this::class.java.simpleName}{${stateToString(
            state)}}@${Integer.toHexString(System.identityHashCode(this))}"

    /**
     * Interface for incomplete [state] of a job.
     */
    impl public interface Incomplete {
        impl val isActive: Boolean
        impl val idempotentStart: Any? // != null if this state is a descendant of trySelect(idempotent)
    }

    private class NodeList(
            active: Boolean
    ) : LockFreeLinkedListHead(), Incomplete {
        @Volatile
        @JvmField
        var _active: Any? = if (active) ACTIVE_STATE else null

        val active: Any? get() {
            while (true) {
                // helper loop for atomic ops
                val active = this._active
                if (active !is OpDescriptor) return active
                active.perform(this)
            }
        }

        override val isActive: Boolean get() = active != null

        override val idempotentStart: Any? get() {
            val active = this.active
            return if (active === ACTIVE_STATE) null else active
        }

        companion object {
            @JvmField
            val ACTIVE: AtomicReferenceFieldUpdater<NodeList, Any?> =
                    AtomicReferenceFieldUpdater.newUpdater(NodeList::class.java,
                            Any::class.java, "_active")

            @JvmField
            val ACTIVE_STATE = Symbol("ACTIVE_STATE")
        }

        override fun toString(): String = buildString {
            append("List")
            append(if (isActive) "{Active}" else "{New}")
            append("[")
            var first = true
            this@NodeList.forEach<JobNode<*>> { node ->
                if (first) first = false else append(", ")
                append(node)
            }
            append("]")
        }
    }

    impl public open class CompletedIdempotentStart(
            impl @JvmField val idempotentStart: Any?
    )

    /**
     * Class for a [state] of a job that had completed exceptionally, including cancellation.
     *
     * @param cause the exceptional completion cause. If `cause` is null, then a [CancellationException]
     *        if created on first get from [exception] property.
     */
    impl public open class CompletedExceptionally(
            idempotentStart: Any?,
            impl @JvmField val cause: Throwable?
    ) : CompletedIdempotentStart(idempotentStart) {
        @Volatile
        private var _exception: Throwable? = cause // materialize CancellationException on first need

        /**
         * Returns completion exception.
         */
        impl public val exception: Throwable get() =
        _exception ?: // atomic read volatile var or else create new
                java.util.concurrent.CancellationException(
                        "Job was cancelled").also { _exception = it }

        override fun toString(): String = "${this::class.java.simpleName}[$exception]"
    }

    /**
     * A specific subclass of [CompletedExceptionally] for cancelled jobs.
     */
    impl public class Cancelled(
            idempotentStart: Any?,
            cause: Throwable?
    ) : CompletedExceptionally(idempotentStart, cause)

    private class ParentOnCompletion(
            parentJob: Job,
            private val subordinateJob: JobSupport
    ) : JobNode<Job>(parentJob) {
        override fun invoke(reason: Throwable?) {
            subordinateJob.onParentCompletion(reason)
        }

        override fun toString(): String = "ParentOnCompletion[$subordinateJob]"
    }
}

private val EmptyNew = Empty(false)
private val EmptyActive = Empty(true)

private class Empty(override val isActive: Boolean) : JobSupport.Incomplete {
    override val idempotentStart: Any? get() = null
    override fun toString(): String = "Empty{${if (isActive) "Active" else "New"}}"
}

impl internal abstract class JobNode<out J : Job>(
        impl @JvmField val job: J
) : LockFreeLinkedListNode(), DisposableHandle, CompletionHandlerI, JobSupport.Incomplete {
    impl final override val isActive: Boolean get() = true
    impl final override val idempotentStart: Any? get() = null
    // if unregister is called on this instance, then Job was an instance of JobSupport that added this node it itself
    // directly without wrapping
    impl final override fun dispose() = (job as JobSupport).removeNode(this)

    impl override abstract fun invoke(reason: Throwable?)
}

private class InvokeOnCompletion(
        job: Job,
        private val handler: CompletionHandler
) : JobNode<Job>(job) {
    override fun invoke(reason: Throwable?) = handler.invoke(reason)
    override fun toString() = "InvokeOnCompletion[${handler::class.java.name}@${Integer.toHexString(
            System.identityHashCode(handler))}]"
}

private class ResumeOnCompletion(
        job: Job,
        private val continuation: Continuation<Unit>
) : JobNode<Job>(job) {
    override fun invoke(reason: Throwable?) = continuation.resume(Unit)
    override fun toString() = "ResumeOnCompletion[$continuation]"
}

private class SelectJoinOnCompletion<R>(
        job: JobSupport,
        private val select: SelectInstance<R>,
        private val block: suspend () -> R
) : JobNode<JobSupport>(job) {
    override fun invoke(reason: Throwable?) {
        if (select.trySelect(idempotent = null))
            block.startCoroutineCancellable(select.completion)
    }

    override fun toString(): String = "SelectJoinOnCompletion[$select]"
}
