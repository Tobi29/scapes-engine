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

package org.tobi29.scapes.engine.graphics

fun GraphicsObjectSupplier.createV(vertex: FloatArray,
                                   renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    return createModelFast(attributes, vertex.size / 3, renderType)
}

fun GraphicsObjectSupplier.createVI(vertex: FloatArray,
                                    index: IntArray,
                                    renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    return createModelStatic(attributes, vertex.size / 3, index, renderType)
}

fun GraphicsObjectSupplier.createVN(vertex: FloatArray,
                                    normal: FloatArray,
                                    renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
            VertexType.BYTE))
    return createModelFast(attributes, vertex.size / 3, renderType)
}

fun GraphicsObjectSupplier.createVNI(vertex: FloatArray,
                                     normal: FloatArray,
                                     index: IntArray,
                                     renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
            VertexType.BYTE))
    return createModelStatic(attributes, vertex.size / 3, index, renderType)
}

fun GraphicsObjectSupplier.createVC(vertex: FloatArray,
                                    color: FloatArray,
                                    renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
            VertexType.UNSIGNED_BYTE))
    return createModelFast(attributes, vertex.size / 3, renderType)
}

fun GraphicsObjectSupplier.createVCI(vertex: FloatArray,
                                     color: FloatArray,
                                     index: IntArray,
                                     renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
            VertexType.UNSIGNED_BYTE))
    return createModelStatic(attributes, vertex.size / 3, index, renderType)
}

fun GraphicsObjectSupplier.createVT(vertex: FloatArray,
                                    texture: FloatArray,
                                    renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    return createModelFast(attributes, vertex.size / 3, renderType)
}

fun GraphicsObjectSupplier.createVTI(vertex: FloatArray,
                                     texture: FloatArray,
                                     index: IntArray,
                                     renderType: RenderType): Model {
    val attributes = ArrayList<ModelAttribute>()
    attributes.add(ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
            0, VertexType.HALF_FLOAT))
    attributes.add(ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
            0, VertexType.FLOAT))
    return createModelStatic(attributes, vertex.size / 3, index, renderType)
}

fun GraphicsObjectSupplier.createVTN(vertex: FloatArray,
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
    return createModelFast(attributes, vertex.size / 3, renderType)
}

fun GraphicsObjectSupplier.createVTNI(vertex: FloatArray,
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
    return createModelStatic(attributes, vertex.size / 3, index, renderType)
}

fun GraphicsObjectSupplier.createVCT(vertex: FloatArray,
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
    return createModelFast(attributes, vertex.size / 3, renderType)
}

fun GraphicsObjectSupplier.createVCTI(vertex: FloatArray,
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
    return createModelStatic(attributes, vertex.size / 3, index, renderType)
}

fun GraphicsObjectSupplier.createVCTN(vertex: FloatArray,
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
    return createModelFast(attributes, vertex.size / 3, renderType)
}

fun GraphicsObjectSupplier.createVCTNI(vertex: FloatArray,
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
    return createModelStatic(attributes, vertex.size / 3, index, renderType)
}
