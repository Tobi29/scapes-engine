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
import java.io.InputStream;
import java.io.OutputStream;

public class TagStructureJSON {
    public static void write(TagStructure tagStructure, OutputStream streamOut)
            throws IOException {
        tagStructure.write(new TagStructureWriterJSON(streamOut));
    }

    public static TagStructure read(InputStream streamIn) throws IOException {
        return read(new TagStructure(), streamIn);
    }

    public static TagStructure read(TagStructure tagStructure,
            InputStream streamIn) throws IOException {
        tagStructure.read(new TagStructureReaderJSON(streamIn));
        return tagStructure;
    }

    public static void write(TagStructure tagStructure,
            WritableByteStream stream) throws IOException {
        tagStructure.write(new TagStructureWriterJSON(stream));
    }

    public static TagStructure read(ReadableByteStream stream)
            throws IOException {
        return read(new TagStructure(), stream);
    }

    public static TagStructure read(TagStructure tagStructure,
            ReadableByteStream stream) throws IOException {
        tagStructure.read(new TagStructureReaderJSON(stream));
        return tagStructure;
    }
}
