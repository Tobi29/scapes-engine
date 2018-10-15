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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.checksums

import org.tobi29.arrays.BytesRO
import org.tobi29.stdex.*

@Suppress("RemoveRedundantCallsOfConversionMethods")
internal class Sha256Context : ChecksumContext {
    private var a = 0
    private var b = 0
    private var c = 0
    private var d = 0
    private var e = 0
    private var f = 0
    private var g = 0
    private var h = 0
    private var i = 0
    private val buffer = ByteArray(64)
    private var bufferIndex = 0
    private val w = IntArray(16)

    init {
        reset()
    }

    override fun update(data: BytesRO) {
        repeat(data.size) {
            buffer[bufferIndex++] = data[it]
            i++
            if (bufferIndex >= 64) flush()
        }
    }

    override fun finish(): ByteArray {
        while (bufferIndex < 64) {
            buffer[bufferIndex++] = 0
        }
        updateWord()
        finishChainSha256(i.toLong() shl 3,
            { a }, { b }, { c }, { d }, { e }, { f }, { g }, { h },
            w, this::update
        )
        val array = toByteArray(a, b, c, d, e, f, g, h)
        reset()
        return array
    }

    private fun flush() {
        updateWord()
        chainSha256({ a }, { b }, { c }, { d }, { e }, { f }, { g }, { h },
            w, this::update
        )
        bufferIndex = 0
    }

    private fun updateWord() {
        assert { bufferIndex == 64 }
        for (i in w.indices) {
            val j = i shl 2
            w[i] = combineToInt(
                buffer[j + 0],
                buffer[j + 1],
                buffer[j + 2],
                buffer[j + 3]
            )
        }
    }

    private fun reset() {
        initChainSha256(this::update)
        i = 0
        bufferIndex = 0
    }

    private fun update(
        a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int
    ) {
        this.a = a
        this.b = b
        this.c = c
        this.d = d
        this.e = e
        this.f = f
        this.g = g
        this.h = h
    }
}

@Suppress("RemoveRedundantCallsOfConversionMethods")
private inline fun <R> initChainSha256(
    output: (Int, Int, Int, Int, Int, Int, Int, Int) -> R
): R = output(
    sha256StartTable[0],
    sha256StartTable[1],
    sha256StartTable[2],
    sha256StartTable[3],
    sha256StartTable[4],
    sha256StartTable[5],
    sha256StartTable[6],
    sha256StartTable[7]
)

private inline fun <R> compressSha256(
    a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int,
    i: Int,
    data: Int,
    output: (Int, Int, Int, Int, Int, Int, Int, Int) -> R
): R {
    val s1 = (e rrot 6) xor (e rrot 11) xor (e rrot 25)
    val ch = (e and f) xor (e.inv() and g)
    val temp1 = h + s1 + ch + sha256ConstantsTable[i] + data
    val s0 = (a rrot 2) xor (a rrot 13) xor (a rrot 22)
    val maj = (a and b) xor (a and c) xor (b and c)
    val temp2 = s0 + maj
    return output(temp1 + temp2, a, b, c, d + temp1, e, f, g)
}

private inline fun finishChainSha256(
    length: Long,
    a: () -> Int, b: () -> Int, c: () -> Int, d: () -> Int,
    e: () -> Int, f: () -> Int, g: () -> Int, h: () -> Int,
    w: IntArray,
    output: (Int, Int, Int, Int, Int, Int, Int, Int) -> Unit
) {
    length.splitToInts { lh, ll ->
        val p = (length and 511).toInt()
        w[p ushr 5] = w[p ushr 5].setAt(31 - (p and 31))
        if (p < 448) {
            w[14] = lh
            w[15] = ll
        }
        chainSha256(a, b, c, d, e, f, g, h, w, output)
        if (p >= 448) {
            for (i in 0 until 14) {
                w[i] = 0
            }
            w[14] = lh
            w[15] = ll
            chainSha256(a, b, c, d, e, f, g, h, w, output)
        }
    }
}

