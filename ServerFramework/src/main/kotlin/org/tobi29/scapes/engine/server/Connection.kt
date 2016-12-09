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

/**
 * Interface for a generic connection inside a [ConnectionWorker]
 */
interface Connection {
    /**
     * Called occasionally to poll the connection
     *
     * **Note:** To increase the frequency of this being called, register
     * something to the [ConnectionWorker]
     * **Note:** [worker] will always stay the same instance, so one can use it
     * at construction time
     */
    fun tick(worker: ConnectionWorker)

    /**
     * Should return `true` once the connection is gracefully closed
     *
     * **Note:** The worker will call [close] afterwards to fully clean up
     */
    val isClosed: Boolean

    /**
     * Requests the connection to gracefully close
     */
    fun requestClose()

    /**
     * Tears down the connection immediately, no resources like sockets should
     * be kept open after this got called, unless passed on to some other
     * component
     *
     * **Note:** This is called after the last time that [tick] got executed
     */
    fun close()
}
