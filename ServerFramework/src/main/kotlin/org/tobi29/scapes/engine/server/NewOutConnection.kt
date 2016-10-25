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

import mu.KLogging
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.*

class NewOutConnection(private val worker: ConnectionWorker,
                       address: RemoteAddress,
                       private val fail: (Exception) -> Unit,
                       private val init: (SocketChannel) -> Unit) : Connection {
    private val startup: Long
    private var state: (() -> Boolean)? = { false }
    private var channel: SocketChannel? = null

    init {
        startup = System.nanoTime()
        AddressResolver.resolve(address,
                worker.connection.taskExecutor) { socketAddress ->
            if (socketAddress == null) {
                state = { throw UnresolvableAddressException(address.address) }
                return@resolve
            }
            worker.joiner.wake()
            state = { step1(socketAddress) }
        }
    }

    private fun step1(socketAddress: InetSocketAddress): Boolean {
        val channel = SocketChannel.open()
        channel.connect(socketAddress)
        channel.configureBlocking(false)
        channel.register(worker.joiner.selector, SelectionKey.OP_CONNECT)
        this.channel = channel
        state = { step2(channel) }
        return true
    }

    private fun step2(channel: SocketChannel): Boolean {
        if (channel.finishConnect()) {
            init(channel)
            this.channel = null
            state = null
            return true
        }
        return false
    }

    override fun tick(worker: ConnectionWorker) {
        try {
            while (state?.invoke() ?: false) {
            }
            if (System.nanoTime() - startup > 10000000000L) {
                throw IOException("Took too long to connect")
            }
        } catch (e: IOException) {
            logger.info { "Error in new connection: $e" }
            state = null
            fail(e)
        }
    }

    override val isClosed: Boolean
        get() = state == null

    override fun requestClose() {
    }

    override fun close() {
        channel?.close()
    }

    companion object : KLogging() {
        private val CONNECTION_KEY: ByteArray

        init {
            val random = Random(12345)
            CONNECTION_KEY = ByteArray(16)
            random.nextBytes(CONNECTION_KEY)
        }
    }
}
