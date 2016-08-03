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

package org.tobi29.scapes.engine.backends.openal.openal;

import java8.util.Optional;
import org.tobi29.scapes.engine.sound.StaticAudio;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

public class OpenALStaticAudio implements OpenALAudio, StaticAudio {
    private final String asset, channel;
    private int buffer = -1, source = -1;
    private float pitch, gain, pitchAL, gainAL;
    private boolean playing, dispose;

    OpenALStaticAudio(String asset, String channel, float pitch, float gain) {
        this.asset = asset;
        this.channel = channel;
        this.pitch = pitch;
        this.gain = gain;
    }

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public void setGain(float gain) {
        this.gain = gain;
    }

    @Override
    public void dispose() {
        dispose = true;
    }

    @Override
    public boolean poll(OpenALSoundSystem sounds, OpenAL openAL,
            Vector3 listenerPosition, double delta) {
        if (buffer == -1) {
            Optional<OpenALAudioData> audio = sounds.get(openAL, asset);
            if (audio.isPresent()) {
                buffer = audio.get().buffer();
            }
        }
        if (source == -1) {
            source = sounds.takeSource(openAL);
        }
        if (gain > 0.001f) {
            float gainAL = gain * sounds.volume(channel);
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
                    sounds.playSound(openAL, buffer, source, pitchAL, gainAL,
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
            stop(sounds, openAL);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlaying(String channel) {
        return this.channel.startsWith(channel);
    }

    @Override
    public void stop(OpenALSoundSystem sounds, OpenAL openAL) {
        sounds.releaseSource(openAL, source);
    }
}
