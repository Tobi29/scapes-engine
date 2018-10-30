/*
 * Copyright 2012-2018 Tobi29
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
package org.tobi29.server

import kotlinx.coroutines.experimental.yield
import org.tobi29.stdex.atomic.AtomicBoolean
import org.tobi29.stdex.atomic.AtomicLong
import org.tobi29.io.IOException
import kotlin.math.max
import org.tobi29.utils.systemClock
import java.net.SocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

/**
 * Async function to resolve and address and connect to it through a
 * [SocketChannel]
 * @param worker This worker will be used for waking when the connection can continue
 * @param address The address to connect to
 */
suspend fun connect(worker: ConnectionWorker,
                    address: RemoteAddress): SocketChannel {
    val socketAddress = address.resolve()
            ?: throw UnresolvableAddressException(address.address)
    return connect(worker, socketAddress)
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
    return try {
        channel.configureBlocking(false)
        channel.connect(address)
        channel.register(worker.selector, SelectionKey.OP_CONNECT)
        while (!channel.finishConnect()) {
            yield()
        }
        channel
    } catch (e: IOException) {
        channel.close()
        throw e
    }
}
