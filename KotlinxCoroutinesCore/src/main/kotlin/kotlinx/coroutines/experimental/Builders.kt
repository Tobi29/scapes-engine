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

import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

// --------------- basic coroutine builders ---------------

/**
 * Launches new coroutine without blocking current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The [context] for the new coroutine must be explicitly specified.
 * See [CoroutineDispatcher] for the standard [context] implementations that are provided by `kotlinx.coroutines`.
 * The [context][CoroutineScope.context] of the parent coroutine from its [scope][CoroutineScope] may be used,
 * in which case the [Job] of the resulting coroutine is a child of the job of the parent coroutine.
 *
 * By default, the coroutine is immediately scheduled for execution.
 * Other options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine cancel parent job in the context by default
 * (unless [CoroutineExceptionHandler] is explicitly specified), which means that when `launch` is used with
 * the context of another coroutine, then any uncaught exception leads to the cancellation of parent coroutine.
 *
 * See [newCoroutineContext] for a description of debugging facilities that are available for newly created coroutine.

 * @param context context of the coroutine
 * @param start coroutine start option
 * @param block the coroutine code
 */
public fun launch(
        context: CoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
): Job = launchImpl(context, start, block)

header internal fun launchImpl(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
): Job

/**
 * @suppress **Deprecated**: Use `start = CoroutineStart.XXX` parameter
 */
@Deprecated(message = "Use `start = CoroutineStart.XXX` parameter",
        replaceWith = ReplaceWith(
                "launch(context, if (start) CoroutineStart.DEFAULT else CoroutineStart.LAZY, block)"))
public fun launch(context: CoroutineContext,
                  start: Boolean,
                  block: suspend CoroutineScope.() -> Unit): Job =
        launch(context,
                if (start) CoroutineStart.DEFAULT else CoroutineStart.LAZY,
                block)

/**
 * Calls the specified suspending block with a given coroutine context, suspends until it completes, and returns
 * the result.
 *
 * This function immediately applies dispatcher from the new context, shifting execution of the block into the
 * different thread inside the block, and back when it completes.
 * The specified [context] is added onto the current coroutine context for the execution of the block.
 *
 * An optional `start` parameter is used only if the specified `context` uses a different [CoroutineDispatcher] than
 * a current one, otherwise it is ignored.
 * By default, the coroutine is immediately scheduled for execution and can be cancelled
 * while it is waiting to be executed and it can be cancelled while the result is scheduled
 * to be be processed by the invoker context.
 * Other options can be specified via `start` parameter. See [CoroutineStart] for details.
 * A value of [CoroutineStart.LAZY] is not supported and produces [IllegalArgumentException].
 */
public suspend fun <T> run(
        context: CoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend () -> T
): T = runImpl(context, start, block)

header internal suspend fun <T> runImpl(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend () -> T
): T

/** @suppress **Deprecated** */
@Suppress(
        "DeprecatedCallableAddReplaceWith") // todo: the warning is incorrectly shown, see KT-17917
@Deprecated(message = "It is here for binary compatibility only",
        level = DeprecationLevel.HIDDEN)
public suspend fun <T> run(context: CoroutineContext,
                           block: suspend () -> T): T =
        run(context, start = CoroutineStart.ATOMIC, block = block)

/**
 * Runs new coroutine and **blocks** current thread _interruptibly_ until its completion.
 * This function should not be used from coroutine. It is designed to bridge regular blocking code
 * to libraries that are written in suspending style, to be used in `main` functions and in tests.
 *
 * The default [CoroutineDispatcher] for this builder in an implementation of [EventLoop] that processes continuations
 * in this blocked thread until the completion of this coroutine.
 * See [CoroutineDispatcher] for the other implementations that are provided by `kotlinx.coroutines`.
 *
 * If this blocked thread is interrupted (see [Thread.interrupt]), then the coroutine job is cancelled and
 * this `runBlocking` invocation throws [InterruptedException].
 *
 * See [newCoroutineContext] for a description of debugging facilities that are available for newly created coroutine.
 */
// TODO: @Throws(InterruptedException::class)
public fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext,
                           block: suspend CoroutineScope.() -> T): T =
        runBlockingImpl(context, block)

header internal fun <T> runBlockingImpl(context: CoroutineContext,
                                        block: suspend CoroutineScope.() -> T): T
