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

package org.tobi29.scapes.engine.openal;

import org.tobi29.scapes.engine.openal.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.io.ByteBufferStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class AudioData {
    private final int buffer;

    public AudioData(ReadableAudioStream stream, OpenAL openAL)
            throws IOException {
        this(read(stream), stream.rate(), stream.channels(), openAL);
    }

    public AudioData(ByteBuffer data, int rate, int channels, OpenAL openAL) {
        buffer = openAL.createBuffer();
        openAL.storeBuffer(buffer,
                channels > 1 ? AudioFormat.STEREO : AudioFormat.MONO, data,
                rate);
    }

    private static ByteBuffer read(ReadableAudioStream input)
            throws IOException {
        ByteBufferStream output = new ByteBufferStream(
                capacity -> BufferCreatorNative.bytes(capacity + 40960));
        FloatBuffer buffer = BufferCreator.floats(40960);
        boolean valid = true;
        while (valid) {
            valid = input.getSome(buffer);
            buffer.flip();
            while (buffer.hasRemaining()) {
                output.putShort(PCMUtil.toInt32(buffer.get()));
            }
            buffer.clear();
        }
        output.buffer().flip();
        return output.buffer();
    }

    public void dispose(SoundSystem soundSystem, OpenAL openAL) {
        soundSystem.removeBufferFromSources(buffer);
        openAL.deleteBuffer(buffer);
    }

    public int buffer() {
        return buffer;
    }
}
