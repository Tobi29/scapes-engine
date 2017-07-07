/*
 * Copyright 2012-2017 Tobi29
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

import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.graphics.GraphicsObjectSupplier
import org.tobi29.scapes.engine.gui.GuiController
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.utils.io.ByteBufferProvider

interface Container : ByteBufferProvider {
    val engine: ScapesEngine
    val gos: GraphicsObjectSupplier
    val sounds: SoundSystem
    val formFactor: FormFactor
    val containerWidth: Int
    val containerHeight: Int

    fun updateContainer()

    fun update(delta: Double)

    fun loadFont(asset: String): Font?

    // TODO: @Throws(DesktopException::class)
    fun run()

    fun stop()

    fun clipboardCopy(value: String)

    fun clipboardPaste(): String

    fun message(messageType: MessageType,
                title: String,
                message: String)

    fun dialog(title: String,
               text: GuiController.TextFieldData,
               multiline: Boolean)

    fun isRenderCall(): Boolean

    enum class MessageType {
        ERROR,
        INFORMATION,
        WARNING,
        QUESTION,
        PLAIN
    }

    enum class FormFactor {
        DESKTOP,
        PHONE
    }
}
