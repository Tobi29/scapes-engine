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
package org.tobi29.scapes.engine.gui.debug

import org.tobi29.scapes.engine.gui.*
import org.tobi29.scapes.engine.utils.profiler.*

class GuiWidgetProfiler(parent: GuiLayoutData) : GuiComponentWidget(parent,
        "Profiler") {
    private val scrollPane: GuiComponentScrollPaneViewport
    private var node: Node? = PROFILER.get()?.root

    init {
        val slab = addVert(2.0, 2.0, -1.0, 20.0, ::GuiComponentGroupSlab)
        val toggle = slab.addHori(2.0, 2.0, -1.0, -1.0) {
            GuiComponentTextButton(it, 12, "Enable")
        }
        val refresh = slab.addHori(2.0, 2.0, -1.0, -1.0) {
            GuiComponentTextButton(it, 12, "Refresh")
        }
        val reset = slab.addHori(2.0, 2.0, -1.0, -1.0) {
            GuiComponentTextButton(it, 12, "Reset")
        }
        scrollPane = addVert(10.0, 10.0, -1.0, -1.0) {
            GuiComponentScrollPane(it, 20)
        }.viewport

        toggle.on(GuiEvent.CLICK_LEFT) {
            if (PROFILER_ENABLED) {
                toggle.setText("Enable")
                profilerDisable()
            } else {
                toggle.setText("Disable")
                profilerEnable()
            }
            node = PROFILER.get()?.root
            nodes()
        }
        refresh.on(GuiEvent.CLICK_LEFT) {
            node = PROFILER.get()?.root
            nodes()
        }
        reset.on(GuiEvent.CLICK_LEFT) {
            profilerReset()
            node = PROFILER.get()?.root
            nodes()
        }
        nodes()
    }

    fun nodes() {
        synchronized(this) {
            val node = node
            scrollPane.removeAll()
            if (node == null) {
                scrollPane.addVert(0.0, 0.0, -1.0, 24.0) {
                    GuiComponentText(it, "Profiler not enabled")
                }
                return@synchronized
            }
            scrollPane.addVert(0.0, 0.0, -1.0, 20.0) {
                Element(it, node, node.parent)
            }
            for (child in node.children.values) {
                scrollPane.addVert(10.0, 0.0, 0.0, 0.0, -1.0, 20.0) {
                    Element(it, child, child)
                }
            }
        }
    }

    fun nodes(node: Node) {
        synchronized(this) {
            this.node = node
            nodes()
        }
    }

    private inner class Element(parent: GuiLayoutData,
                                private val node: Node,
                                go: Node?) : GuiComponentGroupSlabHeavy(
            parent) {
        private val key: GuiComponentTextButton
        private val value: GuiComponentText

        init {
            key = addHori(2.0, 2.0, -1.0, -1.0) {
                GuiComponentTextButton(it, 12, node.name.invoke())
            }
            value = addHori(4.0, 4.0, -1.0, -1.0) {
                GuiComponentText(it, "")
            }
            if (go != null) key.on(GuiEvent.CLICK_LEFT) { nodes(go) }
        }

        public override fun updateComponent(delta: Double) {
            value.text = node.time.toString()
        }
    }
}
