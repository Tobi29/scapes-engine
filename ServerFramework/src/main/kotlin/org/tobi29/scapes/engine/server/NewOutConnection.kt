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
import org.tobi29.scapes.engine.utils.task.Joiner
import java.io.IOException
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.*

class NewOutConnection(address: RemoteAddress,
                       private val connection: ConnectionWorker,
                       private val fail: (Exception) -> Unit,
                       private val init: (SocketChannel) -> Unit) : Connection {
    private val startup: Long
    private var state: (() -> Boolean)? = null
    private var close: (() -> Unit)? = null
    private var selector: ((SocketChannel) -> Unit)? = null

    init {
        startup = System.nanoTime()
        state = { step1(address) }
    }

    private fun step1(address: RemoteAddress): Boolean {
        val socketAddress = try {
            AddressResolver.resolve(address, connection.taskExecutor)
        } catch (e: UnresolvableAddressException) {
            throw IOException(e)
        }
        if (socketAddress != null) {
            val channel = SocketChannel.open()
            channel.configureBlocking(false)
            channel.connect(socketAddress)
            selector?.invoke(channel)
            state = { step2(channel) }
            close = { channel.close() }
            return true
        }
        return false
    }

    private fun step2(channel: SocketChannel): Boolean {
        if (channel.finishConnect()) {
            init(channel)
            state = null
            close = {}
            return true
        }
        return false
    }

    override fun register(joiner: Joiner.SelectorJoinable,
                          opt: Int) {
        this.selector = { channel ->
            channel.register(joiner.selector, opt)
            channel.register(joiner.selector, SelectionKey.OP_CONNECT)
        }
    }

    override fun tick(worker: ConnectionWorker.NetWorkerThread) {
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
        selector = null
        close?.invoke()
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
