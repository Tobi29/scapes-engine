/*
 * Copyright 2012-2016 Tobi29
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
package org.tobi29.scapes.engine.server

import kotlinx.coroutines.experimental.yield
import org.tobi29.scapes.engine.utils.math.max
import java.io.IOException
import java.net.SocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Handle to communicate with the worker from a connection job
 *
 * This class serves 2 purposes:
 * * Notifying the worker that the connection has not timed out
 * * Keeping track of whether or not the connection should gracefully close
 */
class Connection(private val requestClose: AtomicBoolean,
                 private val timeout: AtomicLong) {
    /**
     * Set the timeout to at least [timeout] ms from now
     * @param timeout The time from now in milliseconds
     */
    fun increaseTimeout(timeout: Long) {
        val nextTime = System.currentTimeMillis() + timeout
        while (true) {
            val prev = this.timeout.get()
            val next = max(prev, nextTime)
            if (this.timeout.compareAndSet(prev, next)) {
                break
            }
        }
    }

    /**
     * Sets [shouldClose] to `true`
     */
    fun requestClose() = requestClose.set(true)

    /**
     * Returns `true` whenever a request was made to close this connection job,
     * possibly called by the worker
     */
    val shouldClose get() = requestClose.get()
}

/**
 * Async function to resolve and address and connect to it through a
 * [SocketChannel]
 * @param worker This worker will be used for waking when the connection can continue
 * @param address The address to connect to
 */
suspend fun connect(worker: ConnectionWorker,
                    address: RemoteAddress): SocketChannel {
    val state = AtomicReference<(() -> SocketAddress)?>()
    AddressResolver.resolve(address,
            worker.connection.taskExecutor) { socketAddress ->
        if (socketAddress == null) {
            state.set { throw UnresolvableAddressException(address.address) }
            return@resolve
        }
        worker.joiner.wake()
        state.set { socketAddress }
    }
    var result: (() -> SocketAddress)? = state.get()
    while (result == null) {
        yield()
        result = state.get()
    }
    return connect(worker, result())
}

/**
 * Async function to resolve and address and connect to it through a
 * [SocketChannel]
 * @param worker This worker will be used for waking when the connection can continue
 * @param address The address to connect to
 */
suspend fun connect(worker: ConnectionWorker,
                    address: SocketAddress): SocketChannel {
    val channel = SocketChannel.open()
    try {
        channel.configureBlocking(false)
        channel.connect(address)
        channel.register(worker.joiner.selector, SelectionKey.OP_CONNECT)
        while (!channel.finishConnect()) {
            yield()
        }
        return channel
    } catch (e: IOException) {
        channel.close()
        throw e
    }
}
