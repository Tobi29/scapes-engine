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
import java.nio.channels.SelectionKey
import java.util.*

class UnknownConnection(worker: ConnectionWorker.NetWorkerThread,
                        private val channel: PacketBundleChannel,
                        private val connection: AbstractServerConnection,
                        private val connectionHeader: ByteArray) : Connection {
    private val startup: Long
    private var state = State.OPEN

    init {
        channel.register(worker.joiner, SelectionKey.OP_READ)
        startup = System.nanoTime()
    }

    override fun tick(worker: ConnectionWorker.NetWorkerThread) {
        try {
            if (channel.process({ bundle ->
                if (state == State.CONNECTED) {
                    return@process false
                }
                if (state == State.CLOSED) {
                    return@process true
                }
                val header = ByteArray(connectionHeader.size)
                bundle[header]
                if (Arrays.equals(header, connectionHeader)) {
                    val newConnection = connection.newConnection(worker,
                            channel, bundle.get())
                    if (newConnection != null) {
                        worker.addConnection { newConnection }
                        state = State.CONNECTED
                        return@process true
                    }
                }
                state = State.CLOSED
                true
            }, 1)) {
                state = State.CLOSED
            }
        } catch (e: IOException) {
            logger.info { "Error in new connection: $e" }
            state = State.CLOSED
        }

    }

    override val isClosed: Boolean
        get() = System.nanoTime() - startup > 10000000000L || state != State.OPEN

    override fun requestClose() {
    }

    override fun close() {
        if (state != State.CONNECTED) {
            channel.close()
        }
    }

    internal enum class State {
        OPEN,
        CONNECTED,
        CLOSED
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
