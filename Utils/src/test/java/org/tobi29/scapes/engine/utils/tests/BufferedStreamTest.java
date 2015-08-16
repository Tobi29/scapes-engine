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
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.io.BufferedReadChannelStream;
import org.tobi29.scapes.engine.utils.io.BufferedWriteChannelStream;
import org.tobi29.scapes.engine.utils.io.ByteBufferChannel;
import org.tobi29.scapes.engine.utils.tests.util.RandomInput;

import java.io.IOException;

public class BufferedStreamTest {
    @Test
    public void testWriteRead() throws IOException {
        ByteBufferChannel channel =
                new ByteBufferChannel(BufferCreator.bytes(1 << 16));
        byte[][] arrays = RandomInput.createRandomArrays(16, 8);
        for (int size = 0; size < 16; size++) {
            channel.buffer().clear();
            write(new BufferedWriteChannelStream(channel), arrays);
            channel.buffer().flip();
            read(new BufferedReadChannelStream(channel), arrays);
        }
    }

    private void write(BufferedWriteChannelStream stream, byte[][] arrays)
            throws IOException {
        for (byte[] array : arrays) {
            stream.put(array);
        }
        stream.put(123);
        stream.putShort(1234);
        stream.putInt(12345678);
        stream.putLong(123456789101112L);
        stream.flush();
    }

    private void read(BufferedReadChannelStream stream, byte[][] arrays)
            throws IOException {
        for (byte[] array : arrays) {
            byte[] check = new byte[array.length];
            stream.get(check);
            Assert.assertArrayEquals("Arrays did not match", array, check);
        }
        Assert.assertEquals("Byte did not match", 123, stream.get());
        Assert.assertEquals("Short did not match", 1234, stream.getShort());
        Assert.assertEquals("Integer did not match", 12345678, stream.getInt());
        Assert.assertEquals("Long did not match", 123456789101112L,
                stream.getLong());
    }
}
