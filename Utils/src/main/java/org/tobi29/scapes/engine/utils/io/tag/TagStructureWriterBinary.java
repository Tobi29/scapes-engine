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

package org.tobi29.scapes.engine.utils.io.tag;

import org.tobi29.scapes.engine.utils.io.ByteBufferStream;
import org.tobi29.scapes.engine.utils.io.CompressionUtil;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import java.io.IOException;

public class TagStructureWriterBinary extends TagStructureBinary
        implements TagStructureWriter {
    private final WritableByteStream stream;
    private final ByteBufferStream compressionStream;
    private final byte compression;
    private final boolean useDictionary;
    private KeyDictionary dictionary;
    private ByteBufferStream byteStream;
    private WritableByteStream structureStream;

    public TagStructureWriterBinary(WritableByteStream stream, byte compression,
            boolean useDictionary, ByteBufferStream compressionStream) {
        this.stream = stream;
        this.compression = compression;
        this.useDictionary = useDictionary;
        this.compressionStream = compressionStream;
    }

    @Override
    public void begin(TagStructure root) throws IOException {
        stream.put(HEADER_MAGIC);
        stream.put(HEADER_VERSION);
        stream.put(compression);
        if (compression >= 0) {
            byteStream = new ByteBufferStream();
            structureStream = byteStream;
        } else {
            structureStream = stream;
        }
        if (useDictionary) {
            dictionary = new KeyDictionary(root);
        } else {
            dictionary = new KeyDictionary();
        }
        dictionary.write(structureStream);
    }

    @Override
    public void end() throws IOException {
        structureStream.put(ID_STRUCTURE_TERMINATE);
        if (compression >= 0) {
            byteStream.buffer().flip();
            CompressionUtil.compress(new ByteBufferStream(byteStream.buffer()),
                    compressionStream);
            compressionStream.buffer().flip();
            stream.putInt(compressionStream.buffer().remaining());
            stream.put(compressionStream.buffer());
            compressionStream.buffer().clear();
        }
    }

    @Override
    public void beginStructure() throws IOException {
    }

    @Override
    public void beginStructure(String key) throws IOException {
        structureStream.put(ID_STRUCTURE_BEGIN);
        writeKey(key, structureStream, dictionary);
    }

    @Override
    public void endStructure() throws IOException {
        structureStream.put(ID_STRUCTURE_TERMINATE);
    }

    @Override
    public void structureEmpty() throws IOException {
        structureStream.put(ID_STRUCTURE_TERMINATE);
    }

    @Override
    public void structureEmpty(String key) throws IOException {
        structureStream.put(ID_STRUCTURE_EMPTY);
        writeKey(key, structureStream, dictionary);
    }

    @Override
    public void beginList(String key) throws IOException {
        structureStream.put(ID_LIST_BEGIN);
        writeKey(key, structureStream, dictionary);
    }

    @Override
    public void endListWidthTerminate() throws IOException {
        structureStream.put(ID_LIST_TERMINATE);
    }

    @Override
    public void endListWithEmpty() throws IOException {
        structureStream.put(ID_LIST_TERMINATE);
    }

    @Override
    public void listEmpty(String key) throws IOException {
        structureStream.put(ID_LIST_EMPTY);
        writeKey(key, structureStream, dictionary);
    }

    @Override
    public void writeTag(String key, Object tag) throws IOException {
        if (tag instanceof Boolean) {
            structureStream.put(ID_TAG_BOOLEAN);
            writeKey(key, structureStream, dictionary);
            structureStream.put((boolean) tag ? 1 : 0);
        } else if (tag instanceof Byte) {
            structureStream.put(ID_TAG_BYTE);
            writeKey(key, structureStream, dictionary);
            structureStream.put((byte) tag);
        } else if (tag instanceof byte[]) {
            structureStream.put(ID_TAG_BYTE_ARRAY);
            writeKey(key, structureStream, dictionary);
            structureStream.putByteArrayLong((byte[]) tag);
        } else if (tag instanceof Short) {
            structureStream.put(ID_TAG_INT_16);
            writeKey(key, structureStream, dictionary);
            structureStream.putShort((short) tag);
        } else if (tag instanceof Integer) {
            structureStream.put(ID_TAG_INT_32);
            writeKey(key, structureStream, dictionary);
            structureStream.putInt((int) tag);
        } else if (tag instanceof Long) {
            structureStream.put(ID_TAG_INT_64);
            writeKey(key, structureStream, dictionary);
            structureStream.putLong((long) tag);
        } else if (tag instanceof Float) {
            structureStream.put(ID_TAG_FLOAT_32);
            writeKey(key, structureStream, dictionary);
            structureStream.putFloat((float) tag);
        } else if (tag instanceof Double) {
            structureStream.put(ID_TAG_FLOAT_64);
            writeKey(key, structureStream, dictionary);
            structureStream.putDouble((double) tag);
        } else if (tag instanceof String) {
            structureStream.put(ID_TAG_STRING);
            writeKey(key, structureStream, dictionary);
            structureStream.putString((String) tag);
        } else {
            throw new IOException("Invalid type: " + tag.getClass());
        }
    }
}
