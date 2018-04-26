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

package org.tobi29.scapes.engine.gui

import org.tobi29.stdex.JsName
import org.tobi29.stdex.JvmName

class GuiComponentTextButton(
    parent: GuiLayoutData,
    textX: Int,
    textSize: Int,
    text: String
) : GuiComponentButton(parent) {
    private val _text: GuiComponentText = addSubHori(
        textX.toDouble(), 0.0, -1.0, textSize.toDouble()
    ) { GuiComponentText(it, text) }

    constructor(
        parent: GuiLayoutData,
        textSize: Int,
        text: String
    ) : this(parent, 4, textSize, text)

    var textFilter: (String) -> String
        get() = _text.textFilter
        set(value) {
            _text.textFilter = value
        }
    var text: String
        get() = _text.text
        set(value) {
            _text.text = value
        }
}
