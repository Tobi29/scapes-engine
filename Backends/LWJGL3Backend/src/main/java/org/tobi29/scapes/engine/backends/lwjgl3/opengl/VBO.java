package org.tobi29.scapes.engine.backends.lwjgl3.opengl;

import java8.util.Optional;
import org.lwjgl.opengl.*;
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
    private final int stride;
    private final List<ModelAttributeData> attributes = new ArrayList<>();
    private Optional<ByteBuffer> data;
    private int vertexID;
    private boolean stored;

    public VBO(ScapesEngine engine, List<ModelAttribute> attributes,
            int length) {
        this.engine = engine;
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

    public int stride() {
        return stride;
    }

    public void replaceBuffer(GL gl, ByteBuffer buffer) {
        assert stored;
        gl.check();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity(),
                GL15.GL_STREAM_DRAW);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
    }

    private void storeAttribute(GL gl, ModelAttributeData attribute) {
        gl.check();
        GL20.glEnableVertexAttribArray(attribute.id);
        switch (attribute.vertexType) {
            case FLOAT:
                GL20.glVertexAttribPointer(attribute.id, attribute.size,
                        GL11.GL_FLOAT, attribute.normalized, stride,
                        attribute.offset);
                break;
            case HALF_FLOAT:
                GL20.glVertexAttribPointer(attribute.id, attribute.size,
                        GL30.GL_HALF_FLOAT, attribute.normalized, stride,
                        attribute.offset);
                break;
            case BYTE:
                GL20.glVertexAttribPointer(attribute.id, attribute.size,
                        GL11.GL_BYTE, attribute.normalized, stride,
                        attribute.offset);
                break;
            case UNSIGNED_BYTE:
                GL20.glVertexAttribPointer(attribute.id, attribute.size,
                        GL11.GL_UNSIGNED_BYTE, attribute.normalized, stride,
                        attribute.offset);
                break;
            case SHORT:
                GL20.glVertexAttribPointer(attribute.id, attribute.size,
                        GL11.GL_SHORT, attribute.normalized, stride,
                        attribute.offset);
                break;
            case UNSIGNED_SHORT:
                GL20.glVertexAttribPointer(attribute.id, attribute.size,
                        GL11.GL_UNSIGNED_SHORT, attribute.normalized, stride,
                        attribute.offset);
                break;
            default:
                throw new IllegalArgumentException("Unknown vertex type!");
        }
        GL33.glVertexAttribDivisor(attribute.id, attribute.divisor);
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
        gl.check();
        ByteBuffer data = this.data.get();
        data.rewind();
        vertexID = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        Streams.forEach(attributes, attribute -> storeAttribute(gl, attribute));
        if (weak) {
            this.data = Optional.empty();
        }
    }

    public void dispose(GL gl) {
        assert stored;
        stored = false;
        gl.check();
        GL15.glDeleteBuffers(vertexID);
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
