package org.tobi29.scapes.engine.utils.io

expect interface Channel : Closeable {
    fun isOpen(): Boolean

    /**
     * @throws IOException
     */
    override fun close()
}

expect interface InterruptibleChannel : Channel
