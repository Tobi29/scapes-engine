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

import java.util.*

class GraphicsObjectTracker<in O : GraphicsObject> {
    private val objects = ArrayList<O>()
    private var disposeOffset = 0

    fun disposeUnused(gl: GL) {
        val time = System.currentTimeMillis()
        var i = disposeOffset
        while (i < objects.size) {
            val `object` = objects[i]
            assert(`object`.isStored)
            if (!`object`.isUsed(time)) {
                `object`.dispose(gl)
                `object`.reset()
            }
            i += 16
        }
        disposeOffset++
        disposeOffset = disposeOffset and 15
    }

    fun disposeAll(gl: GL) {
        while (!objects.isEmpty()) {
            val `object` = objects[0]
            `object`.dispose(gl)
            `object`.reset()
        }
        objects.clear()
    }

    fun resetAll() {
        while (!objects.isEmpty()) {
            val `object` = objects[0]
            `object`.reset()
        }
        objects.clear()
    }

    fun count(): Int {
        return objects.size
    }

    fun attach(`object`: O): () -> Unit {
        objects.add(`object`)
        return { objects.remove(`object`) }
    }
}
