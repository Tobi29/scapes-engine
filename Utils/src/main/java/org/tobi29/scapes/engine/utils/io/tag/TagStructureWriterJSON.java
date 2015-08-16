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

import org.tobi29.scapes.engine.utils.io.ByteStreamOutputStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TagStructureWriterJSON extends TagStructureJSON
        implements TagStructureWriter {
    private static final JsonGeneratorFactory FACTORY;

    static {
        Map<String, Object> map = new ConcurrentHashMap<>();
        map.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
        FACTORY = Json.createGeneratorFactory(map);
    }

    private final JsonGenerator writer;

    public TagStructureWriterJSON(WritableByteStream stream) {
        this(new ByteStreamOutputStream(stream));
    }

    public TagStructureWriterJSON(OutputStream streamOut) {
        writer = FACTORY.createGenerator(streamOut, StandardCharsets.UTF_8);
    }

    @Override
    public void begin(TagStructure root) throws IOException {
        writer.writeStartObject();
    }

    @Override
    public void end() throws IOException {
        writer.writeEnd();
        writer.close();
    }

    @Override
    public void beginStructure() throws IOException {
        writer.writeStartObject();
    }

    @Override
    public void beginStructure(String key) throws IOException {
        writer.writeStartObject(key);
    }

    @Override
    public void endStructure() throws IOException {
        writer.writeEnd();
    }

    @Override
    public void structureEmpty() throws IOException {
        writer.writeStartObject();
        writer.writeEnd();
    }

    @Override
    public void structureEmpty(String key) throws IOException {
        writer.writeStartObject(key);
        writer.writeEnd();
    }

    @Override
    public void beginList(String key) throws IOException {
        writer.writeStartArray(key);
    }

    @Override
    public void endListWidthTerminate() throws IOException {
        writer.writeEnd();
        writer.writeEnd();
    }

    @Override
    public void endListWithEmpty() throws IOException {
        writer.writeStartObject();
        writer.writeEnd();
        writer.writeEnd();
    }

    @Override
    public void listEmpty(String key) throws IOException {
        writer.writeStartArray(key);
        writer.writeEnd();
    }

    @Override
    public void writeTag(String key, Object tag) throws IOException {
        if (tag instanceof Boolean) {
            writer.write(key, (Boolean) tag);
        } else if (tag instanceof Byte) {
            writer.write(key, (Byte) tag);
        } else if (tag instanceof byte[]) {
            byte[] array = (byte[]) tag;
            writer.writeStartArray(key);
            writer.write(8);
            for (byte value : array) {
                writer.write(value);
            }
            writer.writeEnd();
        } else if (tag instanceof Short) {
            writer.write(key, (Short) tag);
        } else if (tag instanceof Integer) {
            writer.write(key, (Integer) tag);
        } else if (tag instanceof Long) {
            writer.write(key, (Long) tag);
        } else if (tag instanceof Float) {
            writer.write(key, (Float) tag);
        } else if (tag instanceof Double) {
            writer.write(key, (Double) tag);
        } else if (tag instanceof String) {
            writer.write(key, (String) tag);
        } else {
            throw new IOException("Invalid type: " + tag.getClass());
        }
    }
}
