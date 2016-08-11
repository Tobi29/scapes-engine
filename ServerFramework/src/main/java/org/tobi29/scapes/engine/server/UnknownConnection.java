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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.Random;

public class UnknownConnection implements Connection {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(UnknownConnection.class);
    private static final byte[] CONNECTION_KEY;

    static {
        Random random = new Random(12345);
        CONNECTION_KEY = new byte[16];
        random.nextBytes(CONNECTION_KEY);
    }

    private final byte[] connectionHeader;
    private final AbstractServerConnection connection;
    private final PacketBundleChannel channel;
    private final long startup;
    private State state = State.OPEN;

    public UnknownConnection(PacketBundleChannel channel,
            AbstractServerConnection connection, byte[] connectionHeader) {
        this.channel = channel;
        this.connection = connection;
        this.connectionHeader = connectionHeader;
        startup = System.nanoTime();
    }

    @Override
    public void register(Selector selector, int opt) throws IOException {
        channel.register(selector, opt);
    }

    @Override
    public void tick(AbstractServerConnection.NetWorkerThread worker) {
        try {
            if (channel.process(bundle -> {
                if (state == State.CONNECTED) {
                    return false;
                }
                if (state == State.CLOSED) {
                    return true;
                }
                byte[] header = new byte[connectionHeader.length];
                bundle.get(header);
                if (Arrays.equals(header, connectionHeader)) {
                    Optional<Connection> newConnection =
                            connection.newConnection(channel, bundle.get());
                    if (newConnection.isPresent()) {
                        worker.addConnection(newConnection.get());
                        state = State.CONNECTED;
                        return true;
                    }
                }
                state = State.CLOSED;
                return true;
            }, 1)) {
                state = State.CLOSED;
            }
        } catch (IOException e) {
            LOGGER.info("Error in new connection: {}", e.toString());
            state = State.CLOSED;
        }
    }

    @Override
    public boolean isClosed() {
        return System.nanoTime() - startup > 10000000000L ||
                state != State.OPEN;
    }

    @Override
    public void requestClose() {
    }

    @Override
    public void close() throws IOException {
        if (state != State.CONNECTED) {
            channel.close();
        }
    }

    enum State {
        OPEN,
        CONNECTED,
        CLOSED
    }
}
