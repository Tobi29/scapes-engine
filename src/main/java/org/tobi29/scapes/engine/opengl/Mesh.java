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

import org.tobi29.scapes.engine.utils.math.FastMath;

public class Mesh {
    private static final float[] EMPTY_FLOAT = new float[0];
    private static final int BATCH_SIZE = 12;
    private final boolean triangles, color;
    private int pos, remaining;
    private float[] vertexArray = EMPTY_FLOAT;
    private float[] colorArray = EMPTY_FLOAT;
    private float[] textureArray = EMPTY_FLOAT;
    private float[] normalArray = EMPTY_FLOAT;
    private float r;
    private float g;
    private float b;
    private float a;
    private float tx;
    private float ty;
    private float nx;
    private float ny;
    private float nz;

    public Mesh() {
        this(false);
    }

    public Mesh(boolean triangles) {
        this(triangles, true);
    }

    public Mesh(boolean triangles, boolean color) {
        this.triangles = triangles;
        this.color = color;
    }

    public void color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void texture(float tx, float ty) {
        this.tx = tx;
        this.ty = ty;
    }

    public void normal(float nx, float ny, float nz) {
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }

    public void vertex(float x, float y, float z) {
        addVertex(x, y, z, r, g, b, a, tx, ty, nx, ny, nz);
    }

    public void addVertex(float x, float y, float z, float tx, float ty) {
        addVertex(x, y, z, 1.0f, 1.0f, 1.0f, 1.0f, tx, ty, 0.0f, 0.0f, 1.0f);
    }

    public void addVertex(float x, float y, float z, float r, float g, float b,
            float a, float tx, float ty) {
        addVertex(x, y, z, r, g, b, a, tx, ty, 0.0f, 0.0f, 1.0f);
    }

    public void addVertex(float x, float y, float z, float r, float g, float b,
            float a, float tx, float ty, float nx, float ny, float nz) {
        if (remaining <= 0) {
            changeArraySize(pos + BATCH_SIZE);
            remaining += BATCH_SIZE;
        }
        int i = pos * 3;
        vertexArray[i++] = x;
        vertexArray[i++] = y;
        vertexArray[i] = z;
        if (color) {
            i = pos << 2;
            colorArray[i++] = r;
            colorArray[i++] = g;
            colorArray[i++] = b;
            colorArray[i] = a;
        }
        i = pos << 1;
        textureArray[i++] = tx;
        textureArray[i] = ty;
        i = pos * 3;
        normalArray[i++] = nx;
        normalArray[i++] = ny;
        normalArray[i] = nz;
        pos++;
        remaining--;
    }

    private void changeArraySize(int size) {
        float[] newVertexArray = new float[size * 3];
        float[] newColorArray = new float[(size << 2)];
        float[] newTextureArray = new float[(size << 1)];
        float[] newNormalArray = new float[size * 3];
        System.arraycopy(vertexArray, 0, newVertexArray, 0,
                FastMath.min(vertexArray.length, newVertexArray.length));
        if (color) {
            System.arraycopy(colorArray, 0, newColorArray, 0,
                    FastMath.min(colorArray.length, newColorArray.length));
        }
        System.arraycopy(textureArray, 0, newTextureArray, 0,
                FastMath.min(textureArray.length, newTextureArray.length));
        System.arraycopy(normalArray, 0, newNormalArray, 0,
                FastMath.min(normalArray.length, newNormalArray.length));
        vertexArray = newVertexArray;
        colorArray = newColorArray;
        textureArray = newTextureArray;
        normalArray = newNormalArray;
    }

    public VAO finish() {
        changeArraySize(pos);
        int[] indexArray;
        if (triangles) {
            indexArray = new int[pos];
            for (int i = 0; i < indexArray.length; i++) {
                indexArray[i] = i;
            }
        } else {
            indexArray = new int[(int) (pos * 1.5)];
            int i = 0, p = 0;
            while (i < indexArray.length) {
                indexArray[i++] = p;
                indexArray[i++] = p + 1;
                indexArray[i++] = p + 2;
                indexArray[i++] = p;
                indexArray[i++] = p + 2;
                indexArray[i++] = p + 3;
                p += 4;
            }
        }
        VAO vao;
        if (color) {
            vao = VAOUtility.createVCTNI(vertexArray, colorArray, textureArray,
                    normalArray, indexArray, RenderType.TRIANGLES);
        } else {
            vao = VAOUtility.createVTNI(vertexArray, textureArray, normalArray,
                    indexArray, RenderType.TRIANGLES);
        }
        return vao;
    }
}
