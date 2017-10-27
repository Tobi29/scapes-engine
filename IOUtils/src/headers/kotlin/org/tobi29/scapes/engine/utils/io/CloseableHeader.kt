package org.tobi29.scapes.engine.utils.io

expect interface AutoCloseable {
    /**
     * @throws Exception
     */
    fun close()
}

expect interface Closeable : AutoCloseable {
    /**
     * @throws IOException
     */
    override fun close()
}
