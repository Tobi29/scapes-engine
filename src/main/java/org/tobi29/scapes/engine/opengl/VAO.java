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

import org.tobi29.scapes.engine.opengl.matrix.Matrix;
import org.tobi29.scapes.engine.opengl.shader.Shader;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class VAO {
    private static final List<VAO> VAO_LIST = new ArrayList<>();
    private static int disposeOffset;
    private final RenderType renderType;
    private final int length, stride;
    private final List<VAOAttributeData> attributes = new ArrayList<>();
    private Optional<Pair<ByteBuffer, ByteBuffer>> data;
    private int vertexID, indexID, arrayID;
    private boolean stored, used, markAsDisposed, weak;

    public VAO(List<VAOAttribute> attributes, int vertices, int[] index,
            RenderType renderType) {
        this(attributes, vertices, index, index.length, renderType);
    }

    public VAO(List<VAOAttribute> attributes, int vertices, int[] index,
            int length, RenderType renderType) {
        if (renderType == RenderType.TRIANGLES && index.length % 3 != 0) {
            throw new IllegalArgumentException("Length not multiply of 3");
        } else if (renderType == RenderType.LINES && index.length % 2 != 0) {
            throw new IllegalArgumentException("Length not multiply of 2");
        }
        this.renderType = renderType;
        this.length = length;
        int stride = 0;
        for (VAOAttribute attribute : attributes) {
            if (attribute.length != vertices * attribute.size) {
                throw new IllegalArgumentException(
                        "Inconsistent attribute data length");
            }
            this.attributes.add(new VAOAttributeData(attribute, stride));
            attribute.offset = stride;
            int size = attribute.size * attribute.vertexType.bytes();
            stride += (size | 0x03) + 1;
        }
        this.stride = stride;
        ByteBuffer vertexBuffer = BufferCreatorNative.bytes(vertices * stride)
                .order(ByteOrder.nativeOrder());
        ByteBuffer indexBuffer = BufferCreatorNative.bytes(index.length << 1)
                .order(ByteOrder.nativeOrder());
        attributes.forEach(
                attribute -> addToBuffer(attribute, vertices, vertexBuffer));
        for (int i : index) {
            indexBuffer.putShort((short) i);
        }
        data = Optional.of(new Pair<>(vertexBuffer, indexBuffer));
    }

    @OpenGLFunction
    public static void disposeUnused(GL gl) {
        for (int i = disposeOffset; i < VAO_LIST.size(); i += 16) {
            VAO vao = VAO_LIST.get(i);
            assert vao.stored;
            if (vao.markAsDisposed || !vao.used) {
                vao.dispose(gl);
            }
            vao.used = false;
        }
        disposeOffset++;
        disposeOffset &= 15;
    }

    @OpenGLFunction
    public static void disposeAll(GL gl) {
        VAO_LIST.forEach(VAO::markAsDisposed);
        while (!VAO_LIST.isEmpty()) {
            disposeUnused(gl);
        }
    }

    public static int vaos() {
        return VAO_LIST.size();
    }

    private void storeAttribute(GL gl, VAOAttributeData attribute) {
        gl.setAttribute(attribute.id, attribute.size, attribute.vertexType,
                attribute.normalized, stride, attribute.offset);
    }

    private void addToBuffer(VAOAttribute attribute, int vertices,
            ByteBuffer buffer) {
        if (attribute.floatArray == null) {
            switch (attribute.vertexType) {
                case BYTE:
                case UNSIGNED_BYTE:
                    for (int i = 0; i < vertices; i++) {
                        int is = i * attribute.size;
                        buffer.position(attribute.offset + i * stride);
                        for (int j = 0; j < attribute.size; j++) {
                            int ij = is + j;
                            buffer.put(attribute.byteArray[ij]);
                        }
                    }
                    break;
                case SHORT:
                case UNSIGNED_SHORT:
                    for (int i = 0; i < vertices; i++) {
                        int is = i * attribute.size;
                        buffer.position(attribute.offset + i * stride);
                        for (int j = 0; j < attribute.size; j++) {
                            int ij = is + j << 1;
                            buffer.putShort(
                                    (short) (attribute.byteArray[ij + 1] << 8 |
                                            attribute.byteArray[ij]));
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid array in vao attribute!");
            }
        } else {
            switch (attribute.vertexType) {
                case FLOAT:
                    for (int i = 0; i < vertices; i++) {
                        int is = i * attribute.size;
                        buffer.position(attribute.offset + i * stride);
                        for (int j = 0; j < attribute.size; j++) {
                            int ij = is + j;
                            buffer.putFloat(attribute.floatArray[ij]);
                        }
                    }
                    break;
                case HALF_FLOAT:
                    for (int i = 0; i < vertices; i++) {
                        int is = i * attribute.size;
                        buffer.position(attribute.offset + i * stride);
                        for (int j = 0; j < attribute.size; j++) {
                            int ij = is + j;
                            buffer.putShort(FastMath.convertFloatToHalf(
                                    attribute.floatArray[ij]));
                        }
                    }
                    break;
                case BYTE:
                    if (attribute.normalized) {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size;
                            buffer.position(attribute.offset + i * stride);
                            for (int j = 0; j < attribute.size; j++) {
                                int ij = is + j;
                                buffer.put((byte) FastMath
                                        .round(attribute.floatArray[ij] *
                                                127.0f));
                            }
                        }
                    } else {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size;
                            buffer.position(attribute.offset + i * stride);
                            for (int j = 0; j < attribute.size; j++) {
                                int ij = is + j;
                                buffer.put((byte) FastMath
                                        .round(attribute.floatArray[ij]));
                            }
                        }
                    }
                    break;
                case UNSIGNED_BYTE:
                    if (attribute.normalized) {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size;
                            buffer.position(attribute.offset + i * stride);
                            for (int j = 0; j < attribute.size; j++) {
                                int ij = is + j;
                                buffer.put((byte) FastMath
                                        .round(attribute.floatArray[ij] *
                                                255.0f));
                            }
                        }
                    } else {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size;
                            buffer.position(attribute.offset + i * stride);
                            for (int j = 0; j < attribute.size; j++) {
                                int ij = is + j;
                                buffer.put((byte) FastMath
                                        .round(attribute.floatArray[ij]));
                            }
                        }
                    }
                    break;
                case SHORT:
                    if (attribute.normalized) {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size;
                            buffer.position(attribute.offset + i * stride);
                            for (int j = 0; j < attribute.size; j++) {
                                int ij = is + j;
                                buffer.putShort((short) FastMath
                                        .round(attribute.floatArray[ij] *
                                                32768.0f));
                            }
                        }
                    } else {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size;
                            buffer.position(attribute.offset + i * stride);
                            for (int j = 0; j < attribute.size; j++) {
                                int ij = is + j;
                                buffer.putShort((short) FastMath
                                        .round(attribute.floatArray[ij]));
                            }
                        }
                    }
                    break;
                case UNSIGNED_SHORT:
                    if (attribute.normalized) {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size;
                            buffer.position(attribute.offset + i * stride);
                            for (int j = 0; j < attribute.size; j++) {
                                int ij = is + j;
                                buffer.putShort((short) FastMath
                                        .round(attribute.floatArray[ij] *
                                                65535.0f));
                            }
                        }
                    } else {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size;
                            buffer.position(attribute.offset + i * stride);
                            for (int j = 0; j < attribute.size; j++) {
                                int ij = is + j;
                                buffer.putShort((short) FastMath
                                        .round(attribute.floatArray[ij]));
                            }
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid array in vao attribute!");
            }
        }
    }

    public void markAsDisposed() {
        markAsDisposed = true;
    }

    @OpenGLFunction
    public boolean render(GL gl, Shader shader) {
        ensureStored(gl);
        if (!stored) {
            return false;
        }
        Matrix matrix = gl.matrixStack().current();
        gl.bindVAO(arrayID);
        gl.activateShader(shader.programID());
        Queue<Shader.Uniform> uniforms = shader.uniforms();
        while (!uniforms.isEmpty()) {
            uniforms.poll().set(gl);
        }
        int uniformLocation = shader.uniformLocation(0);
        if (uniformLocation != -1) {
            gl.setUniformMatrix4(uniformLocation, false,
                    matrix.modelView().getBuffer());
        }
        uniformLocation = shader.uniformLocation(1);
        if (uniformLocation != -1) {
            gl.setUniformMatrix4(uniformLocation, false,
                    gl.modelViewProjectionMatrix().getBuffer());
        }
        uniformLocation = shader.uniformLocation(2);
        if (uniformLocation != -1) {
            gl.setUniformMatrix3(uniformLocation, false,
                    matrix.normal().getBuffer());
        }
        switch (renderType) {
            case TRIANGLES:
                gl.drawTriangles(length, 0);
                break;
            case LINES:
                gl.drawLines(length, 0);
                break;
        }
        return true;
    }

    @OpenGLFunction
    public void ensureStored(GL gl) {
        if (!stored) {
            store(gl);
        }
        used = true;
    }

    @OpenGLFunction
    public void ensureDisposed(GL gl) {
        if (stored) {
            dispose(gl);
        }
    }

    private void store(GL gl) {
        data.ifPresent(data -> {
            data.a.rewind();
            data.b.rewind();
            arrayID = gl.createVAO();
            gl.bindVAO(arrayID);
            vertexID = gl.createVBO();
            indexID = gl.createVBO();
            gl.bindVBOArray(vertexID);
            gl.bufferVBODataArray(data.a);
            gl.bindVBOElement(indexID);
            gl.bufferVBODataElement(data.b);
            attributes.stream()
                    .forEach(attribute -> storeAttribute(gl, attribute));
            VAO_LIST.add(this);
            stored = true;
            if (weak) {
                this.data = Optional.empty();
            }
        });
    }

    private void dispose(GL gl) {
        gl.deleteVBO(vertexID);
        gl.deleteVBO(indexID);
        gl.deleteVAO(arrayID);
        VAO_LIST.remove(this);
        stored = false;
    }

    public void setWeak(boolean value) {
        weak = value;
    }

    public static class VAOAttribute {
        private final VertexType vertexType;
        private final int id, length, size;
        private final boolean normalized;
        private final float[] floatArray;
        private final byte[] byteArray;
        private int offset;

        public VAOAttribute(int id, int size, byte[] array,
                VertexType vertexType) {
            this(id, size, array, array.length, vertexType);
        }

        public VAOAttribute(int id, int size, byte[] array, int length,
                VertexType vertexType) {
            this.id = id;
            this.length = length / vertexType.bytes();
            this.size = size;
            this.vertexType = vertexType;
            normalized = false;
            byteArray = array;
            floatArray = null;
        }

        public VAOAttribute(int id, int size, float[] array, boolean normalized,
                VertexType vertexType) {
            this(id, size, array, array.length, normalized, vertexType);
        }

        public VAOAttribute(int id, int size, float[] array, int length,
                boolean normalized, VertexType vertexType) {
            this.id = id;
            this.length = length;
            this.size = size;
            this.normalized = normalized;
            this.vertexType = vertexType;
            floatArray = array;
            byteArray = null;
        }
    }

    private static class VAOAttributeData {
        private final VertexType vertexType;
        private final int id, size, offset;
        private final boolean normalized;

        private VAOAttributeData(VAOAttribute attribute, int offset) {
            this.offset = offset;
            vertexType = attribute.vertexType;
            id = attribute.id;
            size = attribute.size;
            normalized = attribute.normalized;
        }
    }
}
