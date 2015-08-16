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
import org.tobi29.scapes.engine.utils.io.ByteStreamOutputStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class TagStructureWriterXML extends TagStructureXML
        implements TagStructureWriter {
    private static final XMLOutputFactory FACTORY =
            XMLOutputFactory.newInstance();
    private static final TransformerFactory TRANSFORMER_FACTORY =
            TransformerFactory.newInstance();

    static {
        TRANSFORMER_FACTORY.setAttribute("indent-number", 4);
    }

    private final WritableByteStream stream;
    private final XMLStreamWriter writer;
    private final StringWriter stringWriter;

    public TagStructureWriterXML(WritableByteStream stream) throws IOException {
        try {
            this.stream = stream;
            stringWriter = new StringWriter();
            writer = FACTORY.createXMLStreamWriter(stringWriter);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void begin(TagStructure root) throws IOException {
        try {
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement(ELEMENT_ROOT);
            writer.writeAttribute(ATTRIBUTE_VERSION, VERSION);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void end() throws IOException {
        try {
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
            Source source =
                    new StreamSource(new StringReader(stringWriter.toString()));
            StreamResult result = new StreamResult(
                    new OutputStreamWriter(new ByteStreamOutputStream(stream),
                            StandardCharsets.UTF_8));
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (XMLStreamException | TransformerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void beginStructure() throws IOException {
        try {
            writer.writeStartElement(ELEMENT_STRUCTURE);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void beginStructure(String key) throws IOException {
        try {
            writer.writeStartElement(ELEMENT_STRUCTURE);
            writer.writeAttribute(ATTRIBUTE_KEY, key);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endStructure() throws IOException {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void structureEmpty() throws IOException {
        try {
            writer.writeEmptyElement(ELEMENT_STRUCTURE);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void structureEmpty(String key) throws IOException {
        try {
            writer.writeEmptyElement(ELEMENT_STRUCTURE);
            writer.writeAttribute(ATTRIBUTE_KEY, key);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void beginList(String key) throws IOException {
        try {
            writer.writeStartElement(ELEMENT_LIST);
            writer.writeAttribute(ATTRIBUTE_KEY, key);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endListWidthTerminate() throws IOException {
        try {
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void endListWithEmpty() throws IOException {
        try {
            writer.writeEmptyElement(ELEMENT_STRUCTURE);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void listEmpty(String key) throws IOException {
        try {
            writer.writeEmptyElement(ELEMENT_LIST);
            writer.writeAttribute(ATTRIBUTE_KEY, key);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeTag(String key, Object tag) throws IOException {
        try {
            String type;
            String value;
            if (tag instanceof Boolean) {
                type = ELEMENT_BOOLEAN;
                value = tag.toString();
            } else if (tag instanceof Byte) {
                type = ELEMENT_BYTE;
                value = tag.toString();
            } else if (tag instanceof byte[]) {
                type = ELEMENT_BYTE_ARRAY;
                value = ArrayUtil.toHexadecimal((byte[]) tag);
            } else if (tag instanceof Short) {
                type = ELEMENT_SHORT;
                value = tag.toString();
            } else if (tag instanceof Integer) {
                type = ELEMENT_INTEGER;
                value = tag.toString();
            } else if (tag instanceof Long) {
                type = ELEMENT_LONG;
                value = tag.toString();
            } else if (tag instanceof Float) {
                type = ELEMENT_FLOAT;
                value = tag.toString();
            } else if (tag instanceof Double) {
                type = ELEMENT_DOUBLE;
                value = tag.toString();
            } else if (tag instanceof String) {
                type = ELEMENT_STRING;
                value = (String) tag;
            } else {
                throw new IOException("Invalid type: " + tag.getClass());
            }
            writer.writeEmptyElement(type);
            writer.writeAttribute(ATTRIBUTE_KEY, key);
            writer.writeAttribute(ATTRIBUTE_VALUE, value);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
}
