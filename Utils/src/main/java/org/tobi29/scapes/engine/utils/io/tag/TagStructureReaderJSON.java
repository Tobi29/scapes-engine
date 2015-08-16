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
import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TagStructureReaderJSON extends TagStructureJSON
        implements TagStructureReader {
    private final JsonParser reader;
    private String key;
    private JsonParser.Event event;

    public TagStructureReaderJSON(ReadableByteStream stream) {
        this(new ByteStreamInputStream(stream));
    }

    public TagStructureReaderJSON(InputStream streamIn) {
        reader = Json.createParser(streamIn);
    }

    @Override
    public void begin() throws IOException {
        try {
            event = reader.next();
            if (event != JsonParser.Event.START_OBJECT) {
                throw new IOException("No root object start");
            }
            event = reader.next();
        } catch (JsonException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void end() throws IOException {
        try {
            reader.close();
        } catch (JsonException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Pair<String, Object> next() throws IOException {
        try {
            Pair<String, Object> tag = null;
            switch (event) {
                case KEY_NAME:
                    key = reader.getString();
                    event = reader.next();
                    break;
                case START_OBJECT:
                    if (key != null) {
                        String key = this.key;
                        this.key = null;
                        tag = new Pair<>(key, SpecialNext.STRUCTURE);
                    }
                    event = reader.next();
                    break;
                case START_ARRAY: {
                    String key = this.key;
                    this.key = null;
                    JsonParser.Event next = reader.next();
                    switch (next) {
                        case END_ARRAY:
                            tag = new Pair<>(key, SpecialNext.LIST_EMPTY);
                            break;
                        case START_OBJECT:
                            tag = new Pair<>(key, SpecialNext.LIST);
                            break;
                        case VALUE_NUMBER:
                            JsonParser.Event array;
                            int bits = reader.getInt();
                            switch (bits) {
                                case 8:
                                    ByteArrayOutputStream byteStreamOut =
                                            new ByteArrayOutputStream();
                                    array = reader.next();
                                    while (array !=
                                            JsonParser.Event.END_ARRAY) {
                                        if (array ==
                                                JsonParser.Event.VALUE_NUMBER) {
                                            byteStreamOut.write((byte) reader
                                                    .getInt());
                                        } else {
                                            throw new IOException(
                                                    "Illegal contents of byte array");
                                        }
                                        array = reader.next();
                                    }
                                    tag = new Pair<>(key,
                                            byteStreamOut.toByteArray());
                                    break;
                                default:
                                    throw new IOException(
                                            "Unknown array identifier: " +
                                                    bits);
                            }
                            break;
                        default:
                            throw new IOException(
                                    "Illegal contents of array: " + next);
                    }
                    event = reader.next();
                    break;
                }
                case VALUE_NUMBER: {
                    String key = this.key;
                    this.key = null;
                    if (reader.isIntegralNumber()) {
                        tag = new Pair<>(key, reader.getLong());
                    } else {
                        tag = new Pair<>(key,
                                reader.getBigDecimal().doubleValue());
                    }
                    event = reader.next();
                    break;
                }
                case VALUE_STRING: {
                    String key = this.key;
                    this.key = null;
                    tag = new Pair<>(key, reader.getString());
                    event = reader.next();
                    break;
                }
                case VALUE_FALSE: {
                    String key = this.key;
                    this.key = null;
                    tag = new Pair<>(key, false);
                    event = reader.next();
                    break;
                }
                case VALUE_TRUE: {
                    String key = this.key;
                    this.key = null;
                    tag = new Pair<>(key, true);
                    event = reader.next();
                    break;
                }
                case END_OBJECT:
                    tag = new Pair<>(null, SpecialNext.STRUCTURE_TERMINATE);
                    if (reader.hasNext()) {
                        event = reader.next();
                        if (event == JsonParser.Event.END_ARRAY) {
                            tag = new Pair<>(null, SpecialNext.LIST_TERMINATE);
                            event = reader.next();
                        }
                    }
                    break;
                default:
                    throw new IOException("Unknown event: " + event);
            }
            return tag;
        } catch (JsonException e) {
            throw new IOException(e);
        }
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
