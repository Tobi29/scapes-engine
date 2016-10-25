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

open class ConnectionManager(val taskExecutor: TaskExecutor,
                             private val maxWorkerSleep: Long = 1000) {
    private val workers = ArrayList<ConnectionWorker>()
    protected val joiners = ArrayList<Joiner>()

    fun workers(workerCount: Int) {
        logger.info { "Starting worker $workerCount threads..." }
        for (i in 0..workerCount - 1) {
            val joiner = Joiner.SelectorJoinable(Selector.open())
            val worker = ConnectionWorker(this, joiner, maxWorkerSleep)
            joiners.add(taskExecutor.runThread({ joiner ->
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

    fun addConnection(supplier: (ConnectionWorker) -> Connection): Boolean {
        var load = Int.MAX_VALUE
        var bestWorker: ConnectionWorker? = null
        for (worker in workers) {
            val workerLoad = worker.connectionCount
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

    fun stop() {
        Joiner(joiners).join()
        logger.info { "Closed connection workers" }
    }

    companion object : KLogging()
}

inline fun ConnectionManager.addOutConnection(address: RemoteAddress,
                                              noinline error: (Exception) -> Unit,
                                              crossinline init: (ConnectionWorker, SocketChannel) -> Unit) {
    addConnection { worker ->
        NewOutConnection(worker, address, error) { channel ->
            init(worker, channel)
        }
    }
}
