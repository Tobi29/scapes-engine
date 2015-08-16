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
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

import java.util.Optional;

public class StaticAudio implements Audio {
    private final String asset;
    private int buffer = -1, source = -1;
    private float pitch, gain, pitchAL, gainAL;
    private boolean playing, dispose;

    StaticAudio(String asset, float pitch, float gain) {
        this.asset = asset;
        this.pitch = pitch;
        this.gain = gain;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public void dispose() {
        dispose = true;
    }

    @Override
    public boolean poll(SoundSystem sounds, OpenAL openAL,
            Vector3 listenerPosition, double speedFactor, boolean lagSilence) {
        if (buffer == -1) {
            Optional<AudioData> audio = sounds.get(asset);
            if (audio.isPresent()) {
                buffer = audio.get().buffer();
            }
        }
        if (source == -1) {
            source = openAL.createSource();
        }
        if (gain > 0.001f) {
            float gainAL = gain * sounds.soundVolume();
            float pitchAL = pitch;
            if (playing) {
                if (FastMath.abs(gainAL - this.gainAL) > 0.001f) {
                    openAL.setGain(source, gainAL);
                    this.gainAL = gainAL;
                }
                if (FastMath.abs(pitchAL - this.pitchAL) > 0.001f) {
                    openAL.setPitch(source, pitchAL);
                    this.pitchAL = pitchAL;
                }
            } else {
                playing = true;
                if (buffer != -1) {
                    sounds.playSound(buffer, source, pitchAL, gainAL, 16.0f,
                            Vector3d.ZERO, Vector3d.ZERO, true, false);
                }
                this.gainAL = gainAL;
                this.pitchAL = pitchAL;
            }
        } else {
            if (playing) {
                playing = false;
                openAL.stop(source);
            }
        }
        if (dispose) {
            openAL.stop(source);
            openAL.setBuffer(source, 0);
            openAL.deleteSource(source);
            return true;
        }
        return false;
    }
}
