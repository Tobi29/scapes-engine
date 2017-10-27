package org.tobi29.scapes.engine.backends.lwjgl3

import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.ScapesEngineBackend
import org.tobi29.scapes.engine.backends.lwjgl3.openal.LWJGL3OpenAL
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.utils.io.ByteBufferNative
import org.tobi29.scapes.engine.utils.io.ByteViewE
import org.tobi29.scapes.engine.utils.io.ReadSource
import org.tobi29.scapes.engine.utils.io.viewE

object ScapesEngineLWJGL3 : ScapesEngineBackend {
    override fun allocateNative(size: Int): ByteViewE =
            ByteBufferNative(size).viewE

    override suspend fun loadFont(asset: ReadSource): Font =
            STBFont.loadFont(this, asset)

    override fun createSoundSystem(engine: ScapesEngine): SoundSystem =
            OpenALSoundSystem(engine, LWJGL3OpenAL(), 64, 5.0)
}
