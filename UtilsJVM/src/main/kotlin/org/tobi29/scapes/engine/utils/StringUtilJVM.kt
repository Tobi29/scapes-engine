package org.tobi29.scapes.engine.utils

actual internal fun ByteArray.utf8ToStringImpl(offset: Int,
                                             size: Int) =
        String(this, offset, size)

actual internal fun String.utf8ToArrayImpl(destination: ByteArray?,
                                         offset: Int,
                                         size: Int) =
        toByteArray().let {
            if (destination == null && offset == 0
                    && (size < 0 || size == it.size)) it
            else {
                val array = destination ?: ByteArray(offset +
                        if (size < 0) it.size else size)
                copy(it, array, size.coerceAtMost(it.size))
                array
            }
        }

actual internal fun CharArray.copyToStringImpl(offset: Int,
                                             size: Int) =
        String(this, offset, size)

actual internal fun String.copyToArrayImpl(destination: CharArray,
                                         offset: Int,
                                         startIndex: Int,
                                         endIndex: Int) =
        toCharArray(destination, offset, startIndex, endIndex)
