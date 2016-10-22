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

package org.tobi29.scapes.engine.gui

import java.util.*

class GuiComponentTextField(parent: GuiLayoutData, textX: Int, textSize: Int,
                            text: String, maxLength: Int, hiddenText: Boolean, private val major: Boolean) : GuiComponentButtonHeavy(
        parent) {
    private val text: GuiComponentEditableText

    constructor(parent: GuiLayoutData, textSize: Int,
                text: String, hiddenText: Boolean = false) : this(
            parent, textSize, text, Int.MAX_VALUE, hiddenText) {
    }

    constructor(parent: GuiLayoutData, textSize: Int,
                text: String, maxLength: Int, hiddenText: Boolean = false, major: Boolean = false) : this(
            parent, 4, textSize, text, maxLength, hiddenText, major) {
    }

    init {
        this.text = addSubHori(textX.toDouble(), 0.0, -1.0, textSize.toDouble()
        ) { GuiComponentEditableText(it, text, maxLength) }
        if (hiddenText) {
            this.text.textFilter = { str ->
                val array = CharArray(str.length)
                Arrays.fill(array, '*')
                String(array)
            }
        } else {
            this.text.textFilter = { str -> str }
        }
    }

    fun text(): String {
        return text.text()
    }

    fun setText(text: String) {
        this.text.setText(text)
    }

    override fun updateComponent(delta: Double) {
        val current = gui.lastClicked
        if (current === this || current === text || major) {
            text.setActive(true)
        } else {
            text.setActive(false)
        }
    }
}
