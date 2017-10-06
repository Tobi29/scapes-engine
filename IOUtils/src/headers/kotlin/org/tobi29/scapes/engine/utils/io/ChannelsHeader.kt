package org.tobi29.scapes.engine.utils.io

header interface Channel : Closeable {
    fun isOpen(): Boolean

    /**
     * @throws IOException
     */
    override fun close()
}

header interface InterruptibleChannel : Channel
