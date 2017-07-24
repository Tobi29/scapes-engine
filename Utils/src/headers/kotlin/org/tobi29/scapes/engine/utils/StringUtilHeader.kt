package org.tobi29.scapes.engine.utils

header fun ByteArray.strUTF8(): String

header fun String.bytesUTF8(): ByteArray

header internal fun CharArray.copyToStringImpl(offset: Int,
                                               length: Int): String

header internal fun String.copyToArrayImpl(destination: CharArray,
                                           offset: Int,
                                           startIndex: Int,
                                           endIndex: Int): CharArray
