package org.tobi29.scapes.engine.utils.io

header interface AutoCloseable {
    /**
     * @throws Exception
     */
    fun close()
}

header interface Closeable : AutoCloseable {
    /**
     * @throws IOException
     */
    override fun close()
}
