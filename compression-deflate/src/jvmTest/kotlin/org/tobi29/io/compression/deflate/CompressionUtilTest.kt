/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.io.compression.deflate

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.arrays.fromHexadecimal
import org.tobi29.arrays.readAsByteArray
import org.tobi29.arrays.toHexadecimal
import org.tobi29.assertions.byteArrays
import org.tobi29.assertions.data
import org.tobi29.assertions.shouldEqual
import org.tobi29.io.*
import org.tobi29.utils.toArray
import java.util.zip.DeflaterOutputStream

object DeflateTests : Spek({
    describe("deflating and inflating data") {
        data(
            { a -> "compressing \"$a\"" },
            *randomTests
        ) { a, expected ->
            val actual =
                deflate(MemoryViewReadableStream(a.fromHexadecimal().viewBE))
                    .asByteArray().toHexadecimal()
            it("should return \"$expected\"") {
                actual shouldEqual expected
            }
        }
        data(
            { a -> "decompressing \"$a\"" },
            *randomTests.map { (a, b) -> data(b, a) }.toTypedArray()
        ) { a, expected ->
            val actual =
                inflate(MemoryViewReadableStream(a.fromHexadecimal().viewBE))
                    .asByteArray().toHexadecimal()
            it("should return \"$expected\"") {
                actual shouldEqual expected
            }
        }
    }
})

private val randomTests = byteArrays(32, 8).map {
    data(
        it.toHexadecimal(),
        deflateJvm(MemoryViewReadableStream(it.viewBE))
            .asByteArray().toHexadecimal()
    )
}.toArray()

private fun deflateJvm(
    input: ReadableByteStream,
    level: Int = -1
) = MemoryViewStreamDefault().also { stream ->
    DeflaterOutputStream(ByteStreamOutputStream(stream)).use { streamOut ->
        input.process {
            it.readAsByteArray { array, index, offset ->
                streamOut.write(array, index, offset)
            }
        }
    }
    stream.flip()
}.bufferSlice()
