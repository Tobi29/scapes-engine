package org.tobi29.scapes.engine.opengl.vao;

import java8.util.Optional;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.opengl.OpenGLFunction;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VBO {
    protected final ScapesEngine engine;
    protected final int length, stride;
    protected final List<VBOAttributeData> attributes = new ArrayList<>();
    protected Optional<ByteBuffer> data;
    protected int vertexID;
    protected boolean stored;

    public VBO(ScapesEngine engine, List<VBOAttribute> attributes, int length) {
        this.engine = engine;
        this.length = length;
        int stride = 0;
        for (VBOAttribute attribute : attributes) {
            if (attribute.length != length * attribute.size) {
                throw new IllegalArgumentException(
                        "Inconsistent attribute data length");
            }
            this.attributes.add(new VBOAttributeData(attribute, stride));
            attribute.offset = stride;
            int size = attribute.size * attribute.vertexType.bytes();
            stride += (size - 1 | 0x03) + 1;
        }
        this.stride = stride;
        ByteBuffer vertexBuffer = engine.allocate(length * stride);
        Streams.forEach(attributes,
                attribute -> addToBuffer(attribute, length, vertexBuffer));
        data = Optional.of(vertexBuffer);
    }

    @OpenGLFunction
    public void replaceBuffer(GL gl, ByteBuffer buffer) {
        assert stored;
        gl.bindVBOArray(vertexID);
        gl.replaceVBODataArray(buffer);
    }

    public int stride() {
        return stride;
    }

    private void storeAttribute(GL gl, VBOAttributeData attribute) {
        gl.setAttribute(attribute.id, attribute.size, attribute.vertexType,
                attribute.normalized, attribute.divisor, stride,
                attribute.offset);
    }

    private void addToBuffer(VBOAttribute attribute, int vertices,
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

    protected boolean canStore() {
        return data.isPresent();
    }

    protected void store(GL gl, boolean weak) {
        assert !stored;
        ByteBuffer data = this.data.get();
        data.rewind();
        vertexID = gl.createVBO();
        gl.bindVBOArray(vertexID);
        gl.bufferVBODataArray(data);
        Streams.forEach(attributes, attribute -> storeAttribute(gl, attribute));
        if (weak) {
            this.data = Optional.empty();
        }
        stored = true;
    }

    protected void dispose(GL gl) {
        assert stored;
        gl.deleteVBO(vertexID);
        stored = false;
    }

    protected void reset() {
        stored = false;
    }

    public static class VBOAttribute {
        private final VertexType vertexType;
        private final int id, length, size, divisor;
        private final boolean normalized;
        private final float[] floatArray;
        private final byte[] byteArray;
        private int offset;

        public VBOAttribute(int id, int size, byte[] array, int divisor,
                VertexType vertexType) {
            this(id, size, array, array.length, divisor, vertexType);
        }

        public VBOAttribute(int id, int size, byte[] array, int length,
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

        public VBOAttribute(int id, int size, float[] array, boolean normalized,
                int divisor, VertexType vertexType) {
            this(id, size, array, array.length, normalized, divisor,
                    vertexType);
        }

        public VBOAttribute(int id, int size, float[] array, int length,
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
    }

    private static final class VBOAttributeData {
        private final VertexType vertexType;
        private final int id, size, offset, divisor;
        private final boolean normalized;

        private VBOAttributeData(VBOAttribute attribute, int offset) {
            this.offset = offset;
            vertexType = attribute.vertexType;
            id = attribute.id;
            size = attribute.size;
            normalized = attribute.normalized;
            divisor = attribute.divisor;
        }
    }
}
