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

package org.tobi29.scapes.engine.sound;

import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

public interface SoundSystem {
    void setListener(Vector3 position, Vector3 orientation, Vector3 velocity);

    boolean isPlaying(String channel);

    void playMusic(String asset, String channel, float pitch, float gain,
            boolean state);

    void playMusic(String asset, String channel, float pitch, float gain,
            Vector3 position, Vector3 velocity, boolean state);

    void playMusic(ReadSource asset, String channel, float pitch, float gain,
            boolean state);

    void playMusic(ReadSource asset, String channel, float pitch, float gain,
            Vector3 position, Vector3 velocity, boolean state);

    void playSound(String asset, String channel, float pitch, float gain);

    void playSound(String asset, String channel, Vector3 position,
            Vector3 velocity, float pitch, float gain);

    StaticAudio playStaticAudio(String asset, String channel, float pitch,
            float gain);

    void stop(String channel);

    void dispose();
}
