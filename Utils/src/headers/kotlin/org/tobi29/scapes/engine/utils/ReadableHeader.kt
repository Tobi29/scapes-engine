package org.tobi29.scapes.engine.utils

header interface ReadablePlatform {
    fun read(): Char

    fun read(array: CharArray,
             offset: Int,
             size: Int)
}
