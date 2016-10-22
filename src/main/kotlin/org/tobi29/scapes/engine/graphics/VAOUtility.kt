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

import org.tobi29.scapes.engine.ScapesEngine
import java.util.*

fun createV(engine: ScapesEngine,
            vertex: FloatArray,
            renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    return engine.graphics.createModelFast(attributes, vertex.size / 3,
            renderType)
}

fun createVI(engine: ScapesEngine,
             vertex: FloatArray,
             index: IntArray,
             renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    return engine.graphics.createModelStatic(attributes, vertex.size / 3,
            index,
            renderType)
}

fun createVN(engine: ScapesEngine,
             vertex: FloatArray,
             normal: FloatArray,
             renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
            VertexType.BYTE))
    return engine.graphics.createModelFast(attributes, vertex.size / 3,
            renderType)
}

fun createVNI(engine: ScapesEngine,
              vertex: FloatArray,
              normal: FloatArray,
              index: IntArray,
              renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
            VertexType.BYTE))
    return engine.graphics.createModelStatic(attributes, vertex.size / 3,
            index,
            renderType)
}

fun createVC(engine: ScapesEngine,
             vertex: FloatArray,
             color: FloatArray,
             renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
            VertexType.UNSIGNED_BYTE))
    return engine.graphics.createModelFast(attributes, vertex.size / 3,
            renderType)
}

fun createVCI(engine: ScapesEngine,
              vertex: FloatArray,
              color: FloatArray,
              index: IntArray,
              renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
            VertexType.UNSIGNED_BYTE))
    return engine.graphics.createModelStatic(attributes, vertex.size / 3,
            index,
            renderType)
}

fun createVT(engine: ScapesEngine,
             vertex: FloatArray,
             texture: FloatArray,
             renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    return engine.graphics.createModelFast(attributes, vertex.size / 3,
            renderType)
}

fun createVTI(engine: ScapesEngine,
              vertex: FloatArray,
              texture: FloatArray,
              index: IntArray,
              renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    return engine.graphics.createModelStatic(attributes, vertex.size / 3,
            index,
            renderType)
}

fun createVTN(engine: ScapesEngine,
              vertex: FloatArray,
              texture: FloatArray,
              normal: FloatArray,
              renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    attributes.add(ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
            VertexType.BYTE))
    return engine.graphics.createModelFast(attributes, vertex.size / 3,
            renderType)
}

fun createVTNI(engine: ScapesEngine,
               vertex: FloatArray,
               texture: FloatArray,
               normal: FloatArray,
               index: IntArray,
               renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    attributes.add(ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
            VertexType.BYTE))
    return engine.graphics.createModelStatic(attributes, vertex.size / 3,
            index,
            renderType)
}

fun createVCT(engine: ScapesEngine,
              vertex: FloatArray,
              color: FloatArray,
              texture: FloatArray,
              renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
            VertexType.UNSIGNED_BYTE))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    return engine.graphics.createModelFast(attributes, vertex.size / 3,
            renderType)
}

fun createVCTI(engine: ScapesEngine,
               vertex: FloatArray,
               color: FloatArray,
               texture: FloatArray,
               index: IntArray,
               renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
            VertexType.UNSIGNED_BYTE))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    return engine.graphics.createModelStatic(attributes, vertex.size / 3,
            index,
            renderType)
}

fun createVCTN(engine: ScapesEngine,
               vertex: FloatArray,
               color: FloatArray,
               texture: FloatArray,
               normal: FloatArray,
               renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
            VertexType.UNSIGNED_BYTE))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    attributes.add(ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
            VertexType.BYTE))
    return engine.graphics.createModelFast(attributes, vertex.size / 3,
            renderType)
}

fun createVCTNI(engine: ScapesEngine,
                vertex: FloatArray,
                color: FloatArray,
                texture: FloatArray,
                normal: FloatArray,
                index: IntArray,
                renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
            VertexType.UNSIGNED_BYTE))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    attributes.add(ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
            VertexType.BYTE))
    return engine.graphics.createModelStatic(attributes, vertex.size / 3,
            index,
            renderType)
}
