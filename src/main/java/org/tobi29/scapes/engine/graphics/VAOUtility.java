/*
 * Copyright 2012-2015 Tobi29
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
package org.tobi29.scapes.engine.graphics;

import org.tobi29.scapes.engine.ScapesEngine;

import java.util.ArrayList;
import java.util.List;

public class VAOUtility {
    public static Model createV(ScapesEngine engine, float[] vertex,
            RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        return engine.graphics()
                .createModelFast(attributes, vertex.length / 3, renderType);
    }

    public static Model createVI(ScapesEngine engine, float[] vertex,
            int[] index, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        return engine.graphics()
                .createModelStatic(attributes, vertex.length / 3, index,
                        renderType);
    }

    public static Model createVN(ScapesEngine engine, float[] vertex,
            float[] normal, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes
                .add(new ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
                        VertexType.BYTE));
        return engine.graphics()
                .createModelFast(attributes, vertex.length / 3, renderType);
    }

    public static Model createVNI(ScapesEngine engine, float[] vertex,
            float[] normal, int[] index, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes
                .add(new ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
                        VertexType.BYTE));
        return engine.graphics()
                .createModelStatic(attributes, vertex.length / 3, index,
                        renderType);
    }

    public static Model createVC(ScapesEngine engine, float[] vertex,
            float[] color, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes.add(new ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
                VertexType.UNSIGNED_BYTE));
        return engine.graphics()
                .createModelFast(attributes, vertex.length / 3, renderType);
    }

    public static Model createVCI(ScapesEngine engine, float[] vertex,
            float[] color, int[] index, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes.add(new ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
                VertexType.UNSIGNED_BYTE));
        return engine.graphics()
                .createModelStatic(attributes, vertex.length / 3, index,
                        renderType);
    }

    public static Model createVT(ScapesEngine engine, float[] vertex,
            float[] texture, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes
                .add(new ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
                        0, VertexType.FLOAT));
        return engine.graphics()
                .createModelFast(attributes, vertex.length / 3, renderType);
    }

    public static Model createVTI(ScapesEngine engine, float[] vertex,
            float[] texture, int[] index, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes
                .add(new ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
                        0, VertexType.FLOAT));
        return engine.graphics()
                .createModelStatic(attributes, vertex.length / 3, index,
                        renderType);
    }

    public static Model createVTN(ScapesEngine engine, float[] vertex,
            float[] texture, float[] normal, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes
                .add(new ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
                        0, VertexType.FLOAT));
        attributes
                .add(new ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
                        VertexType.BYTE));
        return engine.graphics()
                .createModelFast(attributes, vertex.length / 3, renderType);
    }

    public static Model createVTNI(ScapesEngine engine, float[] vertex,
            float[] texture, float[] normal, int[] index,
            RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes
                .add(new ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
                        0, VertexType.FLOAT));
        attributes
                .add(new ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
                        VertexType.BYTE));
        return engine.graphics()
                .createModelStatic(attributes, vertex.length / 3, index,
                        renderType);
    }

    public static Model createVCT(ScapesEngine engine, float[] vertex,
            float[] color, float[] texture, RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes.add(new ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
                VertexType.UNSIGNED_BYTE));
        attributes
                .add(new ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
                        0, VertexType.FLOAT));
        return engine.graphics()
                .createModelFast(attributes, vertex.length / 3, renderType);
    }

    public static Model createVCTI(ScapesEngine engine, float[] vertex,
            float[] color, float[] texture, int[] index,
            RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes.add(new ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
                VertexType.UNSIGNED_BYTE));
        attributes
                .add(new ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
                        0, VertexType.FLOAT));
        return engine.graphics()
                .createModelStatic(attributes, vertex.length / 3, index,
                        renderType);
    }

    public static Model createVCTN(ScapesEngine engine, float[] vertex,
            float[] color, float[] texture, float[] normal,
            RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes.add(new ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
                VertexType.UNSIGNED_BYTE));
        attributes
                .add(new ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
                        0, VertexType.FLOAT));
        attributes
                .add(new ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
                        VertexType.BYTE));
        return engine.graphics()
                .createModelFast(attributes, vertex.length / 3, renderType);
    }

    public static Model createVCTNI(ScapesEngine engine, float[] vertex,
            float[] color, float[] texture, float[] normal, int[] index,
            RenderType renderType) {
        List<ModelAttribute> attributes = new ArrayList<>();
        attributes.add(new ModelAttribute(GL.VERTEX_ATTRIBUTE, 3, vertex, false,
                0, VertexType.HALF_FLOAT));
        attributes.add(new ModelAttribute(GL.COLOR_ATTRIBUTE, 4, color, true, 0,
                VertexType.UNSIGNED_BYTE));
        attributes
                .add(new ModelAttribute(GL.TEXTURE_ATTRIBUTE, 2, texture, false,
                        0, VertexType.FLOAT));
        attributes
                .add(new ModelAttribute(GL.NORMAL_ATTRIBUTE, 3, normal, true, 0,
                        VertexType.BYTE));
        return engine.graphics()
                .createModelStatic(attributes, vertex.length / 3, index,
                        renderType);
    }
}
