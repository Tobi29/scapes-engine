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

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Runnable
import org.tobi29.io.FileSystemContainer
import org.tobi29.io.tag.MutableTagMap
import org.tobi29.logging.KLogging
import org.tobi29.scapes.engine.graphics.GraphicsSystem
import org.tobi29.scapes.engine.gui.*
import org.tobi29.scapes.engine.gui.debug.GuiWidgetDebugValues
import org.tobi29.scapes.engine.gui.debug.GuiWidgetPerformance
import org.tobi29.scapes.engine.gui.debug.GuiWidgetProfiler
import org.tobi29.scapes.engine.resource.ResourceLoader
import org.tobi29.scapes.engine.sound.SoundSystem
import org.tobi29.utils.*
import kotlin.coroutines.experimental.CoroutineContext

expect class ScapesEngine(
    container: Container,
    defaultGuiStyle: (ScapesEngine) -> GuiStyle,
    taskExecutor: CoroutineContext,
    configMap: MutableTagMap
) : CoroutineDispatcher, ComponentHolder<Any> {
    override val componentStorage: ComponentStorage<Any>
    val taskExecutor: CoroutineContext
    val files: FileSystemContainer
    val events: EventDispatcher
    val resources: ResourceLoader
    val container: Container
    val graphics: GraphicsSystem
    val sounds: SoundSystem
    val guiStyle: GuiStyle
    val guiStack: GuiStack
    var guiController: GuiController
    val notifications: GuiNotifications
    val tooltip: GuiTooltip
    val debugValues: GuiWidgetDebugValues
    val profiler: GuiWidgetProfiler
    val performance: GuiWidgetPerformance
    val state: GameState?

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    )

    fun switchState(state: GameState)
    fun start()
    suspend fun halt()
    suspend fun dispose()
    fun debugMap(): Map<String, String>

    companion object : KLogging {
        val CONFIG_MAP_COMPONENT: ComponentTypeRegistered<ScapesEngine, MutableTagMap, Any>
    }
}

interface ComponentLifecycle : ComponentRegisteredHolder<ScapesEngine> {
    fun start() {}
    fun halt() {}
}

interface ComponentStep {
    fun step(delta: Double) {}
}
