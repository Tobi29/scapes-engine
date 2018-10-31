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
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.utils.ComponentRegisteredHolder
import org.tobi29.utils.ComponentTypeRegistered

class MemoryProfilerComponent(
    private val debugValues: GuiWidgetDebugValues
) : ComponentRegisteredHolder<ScapesEngine>,
    ComponentStep {
    private val runtime = Runtime.getRuntime()
    private var job: Job? = null

    @Synchronized
    override fun init(holder: ScapesEngine) {
        val usedMemoryDebug = debugValues["Runtime-Memory-Used"]
        val heapMemoryDebug = debugValues["Runtime-Memory-Heap"]
        val maxMemoryDebug = debugValues["Runtime-Memory-Max"]
        job = holder.launch(holder.taskExecutor) {
            Timer().loopUntilCancel(Timer.toDiff(4.0)) {
                usedMemoryDebug.setValue(
                    (runtime.totalMemory() - runtime.freeMemory()) / 1048576
                )
                heapMemoryDebug.setValue(
                    runtime.totalMemory() / 1048576
                )
                maxMemoryDebug.setValue(
                    runtime.maxMemory() / 1048576
                )
            }
        }
    }

    @Synchronized
    override fun dispose(holder: ScapesEngine) {
        job?.cancel()
    }

    companion object {
        val COMPONENT = ComponentTypeRegistered<ScapesEngine, MemoryProfilerComponent, Any>()
    }
}
