package org.tobi29.scapes.engine.utils

expect internal fun ByteArray.utf8ToStringImpl(offset: Int,
                                               size: Int): String

expect internal fun String.utf8ToArrayImpl(destination: ByteArray?,
                                           offset: Int,
                                           size: Int): ByteArray

expect internal fun CharArray.copyToStringImpl(offset: Int,
                                               size: Int): String

expect internal fun String.copyToArrayImpl(destination: CharArray,
                                           offset: Int,
                                           startIndex: Int,
                                           endIndex: Int): CharArray
