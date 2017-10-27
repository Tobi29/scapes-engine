package org.tobi29.scapes.engine.graphics

open class GraphicsException : RuntimeException {
    constructor() : super()

    constructor(message: String) : super(message)

    constructor(message: String,
                cause: Throwable) : super(message)
}

class RenderCancelException : GraphicsException()
