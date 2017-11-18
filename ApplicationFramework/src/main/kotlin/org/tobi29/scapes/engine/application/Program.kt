package org.tobi29.scapes.engine.application

typealias StatusCode = Int

interface Program {
    fun execute(args: Array<String>): StatusCode
}
