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

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.utils.ComponentRegisteredHolder
import org.tobi29.scapes.engine.utils.ComponentTypeRegistered
import org.tobi29.scapes.engine.utils.task.Timer
import org.tobi29.scapes.engine.utils.task.loop
import org.tobi29.scapes.engine.utils.toIntClamped

class MemoryProfilerComponent(
        private val debugValues: GuiWidgetDebugValues
) : ComponentRegisteredHolder<ScapesEngine>,
        ComponentStep {
    private val runtime = Runtime.getRuntime()
    private var job: Job? = null

    override fun init(holder: ScapesEngine) {
        val usedMemoryDebug = debugValues["Runtime-Memory-Used"]
        val heapMemoryDebug = debugValues["Runtime-Memory-Heap"]
        val maxMemoryDebug = debugValues["Runtime-Memory-Max"]
        job = launch(holder) {
            Timer().apply { init() }.loop(Timer.toDiff(4.0),
                    { delay((it / 1000000L).toIntClamped()) }) {
                usedMemoryDebug.setValue(
                        (runtime.totalMemory() - runtime.freeMemory()) / 1048576)
                heapMemoryDebug.setValue(runtime.totalMemory() / 1048576)
                maxMemoryDebug.setValue(runtime.maxMemory() / 1048576)
                true
            }
        }
    }

    override fun dispose() {
        job?.let {
            it.cancel()
            job = null
        }
    }

    companion object {
        val COMPONENT = ComponentTypeRegistered<ScapesEngine, MemoryProfilerComponent, Any>()
    }
}
