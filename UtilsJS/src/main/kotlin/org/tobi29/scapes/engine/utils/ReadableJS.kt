package org.tobi29.scapes.engine.utils

impl interface ReadablePlatform {
    impl fun read(): Char

    impl fun read(array: CharArray,
                  offset: Int,
                  size: Int)
}
