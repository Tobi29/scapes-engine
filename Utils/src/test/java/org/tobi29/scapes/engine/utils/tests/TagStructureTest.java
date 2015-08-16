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

package org.tobi29.scapes.engine.utils.tests;

import org.junit.Assert;
import org.junit.Test;
import org.tobi29.scapes.engine.utils.io.ByteBufferStream;
import org.tobi29.scapes.engine.utils.io.tag.TagStructure;
import org.tobi29.scapes.engine.utils.io.tag.TagStructureBinary;
import org.tobi29.scapes.engine.utils.io.tag.TagStructureJSON;
import org.tobi29.scapes.engine.utils.io.tag.TagStructureXML;
import org.tobi29.scapes.engine.utils.tests.util.TagStructureTemplate;

import java.io.IOException;

public class TagStructureTest {
    @Test
    public void testUncompressedBinaryFile() throws IOException {
        TagStructure tagStructure = TagStructureTemplate.createTagStructure();
        ByteBufferStream channel = new ByteBufferStream();
        TagStructureBinary.write(tagStructure, channel, (byte) -1);
        channel.buffer().flip();
        TagStructure read = new TagStructure();
        TagStructureBinary.read(read, new ByteBufferStream(channel.buffer()));
        Assert.assertEquals("Read structure doesn't match written one",
                tagStructure, read);
    }

    @Test
    public void testCompressedBinaryFile() throws IOException {
        TagStructure tagStructure = TagStructureTemplate.createTagStructure();
        ByteBufferStream channel = new ByteBufferStream();
        TagStructureBinary.write(tagStructure, channel, (byte) 1);
        channel.buffer().flip();
        TagStructure read = new TagStructure();
        TagStructureBinary.read(read, new ByteBufferStream(channel.buffer()));
        Assert.assertEquals("Read structure doesn't match written one",
                tagStructure, read);
    }

    @Test
    public void testXMLFile() throws IOException {
        TagStructure tagStructure = TagStructureTemplate.createTagStructure();
        ByteBufferStream channel = new ByteBufferStream();
        TagStructureXML.write(tagStructure, channel);
        channel.buffer().flip();
        TagStructure read = new TagStructure();
        TagStructureXML.read(read, new ByteBufferStream(channel.buffer()));
        Assert.assertEquals("Read structure doesn't match written one",
                tagStructure, read);
    }

    @Test
    public void testJSONFile() throws IOException {
        TagStructure tagStructure = TagStructureTemplate.createTagStructure();
        ByteBufferStream channel = new ByteBufferStream();
        TagStructureJSON.write(tagStructure, channel);
        channel.buffer().flip();
        TagStructure read = new TagStructure();
        TagStructureJSON.read(read, new ByteBufferStream(channel.buffer()));
        channel.buffer().clear();
        TagStructureJSON.write(read, channel);
        channel.buffer().flip();
        TagStructure reread = new TagStructure();
        TagStructureJSON.read(reread, new ByteBufferStream(channel.buffer()));
        Assert.assertEquals("Read structure doesn't match written one", read,
                reread);
    }
}
