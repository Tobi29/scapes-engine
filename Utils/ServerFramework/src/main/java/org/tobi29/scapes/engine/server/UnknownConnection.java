/*
 * Copyright 2012-2015 Tobi29
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.BufferCreator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Optional;
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
    private final ByteBuffer buffer;
    private final AbstractServerConnection connection;
    private SocketChannel channel;
    private boolean done;

    public UnknownConnection(SocketChannel channel,
            AbstractServerConnection connection, byte[] connectionHeader) {
        this.channel = channel;
        this.connection = connection;
        this.connectionHeader = connectionHeader;
        buffer = BufferCreator.bytes(connectionHeader.length + 1);
    }

    @Override
    public void register(Selector selector, int opt) throws IOException {
        channel.register(selector, opt);
    }

    @Override
    public boolean tick(AbstractServerConnection.NetWorkerThread worker) {
        if (!done) {
            try {
                int read = channel.read(buffer);
                if (!buffer.hasRemaining()) {
                    buffer.rewind();
                    byte[] header = new byte[connectionHeader.length];
                    buffer.get(header);
                    if (Arrays.equals(header, connectionHeader)) {
                        Optional<Connection> newConnection =
                                connection.newConnection(channel, buffer.get());
                        if (newConnection.isPresent()) {
                            worker.addConnection(newConnection.get());
                        }
                        channel = null;
                    }
                    done = true;
                } else if (read == -1) {
                    done = true;
                }
            } catch (IOException e) {
                LOGGER.info("Error in new connection: {}", e.toString());
                done = true;
            }
        }
        return true;
    }

    @Override
    public boolean isClosed() {
        return done;
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }
}
