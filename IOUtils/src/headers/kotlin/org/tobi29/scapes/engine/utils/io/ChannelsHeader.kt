package org.tobi29.scapes.engine.utils.io

/**
 * Skip up to the given amount of bytes in the stream
 * @param length The amount of bytes to skip
 * @throws IOException When an IO error occurs
 * @return The actual amount of bytes skipped
 */
header fun ReadableByteChannel.skip(length: Int): Int
