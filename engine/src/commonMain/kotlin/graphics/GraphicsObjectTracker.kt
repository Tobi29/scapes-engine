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

package org.tobi29.scapes.engine.graphics

import org.tobi29.stdex.assert

class GraphicsObjectTracker<in O : GraphicsObject> {
    private val objects = ArrayList<O>()
    private var disposeOffset = 0

    fun disposeUnused(gl: GL) {
        val time = gl.timestamp
        var i = disposeOffset
        while (i < objects.size) {
            val go = objects[i]
            assert { go.isStored }
            if (!go.isUsed(time)) {
                go.dispose(gl)
            }
            i += 16
        }
        disposeOffset++
        disposeOffset = disposeOffset and 15
    }

    fun disposeAll(gl: GL) {
        while (!objects.isEmpty()) {
            objects[0].dispose(gl)
        }
        objects.clear()
    }

    fun resetAll() {
        while (!objects.isEmpty()) {
            objects[0].dispose(null)
        }
        objects.clear()
    }

    fun count(): Int {
        return objects.size
    }

    fun attach(go: O): () -> Unit {
        objects.add(go)
        return { objects.remove(go) }
    }
}
