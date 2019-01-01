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

package org.tobi29.scapes.engine

import kotlinx.coroutines.launch
import org.tobi29.scapes.engine.graphics.busyPipeline
import org.tobi29.stdex.atomic.AtomicReference

class GameStateStartup(
    engine: ScapesEngine,
    private val switch: () -> GameState
) : GameState(
    engine
) {
    val readySwitch = AtomicReference<GameState?>(null)

    override fun init() {
        switchPipeline { gl ->
            val busy = busyPipeline(gl)
            ;{
            val busyRender = busy()
            ;{ _ ->
            gl.clear(0.0f, 0.0f, 0.0f, 1.0f)
            busyRender()
        }
        }
        }
        launch(engine.taskExecutor) {
            readySwitch.set(switch())
        }
    }

    override val isMouseGrabbed = false

    override fun step(delta: Double) {
        readySwitch.get()?.let { switch ->
            if (engine.resources.isDone()) {
                engine.switchState(switch)
            }
        }
    }
}
