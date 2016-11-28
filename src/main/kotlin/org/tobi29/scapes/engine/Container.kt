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

package org.tobi29.scapes.engine

import org.tobi29.scapes.engine.graphics.Font
import org.tobi29.scapes.engine.graphics.GL
import org.tobi29.scapes.engine.gui.GuiController
import org.tobi29.scapes.engine.input.ControllerDefault
import org.tobi29.scapes.engine.input.ControllerJoystick
import org.tobi29.scapes.engine.input.ControllerTouch
import org.tobi29.scapes.engine.input.FileType
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import java.io.IOException
import java.nio.ByteBuffer

interface Container {
    fun formFactor(): FormFactor

    fun containerWidth(): Int

    fun containerHeight(): Int

    fun contentWidth(): Int

    fun contentHeight(): Int

    fun contentResized(): Boolean

    fun setMouseGrabbed(value: Boolean)

    fun updateContainer()

    fun update(delta: Double)

    fun gl(): GL

    fun sound(): SoundSystem

    fun controller(): ControllerDefault?

    fun joysticks(): Collection<ControllerJoystick>

    fun joysticksChanged(): Boolean

    fun touch(): ControllerTouch?

    fun loadFont(asset: String): Font?

    fun allocate(capacity: Int): ByteBuffer

    @Throws(DesktopException::class)
    fun run()

    fun stop()

    fun clipboardCopy(value: String)

    fun clipboardPaste(): String

    @Throws(IOException::class)
    fun openFileDialog(type: FileType,
                       title: String,
                       multiple: Boolean,
                       result: (String, ReadableByteStream) -> Unit)

    fun saveFileDialog(extensions: Array<Pair<String, String>>,
                       title: String): FilePath?

    fun message(messageType: MessageType,
                title: String,
                message: String)

    fun dialog(title: String,
               text: GuiController.TextFieldData,
               multiline: Boolean)

    fun openFile(path: FilePath)

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
