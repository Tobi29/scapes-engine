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

import org.tobi29.scapes.engine.utils.ArrayUtil;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

public class TagStructureReaderXML extends TagStructureXML
        implements TagStructureReader {
    private static final XMLInputFactory FACTORY =
            XMLInputFactory.newInstance();
    private final XMLStreamReader reader;

    public TagStructureReaderXML(ReadableByteStream stream) throws IOException {
        try {
            reader = FACTORY.createXMLStreamReader(
                    new ByteStreamInputStream(stream));
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void begin() throws IOException {
        try {
            reader.nextTag();
            reader.require(XMLStreamConstants.START_ELEMENT, null,
                    ELEMENT_ROOT);
            reader.nextTag();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void end() throws IOException {
    }

    @Override
    public Pair<String, Object> next() throws IOException {
        try {
            int event = reader.getEventType();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT: {
                    String next = reader.getName().getLocalPart();
                    switch (next) {
                        case ELEMENT_STRUCTURE: {
                            String key = reader.getAttributeValue(null,
                                    ATTRIBUTE_KEY);
                            if (key == null) {
                                throw new IOException(
                                        "Missing key in structure!");
                            }
                            return new Pair<>(key, SpecialNext.STRUCTURE);
                        }
                        case ELEMENT_LIST: {
                            String key = reader.getAttributeValue(null,
                                    ATTRIBUTE_KEY);
                            if (key == null) {
                                throw new IOException("Missing key in list!");
                            }
                            if (reader.nextTag() ==
                                    XMLStreamConstants.END_ELEMENT) {
                                if (ELEMENT_LIST.equals(reader.getName()
                                        .getLocalPart())) {
                                    reader.nextTag();
                                    return new Pair<>(key,
                                            SpecialNext.LIST_EMPTY);
                                }
                            }
                            reader.require(XMLStreamConstants.START_ELEMENT,
                                    null, ELEMENT_STRUCTURE);
                            reader.nextTag();
                            return new Pair<>(key, SpecialNext.LIST);
                        }
                        default:
                            String key = reader.getAttributeValue(null,
                                    ATTRIBUTE_KEY);
                            if (key == null) {
                                throw new IOException(
                                        "Missing key in tag or invalid element: " +
                                                next);
                            }
                            String value = reader.getAttributeValue(null,
                                    ATTRIBUTE_VALUE);
                            if (value == null) {
                                throw new IOException(
                                        "Missing value in tag or invalid element: " +
                                                next);
                            }
                            Object tag;
                            switch (next) {
                                case ELEMENT_BOOLEAN:
                                    tag = Boolean.valueOf(value);
                                    break;
                                case ELEMENT_BYTE:
                                    tag = Byte.valueOf(value);
                                    break;
                                case ELEMENT_BYTE_ARRAY:
                                    tag = ArrayUtil.fromHexadecimal(value);
                                    break;
                                case ELEMENT_SHORT:
                                    tag = Short.valueOf(value);
                                    break;
                                case ELEMENT_INTEGER:
                                    tag = Integer.valueOf(value);
                                    break;
                                case ELEMENT_LONG:
                                    tag = Long.valueOf(value);
                                    break;
                                case ELEMENT_FLOAT:
                                    tag = Float.valueOf(value);
                                    break;
                                case ELEMENT_DOUBLE:
                                    tag = Double.valueOf(value);
                                    break;
                                case ELEMENT_STRING:
                                    tag = value;
                                    break;
                                default:
                                    throw new IOException(
                                            "Invalid element: " + next);
                            }
                            reader.nextTag();
                            reader.require(XMLStreamConstants.END_ELEMENT, null,
                                    next);
                            reader.nextTag();
                            return new Pair<>(key, tag);
                    }
                }
                case XMLStreamConstants.END_ELEMENT:
                    String next = reader.getName().getLocalPart();
                    switch (next) {
                        case ELEMENT_ROOT:
                            return new Pair<>(null,
                                    SpecialNext.STRUCTURE_TERMINATE);
                        case ELEMENT_STRUCTURE:
                            if (reader.nextTag() ==
                                    XMLStreamConstants.END_ELEMENT) {
                                if (ELEMENT_LIST.equals(reader.getName()
                                        .getLocalPart())) {
                                    reader.nextTag();
                                    return new Pair<>(null,
                                            SpecialNext.LIST_TERMINATE);
                                }
                            }
                            return new Pair<>(null,
                                    SpecialNext.STRUCTURE_TERMINATE);
                        case ELEMENT_LIST:
                            reader.nextTag();
                            return new Pair<>(null, SpecialNext.LIST_TERMINATE);
                        default:
                            throw new IOException(
                                    "Invalid end element type: " + next);
                    }
                default:
                    throw new IOException("Invalid event from XML!");
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void beginStructure() throws IOException {
        try {
            reader.require(XMLStreamConstants.START_ELEMENT, null,
                    ELEMENT_STRUCTURE);
            reader.nextTag();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
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
