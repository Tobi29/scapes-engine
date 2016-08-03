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

package org.tobi29.scapes.engine.graphics;

public class ModelAttribute {
    private final VertexType vertexType;
    private final int id, length, size, divisor;
    private final boolean normalized;
    private final float[] floatArray;
    private final byte[] byteArray;
    private int offset;

    public ModelAttribute(int id, int size, byte[] array, int divisor,
            VertexType vertexType) {
        this(id, size, array, array.length, divisor, vertexType);
    }

    public ModelAttribute(int id, int size, byte[] array, int length,
            int divisor, VertexType vertexType) {
        this.id = id;
        this.length = length / vertexType.bytes();
        this.size = size;
        this.divisor = divisor;
        this.vertexType = vertexType;
        normalized = false;
        byteArray = array;
        floatArray = null;
    }

    public ModelAttribute(int id, int size, float[] array, boolean normalized,
            int divisor, VertexType vertexType) {
        this(id, size, array, array.length, normalized, divisor, vertexType);
    }

    public ModelAttribute(int id, int size, float[] array, int length,
            boolean normalized, int divisor, VertexType vertexType) {
        this.id = id;
        this.length = length;
        this.size = size;
        this.normalized = normalized;
        this.divisor = divisor;
        this.vertexType = vertexType;
        floatArray = array;
        byteArray = null;
    }

    public VertexType vertexType() {
        return vertexType;
    }

    public int id() {
        return id;
    }

    public int length() {
        return length;
    }

    public int size() {
        return size;
    }

    public int divisor() {
        return divisor;
    }

    public boolean normalized() {
        return normalized;
    }

    public float[] floatArray() {
        return floatArray;
    }

    public byte[] byteArray() {
        return byteArray;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int offset() {
        return offset;
    }
}
