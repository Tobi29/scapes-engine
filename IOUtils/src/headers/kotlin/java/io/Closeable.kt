package java.io

import java.lang.AutoCloseable

@Suppress("HEADER_WITHOUT_IMPLEMENTATION", "NO_ACTUAL_FOR_EXPECT")
header interface Closeable : AutoCloseable {
    /**
     * @throws IOException
     */
    override fun close()
}
