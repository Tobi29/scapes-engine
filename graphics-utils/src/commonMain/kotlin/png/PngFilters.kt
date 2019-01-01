/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.graphics.png

import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import kotlin.math.abs

internal fun pngFilter(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int,
    method: Byte, type: Byte
) = when (method) {
    0.toByte() -> when (type) {
        0.toByte() -> pngFilterNone(lastScanline, scanline, distance)
        1.toByte() -> pngFilterSub(lastScanline, scanline, distance)
        2.toByte() -> pngFilterUp(lastScanline, scanline, distance)
        3.toByte() -> pngFilterAverage(lastScanline, scanline, distance)
        4.toByte() -> pngFilterPaeth(lastScanline, scanline, distance)
        else -> throw IllegalArgumentException("Invalid filter type for method 0: $type")
    }
    else -> throw IllegalArgumentException("Invalid filter method: $method")
}

internal fun pngUnfilter(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int,
    method: Byte, type: Byte
) = when (method) {
    0.toByte() -> when (type) {
        0.toByte() -> pngUnfilterNone(lastScanline, scanline, distance)
        1.toByte() -> pngUnfilterSub(lastScanline, scanline, distance)
        2.toByte() -> pngUnfilterUp(lastScanline, scanline, distance)
        3.toByte() -> pngUnfilterAverage(lastScanline, scanline, distance)
        4.toByte() -> pngUnfilterPaeth(lastScanline, scanline, distance)
        else -> throw IllegalArgumentException("Invalid filter type for method 0: $type")
    }
    else -> throw IllegalArgumentException("Invalid filter method: $method")
}

internal inline fun pngFilterNone(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
}

internal inline fun pngUnfilterNone(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
}

internal fun pngFilterSub(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
    for (i in scanline.size - 1 downTo distance) {
        scanline[i] = (scanline[i] - scanline[i - distance]).toByte()
    }
}

internal fun pngUnfilterSub(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
    for (i in distance until scanline.size) {
        scanline[i] = (scanline[i] + scanline[i - distance]).toByte()
    }
}

internal fun pngFilterUp(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
    if (lastScanline == null) return
    for (i in scanline.size - 1 downTo 0) {
        scanline[i] = (scanline[i] - lastScanline[i]).toByte()
    }
}

internal fun pngUnfilterUp(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
    if (lastScanline == null) return
    for (i in 0 until scanline.size) {
        scanline[i] = (scanline[i] + lastScanline[i]).toByte()
    }
}

internal fun pngFilterAverage(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
    if (lastScanline == null) {
        for (i in scanline.size - 1 downTo distance) {
            scanline[i] = (scanline[i] - floorCombine(
                scanline[i - distance], 0
            )).toByte()
        }
        for (i in distance - 1 downTo 0) {
            scanline[i] = (scanline[i] - floorCombine(
                0, 0
            )).toByte()
        }
    } else {
        for (i in scanline.size - 1 downTo distance) {
            scanline[i] = (scanline[i] - floorCombine(
                scanline[i - distance], lastScanline[i]
            )).toByte()
        }
        for (i in distance - 1 downTo 0) {
            scanline[i] = (scanline[i] - floorCombine(
                0, lastScanline[i]
            )).toByte()
        }
    }
}

internal fun pngUnfilterAverage(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
    if (lastScanline == null) {
        for (i in 0 until distance) {
            scanline[i] = (scanline[i] + floorCombine(
                0, 0
            )).toByte()
        }
        for (i in distance until scanline.size) {
            scanline[i] = (scanline[i] + floorCombine(
                scanline[i - distance], 0
            )).toByte()
        }
    } else {
        for (i in 0 until distance) {
            scanline[i] = (scanline[i] + floorCombine(
                0, lastScanline[i]
            )).toByte()
        }
        for (i in distance until scanline.size) {
            scanline[i] = (scanline[i] + floorCombine(
                scanline[i - distance], lastScanline[i]
            )).toByte()
        }
    }
}

internal fun pngFilterPaeth(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
    if (lastScanline == null) {
        for (i in scanline.size - 1 downTo distance) {
            scanline[i] = (scanline[i] - paethPredictor(
                scanline[i - distance], 0,
                0
            )).toByte()
        }
        for (i in distance - 1 downTo 0) {
            scanline[i] = (scanline[i] - paethPredictor(
                0, 0,
                0
            )).toByte()
        }
    } else {
        for (i in scanline.size - 1 downTo distance) {
            scanline[i] = (scanline[i] - paethPredictor(
                scanline[i - distance], lastScanline[i],
                lastScanline[i - distance]
            )).toByte()
        }
        for (i in distance - 1 downTo 0) {
            scanline[i] = (scanline[i] - paethPredictor(
                0, lastScanline[i],
                0
            )).toByte()
        }
    }
}

internal fun pngUnfilterPaeth(
    lastScanline: BytesRO?,
    scanline: Bytes,
    distance: Int
) {
    if (lastScanline == null) {
        for (i in 0 until distance) {
            scanline[i] = (scanline[i] + paethPredictor(
                0, 0,
                0
            )).toByte()
        }
        for (i in distance until scanline.size) {
            scanline[i] = (scanline[i] + paethPredictor(
                scanline[i - distance], 0,
                0
            )).toByte()
        }
    } else {
        for (i in 0 until distance) {
            scanline[i] = (scanline[i] + paethPredictor(
                0, lastScanline[i],
                0
            )).toByte()
        }
        for (i in distance until scanline.size) {
            scanline[i] = (scanline[i] + paethPredictor(
                scanline[i - distance], lastScanline[i],
                lastScanline[i - distance]
            )).toByte()
        }
    }
}

private inline fun floorCombine(a: Byte, b: Byte): Byte =
    (((a.toInt() and 0xFF) + (b.toInt() and 0xFF)) ushr 1).toByte()

private inline fun paethPredictor(a: Byte, b: Byte, c: Byte): Byte {
    val ai = a.toInt() and 0xFF
    val bi = b.toInt() and 0xFF
    val ci = c.toInt() and 0xFF
    val p = ai + bi - ci
    val pa = abs(p - ai)
    val pb = abs(p - bi)
    val pc = abs(p - ci)
    return if (pa <= pb && pa <= pc) a
    else if (pb <= pc) b
    else c
}
