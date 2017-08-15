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

import org.tobi29.scapes.engine.utils.ComponentType
import org.tobi29.scapes.engine.utils.access
import org.tobi29.scapes.engine.utils.tag.*

class ScapesEngineConfig(val configMap: MutableTagMap) {
    companion object {
        val COMPONENT = ComponentType.of<Any, ScapesEngineConfig> {
            ScapesEngineConfig(it[ScapesEngine.CONFIG_MAP_COMPONENT])
        }
    }
}

val ScapesEngineConfig.engineMap: MutableTagMap by access {
    configMap.tagMap("Engine")
}

val ScapesEngineConfig.volumesMap: MutableTagMap by access {
    configMap.tagMap("Volumes")
}

var ScapesEngineConfig.fps: Double by access {
    engineMap.tagDouble("Framerate", 60.0)
}

var ScapesEngineConfig.vSync: Boolean by access {
    engineMap.tagBoolean("VSync", true)
}

var ScapesEngineConfig.fullscreen: Boolean by access {
    engineMap.tagBoolean("Fullscreen", false)
}

fun ScapesEngineConfig.volume(channel: String) = configMap.mapMut(
        "Volumes").asSequence().filter {
    channel.startsWith(it.key)
}.maxBy { it.key.length }?.value?.toDouble() ?: 1.0


fun ScapesEngineConfig.setVolume(channel: String,
              value: Double) {
    configMap.mapMut("Volumes")[channel] = value.toTag()
}
