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
import org.tobi29.io.IOException
import org.tobi29.io.toChannel
import org.tobi29.logging.KLogging
import org.tobi29.io.view
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

abstract class ConnectionListenWorker(private val connections: ConnectionManager,
                                      private val connectionHeader: ByteArray,
                                      private val ssl: SSLHandle) {
    fun start(port: Int): Int {
        val address = start(InetSocketAddress(port))
        return address.port
    }

    fun start(address: InetSocketAddress): InetSocketAddress {
        logger.info { "Starting socket thread..." }
        val channel = ServerSocketChannel.open()
        try {
            channel.configureBlocking(false)
            channel.socket().bind(address)
            connections.addConnection(-1) { worker, connection ->
                channel.use {
                    channel.register(worker.selector, SelectionKey.OP_ACCEPT)
                    while (!connection.shouldClose) {
                        val client = channel.accept()
                        if (client == null) {
                            yield()
                        } else {
                            client.configureBlocking(false)
                            val result = accept(client)
                            if (result != null) {
                                // Logged as trace to avoid spam
                                logger.trace { "Denied connection: $result" }
                                client.close()
                                continue
                            }
                            if (!connections.addConnection { worker, connection ->
                                try {
                                    client.register(worker.selector,
                                            SelectionKey.OP_READ)
                                    val secureChannel = ssl.newSSLChannel(
                                            RemoteAddress(address),
                                            client.toChannel(),
                                            connections.taskExecutor, false)
                                    val bundleChannel = PacketBundleChannel(
                                            secureChannel)
                                    if (bundleChannel.receive()) {
                                        return@addConnection
                                    }
                                    val header = ByteArray(
                                            connectionHeader.size)
                                    bundleChannel.inputStream.get(header.view)
                                    val id = bundleChannel.inputStream.get()
                                    if (header contentEquals connectionHeader) {
                                        client.register(worker.selector,
                                                SelectionKey.OP_READ)
                                        onConnect(worker, bundleChannel, id,
                                                connection)
                                    }
                                    bundleChannel.flushAsync()
                                    secureChannel.requestClose()
                                    bundleChannel.finishAsync()
                                    secureChannel.finishAsync()
                                } catch (e: IOException) {
                                    logger.info { "Error in connection: $e" }
                                } finally {
                                    try {
                                        client.close()
                                    } catch (e: IOException) {
                                        logger.error { "Failed to close socket: $e" }
                                    }
                                }
                            }) {
                                logger.warn { "Failed to assign connection to worker" }
                                client.close()
                            }
                        }
                    }
                }
                logger.info { "Stopped socket thread..." }
            }
            return channel.socket().localSocketAddress as InetSocketAddress
        } catch (e: Throwable) {
            // Avoid leaking channel if setting up outside of the socket thread
            // fails
            try {
                channel.close()
            } catch (e: IOException) {
                logger.error(e) { "Failed closing socket after error" }
            }

            // Rethrow e, we do not actually handle anything here
            throw e
        }
    }

    protected abstract fun accept(channel: SocketChannel): String?

    abstract suspend fun onConnect(worker: ConnectionWorker,
                                   channel: PacketBundleChannel,
                                   id: Byte,
                                   connection: Connection)

    companion object : KLogging()
}
