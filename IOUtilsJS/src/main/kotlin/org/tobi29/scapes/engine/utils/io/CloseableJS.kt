package org.tobi29.scapes.engine.utils.io

actual interface AutoCloseable {
    /**
     * @throws Exception
     */
    actual fun close()
}

actual interface Closeable : AutoCloseable {
    /**
     * @throws IOException
     */
    actual override fun close()
}
