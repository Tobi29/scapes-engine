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
package org.tobi29.scapes.engine.utils.io;

import java8.util.function.IntFunction;
import java8.util.function.IntUnaryOperator;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferStream
        implements RandomWritableByteStream, RandomReadableByteStream {
    private final IntFunction<ByteBuffer> supplier;
    private final IntUnaryOperator growth;
    private ByteBuffer buffer;

    public ByteBufferStream() {
        this(BufferCreator::bytes);
    }

    public ByteBufferStream(IntFunction<ByteBuffer> supplier) {
        this(supplier, length -> length + 8192);
    }

    public ByteBufferStream(IntFunction<ByteBuffer> supplier,
            IntUnaryOperator growth) {
        this(supplier, growth, supplier.apply(growth.applyAsInt(0)));
    }

    public ByteBufferStream(ByteBuffer buffer) {
        this(BufferCreator::bytes, buffer);
    }

    public ByteBufferStream(IntFunction<ByteBuffer> supplier,
            ByteBuffer buffer) {
        this(supplier, length -> length + 8192, buffer);
    }

    public ByteBufferStream(IntFunction<ByteBuffer> supplier,
            IntUnaryOperator growth, ByteBuffer buffer) {
        this.buffer = buffer;
        this.supplier = supplier;
        this.growth = growth;
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    @Override
    public int position() {
        return buffer.position();
    }

    @Override
    public ByteBufferStream position(int pos) {
        ensurePut(pos - buffer.position());
        buffer.position(pos);
        return this;
    }

    @Override
    public ByteBufferStream put(ByteBuffer buffer, int len) {
        ensurePut(len);
        int limit = buffer.limit();
        buffer.limit(buffer.position() + len);
        this.buffer.put(buffer);
        buffer.limit(limit);
        return this;
    }

    @Override
    public ByteBufferStream put(int b) {
        ensurePut(1);
        buffer.put((byte) b);
        return this;
    }

    @Override
    public ByteBufferStream putShort(int value) {
        ensurePut(2);
        buffer.putShort((short) value);
        return this;
    }

    @Override
    public ByteBufferStream putInt(int value) {
        ensurePut(4);
        buffer.putInt(value);
        return this;
    }

    @Override
    public ByteBufferStream putLong(long value) {
        ensurePut(8);
        buffer.putLong(value);
        return this;
    }

    @Override
    public ByteBufferStream putFloat(float value) {
        ensurePut(4);
        buffer.putFloat(value);
        return this;
    }

    @Override
    public ByteBufferStream putDouble(double value) {
        ensurePut(8);
        buffer.putDouble(value);
        return this;
    }

    public void ensurePut(int len) {
        int used = buffer.position();
        int size = buffer.capacity();
        if (len <= size - used) {
            return;
        }
        do {
            size = growth.applyAsInt(size);
        } while (len > size - used);
        grow(size);
    }

    public void grow() {
        grow(growth.applyAsInt(buffer.capacity()));
    }

    public void grow(int size) {
        if (size < buffer.capacity()) {
            throw new IllegalArgumentException(
                    "Tried to shrink buffer with " + buffer.capacity() +
                            " bytes to " + size);
        }
        ByteBuffer newBuffer = supplier.apply(size);
        assert newBuffer.capacity() == size;
        buffer.flip();
        newBuffer.put(buffer);
        buffer = newBuffer;
    }

    @Override
    public int limit() {
        return buffer.limit();
    }

    @Override
    public ReadableByteStream limit(int limit) {
        buffer.limit(limit);
        return this;
    }

    @Override
    public int remaining() {
        return buffer.remaining();
    }

    @Override
    public ReadableByteStream get(ByteBuffer buffer, int len)
            throws IOException {
        ensureGet(len);
        int limit = this.buffer.limit();
        this.buffer.limit(this.buffer.position() + len);
        buffer.put(this.buffer);
        this.buffer.limit(limit);
        return this;
    }

    @Override
    public boolean getSome(ByteBuffer buffer, int len) throws IOException {
        len = FastMath.min(len, this.buffer.remaining());
        int limit = this.buffer.limit();
        this.buffer.limit(this.buffer.position() + len);
        buffer.put(this.buffer);
        this.buffer.limit(limit);
        return this.buffer.remaining() > 0;
    }

    @Override
    public byte get() throws IOException {
        ensureGet(1);
        return buffer.get();
    }

    @Override
    public short getShort() throws IOException {
        ensureGet(2);
        return buffer.getShort();
    }

    @Override
    public int getInt() throws IOException {
        ensureGet(4);
        return buffer.getInt();
    }

    @Override
    public long getLong() throws IOException {
        ensureGet(8);
        return buffer.getLong();
    }

    @Override
    public float getFloat() throws IOException {
        ensureGet(4);
        return buffer.getFloat();
    }

    @Override
    public double getDouble() throws IOException {
        ensureGet(8);
        return buffer.getDouble();
    }

    private void ensureGet(int len) throws IOException {
        if (buffer.remaining() < len) {
            throw new IOException("End of stream");
        }
    }
}