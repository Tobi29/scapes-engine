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

package org.tobi29.scapes.engine.gui.debug

import org.tobi29.scapes.engine.gui.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

open class GuiWidgetDebugValues(parent: GuiLayoutData) : GuiComponentWidget(
        parent, "Debug Values") {
    private val elements = ConcurrentHashMap<String, Element>()
    private val scrollPane: GuiComponentScrollPaneViewport

    init {
        scrollPane = addVert(10.0, 10.0, -1.0,
                -1.0) {  GuiComponentScrollPane(it, 20) }.viewport()
    }

    @Synchronized operator fun get(key: String): Element {
        var element: Element? = elements[key]
        if (element == null) {
            element = scrollPane.addVert(0.0, 0.0, -1.0, 20.0) {
                Element(it, key)
            }
            elements.put(key, element)
        }
        return element
    }

    @Synchronized fun clear() {
        val iterator = elements.entries.iterator()
        while (iterator.hasNext()) {
            scrollPane.remove(iterator.next().value)
            iterator.remove()
        }
    }

    fun elements(): Set<Map.Entry<String, Element>> {
        return elements.entries
    }

    class Element(parent: GuiLayoutData, key: String) : GuiComponentGroupSlabHeavy(
            parent) {
        private val value: GuiComponentText
        private val text = AtomicReference<String>()

        init {
            addHori(2.0, 2.0, -1.0, -1.0) {
                GuiComponentTextButton(it, 12, key)
            }
            value = addHori(4.0, 4.0, -1.0, -1.0) {
                GuiComponentText(it, "")
            }
        }

        fun setValue(value: String) {
            text.lazySet(value)
        }

        fun setValue(value: Boolean) {
            setValue(value.toString())
        }

        fun setValue(value: Byte) {
            setValue(value.toString())
        }

        fun setValue(value: Short) {
            setValue(value.toString())
        }

        fun setValue(value: Int) {
            setValue(value.toString())
        }

        fun setValue(value: Long) {
            setValue(value.toString())
        }

        fun setValue(value: Float) {
            setValue(value.toString())
        }

        fun setValue(value: Double) {
            setValue(value.toString())
        }

        fun setValue(value: Any) {
            setValue(value.toString())
        }

        public override fun updateComponent(delta: Double) {
            val newText = text.getAndSet(null)
            if (newText != null) {
                value.text = newText
            }
        }

        override fun toString(): String {
            val text = this.text.get()
            if (text != null) {
                return text
            }
            return value.text()
        }
    }
}
