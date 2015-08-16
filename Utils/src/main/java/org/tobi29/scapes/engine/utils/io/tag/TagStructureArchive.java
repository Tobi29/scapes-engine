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

import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.io.ByteBufferStream;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TagStructureArchive {
    private static final byte HEADER_VERSION = 1;
    private static final byte[] HEADER_MAGIC = {'S', 'T', 'A', 'R'};
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final Map<String, ByteBuffer> tagStructures =
            new ConcurrentHashMap<>();
    private final ByteBufferStream byteStream, compressionStream;

    public TagStructureArchive() {
        this(new ByteBufferStream(), new ByteBufferStream());
    }

    public TagStructureArchive(ByteBufferStream byteStream,
            ByteBufferStream compressionStream) {
        this.byteStream = byteStream;
        this.compressionStream = compressionStream;
    }

    public static Optional<TagStructure> extract(String name,
            ReadableByteStream stream) throws IOException {
        List<Entry> entries = readHeader(stream);
        int offset = 0;
        for (Entry entry : entries) {
            if (entry.name.equals(name)) {
                stream.skip(offset);
                return Optional.of(TagStructureBinary.read(stream));
            }
            offset += entry.length;
        }
        return Optional.empty();
    }

    public static List<Entry> readHeader(ReadableByteStream stream)
            throws IOException {
        byte[] magic = new byte[HEADER_MAGIC.length];
        stream.get(magic);
        if (!Arrays.equals(magic, magic)) {
            throw new IOException("Not in tag-archive format! (Magic-Header: " +
                    Arrays.toString(magic) +
                    ')');
        }
        byte version = stream.get();
        if (version > HEADER_VERSION) {
            throw new IOException(
                    "Unsupported version or not in tag-container format! (Version: " +
                            version + ')');
        }
        List<Entry> entries = new ArrayList<>();
        while (true) {
            int length = stream.getUByte();
            if (length == 255) {
                break;
            } else if (length == 254) {
                length = stream.getInt();
            }
            byte[] array = new byte[length];
            stream.get(array);
            String name = new String(array, CHARSET);
            length = stream.getInt();
            entries.add(new Entry(name, length));
        }
        return entries;
    }

    public void setTagStructure(String key, TagStructure tagStructure)
            throws IOException {
        setTagStructure(key, tagStructure, (byte) 1);
    }

    public synchronized void setTagStructure(String key,
            TagStructure tagStructure, byte compression) throws IOException {
        byteStream.buffer().clear();
        TagStructureBinary.write(tagStructure, byteStream, compression, true,
                compressionStream);
        byteStream.put(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        byteStream.buffer().flip();
        ByteBuffer buffer =
                BufferCreator.bytes(byteStream.buffer().remaining());
        buffer.put(byteStream.buffer());
        buffer.flip();
        tagStructures.put(key, buffer);
    }

    public synchronized Optional<TagStructure> getTagStructure(String key)
            throws IOException {
        ByteBuffer buffer = tagStructures.get(key);
        if (buffer == null) {
            return Optional.empty();
        }
        TagStructure tagStructure = new TagStructure();
        TagStructureBinary
                .read(tagStructure, new ByteBufferStream(buffer.duplicate()),
                        compressionStream);
        return Optional.of(tagStructure);
    }

    public synchronized void removeTagStructure(String key) {
        tagStructures.remove(key);
    }

    public synchronized void moveTagStructure(String from, String to) {
        ByteBuffer tag = tagStructures.remove(from);
        if (tag != null) {
            tagStructures.put(to, tag);
        }
    }

    public boolean hasTagStructure(String key) {
        return tagStructures.containsKey(key);
    }

    public Collection<String> getKeys() {
        return tagStructures.keySet();
    }

    @SuppressWarnings("AccessToStaticFieldLockedOnInstance")
    public synchronized void write(WritableByteStream stream)
            throws IOException {
        stream.put(HEADER_MAGIC);
        stream.put(HEADER_VERSION);
        List<ByteBuffer> buffers = new ArrayList<>();
        for (Map.Entry<String, ByteBuffer> entry : tagStructures.entrySet()) {
            byte[] array = entry.getKey().getBytes(CHARSET);
            if (array.length >= 254) {
                stream.put(254);
                stream.putInt(array.length);
            } else {
                stream.put(array.length);
            }
            stream.put(array);
            ByteBuffer buffer = entry.getValue().duplicate();
            stream.putInt(buffer.remaining());
            buffers.add(buffer);
        }
        stream.put(255);
        for (ByteBuffer buffer : buffers) {
            stream.put(buffer);
        }
    }

    public synchronized void read(ReadableByteStream stream)
            throws IOException {
        List<Entry> entries = readHeader(stream);
        for (Entry entry : entries) {
            ByteBuffer array = BufferCreator.bytes(entry.length);
            stream.get(array);
            array.flip();
            tagStructures.put(entry.name, array);
        }
    }

    public static class Entry {
        private final String name;
        private final int length;

        private Entry(String name, int length) {
            this.name = name;
            this.length = length;
        }

        public String getName() {
            return name;
        }
    }
}
