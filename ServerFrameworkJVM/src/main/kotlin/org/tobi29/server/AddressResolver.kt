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

package org.tobi29.server

import kotlinx.coroutines.experimental.CoroutineName
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Resolves the [hostname] and calls [callback] once done
 * @param hostname The hostname or ip-address to resolve
 * @param taskExecutor The [CoroutineContext] to run a task with
 * @param callback Called with the resulting address or `null` if it failed to resolve
 */
fun resolve(hostname: String,
            taskExecutor: CoroutineContext,
            callback: (InetAddress?) -> Unit) {
    launch(taskExecutor + CoroutineName("Resolve-Address")) {
        try {
            val address = InetAddress.getByName(hostname)
            callback(address)
        } catch (e: UnknownHostException) {
            callback(null)
        }
    }
}

/**
 * Resolves the [hostname] and calls [callback] once done
 * @param hostname The hostname or ip-address to resolve
 * @param port The port that will be used to construct the socket address
 * @param taskExecutor The [CoroutineContext] to run a task with
 * @param callback Called with the resulting socket address or `null` if it failed to resolve
 */
inline fun resolve(hostname: String,
                   port: Int,
                   taskExecutor: CoroutineContext,
                   crossinline callback: (InetSocketAddress?) -> Unit) =
        resolve(hostname, taskExecutor) {
            callback(it?.let { InetSocketAddress(it, port) })
        }

/**
 * Resolves the hostname of the given address and calls [callback] once done
 * @receiver The [RemoteAddress] to resolve
 * @param taskExecutor The [CoroutineContext] to run a task with
 * @param callback Called with the resulting socket address or `null` if it failed to resolve
 */
inline fun RemoteAddress.resolve(
        taskExecutor: CoroutineContext,
        crossinline callback: (InetSocketAddress?) -> Unit) =
        resolve(address, port, taskExecutor, callback)

/**
 * Resolves the [hostname]
 * @param hostname The hostname or ip-address to resolve
 * @param taskExecutor The [CoroutineContext] to run a task with
 * @return The resulting address or `null` if it failed to resolve
 */
suspend fun resolve(hostname: String,
                    taskExecutor: CoroutineContext): InetAddress? {
    return suspendCancellableCoroutine { cont ->
        resolve(hostname, taskExecutor) { cont.resume(it) }
    }
}

/**
 * Resolves the [hostname]
 * @param hostname The hostname or ip-address to resolve
 * @param port The port that will be used to construct the socket address
 * @param taskExecutor The [CoroutineContext] to run a task with
 * @return The resulting socket address or `null` if it failed to resolve
 */
suspend fun resolve(hostname: String,
                    port: Int,
                    taskExecutor: CoroutineContext) =
        resolve(hostname, taskExecutor)?.let { InetSocketAddress(it, port) }

/**
 * Resolves the hostname of the given address
 * @receiver The [RemoteAddress] to resolve
 * @param taskExecutor The [CoroutineContext] to run a task with
 * @return The resulting socket address or `null` if it failed to resolve
 */
suspend fun RemoteAddress.resolve(taskExecutor: CoroutineContext) =
        resolve(address, port, taskExecutor)
