/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.scapes.engine.backends.lwjgl3

import org.tobi29.io.ByteBufferNative
import org.tobi29.io.ByteViewE
import org.tobi29.io.ReadSource
import org.tobi29.io.viewE
import org.tobi29.scapes.engine.ScapesEngine
import org.tobi29.scapes.engine.ScapesEngineBackend
import org.tobi29.scapes.engine.backends.lwjgl3.openal.LWJGL3OpenAL
import org.tobi29.scapes.engine.backends.openal.openal.OpenALSoundSystem
import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.sound.SoundSystem

object ScapesEngineLWJGL3 : ScapesEngineBackend {
    override fun allocateNative(size: Int): ByteViewE =
        ByteBufferNative(size).viewE

    override suspend fun loadFont(asset: ReadSource): Font =
        STBFont.loadFont(this, asset)

    override fun createSoundSystem(engine: ScapesEngine): SoundSystem =
        OpenALSoundSystem(engine, LWJGL3OpenAL(), 64, 5.0)
}
