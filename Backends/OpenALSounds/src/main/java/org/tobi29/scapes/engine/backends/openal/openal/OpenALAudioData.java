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
package org.tobi29.scapes.engine.backends.openal.openal;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.sound.AudioFormat;
import org.tobi29.scapes.engine.utils.codec.AudioBuffer;
import org.tobi29.scapes.engine.utils.codec.ReadableAudioStream;
import org.tobi29.scapes.engine.utils.io.ByteBufferStream;

import java.io.IOException;
import java.nio.ByteBuffer;

public class OpenALAudioData {
    private final int buffer;

    public OpenALAudioData(ByteBuffer data, int channels, int rate,
            OpenAL openAL) {
        buffer = openAL.createBuffer();
        openAL.storeBuffer(buffer,
                channels > 1 ? AudioFormat.STEREO : AudioFormat.MONO, data,
                rate);
    }

    public static OpenALAudioData read(ScapesEngine engine,
            ReadableAudioStream input, OpenAL openAL)
            throws IOException {
        ByteBufferStream output = new ByteBufferStream(engine::allocate,
                length -> length + 409600);
        AudioBuffer buffer = new AudioBuffer(4096, engine::allocate);
        int channels = -1, rate = -1;
        boolean valid = true;
        while (valid) {
            while (!buffer.isDone()) {
                if (!input.get(buffer)) {
                    valid = false;
                    break;
                }
            }
            if (!buffer.isDone()) {
                break;
            }
            if (channels == -1) {
                channels = buffer.channels();
            } else {
                if (channels != buffer.channels()) {
                    throw new IOException(
                            "Number of channels changed in audio file, this is not supported for non-streams");
                }
            }
            if (rate == -1) {
                rate = buffer.rate();
            } else {
                if (rate != buffer.rate()) {
                    throw new IOException(
                            "Sample rate changed in audio file, this is not supported for non-streams");
                }
            }
            buffer.toPCM16(output::putShort);
            buffer.clear();
        }
        output.buffer().flip();
        return new OpenALAudioData(output.buffer(), channels, rate, openAL);
    }

    public void dispose(OpenALSoundSystem soundSystem, OpenAL openAL) {
        soundSystem.removeBufferFromSources(openAL, buffer);
        openAL.deleteBuffer(buffer);
    }

    public int buffer() {
        return buffer;
    }
}
