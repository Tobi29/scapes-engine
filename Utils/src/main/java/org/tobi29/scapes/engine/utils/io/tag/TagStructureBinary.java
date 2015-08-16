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
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TagStructureBinary {
    // Header
    protected static final byte[] HEADER_MAGIC = {'S', 'T', 'A', 'G'};
    protected static final byte HEADER_VERSION = 0x1;
    // Components
    //  Structure
    protected static final byte ID_STRUCTURE_BEGIN = 0x10;
    protected static final byte ID_STRUCTURE_TERMINATE = 0x11;
    protected static final byte ID_STRUCTURE_EMPTY = 0x12;
    //  List
    protected static final byte ID_LIST_BEGIN = 0x20;
    protected static final byte ID_LIST_TERMINATE = 0x21;
    protected static final byte ID_LIST_EMPTY = 0x22;
    //  Tags
    //   Boolean
    protected static final byte ID_TAG_BOOLEAN = 0x30;
    //   Byte
    protected static final byte ID_TAG_BYTE = 0x40;
    protected static final byte ID_TAG_BYTE_ARRAY = 0x41;
    //   Integer
    protected static final byte ID_TAG_INT_16 = 0x50;
    protected static final byte ID_TAG_INT_32 = 0x51;
    protected static final byte ID_TAG_INT_64 = 0x52;
    //   Float
    protected static final byte ID_TAG_FLOAT_32 = 0x60;
    protected static final byte ID_TAG_FLOAT_64 = 0x61;
    //   String
    protected static final byte ID_TAG_STRING = 0x71;

    protected static String readKey(ReadableByteStream stream,
            KeyDictionary dictionary) throws IOException {
        byte alias = stream.get();
        if (alias == -1) {
            return stream.getString();
        } else {
            return dictionary.getKey(alias);
        }
    }

    protected static void writeKey(String key, WritableByteStream stream,
            KeyDictionary dictionary) throws IOException {
        Byte alias = dictionary.getAlias(key);
        if (alias == null) {
            stream.put(0xFF);
            stream.putString(key);
        } else {
            stream.put(alias);
        }
    }

    public static TagStructure write(TagStructure tagStructure,
            WritableByteStream stream) throws IOException {
        return write(tagStructure, stream, (byte) -1);
    }

    public static TagStructure write(TagStructure tagStructure,
            WritableByteStream stream, byte compression) throws IOException {
        return write(tagStructure, stream, compression, true);
    }

    public static TagStructure write(TagStructure tagStructure,
            WritableByteStream stream, byte compression, boolean useDictionary)
            throws IOException {
        return write(tagStructure, stream, compression, useDictionary,
                new ByteBufferStream());
    }

    public static TagStructure write(TagStructure tagStructure,
            WritableByteStream stream, byte compression, boolean useDictionary,
            ByteBufferStream compressionStream) throws IOException {
        tagStructure.write(new TagStructureWriterBinary(stream, compression,
                useDictionary, compressionStream));
        return tagStructure;
    }

    public static TagStructure read(ReadableByteStream stream)
            throws IOException {
        return read(new TagStructure(), stream);
    }

    public static TagStructure read(TagStructure tagStructure,
            ReadableByteStream stream) throws IOException {
        return read(tagStructure, stream, new ByteBufferStream());
    }

    public static TagStructure read(TagStructure tagStructure,
            ReadableByteStream stream, ByteBufferStream compressionStream)
            throws IOException {
        tagStructure
                .read(new TagStructureReaderBinary(stream, compressionStream));
        return tagStructure;
    }

    protected static class KeyDictionary {
        protected final List<String> keyAliases = new ArrayList<>();
        protected final Map<String, Byte> keyAliasMap =
                new ConcurrentHashMap<>();
        protected final Map<Byte, String> aliasKeyMap =
                new ConcurrentHashMap<>();
        protected byte currentId;

        protected KeyDictionary() {
        }

        protected KeyDictionary(ReadableByteStream streamIn)
                throws IOException {
            int length = streamIn.get();
            if (length < 0) {
                length += 256;
            }
            while (length-- > 0) {
                addKeyAlias(streamIn.getString());
            }
        }

        protected KeyDictionary(TagStructure tagStructure) {
            Map<String, KeyOccurrence> keys = new ConcurrentHashMap<>();
            analyze(tagStructure, keys);
            if (keys.size() > 255) {
                keys.entrySet().stream().sorted((entry1, entry2) ->
                        entry1.getValue().count == entry2.getValue().count ? 0 :
                                entry1.getValue().count <
                                        entry2.getValue().count ? 1 : -1)
                        .limit(255).map(Map.Entry::getKey)
                        .forEach(this::addKeyAlias);
            } else {
                keys.entrySet().stream().map(Map.Entry::getKey)
                        .forEach(this::addKeyAlias);
            }
        }

        @SuppressWarnings("unchecked")
        private static void analyze(TagStructure tagStructure,
                Map<String, KeyOccurrence> keys) {
            for (Map.Entry<String, Object> entry : tagStructure
                    .getTagEntrySet()) {
                String key = entry.getKey();
                KeyOccurrence occurrence = keys.get(key);
                if (occurrence == null) {
                    keys.put(key, new KeyOccurrence(key));
                } else {
                    occurrence.count += occurrence.length;
                }
                Object value = entry.getValue();
                if (value instanceof TagStructure) {
                    analyze((TagStructure) value, keys);
                } else if (value instanceof List<?>) {
                    ((List<TagStructure>) value).stream()
                            .forEach(child -> analyze(child, keys));
                }
            }
        }

        protected String getKey(byte alias) {
            return aliasKeyMap.get(alias);
        }

        protected void addKeyAlias(String key) {
            keyAliases.add(key);
            keyAliasMap.put(key, currentId);
            aliasKeyMap.put(currentId++, key);
        }

        protected Byte getAlias(String key) {
            return keyAliasMap.get(key);
        }

        protected void write(WritableByteStream output) throws IOException {
            output.put(keyAliases.size());
            for (String keyAlias : keyAliases) {
                output.putString(keyAlias);
            }
        }

        private static class KeyOccurrence {
            private final int length;
            private int count;

            private KeyOccurrence(String key) {
                length = key.length();
                count += length;
            }
        }
    }
}
