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

package org.tobi29.scapes.engine.backends.glfw

import org.tobi29.io.ReadableByteStream
import org.tobi29.io.filesystem.FilePath
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.gui.GuiController

expect fun openFileDialog(
    window: ContainerGLFW,
    extensions: Array<Pair<String, String>>,
    multiple: Boolean,
    result: (String, ReadableByteStream) -> Unit
)

expect fun saveFileDialog(
    window: ContainerGLFW,
    extensions: Array<Pair<String, String>>
): FilePath?

expect fun message(
    window: ContainerGLFW,
    messageType: Container.MessageType,
    title: String,
    message: String
)

expect fun dialog(
    window: ContainerGLFW,
    title: String,
    text: GuiController.TextFieldData,
    multiline: Boolean
)

expect fun openFile(path: FilePath)
