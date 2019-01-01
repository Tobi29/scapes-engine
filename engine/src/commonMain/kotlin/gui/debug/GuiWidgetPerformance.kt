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

package org.tobi29.scapes.engine.gui.debug

import org.tobi29.scapes.engine.gui.GuiComponentGraph
import org.tobi29.scapes.engine.gui.GuiComponentWidget
import org.tobi29.scapes.engine.gui.GuiLayoutData

class GuiWidgetPerformance(
    parent: GuiLayoutData
) : GuiComponentWidget(parent, "Performance Graph") {
    private val graph: GuiComponentGraph = addVert(0.0, 0.0, -1.0, -1.0) {
        GuiComponentGraph(
            it, 2, floatArrayOf(1.0f, 0.0f),
            floatArrayOf(0.0f, 0.0f), floatArrayOf(0.0f, 1.0f),
            floatArrayOf(1.0f, 1.0f)
        )
    }

    fun renderTimestamp(delta: Double) {
        graph.addStamp(delta, 0)
    }

    fun updateTimestamp(delta: Double) {
        graph.addStamp(delta, 1)
    }
}
