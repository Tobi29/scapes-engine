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

package org.tobi29.scapes.engine.utils.io.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.byteArrays
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.io.view

object BufferedStreamTests : Spek({
    for (size in (0..15).map { 1 shl 16 }) {
        on("writing to and reading from a buffered stream with buffer size $size") {
            val buffer = MemoryViewStreamDefault()
            val arrays = byteArrays(16, 8)
            buffer.reset()
            BufferedWriteChannelStream(
                    WritableByteStreamChannel(buffer)).apply {
                for (array in arrays) {
                    put(array.view)
                }
                put(123)
                putShort(1234)
                putInt(12345678)
                putLong(123456789101112L)
                flush()
            }
            buffer.flip()
            BufferedReadChannelStream(
                    ReadableByteStreamChannel(buffer)).apply {
                for (array in arrays) {
                    val check = ByteArray(array.size)
                    get(check.view)
                    it("should equal to original array") {
                        check shouldEqual array
                    }
                }
                val getByte = get()
                val getShort = getShort()
                val getInt = getInt()
                val getLong = getLong()
                it("should equal the original byte") {
                    getByte shouldEqual 123.toByte()
                }
                it("should equal the original short") {
                    getShort shouldEqual 1234.toShort()
                }
                it("should equal the original int") {
                    getInt shouldEqual 12345678
                }
                it("should equal the original long") {
                    getLong shouldEqual 123456789101112L
                }
            }
        }
    }
})

