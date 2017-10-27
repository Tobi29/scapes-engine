package org.tobi29.scapes.engine.utils.io

header open class IOException : Exception {
    constructor()
    constructor(message: String)
    constructor(cause: Throwable)
}

header class ClosedChannelException : IOException

header class BufferOverflowException : RuntimeException
header class BufferUnderflowException : RuntimeException
