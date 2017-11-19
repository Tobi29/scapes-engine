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

package org.tobi29.scapes.engine.input

inline fun keyPressesForScroll(
        delta: ScrollDelta,
        addPressEvent: (ControllerKey, ControllerButtons.Action) -> Unit
) {
    val x: Double
    val y: Double
    when (delta) {
        is ScrollDelta.Pixel -> {
            x = delta.delta.x
            y = delta.delta.y
        }
        is ScrollDelta.Line -> {
            x = delta.delta.x
            y = delta.delta.y
        }
        is ScrollDelta.Page -> {
            x = delta.delta.x
            y = delta.delta.y
        }
    }
    keyPressesForScroll(x, y, addPressEvent)
}

inline fun keyPressesForScroll(
        x: Double,
        y: Double,
        addPressEvent: (ControllerKey, ControllerButtons.Action) -> Unit
) {
    if (x < 0.0) {
        addPressEvent(ControllerKey.SCROLL_LEFT,
                ControllerButtons.Action.PRESS)
        addPressEvent(ControllerKey.SCROLL_LEFT,
                ControllerButtons.Action.RELEASE)
    } else if (x > 0.0) {
        addPressEvent(ControllerKey.SCROLL_RIGHT,
                ControllerButtons.Action.PRESS)
        addPressEvent(ControllerKey.SCROLL_RIGHT,
                ControllerButtons.Action.RELEASE)
    }
    if (y < 0.0) {
        addPressEvent(ControllerKey.SCROLL_DOWN,
                ControllerButtons.Action.PRESS)
        addPressEvent(ControllerKey.SCROLL_DOWN,
                ControllerButtons.Action.RELEASE)
    } else if (y > 0.0) {
        addPressEvent(ControllerKey.SCROLL_UP,
                ControllerButtons.Action.PRESS)
        addPressEvent(ControllerKey.SCROLL_UP,
                ControllerButtons.Action.RELEASE)
    }
}
