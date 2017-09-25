package org.tobi29.scapes.engine.utils

header internal fun ByteArray.utf8ToStringImpl(offset: Int,
                                               size: Int): String

header internal fun String.utf8ToArrayImpl(destination: ByteArray?,
                                           offset: Int,
                                           size: Int): ByteArray

header internal fun CharArray.copyToStringImpl(offset: Int,
                                               size: Int): String

header internal fun String.copyToArrayImpl(destination: CharArray,
                                           offset: Int,
                                           startIndex: Int,
                                           endIndex: Int): CharArray
