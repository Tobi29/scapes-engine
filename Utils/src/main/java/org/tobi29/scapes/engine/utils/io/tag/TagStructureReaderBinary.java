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

import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.io.ByteBufferStream;
import org.tobi29.scapes.engine.utils.io.CompressionUtil;
import org.tobi29.scapes.engine.utils.io.LimitedBufferStream;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;

import java.io.IOException;
import java.util.Arrays;

public class TagStructureReaderBinary extends TagStructureBinary
        implements TagStructureReader {
    private final ReadableByteStream stream;
    private final ByteBufferStream compressionStream;
    private KeyDictionary dictionary;
    private ReadableByteStream structureBuffer;

    public TagStructureReaderBinary(ReadableByteStream stream,
            ByteBufferStream compressionStream) {
        this.stream = stream;
        this.compressionStream = compressionStream;
    }

    @Override
    public void begin() throws IOException {
        byte[] magic = new byte[HEADER_MAGIC.length];
        stream.get(magic);
        if (!Arrays.equals(magic, magic)) {
            throw new IOException("Not in tag format! (Magic-Header: " +
                    Arrays.toString(magic) +
                    ')');
        }
        byte version = stream.get();
        if (version > HEADER_VERSION) {
            throw new IOException(
                    "Unsupported version or not in tag format! (Version: " +
                            version + ')');
        }
        byte compression = stream.get();
        if (compression >= 0) {
            int len = stream.getInt();
            CompressionUtil.decompress(new LimitedBufferStream(stream, len),
                    compressionStream);
            compressionStream.buffer().flip();
            structureBuffer = new ByteBufferStream(compressionStream.buffer());
        } else {
            structureBuffer = stream;
        }
        dictionary = new KeyDictionary(structureBuffer);
    }

    @Override
    public void end() throws IOException {
        compressionStream.buffer().clear();
    }

    @Override
    public Pair<String, Object> next() throws IOException {
        byte componentID = structureBuffer.get();
        if (componentID == ID_STRUCTURE_TERMINATE) {
            return new Pair<>(null, SpecialNext.STRUCTURE_TERMINATE);
        } else if (componentID == ID_LIST_TERMINATE) {
            return new Pair<>(null, SpecialNext.LIST_TERMINATE);
        }
        String key = readKey(structureBuffer, dictionary);
        Object tag;
        switch (componentID) {
            case ID_STRUCTURE_BEGIN:
                tag = SpecialNext.STRUCTURE;
                break;
            case ID_STRUCTURE_EMPTY:
                tag = SpecialNext.STRUCTURE_EMPTY;
                break;
            case ID_LIST_BEGIN:
                tag = SpecialNext.LIST;
                break;
            case ID_LIST_EMPTY:
                tag = SpecialNext.LIST_EMPTY;
                break;
            case ID_TAG_BOOLEAN:
                tag = structureBuffer.get() != 0;
                break;
            case ID_TAG_BYTE:
                tag = structureBuffer.get();
                break;
            case ID_TAG_BYTE_ARRAY:
                tag = structureBuffer.getByteArrayLong();
                break;
            case ID_TAG_INT_16:
                tag = structureBuffer.getShort();
                break;
            case ID_TAG_INT_32:
                tag = structureBuffer.getInt();
                break;
            case ID_TAG_INT_64:
                tag = structureBuffer.getLong();
                break;
            case ID_TAG_FLOAT_32:
                tag = structureBuffer.getFloat();
                break;
            case ID_TAG_FLOAT_64:
                tag = structureBuffer.getDouble();
                break;
            case ID_TAG_STRING:
                tag = structureBuffer.getString();
                break;
            default:
                throw new IOException(
                        "Not in tag format! (Invalid component-id: " +
                                componentID + ')');
        }
        return new Pair<>(key, tag);
    }

    @Override
    public void beginStructure() throws IOException {
    }

    @Override
    public void endStructure() throws IOException {
    }

    @Override
    public void beginList() throws IOException {
    }

    @Override
    public void endList() throws IOException {
    }
}
