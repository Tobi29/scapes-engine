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

package org.tobi29.scapes.engine.gui.debug

import org.tobi29.coroutines.JobHandle
import org.tobi29.coroutines.Timer
import org.tobi29.coroutines.launchOrStop
import org.tobi29.coroutines.loopUntilCancel
import org.tobi29.scapes.engine.gui.*
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.atomic.AtomicReference
import org.tobi29.stdex.concurrent.withLock

open class GuiWidgetDebugValues(
    parent: GuiLayoutData
) : GuiComponentWidget(parent, "Debug Values") {
    private val elements = ConcurrentHashMap<String, Element>()
    private val scrollPane: GuiComponentScrollPaneViewport = addVert(
        10.0, 10.0,
        -1.0, -1.0
    ) { GuiComponentScrollPane(it, 20) }.viewport
    private var updateJob = JobHandle(engine)

    operator fun get(key: String): Element {
        return lock.withLock {
            var element: Element? = elements[key]
            if (element == null) {
                element = scrollPane.addVert(0.0, 0.0, -1.0, 20.0) {
                    Element(it, key)
                }
                elements.put(key, element)
            }
            element
        }
    }

    fun clear() {
        lock.withLock {
            val iterator = elements.entries.iterator()
            while (iterator.hasNext()) {
                scrollPane.remove(iterator.next().value)
                iterator.remove()
            }
        }
    }

    fun elements(): Set<Map.Entry<String, Element>> {
        return elements.entries
    }

    override fun updateVisible() {
        updateJob.launchOrStop(isVisible, renderExecutor) {
            Timer().apply { init() }.loopUntilCancel(Timer.toDiff(4.0)) {
                for (component in elements.values) {
                    component.update()
                }
            }
        }
    }

    class Element(
        parent: GuiLayoutData,
        key: String
    ) : GuiComponentGroupSlab(parent) {
        private val value: GuiComponentText
        private val text = AtomicReference<String?>(null)

        init {
            addHori(2.0, 2.0, -1.0, -1.0) {
                GuiComponentTextButton(it, 12, key)
            }
            value = addHori(4.0, 4.0, -1.0, -1.0) {
                GuiComponentText(it, "")
            }
        }

        fun setValue(value: String) {
            text.set(value)
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

        fun update() {
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
            return value.text
        }
    }
}
