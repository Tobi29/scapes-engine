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

package org.tobi29.scapes.engine.gui

import org.tobi29.scapes.engine.utils.math.max

class GuiComponentScrollPane(parent: GuiLayoutData,
                             scrollStep: Int) : GuiComponentVisibleSlabHeavy(
        parent) {
    val viewport: GuiComponentScrollPaneViewport

    init {
        viewport = addHori(0.0, 0.0, -1.0, -1.0) {
            GuiComponentScrollPaneViewport(it, scrollStep)
        }
        val slider = addHori(0.0, 0.0, 10.0, -1.0) {
            GuiComponentSliderVert(it, 0.0)
        }
        slider.on(GuiEvent.CHANGE) { event ->
            viewport.scrollY = slider.value() * max(0.0,
                    viewport.max.y - event.size.y)
        }
        slider.on(GuiEvent.SCROLL) { event ->
            viewport.scrollY = slider.value() * max(0.0,
                    viewport.max.y - event.size.y)
        }
        viewport.sliderY = slider
    }
}
