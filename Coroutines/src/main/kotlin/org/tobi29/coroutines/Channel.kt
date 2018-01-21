/*
 * Copyright 2012-2017 Tobi29
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

import kotlinx.coroutines.experimental.CancellationException

/**
 * Common interface for thread-safe channels
 *
 * **Note:** This interface is incomplete and shall *never* be implemented
 * manually as it would be missing methods on jvm!
 */
expect interface Channel<E> {
    val isEmpty: Boolean
    val isFull: Boolean
    val isClosedForSend: Boolean
    val isClosedForReceive: Boolean

    suspend fun send(element: E)
    fun offer(element: E): Boolean

    suspend fun receive(): E
    suspend fun receiveOrNull(): E?
    fun poll(): E?

    fun close(cause: Throwable?): Boolean
    fun cancel(cause: Throwable?): Boolean
}

typealias UnboundChannel<E> = Channel<E>

expect class ClosedSendChannelException(message: String?) : CancellationException

expect class ClosedReceiveChannelException(message: String?) : NoSuchElementException

expect fun <E> LinkedListChannel(): UnboundChannel<E>