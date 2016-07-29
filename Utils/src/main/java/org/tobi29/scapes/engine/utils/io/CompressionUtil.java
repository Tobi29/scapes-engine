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
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Utility class for compressing and decompressing data
 */
public final class CompressionUtil {
    private CompressionUtil() {
    }

    public static ByteBuffer compress(ReadableByteStream input)
            throws IOException {
        return compress(input, Deflater.DEFAULT_COMPRESSION);
    }

    public static ByteBuffer compress(ReadableByteStream input, int level)
            throws IOException {
        return compress(input, level, BufferCreator::bytes);
    }

    public static ByteBuffer compress(ReadableByteStream input, int level,
            IntFunction<ByteBuffer> supplier) throws IOException {
        return compress(input, level, supplier, length -> length + 1024);
    }

    public static ByteBuffer compress(ReadableByteStream input, int level,
            IntFunction<ByteBuffer> supplier, IntUnaryOperator growth)
            throws IOException {
        ByteBufferStream stream = new ByteBufferStream(supplier, growth);
        compress(input, stream, level);
        return stream.buffer();
    }

    public static void compress(ReadableByteStream input,
            WritableByteStream output) throws IOException {
        compress(input, output, 1);
    }

    public static void compress(ReadableByteStream input,
            WritableByteStream output, int level) throws IOException {
        try (Filter filter = new ZDeflater(level)) {
            filter(input, output, filter);
        }
    }

    public static ByteBuffer decompress(ReadableByteStream input)
            throws IOException {
        return decompress(input, BufferCreator::bytes);
    }

    public static ByteBuffer decompress(ReadableByteStream input,
            IntFunction<ByteBuffer> supplier) throws IOException {
        return decompress(input, supplier, length -> length + 1024);
    }

    public static ByteBuffer decompress(ReadableByteStream input,
            IntFunction<ByteBuffer> supplier, IntUnaryOperator growth)
            throws IOException {
        ByteBufferStream output = new ByteBufferStream(supplier, growth);
        decompress(input, output);
        return output.buffer();
    }

    public static void decompress(ReadableByteStream input,
            WritableByteStream output) throws IOException {
        try (Filter filter = new ZInflater()) {
            filter(input, output, filter);
        }
    }

    public static void filter(ReadableByteStream input,
            WritableByteStream output, Filter filter) throws IOException {
        while (input.hasAvailable()) {
            filter.input(input);
            while (!filter.needsInput()) {
                int len = filter.output(output);
                if (len <= 0) {
                    break;
                }
            }
        }
        filter.finish();
        while (!filter.finished()) {
            int len = filter.output(output);
            if (len <= 0) {
                break;
            }
        }
        filter.reset();
    }

    public interface Filter extends AutoCloseable {
        void input(ReadableByteStream buffer) throws IOException;

        int output(WritableByteStream buffer) throws IOException;

        void finish();

        boolean needsInput();

        boolean finished();

        void reset();

        @Override
        void close();
    }

    public static class ZDeflater implements Filter {
        protected final Deflater deflater;
        protected final int buffer;
        protected final ByteBuffer output;
        protected ByteBuffer input;

        public ZDeflater(int level) {
            this(level, 8192);
        }

        public ZDeflater(int level, int buffer) {
            deflater = new Deflater(level);
            this.buffer = buffer;
            input = ByteBuffer.allocate(buffer);
            output = ByteBuffer.allocate(buffer);
        }

        @Override
        public void input(ReadableByteStream buffer) throws IOException {
            if (!input.hasRemaining()) {
                ByteBuffer newInput =
                        ByteBuffer.allocate(input.capacity() << 1);
                input.flip();
                newInput.put(input);
                input = newInput;
            }
            buffer.getSome(input);
            input.flip();
            deflater.setInput(input.array(), input.arrayOffset(),
                    input.remaining());
        }

        @Override
        public int output(WritableByteStream buffer) throws IOException {
            int len = deflater.deflate(output.array());
            output.limit(len);
            buffer.put(output);
            output.clear();
            return len;
        }

        @Override
        public void finish() {
            deflater.finish();
        }

        @Override
        public boolean needsInput() {
            return deflater.needsInput();
        }

        @Override
        public boolean finished() {
            return deflater.finished();
        }

        @Override
        public void reset() {
            deflater.reset();
        }

        @Override
        public void close() {
            deflater.end();
        }
    }

    public static class ZInflater implements Filter {
        protected final Inflater inflater = new Inflater();
        protected final int buffer;
        protected final ByteBuffer output;
        protected ByteBuffer input;

        public ZInflater() {
            this(8192);
        }

        public ZInflater(int buffer) {
            this.buffer = buffer;
            input = ByteBuffer.allocate(buffer);
            output = ByteBuffer.allocate(buffer);
        }

        @Override
        public void input(ReadableByteStream buffer) throws IOException {
            if (!input.hasRemaining()) {
                ByteBuffer newInput =
                        ByteBuffer.allocate(input.capacity() << 1);
                input.flip();
                newInput.put(input);
                input = newInput;
            }
            buffer.getSome(input);
            input.flip();
            inflater.setInput(input.array(), input.arrayOffset(),
                    input.remaining());
        }

        @Override
        public int output(WritableByteStream buffer) throws IOException {
            try {
                int len = inflater.inflate(output.array());
                output.limit(len);
                buffer.put(output);
                output.clear();
                return len;
            } catch (DataFormatException e) {
                return -1;
            }
        }

        @Override
        public void finish() {
        }

        @Override
        public boolean needsInput() {
            return inflater.needsInput();
        }

        @Override
        public boolean finished() {
            return inflater.finished();
        }

        @Override
        public void reset() {
            inflater.reset();
        }

        @Override
        public void close() {
            inflater.end();
        }
    }
}