private inline fun chainSha256(
    a: () -> Int, b: () -> Int, c: () -> Int, d: () -> Int,
    e: () -> Int, f: () -> Int, g: () -> Int, h: () -> Int,
    w: IntArray,
    output: (Int, Int, Int, Int, Int, Int, Int, Int) -> Unit
) {
    val ca = a()
    val cb = b()
    val cc = c()
    val cd = d()
    val ce = e()
    val cf = f()
    val cg = g()
    val ch = h()
    repeat(64) { i ->
        val l = if (i < 16) w[i] else {
            val im15 = (i + 1) and 0xF
            val im2 = (i + 14) and 0xF
            val im16 = (i + 0) and 0xF
            val im7 = (i + 9) and 0xF
            val s0 = (w[im15] rrot 7) xor (w[im15] rrot 18) xor (w[im15] ushr 3)
            val s1 = (w[im2] rrot 17) xor (w[im2] rrot 19) xor (w[im2] ushr 10)
            val n = w[im16] + s0 + w[im7] + s1
            w[i and 0xF] = n
            n
        }
        compressSha256(
            a(), b(), c(), d(), e(), f(), g(), h(), i, l, output
        )
    }
    output(
        ca + a(),
        cb + b(),
        cc + c(),
        cd + d(),
        ce + e(),
        cf + f(),
        cg + g(),
        ch + h()
    )
}

// @PublishedApi
// internal
private inline fun toByteArray(
    i7: Int, i6: Int, i5: Int, i4: Int, i3: Int, i2: Int, i1: Int, i0: Int
): ByteArray = ByteArray(32).apply {
    setInt(0, i7)
    setInt(4, i6)
    setInt(8, i5)
    setInt(12, i4)
    setInt(16, i3)
    setInt(20, i2)
    setInt(24, i1)
    setInt(28, i0)
}

// @PublishedApi
// internal
private inline fun ByteArray.setInt(index: Int, value: Int) {
    value.splitToBytes { b3, b2, b1, b0 ->
        this[index + 0] = b3
        this[index + 1] = b2
        this[index + 2] = b1
        this[index + 3] = b0
    }
}

@Suppress("RemoveRedundantCallsOfConversionMethods")
// @PublishedApi
// internal
private val sha256StartTable = intArrayOf(
    0x6a09e667.toInt(),
    0xbb67ae85.toInt(),
    0x3c6ef372.toInt(),
    0xa54ff53a.toInt(),
    0x510e527f.toInt(),
    0x9b05688c.toInt(),
    0x1f83d9ab.toInt(),
    0x5be0cd19.toInt()
)

@Suppress("RemoveRedundantCallsOfConversionMethods")
// @PublishedApi
// internal
private val sha256ConstantsTable = intArrayOf(
    0x428a2f98.toInt(), 0x71374491.toInt(),
    0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
    0x3956c25b.toInt(), 0x59f111f1.toInt(),
    0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
    0xd807aa98.toInt(), 0x12835b01.toInt(),
    0x243185be.toInt(), 0x550c7dc3.toInt(),
    0x72be5d74.toInt(), 0x80deb1fe.toInt(),
    0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
    0xe49b69c1.toInt(), 0xefbe4786.toInt(),
    0x0fc19dc6.toInt(), 0x240ca1cc.toInt(),
    0x2de92c6f.toInt(), 0x4a7484aa.toInt(),
    0x5cb0a9dc.toInt(), 0x76f988da.toInt(),
    0x983e5152.toInt(), 0xa831c66d.toInt(),
    0xb00327c8.toInt(), 0xbf597fc7.toInt(),
    0xc6e00bf3.toInt(), 0xd5a79147.toInt(),
    0x06ca6351.toInt(), 0x14292967.toInt(),
    0x27b70a85.toInt(), 0x2e1b2138.toInt(),
    0x4d2c6dfc.toInt(), 0x53380d13.toInt(),
    0x650a7354.toInt(), 0x766a0abb.toInt(),
    0x81c2c92e.toInt(), 0x92722c85.toInt(),
    0xa2bfe8a1.toInt(), 0xa81a664b.toInt(),
    0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
    0xd192e819.toInt(), 0xd6990624.toInt(),
    0xf40e3585.toInt(), 0x106aa070.toInt(),
    0x19a4c116.toInt(), 0x1e376c08.toInt(),
    0x2748774c.toInt(), 0x34b0bcb5.toInt(),
    0x391c0cb3.toInt(), 0x4ed8aa4a.toInt(),
    0x5b9cca4f.toInt(), 0x682e6ff3.toInt(),
    0x748f82ee.toInt(), 0x78a5636f.toInt(),
    0x84c87814.toInt(), 0x8cc70208.toInt(),
    0x90befffa.toInt(), 0xa4506ceb.toInt(),
    0xbef9a3f7.toInt(), 0xc67178f2.toInt()
)
