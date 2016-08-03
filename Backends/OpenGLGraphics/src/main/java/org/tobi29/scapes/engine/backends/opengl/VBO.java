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

package org.tobi29.scapes.engine.backends.opengl;

import java8.util.Optional;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.graphics.GL;
import org.tobi29.scapes.engine.graphics.ModelAttribute;
import org.tobi29.scapes.engine.graphics.VertexType;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

final class VBO {
    private final ScapesEngine engine;
    private final OpenGLBind openGL;
    private final int stride;
    private final List<ModelAttributeData> attributes = new ArrayList<>();
    private Optional<ByteBuffer> data;
    private int vertexID;
    private boolean stored;

    public VBO(ScapesEngine engine, OpenGLBind openGL,
            List<ModelAttribute> attributes, int length) {
        this.engine = engine;
        this.openGL = openGL;
        int stride = 0;
        for (ModelAttribute attribute : attributes) {
            if (attribute.length() != length * attribute.size()) {
                throw new IllegalArgumentException(
                        "Inconsistent attribute data length");
            }
            this.attributes.add(new ModelAttributeData(attribute, stride));
            attribute.setOffset(stride);
            int size = attribute.size() * attribute.vertexType().bytes();
            stride += (size - 1 | 0x03) + 1;
        }
        this.stride = stride;
        ByteBuffer vertexBuffer = engine.allocate(length * stride);
        Streams.forEach(attributes,
                attribute -> addToBuffer(attribute, length, vertexBuffer));
        data = Optional.of(vertexBuffer);
    }

    public ScapesEngine engine() {
        return engine;
    }

    public OpenGLBind openGL() {
        return openGL;
    }

    public int stride() {
        return stride;
    }

    public void replaceBuffer(GL gl, ByteBuffer buffer) {
        assert stored;
        OpenGL openGL = this.openGL.get(gl);
        openGL.bindVBOArray(vertexID);
        openGL.replaceVBODataArray(buffer);
    }

    private void storeAttribute(GL gl, ModelAttributeData attribute) {
        OpenGL openGL = this.openGL.get(gl);
        openGL.setAttribute(attribute.id, attribute.size, attribute.vertexType,
                attribute.normalized, attribute.divisor, stride,
                attribute.offset);
    }

