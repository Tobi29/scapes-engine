/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.io

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.assertions.byteArrays
import org.tobi29.assertions.shouldEqual
import org.tobi29.io.CompressionUtil
import org.tobi29.io.MemoryViewStream
import org.tobi29.io.MemoryViewStreamDefault
import org.tobi29.io.viewBE
import org.tobi29.io.view

object CompressionUtilTests : Spek({
    given("any byte array") {
        val arrays by memoized { byteArrays(32, 8) }
        on("compressing and decompressing") {
            for (array in arrays) {
                val compressed = MemoryViewStreamDefault()
                CompressionUtil.compress(
                        MemoryViewStream(array.viewBE), compressed)
                compressed.flip()
                val decompressed = MemoryViewStreamDefault()
                CompressionUtil.decompress(compressed, decompressed)
                decompressed.flip()
                val check = ByteArray(decompressed.remaining())
                decompressed.get(check.view)
                it("should result with the same array") {
                    check shouldEqual array
                }
            }
        }
    }
})
