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

import org.tobi29.scapes.engine.sound.VolumeChannelEnvironment
import org.tobi29.utils.ComponentType
import org.tobi29.io.tag.*
import org.tobi29.stdex.access

class ScapesEngineConfig(val configMap: MutableTagMap) {
    companion object {
        val COMPONENT = ComponentType.of<ScapesEngine, ScapesEngineConfig, Any> {
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

fun ScapesEngineConfig.volume(channel: String) =
        VolumeChannelEnvironment.run {
            configMap.mapMut("Volumes").asSequence()
                    .filter { channel in it.key }
                    .mapNotNull { it.value.toDouble() }
                    .fold(1.0) { a, b -> a * b }
        }


fun ScapesEngineConfig.setVolume(channel: String,
                                 value: Double) {
    configMap.mapMut("Volumes")[channel] = value.toTag()
}
