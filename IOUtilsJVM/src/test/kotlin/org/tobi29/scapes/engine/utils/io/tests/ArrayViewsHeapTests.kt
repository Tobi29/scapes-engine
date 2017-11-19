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
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.io.ByteViewE
import org.tobi29.scapes.engine.utils.io.*

object ArrayViewsHeapTests : Spek({
    describe("writing and reading in two different array views") {
        given("two array views of same length and endianess") {
            val tests = listOf(
                    *testsForType({ ByteArray(it) },
                            ::HeapViewByteBE, "big-endian byte arrays"),
                    *testsForType({ ByteArray(it) },
                            ::HeapViewByteLE, "little-endian byte arrays"))
            for ((name, left, right) in tests.map { (supplier, name) ->
                name to supplier(269)
            }.map { (name, views) ->
                Triple(name, views.first, views.second)
            }) {
                on("doing write and read operations on $name") {
                    writeToView(left)
                    writeToView(right)
                    it("should result in two equal views") {
                        for (i in 0..left.size - 1) {
                            left.getByte(i) shouldEqual right.getByte(i)
                        }
                        for (i in 0..left.size - 2) {
                            left.getShort(i) shouldEqual right.getShort(i)
                            left.getChar(i) shouldEqual right.getChar(i)
                        }
                        for (i in 0..left.size - 4) {
                            left.getInt(i) shouldEqual right.getInt(i)
                            left.getFloat(i) shouldEqual right.getFloat(i)
                        }
                        for (i in 0..left.size - 8) {
                            left.getLong(i) shouldEqual right.getLong(i)
                            left.getDouble(i) shouldEqual right.getDouble(i)
                        }
                    }
                }
            }
        }
    }
})

private fun <B> testsForType(buffer: (Int) -> B,
                             supplier: (B, Int, Int) -> ByteViewE,
                             name: String) = arrayOf(
        { length: Int ->
            supplier(buffer(length), 0, length) to
                    supplier(buffer(length), 0, length)
        } to "aligned $name",
        { length: Int ->
            supplier(buffer(length + 1), 1, length) to
                    supplier(buffer(length + 2), 2, length)
        } to "unaligned $name",
        { length: Int ->
            buffer(length shl 2).let {
                supplier(it, 0, length) to
                        supplier(it, length, length)
            }
        } to "shared aligned $name",
        { length: Int ->
            buffer((length shl 2) + 1).let {
                supplier(it, 0, length) to
                        supplier(it, length + 1, length)
            }
        } to "shared unaligned $name",
        { length: Int ->
            supplier(buffer(length), 0, length).let {
                it to if (it.isBigEndian)
                    HeapViewByteBE(ByteArray(length), 0, length)
                else HeapViewByteLE(ByteArray(length), 0, length)
            }
        } to "aligned compare $name to byte array",
        { length: Int ->
            supplier(buffer(length + 1), 1, length).let {
                it to if (it.isBigEndian)
                    HeapViewByteBE(ByteArray(length), 0, length)
                else HeapViewByteLE(ByteArray(length), 0, length)
            }
        } to "unaligned compare $name to byte array")

private fun <B : ByteViewE> writeToView(view: B): B {
    for (i in 0 until 16) {
        view.setByte(i, i.toByte())
    }
    for (i in 16 until 32 step 2) {
        view.setShort(i, (i + 512).toShort())
    }
    for (i in 33 until 49) {
        view.setByte(i, i.toByte())
    }
    for (i in 49 until 65 step 2) {
        view.setShort(i, (i + 512).toShort())
    }
    view.setInt(70, 0x012345678)
    view.setInt(73, 0x012345678) // Intentional overwrite
    view.setLong(80, 0x0123456789ABCDEF)
    view.setLong(85, 0x0123456789ABCDEF) // Intentional overwrite
    view.setChar(90, '?')
    view.setChar(91, '?') // Intentional overwrite
    view.setFloat(94, 1.1f)
    view.setFloat(99, 1.1f)
    view.setDouble(105, 1.1)
    view.setDouble(114, 1.1)
    view.setBytes(121, byteArrayOf(127, -1, -34, 4, 1, 4, 2, 6, 9).let {
        if (view.isBigEndian) it.viewBE else it.viewLE
    })
    return view
}
