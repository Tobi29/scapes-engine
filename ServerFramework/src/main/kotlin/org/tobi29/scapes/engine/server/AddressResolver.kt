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

import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException

object AddressResolver {
    /**
     * Resolves the [hostname] and calls [callback] once done
     * @param hostname The hostname or ip-address to resolve
     * @param taskExecutor The [TaskExecutor] to run a task with
     * @param callback Called with the resulting address or `null` if it failed to resolve
     */
    fun resolve(hostname: String,
                taskExecutor: TaskExecutor,
                callback: (InetAddress?) -> Unit) {
        taskExecutor.runTask({
            try {
                val address = InetAddress.getByName(hostname)
                callback(address)
            } catch (e: UnknownHostException) {
                callback(null)
            }
        }, "Resolve-Address")
    }

    /**
     * Resolves the [hostname] and calls [callback] once done
     * @param hostname The hostname or ip-address to resolve
     * @param port The port that will be used to construct the socket address
     * @param taskExecutor The [TaskExecutor] to run a task with
     * @param callback Called with the resulting socket address or `null` if it failed to resolve
     */
    inline fun resolve(hostname: String,
                       port: Int,
                       taskExecutor: TaskExecutor,
                       crossinline callback: (InetSocketAddress?) -> Unit) {
        resolve(hostname, taskExecutor) { address ->
            val socketAddress = if (address != null) {
                InetSocketAddress(address, port)
            } else {
                null
            }
            callback(socketAddress)
        }
    }

    /**
     * Resolves the hostname of [address] and calls [callback] once done
     * @param address The [RemoteAddress] to resolve
     * @param taskExecutor The [TaskExecutor] to run a task with
     * @param callback Called with the resulting socket address or `null` if it failed to resolve
     */
    inline fun resolve(address: RemoteAddress,
                       taskExecutor: TaskExecutor,
                       crossinline callback: (InetSocketAddress?) -> Unit) {
        return resolve(address.address, address.port, taskExecutor, callback)
    }
}
