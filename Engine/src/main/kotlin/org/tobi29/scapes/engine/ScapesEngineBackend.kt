package org.tobi29.scapes.engine

import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.utils.io.ByteViewE
import org.tobi29.scapes.engine.utils.io.ReadSource

interface ScapesEngineBackend {
    fun allocateNative(size: Int): ByteViewE

    suspend fun loadFont(asset: ReadSource): Font

    fun createSoundSystem(engine: ScapesEngine): SoundSystem
}
