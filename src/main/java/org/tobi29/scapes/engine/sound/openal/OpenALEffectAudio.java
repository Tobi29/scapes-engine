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

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

public class OpenALEffectAudio implements OpenALAudio {
    private final String asset, channel;
    private final Vector3 pos, velocity;
    private final float pitch, gain;
    private final boolean hasPosition;
    private double time;

    public OpenALEffectAudio(String asset, String channel, Vector3 pos,
            Vector3 velocity, float pitch, float gain, boolean hasPosition) {
        this.asset = asset;
        this.channel = channel;
        this.pos = pos;
        this.velocity = velocity;
        this.pitch = pitch;
        this.gain = gain;
        this.hasPosition = hasPosition;
    }

    @Override
    public boolean poll(OpenALSoundSystem sounds, OpenAL openAL,
            Vector3 listenerPosition, double speedFactor, boolean lagSilence) {
        boolean flag;
        if (hasPosition) {
            time += speedFactor;
            flag = time >=
                    FastMath.pointDistance(listenerPosition, pos) / 343.3;
        } else {
            flag = true;
        }
        if (flag) {
            if (!lagSilence) {
                Optional<OpenALAudioData> audio = sounds.get(openAL, asset);
                if (audio.isPresent()) {
                    float gain = this.gain * sounds.volume(channel);
                    sounds.playSound(openAL, audio.get().buffer(), pitch, gain,
                            pos, velocity, false, hasPosition);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlaying(String channel) {
        return false;
    }

    @Override
    public void stop(OpenALSoundSystem sounds, OpenAL openAL) {
    }
}
