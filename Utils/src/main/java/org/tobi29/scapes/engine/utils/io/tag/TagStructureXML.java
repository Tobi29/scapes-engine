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

import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import java.io.IOException;

public class TagStructureXML {
    protected static final String VERSION = "1.0.0";
    protected static final String ELEMENT_ROOT = "ScapesTag";
    protected static final String ELEMENT_STRUCTURE = "Structure";
    protected static final String ELEMENT_LIST = "List";
    protected static final String ELEMENT_BOOLEAN = "Boolean";
    protected static final String ELEMENT_BYTE = "Byte";
    protected static final String ELEMENT_BYTE_ARRAY = "ByteArray";
    protected static final String ELEMENT_SHORT = "Int16";
    protected static final String ELEMENT_INTEGER = "Int32";
    protected static final String ELEMENT_LONG = "Int64";
    protected static final String ELEMENT_FLOAT = "Float32";
    protected static final String ELEMENT_DOUBLE = "Float64";
    protected static final String ELEMENT_STRING = "String";
    protected static final String ATTRIBUTE_KEY = "key";
    protected static final String ATTRIBUTE_VALUE = "value";
    protected static final String ATTRIBUTE_VERSION = "version";

    public static void write(TagStructure tagStructure,
            WritableByteStream stream) throws IOException {
        tagStructure.write(new TagStructureWriterXML(stream));
    }

    public static TagStructure read(ReadableByteStream stream)
            throws IOException {
        return read(new TagStructure(), stream);
    }

    public static TagStructure read(TagStructure tagStructure,
            ReadableByteStream stream) throws IOException {
        tagStructure.read(new TagStructureReaderXML(stream));
        return tagStructure;
    }
}
