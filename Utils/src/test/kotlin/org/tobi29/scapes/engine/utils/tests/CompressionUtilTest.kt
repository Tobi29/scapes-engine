/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils.tests

import org.junit.Assert
import org.junit.Test
import org.tobi29.scapes.engine.utils.io.ByteBufferStream
import org.tobi29.scapes.engine.utils.io.CompressionUtil
import org.tobi29.scapes.engine.utils.tests.util.RandomInput

import java.io.IOException
import java.nio.ByteBuffer

class CompressionUtilTest {
    @Test
    @Throws(IOException::class)
    fun testCompressArray() {
        for (array in RandomInput.createRandomArrays(32, 8)) {
            val compressed = ByteBufferStream()
            CompressionUtil.compress(ByteBufferStream(ByteBuffer.wrap(array)),
                    compressed)
            compressed.buffer().flip()
            val decompressed = ByteBufferStream()
            CompressionUtil.decompress(ByteBufferStream(compressed.buffer()),
                    decompressed)
            decompressed.buffer().flip()
            val check = ByteArray(decompressed.buffer().remaining())
            decompressed.buffer().get(check)
            Assert.assertArrayEquals("Data not equal after decompression",
                    array, check)
        }
    }
}
