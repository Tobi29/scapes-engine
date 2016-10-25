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
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class ConnectionWorker(val connection: ConnectionManager,
                       val joiner: Joiner.SelectorJoinable,
                       private val maxWorkerSleep: Long) {
    private val connectionQueue = ConcurrentLinkedQueue<(ConnectionWorker) -> Connection>()
    private val connections = ArrayList<Connection>()

    /**
     * Returns an estimate for how many connections this worker is processing
     *
     * Note: This technically violates thread-safety, hence the value should be
     * taken with a grain of salt
     * @return The amount of connections on this worker
     */
    val connectionCount: Int
        get() = connections.size

    /**
     * Adds a new connection to this worker
     * @param supplier Code that will be executed on this worker's thread
     */
    fun addConnection(supplier: (ConnectionWorker) -> Connection) {
        connectionQueue.add(supplier)
        joiner.wake()
    }

    /**
     * Executes this worker on the current thread
     *
     * Note: Should never be called twice or concurrently
     */
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

    companion object : KLogging()
}