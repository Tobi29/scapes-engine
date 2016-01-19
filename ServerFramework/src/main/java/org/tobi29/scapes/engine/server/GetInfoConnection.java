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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class GetInfoConnection implements Connection {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GetInfoConnection.class);
    private final SocketChannel channel;
    private final ByteBuffer buffer;
    private boolean done;

    public GetInfoConnection(SocketChannel channel, ServerInfo serverInfo) {
        this.channel = channel;
        buffer = serverInfo.getBuffer();
    }

    @Override
    public void register(Selector selector, int opt) throws IOException {
        channel.register(selector, opt);
    }

    @Override
    public boolean tick(AbstractServerConnection.NetWorkerThread worker) {
        if (!done) {
            try {
                int write = channel.write(buffer);
                if (!buffer.hasRemaining() || write == -1) {
                    done = true;
                }
            } catch (IOException e) {
                LOGGER.info("Error in info connection: {}", e.toString());
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
        channel.close();
    }
}
