/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.graphics

import java.nio.ByteBuffer

open class TextureAtlasEntry(var buffer: ByteBuffer?,
                             val resolution: Int,
                             protected val texture: () -> Texture) {
    var tileX = 0
    var tileY = 0
    var x = 0.0
    var y = 0.0
    var size = 0.0

    fun x(): Double {
        return x + size * 0.005
    }

    fun realX(): Double {
        return x
    }

    fun y(): Double {
        return y + size * 0.005
    }

    fun realY(): Double {
        return y
    }

    fun size(): Double {
        return size * 0.99
    }

    fun realSize(): Double {
        return size
    }

    fun resolution(): Int {
        return resolution
    }

    fun texture(): Texture {
        return texture.invoke()
    }
}
