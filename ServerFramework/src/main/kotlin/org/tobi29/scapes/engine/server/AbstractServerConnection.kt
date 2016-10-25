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
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

abstract class AbstractServerConnection(taskExecutor: TaskExecutor,
                                        private val connectionHeader: ByteArray,
                                        private val ssl: SSLHandle,
                                        maxWorkerSleep: Long = 1000) : ConnectionManager(
        taskExecutor, maxWorkerSleep) {
    fun start(port: Int): Int {
        val address = start(InetSocketAddress(port))
        return address.port
    }

    fun start(address: InetSocketAddress): InetSocketAddress {
        logger.info { "Starting socket thread..." }
        val channel = ServerSocketChannel.open()
        channel.configureBlocking(false)
        channel.socket().bind(address)
        joiners.add(taskExecutor.runThread({ joiner ->
            try {
                channel.register(joiner.selector, SelectionKey.OP_ACCEPT)
                try {
                    while (!joiner.marked) {
                        val client = channel.accept()
                        if (client == null) {
                            joiner.sleep()
                        } else {
                            client.configureBlocking(false)
                            val result = accept(client)
                            if (result != null) {
                                // Logged as trace to avoid spam
                                logger.trace { "Denied connection: $result" }
                                client.close()
                                continue
                            }
                            if (!addConnection { worker ->
                                val bundleChannel = PacketBundleChannel(
                                        RemoteAddress(address), client,
                                        taskExecutor,
                                        ssl, false)
                                UnknownConnection(worker, bundleChannel, this,
                                        connectionHeader)
                            }) {
                                logger.warn { "Failed to assign connection to worker" }
                                client.close()
                            }
                        }
                    }
                } finally {
                    channel.close()
                }
            } finally {
                joiner.selector.close()
            }
            logger.info { "Stopped socket thread..." }
        }, "Socket", TaskExecutor.Priority.MEDIUM,
                Joiner.SelectorJoinable(Selector.open())))
        return channel.socket().localSocketAddress as InetSocketAddress
    }

    protected abstract fun accept(channel: SocketChannel): String?

    abstract fun newConnection(worker: ConnectionWorker,
                               channel: PacketBundleChannel,
                               id: Byte): Connection?

    companion object : KLogging()
}
