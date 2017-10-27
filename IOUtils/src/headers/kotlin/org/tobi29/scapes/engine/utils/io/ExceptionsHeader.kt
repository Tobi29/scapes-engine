package org.tobi29.scapes.engine.utils.io

expect open class IOException() : Exception {
    constructor(message: String)
    constructor(cause: Throwable)
}

expect class ClosedChannelException() : IOException

expect class BufferOverflowException() : RuntimeException
expect class BufferUnderflowException() : RuntimeException
