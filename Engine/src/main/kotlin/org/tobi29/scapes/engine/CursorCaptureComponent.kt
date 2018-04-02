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

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.tobi29.coroutines.Timer
import org.tobi29.coroutines.loopUntilCancel
import org.tobi29.stdex.Volatile
import org.tobi29.utils.ComponentRegisteredHolder
import org.tobi29.utils.ComponentTypeRegistered

class CursorCaptureComponent : ComponentRegisteredHolder<ScapesEngine>,
    ComponentLifecycle, ComponentStep {
    @Volatile
    private var engine: ScapesEngine? = null
    private var job: Job? = null
    private var cursorCaptured = false

    override fun init(holder: ScapesEngine) {
        engine = holder
        start()
    }

    override fun start() {
        synchronized(this) {
            engine?.let { engine ->
                job = launch(engine) {
                    Timer().apply { init() }
                        .loopUntilCancel(Timer.toDiff(10.0)) {
                            val cursorCapture = engine.isMouseGrabbed
                            if (cursorCapture != cursorCaptured) {
                                cursorCaptured = cursorCapture
                                engine.container.cursorCapture(cursorCapture)
                            }
                        }
                }
            }
        }
    }

    override fun halt() {
        synchronized(this) {
            job?.cancel()
        }
    }

    override fun dispose(holder: ScapesEngine) {
        halt()
        engine = null
    }

    companion object {
        val COMPONENT =
            ComponentTypeRegistered<ScapesEngine, CursorCaptureComponent, Any>()
    }
}

private inline val ScapesEngine.isMouseGrabbed
    get() = state?.isMouseGrabbed == true || guiController.captureCursor()
