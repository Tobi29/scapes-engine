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
import kotlinx.coroutines.experimental.suspendAtomicCancellableCoroutine
import org.tobi29.utils.*

actual interface Channel<E> {
    actual val isClosedForSend: Boolean

    actual val isFull: Boolean

    actual suspend fun send(element: E)

    actual fun offer(element: E): Boolean

    actual fun close(cause: Throwable?): Boolean

    actual val isClosedForReceive: Boolean

    actual val isEmpty: Boolean

    actual suspend fun receive(): E

    actual suspend fun receiveOrNull(): E?

    actual fun poll(): E?

    actual fun cancel(cause: Throwable?): Boolean
}

actual class ClosedSendChannelException actual constructor(
    message: String?
) : CancellationException(message ?: "Channel was closed")

actual class ClosedReceiveChannelException actual constructor(
    message: String?
) : NoSuchElementException(message)

// TODO: This desperately needs testing
actual fun <E> LinkedListChannel(): UnboundChannel<E> =
    object : UnboundChannel<E> {
        private var head: Node<E>? = null
        private var tail: Node<E>? = null
        private var receiveHead: ReceiveNode<E>? = null
        private var receiveTail: ReceiveNode<E>? = null

        override val isClosedForSend get() = tail is Node.End
        override val isClosedForReceive get() = head is Node.End
        override val isFull get() = false
        override val isEmpty get() = tail == null

        override suspend fun send(element: E) {
            offer(element)
        }

        override fun offer(element: E): Boolean {
            checkSend()
            if (receiveHead == null) append(Node.Element(element))
            else receive(element)
            return true
        }

        override suspend fun receive(): E {
            checkReceive()
            val head = head
            if (head != null) {
                head as Node.Element
                if (head.next == null) {
                    this.head = null
                    this.tail = null
                } else this.head = head.next
                head.next = null
                return head.value
            }
            return receiveSuspend()
        }

        override suspend fun receiveOrNull(): E? {
            checkReceive()
            val head = head
            if (head != null) {
                head as Node.Element
                if (head.next == null) {
                    this.head = null
                    this.tail = null
                } else this.head = head.next
                head.next = null
                return head.value
            }
            return receiveSuspendOrNull()
        }

        override fun poll(): E? {
            checkReceive()
            val head = head
            if (head != null) {
                head as Node.Element
                if (head.next == null) {
                    this.head = null
                    this.tail = null
                } else this.head = head.next
                head.next = null
                return head.value
            }
            return null
        }

        override fun close(cause: Throwable?): Boolean {
            if (isClosedForSend) return false
            if (receiveHead == null) append(Node.End(cause))
            else receive(cause)
            return true
        }

        override fun cancel(cause: Throwable?): Boolean {
            if (isClosedForSend) return false
            head = Node.End(cause)
            tail = null
            receive(cause)
            return true
        }

        private suspend fun receiveSuspend(): E =
            suspendAtomicCancellableCoroutine(
                holdCancellability = true
            ) sc@ { cont ->
                appendReceive(ReceiveNode {
                    when (it) {
                        is EitherLeft -> cont.resume(it.value)
                        is EitherRight -> cont.resumeWithException(
                            it.value ?: ClosedReceiveChannelException(
                                "Channel was closed"
                            )
                        )
                    }
                })
            }

        private suspend fun receiveSuspendOrNull(): E? =
            suspendAtomicCancellableCoroutine(
                holdCancellability = true
            ) sc@ { cont ->
                appendReceive(ReceiveNode {
                    when (it) {
                        is EitherLeft -> cont.resume(it.value)
                        is EitherRight -> {
                            val cause = it.value
                            if (cause == null) cont.resume(null)
                            else cont.resumeWithException(cause)
                        }
                    }
                })
            }

        private fun checkSend() {
            val tail = tail
            if (tail is Node.End) {
                throw tail.cause ?: ClosedSendChannelException(
                    "Channel was closed"
                )
            }
        }

        private fun checkReceive() {
            val head = head
            if (head is Node.End) {
                throw head.cause ?: ClosedReceiveChannelException(
                    "Channel was closed"
                )
            }
        }

        private fun receive(element: E) {
            val head = receiveHead
            head as ReceiveNode
            if (head.next == null) {
                receiveHead = null
                receiveTail = null
            } else this.receiveHead = head.next
            head.next = null
            head.callback(ResultOk(element))
        }

        private fun receive(cause: Throwable?) {
            var head = receiveHead
            if (head != null) {
                val error = ResultError(cause)
                do {
                    head!!.callback(error)
                    head = head!!.next
                } while (head != null)
                receiveHead = null
                receiveTail = null
            }
        }

        private fun append(node: Node<E>) {
            val tail = tail
            if (tail == null) {
                this.head = node
                this.tail = node
            } else {
                (tail as Node.Element).next = node
                this.tail = node
            }
        }

        private fun appendReceive(node: ReceiveNode<E>) {
            val tail = receiveTail
            if (tail == null) {
                receiveHead = node
                receiveTail = node
            } else {
                tail.next = node
                receiveTail = node
            }
        }
    }

@Suppress("unused")
private sealed class Node<out E> {
    class Element<E>(
        val value: E
    ) : Node<E>() {
        var next: Node<E>? = null
    }

    class End(
        val cause: Throwable?
    ) : Node<Nothing>()
}

private class ReceiveNode<E>(val callback: (Result<E, Throwable?>) -> Unit) {
    var next: ReceiveNode<E>? = null
}
