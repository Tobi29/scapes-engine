/*
 * Copyright 2012-2019 Tobi29
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

import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

/**
 * Resolves the [hostname]
 * @param hostname The hostname or ip-address to resolve
 * @param context Context for running resolve
 * @return The resulting address or `null` if it failed to resolve
 */
suspend inline fun resolve(
    hostname: String,
    context: CoroutineContext = Dispatchers.IO
): InetAddress? = withContext(context) {
    try {
        InetAddress.getByName(hostname)
    } catch (e: UnknownHostException) {
        null
    }
}

/**
 * Resolves the [hostname]
 * @param hostname The hostname or ip-address to resolve
 * @param port The port that will be used to construct the socket address
 * @param context Context for running resolve
 * @return The resulting socket address or `null` if it failed to resolve
 */
suspend inline fun resolve(
    hostname: String,
    port: Int,
    context: CoroutineContext = Dispatchers.IO
): InetSocketAddress? = resolve(hostname, context)
    ?.let { InetSocketAddress(it, port) }

/**
 * Resolves the hostname of the given address
 * @param context Context for running resolve
 * @return The resulting socket address or `null` if it failed to resolve
 */
suspend fun RemoteAddress.resolve(
    context: CoroutineContext = Dispatchers.IO
): InetSocketAddress? = resolve(address, port, context)

// TODO: Remove after 0.0.14

@Deprecated("Use suspending functions")
fun resolve(
    hostname: String,
    taskExecutor: CoroutineContext,
    callback: (InetAddress?) -> Unit
) {
    GlobalScope.launch(taskExecutor + CoroutineName("Resolve-Address")) {
        try {
            val address = InetAddress.getByName(hostname)
            callback(address)
        } catch (e: UnknownHostException) {
            callback(null)
        }
    }
}

@Deprecated("Use suspending functions")
inline fun resolve(
    hostname: String,
    port: Int,
    taskExecutor: CoroutineContext,
    crossinline callback: (InetSocketAddress?) -> Unit
) =
    resolve(hostname, taskExecutor) {
        callback(it?.let { InetSocketAddress(it, port) })
    }

@Deprecated("Use suspending functions")
inline fun RemoteAddress.resolve(
    taskExecutor: CoroutineContext,
    crossinline callback: (InetSocketAddress?) -> Unit
) =
    resolve(address, port, taskExecutor, callback)
