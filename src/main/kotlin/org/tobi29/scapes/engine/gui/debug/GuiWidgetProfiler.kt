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
import org.tobi29.scapes.engine.utils.profiler.Node
import org.tobi29.scapes.engine.utils.profiler.Profiler

class GuiWidgetProfiler(parent: GuiLayoutData) : GuiComponentWidget(parent,
        "Profiler") {
    private val scrollPane: GuiComponentScrollPaneViewport
    private var node: Node? = null

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
        }.viewport()

        toggle.on(GuiEvent.CLICK_LEFT, { event ->
            if (Profiler.enabled()) {
                toggle.setText("Enable")
                Profiler.enabled = false
            } else {
                toggle.setText("Disable")
                Profiler.enabled = true
            }
            nodes()
        })
        refresh.on(GuiEvent.CLICK_LEFT, { event -> nodes() })
        reset.on(GuiEvent.CLICK_LEFT, { event ->
            Profiler.reset()
            threads()
        })
        nodes()
    }

    @Synchronized fun nodes() {
        val node = node
        if (node == null) {
            threads()
            return
        }
        scrollPane.removeAll()
        scrollPane.addVert(0.0, 0.0, -1.0, 20.0) {
            Element(it, node, node.parent)
        }
        for (child in node.children.values) {
            scrollPane.addVert(10.0, 0.0, 0.0, 0.0, -1.0, 20.0) {
                Element(it, child, child)
            }
        }
    }

    @Synchronized fun threads() {
        scrollPane.removeAll()
        node = null
        var threads: Array<Thread?>
        var count = Thread.activeCount()
        do {
            threads = arrayOfNulls(count)
            count = Thread.enumerate(threads)
        } while (threads.size < count)
        for (i in 0..count - 1) {
            threads[i]?.let { thread ->
                val node = Profiler.node(thread)
                if (node != null) {
                    scrollPane.addVert(10.0, 0.0, 0.0, 0.0, -1.0, 20.0) {
                        ElementThread(it, node, node)
                    }
                }
            }
        }
    }

    @Synchronized fun nodes(node: Node?) {
        this.node = node
        nodes()
    }

    private inner class ElementThread(parent: GuiLayoutData, private val node: Node, go: Node?) : GuiComponentGroupSlabHeavy(
            parent) {
        private val key: GuiComponentTextButton
        init {
            key = addHori(2.0, 2.0, -1.0, -1.0) {
                GuiComponentTextButton(it, 12, node.name.invoke())
            }
            key.on(GuiEvent.CLICK_LEFT, { event -> nodes(go) })
        }
    }

    private inner class Element(parent: GuiLayoutData, private val node: Node, go: Node?) : GuiComponentGroupSlabHeavy(
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
            key.on(GuiEvent.CLICK_LEFT, { event -> nodes(go) })
        }

        public override fun updateComponent(delta: Double) {
            value.text = node.time().toString()
        }
    }
}
