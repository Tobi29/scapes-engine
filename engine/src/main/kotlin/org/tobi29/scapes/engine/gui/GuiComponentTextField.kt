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

import org.tobi29.stdex.copyToString

class GuiComponentTextField(
    parent: GuiLayoutData,
    textX: Int,
    textSize: Int,
    text: String,
    maxLength: Int,
    hiddenText: Boolean
) : GuiComponentButtonHeavy(parent) {
    private val textComponent = addSubHori(
        textX.toDouble(), 0.0, -1.0,
        textSize.toDouble()
    ) {
        GuiComponentEditableText(it, text, maxLength, {
            gui.isFocused && gui.currentSelection === this@GuiComponentTextField
        })
    }

    var text: String
        get() = textComponent.text
        set(value) {
            textComponent.text = value
        }

    init {
        if (hiddenText) {
            this.textComponent.textFilter = { str ->
                CharArray(str.length) { '*' }.copyToString()
            }
        } else {
            this.textComponent.textFilter = { str -> str }
        }
    }

    constructor(
        parent: GuiLayoutData,
        textSize: Int,
        text: String,
        hiddenText: Boolean = false
    ) : this(parent, textSize, text, Int.MAX_VALUE, hiddenText)

    constructor(
        parent: GuiLayoutData,
        textSize: Int,
        text: String,
        maxLength: Int,
        hiddenText: Boolean = false
    ) : this(parent, 4, textSize, text, maxLength, hiddenText)
}
