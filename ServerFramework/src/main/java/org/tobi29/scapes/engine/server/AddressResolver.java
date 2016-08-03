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

package org.tobi29.scapes.engine.server;

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.task.TaskExecutor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class AddressResolver {
    private static final long CACHE_TIMEOUT = 10000000000L;
    private static final Map<String, Resolver> THREADS =
            new ConcurrentHashMap<>();
    private static final Map<String, ResolvedAddress> ADDRESSES =
            new WeakHashMap<>();

    private AddressResolver() {
    }

    public static Optional<InetAddress> resolve(String hostname,
            TaskExecutor taskExecutor) throws UnresolvableAddressException {
        ResolvedAddress address = ADDRESSES.get(hostname);
        if (address != null &&
                System.nanoTime() - address.resolvedTime <= CACHE_TIMEOUT) {
            if (address.address == null) {
                throw new UnresolvableAddressException(hostname);
            }
            return Optional.of(address.address);
        }
        // Check outside of synchronized as well for better performance
        if (!THREADS.containsKey(hostname)) {
            synchronized (THREADS) {
                if (!THREADS.containsKey(hostname)) {
                    Resolver resolver = new Resolver(hostname);
                    THREADS.put(hostname, resolver);
                    taskExecutor.runTask(resolver, "Resolve-Address");
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<InetSocketAddress> resolve(String hostname, int port,
            TaskExecutor taskExecutor) throws UnresolvableAddressException {
        Optional<InetAddress> address = resolve(hostname, taskExecutor);
        if (!address.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new InetSocketAddress(address.get(), port));
    }

    public static Optional<InetSocketAddress> resolve(RemoteAddress address,
            TaskExecutor taskExecutor) throws UnresolvableAddressException {
        return resolve(address.address, address.port, taskExecutor);
    }

    private static final class Resolver implements Runnable {
        private final String hostname;

        private Resolver(String hostname) {
            this.hostname = hostname;
        }

        @Override
        public void run() {
            try {
                InetAddress address = InetAddress.getByName(hostname);
                ADDRESSES.put(hostname,
                        new ResolvedAddress(address, System.nanoTime()));
            } catch (UnknownHostException e) {
                ADDRESSES.put(hostname,
                        new ResolvedAddress(null, System.nanoTime()));
            } finally {
                THREADS.remove(hostname);
            }
        }
    }

    private static final class ResolvedAddress {
        private final InetAddress address;
        private final long resolvedTime;

        private ResolvedAddress(InetAddress address, long resolvedTime) {
            this.address = address;
            this.resolvedTime = resolvedTime;
        }
    }
}
