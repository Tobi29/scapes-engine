/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.scapes.engine

import org.tobi29.io.ByteViewE
import org.tobi29.io.ReadSource
import org.tobi29.io.tag.ReadTagMutableMap
import org.tobi29.io.tag.TagMap
import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.graphics.dummy.DummyFont
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.sound.dummy.DummySoundSystem

interface ScapesEngineBackend {
    suspend fun loadFont(asset: ReadSource): Font =
        DummyFont

    fun createSoundSystem(engine: ScapesEngine): SoundSystem =
        DummySoundSystem(engine)

    // TODO: Remove after 0.0.14

    @Deprecated(
        "Use allocateMemoryBuffer",
        ReplaceWith("allocateMemoryBuffer(size)")
    )
    fun allocateNative(size: Int): ByteViewE =
        allocateMemoryBuffer(size)
}

expect sealed class MemoryBuffer : ByteViewE
expect sealed class MemoryBufferPinned : /* MemoryBuffer, */ ByteViewE

expect fun MemoryBufferPinned.asMemoryBuffer(): MemoryBuffer

expect fun MemoryBufferPinned.close()

expect fun allocateMemoryBuffer(size: Int): MemoryBuffer
expect fun allocateMemoryBufferPinned(size: Int): MemoryBufferPinned

inline fun <B : MemoryBufferPinned, R> B.use(block: (B) -> R) = try {
    block(this)
} finally {
    close()
}

interface GraphicsBackend

interface GLBackend : GraphicsBackend {
    fun createGL(container: Container): GL
    fun requestLegacy(config: ReadTagMutableMap = TagMap()): String? = null
}

interface GLESBackend : GraphicsBackend {
    fun createGL(container: Container): GL
}
