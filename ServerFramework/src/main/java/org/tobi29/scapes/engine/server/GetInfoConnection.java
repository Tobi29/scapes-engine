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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import java.io.IOException;
import java.nio.channels.Selector;

public class GetInfoConnection implements Connection {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GetInfoConnection.class);
    private final PacketBundleChannel channel;
    private final long startup;
    private State state = State.OPEN;

    public GetInfoConnection(PacketBundleChannel channel, ServerInfo serverInfo)
            throws IOException {
        this.channel = channel;
        startup = System.nanoTime();
        WritableByteStream output = channel.getOutputStream();
        output.put(serverInfo.getBuffer());
        channel.queueBundle();
        channel.requestClose();
    }

    @Override
    public void register(Selector selector, int opt) throws IOException {
        channel.register(selector, opt);
    }

    @Override
    public boolean tick(AbstractServerConnection.NetWorkerThread worker) {
        try {
            if (channel.process()) {
                state = State.CLOSED;
            }
        } catch (IOException e) {
            LOGGER.info("Error in info connection: {}", e.toString());
            state = State.CLOSED;
        }
        return false;
    }

    @Override
    public boolean isClosed() {
        return System.nanoTime() - startup > 10000000000L ||
                state == State.CLOSED;
    }

    @Override
    public void requestClose() {
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    private enum State {
        OPEN,
        CLOSED
    }
}
