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
import java.io.IOException
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class ConnectionWorker(val taskExecutor: TaskExecutor,
                            private val maxWorkerSleep: Long = 1000) {
    private val workers = ArrayList<NetWorkerThread>()
    private val workerJoiners = ArrayList<Joiner>()

    fun workers(workerCount: Int) {
        logger.info { "Starting worker $workerCount threads..." }
        for (i in 0..workerCount - 1) {
            val joiner = Joiner.SelectorJoinable(Selector.open())
            val worker = NetWorkerThread(this, joiner, maxWorkerSleep)
            workerJoiners.add(taskExecutor.runThread({ joiner ->
                try {
                    worker.run()
                } finally {
                    try {
                        joiner.selector.close()
                    } catch (e: IOException) {
                        logger.warn { "Failed to close selector: $e" }
                    }
                }
            }, "Connection-Worker-" + i, joiner = joiner))
            workers.add(worker)
        }
    }

    fun addConnection(supplier: (NetWorkerThread) -> Connection): Boolean {
        var load = Int.MAX_VALUE
        var bestWorker: NetWorkerThread? = null
        for (worker in workers) {
            val workerLoad = worker.connections.size
            if (workerLoad < load) {
                bestWorker = worker
                load = workerLoad
            }
        }
        if (bestWorker == null) {
            return false
        }
        bestWorker.addConnection(supplier)
        return true
    }

    open fun stop() {
        Joiner(workerJoiners).join()
        logger.info { "Closed connection workers" }
    }

    class NetWorkerThread(val connection: ConnectionWorker,
                          val joiner: Joiner.SelectorJoinable,
                          private val maxWorkerSleep: Long) {
        internal val connectionQueue = ConcurrentLinkedQueue<(NetWorkerThread) -> Connection>()
        internal val connections = ArrayList<Connection>()

        fun addConnection(supplier: (NetWorkerThread) -> Connection) {
            connectionQueue.add(supplier)
            wake()
        }

        fun wake() {
            joiner.wake()
        }

        fun run() {
            try {
                while (!joiner.marked) {
                    process()
                    if (!connectionQueue.isEmpty()) {
                        while (!connectionQueue.isEmpty()) {
                            val connection = connectionQueue.poll()(this)
                            connections.add(connection)
                        }
                    } else if (!joiner.marked) {
                        val sleep = if (connections.isEmpty()) 0 else maxWorkerSleep
                        joiner.sleep(sleep)
                    }
                }
                connections.forEach { it.requestClose() }
                val stopTimeout = System.nanoTime()
                while (!connections.isEmpty() && System.nanoTime() - stopTimeout < 10000000000L) {
                    process()
                    if (!joiner.marked) {
                        joiner.sleep(10)
                    }
                }
                while (!connectionQueue.isEmpty()) {
                    connectionQueue.poll()(this).close()
                }
            } finally {
                for (connection in connections) {
                    try {
                        connection.close()
                    } catch (e: IOException) {
                        logger.warn { "Failed to close connection: $e" }
                    }
                }
            }
        }

        private fun process() {
            val iterator = connections.iterator()
            while (iterator.hasNext()) {
                val connection = iterator.next()
                if (!connection.isClosed) {
                    connection.tick(this)
                }
                if (connection.isClosed) {
                    try {
                        connection.close()
                    } catch (e: IOException) {
                        logger.warn { "Failed to close connection: $e" }
                    }
                    iterator.remove()
                }
            }
        }
    }

    companion object : KLogging()
}

inline fun ConnectionWorker.addOutConnection(address: RemoteAddress,
                                             noinline error: (Exception) -> Unit,
                                             crossinline init: (ConnectionWorker.NetWorkerThread, SocketChannel) -> Unit) {
    addConnection { worker ->
        NewOutConnection(worker, address, error) { channel ->
            init(worker, channel)
        }
    }
}
