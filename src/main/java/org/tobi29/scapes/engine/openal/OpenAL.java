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

import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.nio.ByteBuffer;

public interface OpenAL {
    void checkError(String message);

    void create();

    void destroy();

    void setListener(Vector3 position, Vector3 orientation, Vector3 velocity);

    int createSource();

    void deleteSource(int id);

    void setBuffer(int id, int value);

    void setPitch(int id, float value);

    void setGain(int id, float value);

    void setLooping(int id, boolean value);

    void setRelative(int id, boolean value);

    void setPosition(int id, Vector3 pos);

    void setVelocity(int id, Vector3 vel);

    void play(int id);

    void stop(int id);

    int createBuffer();

    void deleteBuffer(int id);

    void storeBuffer(int id, AudioFormat format, ByteBuffer buffer, int rate);

    boolean isPlaying(int id);

    boolean isStopped(int id);

    int getBuffersQueued(int id);

    int getBuffersPrecessed(int id);

    void queue(int id, int buffer);

    int unqueue();

    int getBuffer(int id);
}
