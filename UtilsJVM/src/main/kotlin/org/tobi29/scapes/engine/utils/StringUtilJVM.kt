package org.tobi29.scapes.engine.utils

impl fun ByteArray.strUTF8() = String(this)

impl fun String.bytesUTF8() = toByteArray()

impl internal fun CharArray.copyToStringImpl(offset: Int,
                                             length: Int) =
        String(this, offset, length)

impl internal fun String.copyToArrayImpl(destination: CharArray,
                                         offset: Int,
                                         startIndex: Int,
                                         endIndex: Int) =
        toCharArray(destination, offset, startIndex, endIndex)
