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

package org.tobi29.scapes.engine.backends.glfw

import org.tobi29.arrays.Floats
import java.nio.FloatBuffer

@PublishedApi
internal fun FloatBuffer.asFloats() = object : Floats {
    override val size: Int get() = remaining()

    override fun get(index: Int): Float = this@asFloats.get(position() + index)

    override fun set(index: Int, value: Float) {
        this@asFloats.put(position() + index, value)
    }
}
