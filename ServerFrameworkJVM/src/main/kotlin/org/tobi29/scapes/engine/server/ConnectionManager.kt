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
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import org.tobi29.scapes.engine.utils.AtomicBoolean
import org.tobi29.scapes.engine.utils.ComponentRegistered
import org.tobi29.scapes.engine.utils.ComponentTypeRegisteredUniversal
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.task.launchThread
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Class for processing non-blocking connections asynchronously with possibly
 * multiple threads
 * @param taskExecutor The [CoroutineContext] to start threads with
 * @param maxWorkerSleep Maximum sleep time in milliseconds
 */
class ConnectionManager(
        /**
         * The [CoroutineContext] to start threads with
         */
        val taskExecutor: CoroutineContext,
        private val maxWorkerSleep: Long = 1000) : ComponentRegistered {
    private val workers = ArrayList<Triple<ConnectionWorker, Job, AtomicBoolean>>()

    /**
     * Starts a specified number of threads for processing connections
     *
     * May be called multiple times to start more threads
     * @param workerCount The amount of worker threads to start
     */
    fun workers(workerCount: Int) {
        repeat(workerCount) { worker() }
    }

    /**
     * Starts a thread for processing connections
     *
     * May be called multiple times to start more threads
     */
    fun worker() {
        val worker = ConnectionWorker(this, maxWorkerSleep)
        val stop = AtomicBoolean(false)
        val w = Triple(worker,
                launchThread("Connection-Worker", taskExecutor[Job]) {
                    try {
                        worker.run(stop)
                    } finally {
                        try {
                            worker.close()
                        } catch (e: IOException) {
                            logger.warn { "Failed to close worker: $e" }
                        }
                    }
                }, stop)
        synchronized(workers) { workers.add(w) }
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
        for ((worker, _, _) in workers) {
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
        val closingWorkers = synchronized(workers) {
            workers.toList().also { workers.clear() }
        }
        closingWorkers.forEach { (_, _, stop) -> stop.set(true) }
        runBlocking {
            closingWorkers.forEach { (worker, job, _) ->
                worker.wake()
                job.join()
            }
        }
    }

    companion object : KLogging() {
        val COMPONENT = ComponentTypeRegisteredUniversal<ConnectionManager>()
    }
}
