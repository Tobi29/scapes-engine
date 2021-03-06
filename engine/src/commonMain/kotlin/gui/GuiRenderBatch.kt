/*
 * Copyright 2012-2019 Tobi29
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
package org.tobi29.scapes.engine.gui

import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector3d
import org.tobi29.scapes.engine.graphics.Mesh
import org.tobi29.scapes.engine.graphics.Model
import org.tobi29.scapes.engine.graphics.Texture
import org.tobi29.stdex.computeAbsent

open class GuiRenderBatch(var pixelSize: Vector2d) {
    private val meshes = HashMap<Int, MutableMap<Texture, Mesh>>()
    private var currentMesh: Mesh? = null
    protected var offset = 0
    protected var count = 0

    open fun vector(x: Double,
                    y: Double): Vector3d {
        return Vector3d(x, y, 0.0)
    }

    fun texture(texture: Texture,
                priority: Int) {
        val layer = priority + offset
        val map = meshes.computeAbsent(layer) { HashMap() }
        currentMesh = map[texture]
        if (currentMesh == null) {
            val newMesh = Mesh(true)
            map.put(texture, newMesh)
            currentMesh = newMesh
            count++
        }
    }

    fun offset(offset: Int) {
        this.offset += offset
    }

    fun mesh(): Mesh {
        return currentMesh ?: throw IllegalStateException("No texture set")
    }

    fun finish(): List<Pair<Model, Texture>> {
        val meshes = ArrayList<Pair<Model, Texture>>(count)
        this.meshes.asSequence().sortedBy { it.key }.forEach { (_, map) ->
            map.entries.forEach { entry ->
                val texture = entry.key
                meshes.add(entry.value.finish(texture.gos) to texture)
            }
        }
        this.meshes.clear()
        count = 0
        return meshes
    }
}
