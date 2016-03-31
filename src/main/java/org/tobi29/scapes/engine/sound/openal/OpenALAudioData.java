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
package org.tobi29.scapes.engine.sound.openal;

import org.tobi29.scapes.engine.sound.AudioFormat;
import org.tobi29.scapes.engine.sound.PCMUtil;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.io.ByteBufferStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class OpenALAudioData {
    private final int buffer;

    public OpenALAudioData(ReadableAudioStream stream, OpenAL openAL)
            throws IOException {
        this(read(stream), stream.rate(), stream.channels(), openAL);
    }

    public OpenALAudioData(ByteBuffer data, int rate, int channels,
            OpenAL openAL) {
        buffer = openAL.createBuffer();
        openAL.storeBuffer(buffer,
                channels > 1 ? AudioFormat.STEREO : AudioFormat.MONO, data,
                rate);
    }

    private static ByteBuffer read(ReadableAudioStream input)
            throws IOException {
        ByteBufferStream output =
                new ByteBufferStream(BufferCreatorNative::bytes,
                        length -> length + 409600);
        FloatBuffer buffer = BufferCreator.floats(409600);
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

    public void dispose(OpenALSoundSystem soundSystem, OpenAL openAL) {
        soundSystem.removeBufferFromSources(openAL, buffer);
        openAL.deleteBuffer(buffer);
    }

    public int buffer() {
        return buffer;
    }
}
