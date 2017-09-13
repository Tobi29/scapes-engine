package org.tobi29.scapes.engine.utils.io

/**
 * Skip up to the given amount of bytes in the stream
 * @param length The amount of bytes to skip
 * @throws IOException When an IO error occurs
 * @return The actual amount of bytes skipped
 */
header fun ReadableByteChannel.skip(length: Int): Int

/**
 * Skip up to the given amount of bytes in the stream
 * @param length The amount of bytes to skip
 * @throws IOException When an IO error occurs
 * @return The actual amount of bytes skipped
 */
fun ReadableByteChannel.skip(length: Long): Long {
    var l = length
    var skipped = 0L
    while (l > Int.MAX_VALUE) {
        val s = skip(Int.MAX_VALUE)
        skipped += s
        if (s < Int.MAX_VALUE) return skipped
        l -= Int.MAX_VALUE
    }
    skipped += skip(l.toInt())
    return skipped
}
