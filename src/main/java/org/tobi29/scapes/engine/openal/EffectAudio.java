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

import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;

import java.util.Optional;

public class EffectAudio implements Audio {
    private final String asset;
    private final Vector3 pos, velocity;
    private final float pitch, gain, range;
    private final boolean hasPosition;
    private double time;

    public EffectAudio(String asset, Vector3 pos, Vector3 velocity, float pitch,
            float gain, float range, boolean hasPosition) {
        this.asset = asset;
        this.pos = pos;
        this.velocity = velocity;
        this.pitch = pitch;
        this.gain = gain;
        this.range = range;
        this.hasPosition = hasPosition;
    }

    @Override
    public boolean poll(SoundSystem sounds, OpenAL openAL,
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
                Optional<AudioData> audio = sounds.get(asset);
                if (audio.isPresent()) {
                    sounds.playSound(audio.get().buffer(), pitch, gain, range,
                            pos, velocity, false, hasPosition);
                }
            }
            return true;
        }
        return false;
    }
}
