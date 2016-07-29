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
package org.tobi29.scapes.engine.utils.codec.ogg;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.ReadableByteChannel;

public class OGGReadStream implements ReadableAudioStream {
    private static final int BUFFER_SIZE = 4096;
    private final ReadableByteChannel channel;
    private final Packet packet = new Packet();
    private final Page page = new Page();
    private final StreamState streamState = new StreamState();
    private final SyncState syncState = new SyncState();
    private final DspState dspState = new DspState();
    private final Block block = new Block(dspState);
    private final Comment comment = new Comment();
    private final Info info = new Info();
    private final int channels, rate;
    private final int[] index;
    private final float[][][] pcm = new float[1][][];
    private boolean eos;

    public OGGReadStream(ReadableByteChannel channel) throws IOException {
        this.channel = channel;
        syncState.init();
        info.init();
        comment.init();
        readHeader();
        channels = info.channels;
        rate = info.rate;
        index = new int[channels];
    }

    @Override
    public int channels() {
        return channels;
    }

    @Override
    public int rate() {
        return rate;
    }

    @Override
    public void frame() {
    }

    @Override
    public boolean getSome(FloatBuffer buffer, int len) throws IOException {
        int limit = buffer.limit();
        buffer.limit(buffer.position() + len);
        while (buffer.hasRemaining() && !eos) {
            if (!decodePacket(buffer)) {
                break;
            }
        }
        buffer.limit(limit);
        return !eos;
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
        }
    }

    private void readHeader() throws IOException {
        while (!readPage()) {
        }
        streamState.init(page.serialno());
        if (streamState.pagein(page) == -1) {
            throw new IOException("Error reading first header page");
        }
        if (streamState.packetout(packet) != 1) {
            throw new IOException("Error reading first header packet");
        }
        if (info.synthesis_headerin(comment, packet) < 0) {
            throw new IOException("Error interpreting first header packet");
        }
        for (int i = 0; i < 2; i++) {
            while (!readPacket()) {
            }
            info.synthesis_headerin(comment, packet);
        }
        dspState.synthesis_init(info);
        block.init(dspState);
    }

    private boolean decodePacket(FloatBuffer buffer) throws IOException {
        while (true) {
            int samples = dspState.synthesis_pcmout(pcm, index);
            if (samples == 0) {
                if (!readPacket()) {
                    return false;
                }
                if (block.synthesis(packet) != 0) {
                    return true;
                }
                dspState.synthesis_blockin(block);
                continue;
            }
            float[][] pcmSamples = pcm[0];
            int length = FastMath.min(samples, buffer.remaining() / channels);
            int offset = buffer.position();
            for (int i = 0; i < channels; i++) {
                float[] channel = pcmSamples[i];
                int location = index[i];
                int position = offset + i;
                for (int j = 0; j < length; j++) {
                    buffer.put(position, channel[location + j]);
                    position += channels;
                }
            }
            buffer.position(offset + length * channels);
            dspState.synthesis_read(length);
            return true;
        }
    }

    private boolean readPacket() throws IOException {
        while (true) {
            switch (streamState.packetout(packet)) {
                case -1:
                    throw new IOException("Hole in packet");
                case 1:
                    return true;
            }
            if (!readPage()) {
                return false;
            }
            streamState.pagein(page);
        }
    }

    private boolean readPage() throws IOException {
        while (true) {
            switch (syncState.pageout(page)) {
                case -1:
                    throw new IOException("Hole in page");
                case 1:
                    return true;
            }
            if (!fillBuffer()) {
                return false;
            }
        }
    }

    private boolean fillBuffer() throws IOException {
        int offset = syncState.buffer(BUFFER_SIZE);
        int read = channel.read(
                ByteBuffer.wrap(syncState.data, offset, BUFFER_SIZE));
        if (read == -1) {
            eos = true;
        }
        if (read <= 0) {
            return false;
        }
        syncState.wrote(read);
        return true;
    }
}
