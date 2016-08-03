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

package org.tobi29.scapes.engine.utils.codec.mp3;

import javazoom.jl.decoder.*;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.codec.AudioBuffer;
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class MP3ReadStream implements ReadableAudioStream {
    private final ReadableByteChannel channel;
    private final Decoder decoder;
    private final Bitstream bitstream;
    private final OutputBuffer output;
    private final int channels;
    private int rate, outputRate;
    private boolean eos;

    public MP3ReadStream(ReadableByteChannel channel) throws IOException {
        this.channel = channel;
        bitstream = new Bitstream(Channels.newInputStream(channel));
        decoder = new Decoder();
        Header header = readFrame();
        if (header == null) {
            throw new IOException("Unable to read first frame");
        } else {
            channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
            output = new OutputBuffer();
            decoder.setOutputBuffer(output);
            decodeFrame(header);
            outputRate = rate;
        }
    }

    private static int getSampleRate(Header header) {
        int version = header.version();
        switch (header.sample_frequency()) {
            case 0:
                if (version == 1) {
                    return 44100;
                } else if (version == 0) {
                    return 22050;
                }
                return 11025;
            case 1:
                if (version == 1) {
                    return 48000;
                } else if (version == 0) {
                    return 24000;
                }
                return 12000;
            case 2:
                if (version == 1) {
                    return 32000;
                } else if (version == 0) {
                    return 16000;
                }
                return 8000;
        }
        return 0;
    }

    @Override
    public boolean get(AudioBuffer buffer) throws IOException {
        FloatBuffer pcmBuffer = buffer.buffer(channels, rate);
        while (pcmBuffer.hasRemaining() && !eos) {
            if (!decodeFrame(pcmBuffer)) {
                break;
            }
        }
        buffer.done();
        outputRate = rate;
        return !eos;
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
        }
    }

    private boolean decodeFrame(FloatBuffer buffer) throws IOException {
        if (outputRate != rate) {
            // TODO: Need to git an mp3 file that actually does this to test
            return false;
        }
        if (!checkFrame()) {
            return false;
        }
        int len = FastMath.min(buffer.remaining(), output.buffer.remaining());
        int limit = output.buffer.limit();
        output.buffer.limit(output.buffer.position() + len);
        buffer.put(output.buffer);
        output.buffer.limit(limit);
        checkFrame();
        return true;
    }

    private boolean checkFrame() throws IOException {
        if (!output.buffer.hasRemaining()) {
            Header header = readFrame();
            if (header == null) {
                eos = true;
                return false;
            }
            decodeFrame(header);
        }
        return true;
    }

    private Header readFrame() throws IOException {
        try {
            return bitstream.readFrame();
        } catch (BitstreamException e) {
            throw new IOException(e);
        }
    }

    private void decodeFrame(Header header) throws IOException {
        try {
            rate = getSampleRate(header);
            output.buffer.clear();
            decoder.decodeFrame(header, bitstream);
            output.buffer.limit(output.index[0]);
            bitstream.closeFrame();
        } catch (DecoderException e) {
            throw new IOException(e);
        }
    }

    private static class OutputBuffer extends Obuffer {
        private final FloatBuffer buffer;
        private final int[] index;

        public OutputBuffer() {
            buffer = BufferCreator.floats(OBUFFERSIZE * MAXCHANNELS);
            index = new int[MAXCHANNELS];
            clear_buffer();
        }

        @Override
        public void append(int channel, short value) {
            buffer.put(index[channel], (float) value / Short.MAX_VALUE);
            index[channel] += index.length;
        }

        @Override
        public void appendSamples(int channel, float[] f) {
            for (float sample : f) {
                buffer.put(index[channel], sample / Short.MAX_VALUE);
                index[channel] += index.length;
            }
        }

        @Override
        public void write_buffer(int val) {
        }

        @Override
        public void close() {
        }

        @Override
        public void clear_buffer() {
            for (int i = 0; i < index.length; i++) {
                index[i] = i;
            }
        }

        @Override
        public void set_stop_flag() {
        }
    }
}