    private void addToBuffer(ModelAttribute attribute, int vertices,
            ByteBuffer buffer) {
        if (attribute.floatArray() == null) {
            switch (attribute.vertexType()) {
                case BYTE:
                case UNSIGNED_BYTE:
                    for (int i = 0; i < vertices; i++) {
                        int is = i * attribute.size();
                        buffer.position(attribute.offset() + i * stride);
                        for (int j = 0; j < attribute.size(); j++) {
                            int ij = is + j;
                            buffer.put(attribute.byteArray()[ij]);
                        }
                    }
                    break;
                case SHORT:
                case UNSIGNED_SHORT:
                    for (int i = 0; i < vertices; i++) {
                        int is = i * attribute.size();
                        buffer.position(attribute.offset() + i * stride);
                        for (int j = 0; j < attribute.size(); j++) {
                            int ij = is + j << 1;
                            buffer.putShort((short) (
                                    attribute.byteArray()[ij + 1] << 8 |
                                            attribute.byteArray()[ij]));
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid array in vao attribute!");
            }
        } else {
            switch (attribute.vertexType()) {
                case FLOAT:
                    for (int i = 0; i < vertices; i++) {
                        int is = i * attribute.size();
                        buffer.position(attribute.offset() + i * stride);
                        for (int j = 0; j < attribute.size(); j++) {
                            int ij = is + j;
                            buffer.putFloat(attribute.floatArray()[ij]);
                        }
                    }
                    break;
                case HALF_FLOAT:
                    for (int i = 0; i < vertices; i++) {
                        int is = i * attribute.size();
                        buffer.position(attribute.offset() + i * stride);
                        for (int j = 0; j < attribute.size(); j++) {
                            int ij = is + j;
                            buffer.putShort(FastMath.convertFloatToHalf(
                                    attribute.floatArray()[ij]));
                        }
                    }
                    break;
                case BYTE:
                    if (attribute.normalized()) {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size();
                            buffer.position(attribute.offset() + i * stride);
                            for (int j = 0; j < attribute.size(); j++) {
                                int ij = is + j;
                                buffer.put((byte) FastMath
                                        .round(attribute.floatArray()[ij] *
                                                127.0f));
                            }
                        }
                    } else {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size();
                            buffer.position(attribute.offset() + i * stride);
                            for (int j = 0; j < attribute.size(); j++) {
                                int ij = is + j;
                                buffer.put((byte) FastMath
                                        .round(attribute.floatArray()[ij]));
                            }
                        }
                    }
                    break;
                case UNSIGNED_BYTE:
                    if (attribute.normalized()) {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size();
                            buffer.position(attribute.offset() + i * stride);
                            for (int j = 0; j < attribute.size(); j++) {
                                int ij = is + j;
                                buffer.put((byte) FastMath
                                        .round(attribute.floatArray()[ij] *
                                                255.0f));
                            }
                        }
                    } else {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size();
                            buffer.position(attribute.offset() + i * stride);
                            for (int j = 0; j < attribute.size(); j++) {
                                int ij = is + j;
                                buffer.put((byte) FastMath
                                        .round(attribute.floatArray()[ij]));
                            }
                        }
                    }
                    break;
                case SHORT:
                    if (attribute.normalized()) {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size();
                            buffer.position(attribute.offset() + i * stride);
                            for (int j = 0; j < attribute.size(); j++) {
                                int ij = is + j;
                                buffer.putShort((short) FastMath
                                        .round(attribute.floatArray()[ij] *
                                                32768.0f));
                            }
                        }
                    } else {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size();
                            buffer.position(attribute.offset() + i * stride);
                            for (int j = 0; j < attribute.size(); j++) {
                                int ij = is + j;
                                buffer.putShort((short) FastMath
                                        .round(attribute.floatArray()[ij]));
                            }
                        }
                    }
                    break;
                case UNSIGNED_SHORT:
                    if (attribute.normalized()) {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size();
                            buffer.position(attribute.offset() + i * stride);
                            for (int j = 0; j < attribute.size(); j++) {
                                int ij = is + j;
                                buffer.putShort((short) FastMath
                                        .round(attribute.floatArray()[ij] *
                                                65535.0f));
                            }
                        }
                    } else {
                        for (int i = 0; i < vertices; i++) {
                            int is = i * attribute.size();
                            buffer.position(attribute.offset() + i * stride);
                            for (int j = 0; j < attribute.size(); j++) {
                                int ij = is + j;
                                buffer.putShort((short) FastMath
                                        .round(attribute.floatArray()[ij]));
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

    public boolean canStore() {
        return data.isPresent();
    }

    public void store(GL gl, boolean weak) {
        assert !stored;
        stored = true;
        OpenGL openGL = this.openGL.get(gl);
        ByteBuffer data = this.data.get();
        data.rewind();
        vertexID = openGL.createVBO();
        openGL.bindVBOArray(vertexID);
        openGL.bufferVBODataArray(data);
        Streams.forEach(attributes, attribute -> storeAttribute(gl, attribute));
        if (weak) {
            this.data = Optional.empty();
        }
    }

    public void dispose(GL gl) {
        assert stored;
        stored = false;
        OpenGL openGL = this.openGL.get(gl);
        openGL.deleteVBO(vertexID);
    }

    public void reset() {
        stored = false;
    }

    private static final class ModelAttributeData {
        private final VertexType vertexType;
        private final int id, size, offset, divisor;
        private final boolean normalized;

        private ModelAttributeData(ModelAttribute attribute, int offset) {
            this.offset = offset;
            vertexType = attribute.vertexType();
            id = attribute.id();
            size = attribute.size();
            normalized = attribute.normalized();
            divisor = attribute.divisor();
        }
    }
}
