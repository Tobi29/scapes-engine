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

package org.tobi29.scapes.engine.opengl;

import java.util.ArrayList;
import java.util.List;

public class VAOUtility {
    public static VAO createVI(float[] vertex, int[] index,
            RenderType renderType) {
        List<VAO.VAOAttribute> vaoAttributes = new ArrayList<>();
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.VERTEX_ATTRIBUTE, 3, vertex,
                        false, VertexType.HALF_FLOAT));
        return new VAO(vaoAttributes, vertex.length / 3, index, renderType);
    }

    public static VAO createVNI(float[] vertex, float[] normal, int[] index,
            RenderType renderType) {
        List<VAO.VAOAttribute> vaoAttributes = new ArrayList<>();
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.VERTEX_ATTRIBUTE, 3, vertex,
                        false, VertexType.HALF_FLOAT));
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.NORMAL_ATTRIBUTE, 3, normal,
                        true, VertexType.BYTE));
        return new VAO(vaoAttributes, vertex.length / 3, index, renderType);
    }

    public static VAO createVCI(float[] vertex, float[] color, int[] index,
            RenderType renderType) {
        List<VAO.VAOAttribute> vaoAttributes = new ArrayList<>();
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.VERTEX_ATTRIBUTE, 3, vertex,
                        false, VertexType.HALF_FLOAT));
        vaoAttributes.add(new VAO.VAOAttribute(OpenGL.COLOR_ATTRIBUTE, 4, color,
                false, VertexType.UNSIGNED_BYTE));
        return new VAO(vaoAttributes, vertex.length / 3, index, renderType);
    }

    public static VAO createVTI(float[] vertex, float[] texture, int[] index,
            RenderType renderType) {
        List<VAO.VAOAttribute> vaoAttributes = new ArrayList<>();
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.VERTEX_ATTRIBUTE, 3, vertex,
                        false, VertexType.HALF_FLOAT));
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.TEXTURE_ATTRIBUTE, 2, texture,
                        false, VertexType.FLOAT));
        return new VAO(vaoAttributes, vertex.length / 3, index, renderType);
    }

    public static VAO createVTNI(float[] vertex, float[] texture,
            float[] normal, int[] index, RenderType renderType) {
        List<VAO.VAOAttribute> vaoAttributes = new ArrayList<>();
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.VERTEX_ATTRIBUTE, 3, vertex,
                        false, VertexType.HALF_FLOAT));
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.TEXTURE_ATTRIBUTE, 2, texture,
                        false, VertexType.FLOAT));
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.NORMAL_ATTRIBUTE, 3, normal,
                        true, VertexType.BYTE));
        return new VAO(vaoAttributes, vertex.length / 3, index, renderType);
    }

    public static VAO createVCTI(float[] vertex, float[] color, float[] texture,
            int[] index, RenderType renderType) {
        List<VAO.VAOAttribute> vaoAttributes = new ArrayList<>();
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.VERTEX_ATTRIBUTE, 3, vertex,
                        false, VertexType.HALF_FLOAT));
        vaoAttributes.add(new VAO.VAOAttribute(OpenGL.COLOR_ATTRIBUTE, 4, color,
                true, VertexType.UNSIGNED_BYTE));
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.TEXTURE_ATTRIBUTE, 2, texture,
                        false, VertexType.FLOAT));
        return new VAO(vaoAttributes, vertex.length / 3, index, renderType);
    }

    public static VAO createVCTNI(float[] vertex, float[] color,
            float[] texture, float[] normal, int[] index,
            RenderType renderType) {
        List<VAO.VAOAttribute> vaoAttributes = new ArrayList<>();
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.VERTEX_ATTRIBUTE, 3, vertex,
                        false, VertexType.HALF_FLOAT));
        vaoAttributes.add(new VAO.VAOAttribute(OpenGL.COLOR_ATTRIBUTE, 4, color,
                true, VertexType.UNSIGNED_BYTE));
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.TEXTURE_ATTRIBUTE, 2, texture,
                        false, VertexType.FLOAT));
        vaoAttributes
                .add(new VAO.VAOAttribute(OpenGL.NORMAL_ATTRIBUTE, 3, normal,
                        true, VertexType.BYTE));
        return new VAO(vaoAttributes, vertex.length / 3, index, renderType);
    }
}
