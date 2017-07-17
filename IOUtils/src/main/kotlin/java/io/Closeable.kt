package java.io

import java.lang.AutoCloseable

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface Closeable : AutoCloseable {
    /**
     * @throws IOException
     */
    override fun close()
}
