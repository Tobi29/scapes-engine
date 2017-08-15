/*
 * Copyright 2012-2017 Tobi29
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

import kotlinx.coroutines.experimental.CoroutineScope
import org.tobi29.scapes.engine.utils.ComponentRegistered
import org.tobi29.scapes.engine.utils.ComponentTypeRegistered
import org.tobi29.scapes.engine.utils.ConcurrentLinkedQueue
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.task.BasicJoinable
import org.tobi29.scapes.engine.utils.task.Joiner
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.nio.channels.Selector

/**
 * Class for processing non-blocking connections asynchronously with possibly
 * multiple threads
 * @param taskExecutor The [TaskExecutor] to start threads with
 * @param maxWorkerSleep Maximum sleep time in milliseconds
 */
class ConnectionManager(
        /**
         * The [TaskExecutor] to start threads with
         */
        val taskExecutor: TaskExecutor,
        private val maxWorkerSleep: Long = 1000) : ComponentRegistered {
    private val workers = ArrayList<ConnectionWorker>()
    private val joiners = ConcurrentLinkedQueue<Joiner>()

    /**
     * Starts a specified number of threads for processing connections
     *
     * May be called multiple times to start more threads
     * @param workerCount The amount of worker threads to start
     */
    fun workers(workerCount: Int) {
        logger.info { "Starting worker $workerCount threads..." }
        val newWorkers = ConcurrentLinkedQueue<ConnectionWorker>()
        val joiners = ArrayList<Joiner>()
        for (i in 0 until workerCount) {
            val joiner = SelectorJoinable(Selector.open())
            val startJoiner = BasicJoinable()
            this.joiners.add(taskExecutor.runThread({ joiner ->
                val worker = ConnectionWorker(this, joiner, maxWorkerSleep)
                newWorkers.add(worker)
                startJoiner.join()
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
            joiners.add(startJoiner.joiner)
        }
        Joiner(joiners).join()
        while (newWorkers.isNotEmpty()) {
            newWorkers.poll()?.let { workers.add(it) }
        }
    }

    /**
     * Adds a new connection to the least occupied worker
     * @param block Code that will be executed on this worker's thread
     * @return `false` if no workers were running
     */
    fun addConnection(block: suspend CoroutineScope.(ConnectionWorker, Connection) -> Unit) = addConnection(
            20000, block)

    /**
     * Adds a new connection to the least occupied worker
     * @param block Code that will be executed on this worker's thread
     * @return `false` if no workers were running
     */
    fun addConnection(timeout: Long,
                      block: suspend CoroutineScope.(ConnectionWorker, Connection) -> Unit): Boolean {
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
        val worker = bestWorker
        worker.addConnection(timeout) { block(this, worker, it) }
        return true
    }

    /**
     * Stops all worker threads and blocks until they shut down
     */
    override fun dispose() {
        val wait = ArrayList<Joiner>()
        while (joiners.isEmpty()) joiners.poll()?.let { wait.add(it) }
        Joiner(wait).join()
        logger.info { "Closed connection workers" }
    }

    companion object : KLogging() {
        val COMPONENT = ComponentTypeRegistered<ConnectionManager>()
    }
}
