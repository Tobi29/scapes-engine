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
package org.tobi29.scapes.engine

import org.tobi29.scapes.engine.gui.GuiStyle
import org.tobi29.scapes.engine.utils.AtomicBoolean
import org.tobi29.scapes.engine.utils.EventDispatcher
import org.tobi29.scapes.engine.utils.Version

abstract class Game(val engine: ScapesEngine) {
    private var disposed = AtomicBoolean(false)

    abstract val events: EventDispatcher

    abstract val name: String

    abstract val id: String

    abstract val version: Version

    abstract val defaultGuiStyle: GuiStyle

    abstract fun initEarly()

    abstract fun init()

    abstract fun start()

    abstract fun halt()

    abstract fun step(delta: Double)

    open fun dispose() {
        events.disable()
        disposed.set(true)
    }
}
