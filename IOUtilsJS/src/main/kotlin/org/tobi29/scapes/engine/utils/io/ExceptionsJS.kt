package org.tobi29.scapes.engine.utils.io

actual open class IOException : Exception {
    actual constructor() : super()
    actual constructor(message: String) : super(message)
    actual constructor(cause: Throwable) : super(cause.toString())
}

actual class ClosedChannelException : IOException()

actual class BufferOverflowException : RuntimeException()
actual class BufferUnderflowException : RuntimeException()
