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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to read an entire stream and process it as a byte array.
 */
public final class ProcessStream {
    private ProcessStream() {
    }

    public static <E> E process(ReadableByteStream input,
            StreamProcessor<E> processor) throws IOException {
        return process(input, processor, 1024);
    }

    public static <E> E process(ReadableByteStream input,
            StreamProcessor<E> processor, int bufferSize) throws IOException {
        ByteBuffer buffer = BufferCreator.bytes(bufferSize);
        boolean available = true;
        while (available) {
            available = input.getSome(buffer);
            buffer.flip();
            processor.process(buffer);
            buffer.clear();
        }
        return processor.result();
    }

    public static StreamProcessor<byte[]> asArray() {
        return new StreamProcessor<byte[]>() {
            private final ByteBufferStream stream = new ByteBufferStream();

            @Override
            public void process(ByteBuffer buffer) throws IOException {
                stream.put(buffer);
            }

            @Override
            public byte[] result() {
                stream.buffer().flip();
                byte[] array = new byte[stream.buffer().remaining()];
                stream.buffer().get(array);
                return array;
            }
        };
    }

    public static StreamProcessor<ByteBuffer> asBuffer() {
        return asBuffer(BufferCreator::bytes);
    }

    public static StreamProcessor<ByteBuffer> asBuffer(
            IntFunction<ByteBuffer> supplier) {
        return asBuffer(supplier, length -> length + 8192);
    }

    public static StreamProcessor<ByteBuffer> asBuffer(
            IntFunction<ByteBuffer> supplier, IntUnaryOperator growth) {
        return new StreamProcessor<ByteBuffer>() {
            private final ByteBufferStream stream =
                    new ByteBufferStream(supplier, growth);

            @Override
            public void process(ByteBuffer buffer) throws IOException {
                stream.put(buffer);
            }

            @Override
            public ByteBuffer result() {
                stream.buffer().flip();
                return stream.buffer();
            }
        };
    }

    /**
     * A default {@link StreamProcessor} that encoded the input into a {@link
     * String}
     *
     * @return UTF8 encoded {@link String}
     */
    public static StreamProcessor<String> asString() {
        return asString(StandardCharsets.UTF_8);
    }

    /**
     * A default {@link StreamProcessor} that encoded the input into a {@link
     * String}
     *
     * @param charset Encoding charset for data
     * @return Encoded {@link String}
     */
    public static StreamProcessor<String> asString(Charset charset) {
        return new StreamProcessor<String>() {
            private final ByteBufferStream stream = new ByteBufferStream(
                    length -> ByteBuffer.wrap(new byte[length]),
                    length -> length + 1024);

            @Override
            public void process(ByteBuffer buffer) throws IOException {
                stream.put(buffer);
            }

            @Override
            public String result() {
                return new String(stream.buffer().array(), 0,
                        stream.buffer().position(), charset);
            }
        };
    }

    public interface StreamProcessor<E> {
        void process(ByteBuffer buffer) throws IOException;

        @SuppressWarnings("ReturnOfNull")
        default E result() {
            return null;
        }
    }
}
