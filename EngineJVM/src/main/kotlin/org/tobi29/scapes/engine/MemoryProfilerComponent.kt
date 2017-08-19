package org.tobi29.scapes.engine

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.utils.ComponentRegisteredHolder
import org.tobi29.scapes.engine.utils.ComponentTypeRegistered
import org.tobi29.scapes.engine.utils.task.Timer
import org.tobi29.scapes.engine.utils.task.loop
import java.util.concurrent.TimeUnit

class MemoryProfilerComponent(
        private val debugValues: GuiWidgetDebugValues
) : ComponentRegisteredHolder<ScapesEngine>, ComponentStep {
    private val runtime = Runtime.getRuntime()
    private var job: Job? = null

    override fun init(holder: ScapesEngine) {
        val usedMemoryDebug = debugValues["Runtime-Memory-Used"]
        val heapMemoryDebug = debugValues["Runtime-Memory-Heap"]
        val maxMemoryDebug = debugValues["Runtime-Memory-Max"]
        job = launch(holder) {
            Timer().loop(Timer.toDiff(4.0),
                    { delay(it, TimeUnit.NANOSECONDS) }) {
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
