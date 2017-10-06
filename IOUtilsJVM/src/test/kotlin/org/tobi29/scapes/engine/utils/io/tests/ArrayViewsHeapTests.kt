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
                            ::HeapViewByteLE, "little-endian byte arrays"),
                    *testsForType({ ShortArray(it + 1 shr 1) },
                            ::HeapViewShortBE, "big-endian short arrays"),
                    *testsForType({ ShortArray(it + 1 shr 1) },
                            ::HeapViewShortLE, "little-endian short arrays"),
                    *testsForType({ CharArray(it + 1 shr 1) },
                            ::HeapViewCharBE, "big-endian char arrays"),
                    *testsForType({ CharArray(it + 1 shr 1) },
                            ::HeapViewCharLE, "little-endian char arrays"),
                    *testsForType({ IntArray(it + 1 shr 1) },
                            ::HeapViewIntBE, "big-endian int arrays"),
                    *testsForType({ IntArray(it + 1 shr 1) },
                            ::HeapViewIntLE, "little-endian int arrays"),
                    *testsForType({ FloatArray(it + 1 shr 1) },
                            ::HeapViewFloatBE, "big-endian float arrays"),
                    *testsForType({ FloatArray(it + 1 shr 1) },
                            ::HeapViewFloatLE, "little-endian float arrays"),
                    *testsForType({ LongArray(it + 1 shr 1) },
                            ::HeapViewLongBE, "big-endian long arrays"),
                    *testsForType({ LongArray(it + 1 shr 1) },
                            ::HeapViewLongLE, "little-endian long arrays"),
                    *testsForType({ DoubleArray(it + 1 shr 1) },
                            ::HeapViewDoubleBE, "big-endian double arrays"),
                    *testsForType({ DoubleArray(it + 1 shr 1) },
                            ::HeapViewDoubleLE, "little-endian double arrays"))
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
    view.setBytes(131, shortArrayOf(8924, -1232, 21489, 2314, 231).let {
        if (view.isBigEndian) it.viewBE else it.viewLE
    })
    view.setBytes(141, charArrayOf('G', 'r', '8', 'm', '8').let {
        if (view.isBigEndian) it.viewBE else it.viewLE
    })
    view.setBytes(151, intArrayOf(4234532, -923523523).let {
        if (view.isBigEndian) it.viewBE else it.viewLE
    })
    view.setBytes(161, floatArrayOf(1.1f, 2.1f).let {
        if (view.isBigEndian) it.viewBE else it.viewLE
    })
    view.setBytes(171, longArrayOf(37582735798235, -134835892378829).let {
        if (view.isBigEndian) it.viewBE else it.viewLE
    })
    view.setBytes(191, doubleArrayOf(1.1, 2.1).let {
        if (view.isBigEndian) it.viewBE else it.viewLE
    })
    return view
}
