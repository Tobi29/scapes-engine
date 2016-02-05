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
