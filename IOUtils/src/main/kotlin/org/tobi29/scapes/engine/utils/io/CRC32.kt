package org.tobi29.scapes.engine.utils.io

@Suppress("NOTHING_TO_INLINE")
inline fun initChainCRC32(crc: Int = 0): Int =
        crc xor -1

@Suppress("NOTHING_TO_INLINE")
inline fun chainCRC32(crc: Int,
                      data: Byte,
                      table: IntArray): Int =
        table[(crc xor data.toInt()) and 0xFF] xor (crc ushr 8)

@Suppress("NOTHING_TO_INLINE")
inline fun Int.finishChainCRC32(): Int = initChainCRC32(this)

@Suppress("NOTHING_TO_INLINE")
inline fun nextCRC32(crc: Int,
                     data: Byte,
                     table: IntArray): Int =
        initChainCRC32(crc).let {
            chainCRC32(it, data, table)
        }.finishChainCRC32()

@Suppress("NOTHING_TO_INLINE")
inline fun computeCRC32(buffer: ByteBuffer,
                        table: IntArray): Int =
        computeCRC32(0, buffer, table)

@Suppress("NOTHING_TO_INLINE")
inline fun computeCRC32(crc: Int,
                        buffer: ByteBuffer,
                        table: IntArray): Int =
        initChainCRC32(crc).let {
            computeChainCRC32(it, buffer, table)
        }.finishChainCRC32()

@Suppress("NOTHING_TO_INLINE")
inline fun computeChainCRC32(buffer: ByteBuffer,
                             table: IntArray): Int =
        computeCRC32(0, buffer, table)

fun computeChainCRC32(crc: Int,
                      buffer: ByteBuffer,
                      table: IntArray): Int {
    var cc = crc
    while (buffer.hasRemaining()) {
        cc = chainCRC32(cc, buffer.get(), table)
    }
    return cc
}

fun tableCRC32(key: Int): IntArray = IntArray(256) { i ->
    var crc = i
    repeat(8) {
        if (crc and 1 != 0) {
            crc = key xor (crc ushr 1)
        } else {
            crc = crc ushr 1
        }
    }
    crc
}