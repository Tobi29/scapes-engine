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

package org.tobi29.scapes.engine.gui

sealed class GuiEvent<T : GuiComponentEvent> {
    object CLICK_LEFT : GuiEvent<GuiComponentEvent>()
    object PRESS_LEFT : GuiEvent<GuiComponentEvent>()
    object DRAG_LEFT : GuiEvent<GuiComponentEventDrag>()
    object DROP_LEFT : GuiEvent<GuiComponentEvent>()
    object CLICK_RIGHT : GuiEvent<GuiComponentEvent>()
    object PRESS_RIGHT : GuiEvent<GuiComponentEvent>()
    object DRAG_RIGHT : GuiEvent<GuiComponentEventDrag>()
    object DROP_RIGHT : GuiEvent<GuiComponentEvent>()
    object SCROLL : GuiEvent<GuiComponentEventScroll>()
    object HOVER_ENTER : GuiEvent<GuiComponentEvent>()
    object HOVER : GuiEvent<GuiComponentEvent>()
    object HOVER_LEAVE : GuiEvent<GuiComponentEvent>()
    object CHANGE : GuiEvent<GuiComponentEvent>()
}
