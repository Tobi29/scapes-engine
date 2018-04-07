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

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.tobi29.coroutines.Timer
import org.tobi29.coroutines.loopUntilCancel
import org.tobi29.profiler.*
import org.tobi29.scapes.engine.gui.*

class GuiWidgetProfiler(
    parent: GuiLayoutData
) : GuiComponentWidget(parent, "Profiler") {
    private val scrollPane: GuiComponentScrollPaneViewport
    private val profilerNotEnabled: GuiComponentText
    private var elements: List<Element> = emptyList()
    private var node: Node? = profiler?.root
    private var updateJob: Job? = null

    init {
        val slab = addVert(2.0, 2.0, -1.0, 20.0) { GuiComponentGroupSlab(it) }
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
        profilerNotEnabled = scrollPane.addVert(0.0, 0.0, -1.0, 24.0) {
            GuiComponentText(it, "Profiler not enabled")
        }

        toggle.on(GuiEvent.CLICK_LEFT) {
            if (profilerEnabled) {
                toggle.text = "Enable"
                profilerDisable()
            } else {
                toggle.text = "Disable"
                profilerEnable()
            }
            node = profiler?.root
            nodes()
        }
        refresh.on(GuiEvent.CLICK_LEFT) {
            node = profiler?.root
            nodes()
        }
        reset.on(GuiEvent.CLICK_LEFT) {
            profilerReset()
            node = profiler?.root
            nodes()
        }
        nodes()
    }

    fun nodes() {
        synchronized(this) {
            val node = node
            for (element in this.elements) {
                element.remove()
            }
            val elements = ArrayList<Element>()
            if (node == null) {
                profilerNotEnabled.visible = true
                return@synchronized
            }
            profilerNotEnabled.visible = false
            elements.add(scrollPane.addVert(0.0, 0.0, -1.0, 20.0) {
                Element(it, node, node.parent)
            })
            for (child in node.children.values) {
                elements.add(
                    scrollPane.addVert(10.0, 0.0, 0.0, 0.0, -1.0, 20.0) {
                        Element(it, child, child)
                    })
            }
            this.elements = elements
        }
    }

    fun nodes(node: Node) {
        synchronized(this) {
            this.node = node
            nodes()
        }
    }

    override fun updateVisible() {
        synchronized(this) {
            updateJob?.cancel()
            if (!isVisible) return@synchronized
            updateJob = launch(taskExecutor) {
                Timer().apply { init() }.loopUntilCancel(Timer.toDiff(4.0)) {
                    for (component in elements) {
                        component.update()
                    }
                }
            }
        }
    }

    private inner class Element(
        parent: GuiLayoutData,
        private val node: Node,
        go: Node?
    ) : GuiComponentGroupSlab(parent) {
        private val key: GuiComponentTextButton = addHori(
            2.0, 2.0, -1.0,
            -1.0
        ) {
            GuiComponentTextButton(it, 12, node.name)
        }
        private val value: GuiComponentText = addHori(4.0, 4.0, -1.0, -1.0) {
            GuiComponentText(it, "")
        }

        init {
            if (go != null) key.on(GuiEvent.CLICK_LEFT) { nodes(go) }
        }

        fun update() {
            value.text = node.time.toString()
        }
    }
}
