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

import java8.util.concurrent.ConcurrentMaps
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AddressResolver {
    private val CACHE_TIMEOUT = 10000000000L
    private val THREADS = ConcurrentHashMap<String, Resolver>()
    private val ADDRESSES = WeakHashMap<String, ResolvedAddress>()

    @Throws(UnresolvableAddressException::class)
    fun resolve(hostname: String,
                taskExecutor: TaskExecutor): InetAddress? {
        val address = ADDRESSES[hostname]
        if (address != null && System.nanoTime() - address.resolvedTime <= CACHE_TIMEOUT) {
            if (address.address == null) {
                throw UnresolvableAddressException(hostname)
            }
            return address.address
        }
        ConcurrentMaps.computeIfAbsent(THREADS, hostname) {
            val resolver = Resolver(hostname)
            taskExecutor.runTask(resolver, "Resolve-Address")
            resolver
        }
        return null
    }

    @Throws(UnresolvableAddressException::class)
    fun resolve(hostname: String,
                port: Int,
                taskExecutor: TaskExecutor): InetSocketAddress? {
        val address = resolve(hostname, taskExecutor) ?: return null
        return InetSocketAddress(address, port)
    }

    @Throws(UnresolvableAddressException::class)
    fun resolve(address: RemoteAddress,
                taskExecutor: TaskExecutor): InetSocketAddress? {
        return resolve(address.address, address.port, taskExecutor)
    }

    private class Resolver(val hostname: String) : () -> Unit {
        override fun invoke() {
            try {
                val address = InetAddress.getByName(hostname)
                ADDRESSES.put(hostname,
                        ResolvedAddress(address, System.nanoTime()))
            } catch (e: UnknownHostException) {
                ADDRESSES.put(hostname,
                        ResolvedAddress(null, System.nanoTime()))
            } finally {
                THREADS.remove(hostname)
            }
        }
    }

    private class ResolvedAddress(val address: InetAddress?, val resolvedTime: Long)
}
