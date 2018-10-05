/*
 * Copyright 2017, Gray Watson
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.j256.simplemagik.types

import com.j256.simplemagik.entries.MagicFormatter
import com.j256.simplemagik.entries.MagicMatcher
import com.j256.simplemagik.entries.getCompactString
import com.j256.simplemagik.entries.putCompactString
import org.tobi29.arrays.BytesRO
import org.tobi29.io.HeapViewByteBE
import org.tobi29.io.MemoryViewReadableStream
import org.tobi29.io.WritableByteStream

data class UseType(
    val name: String
) : MagicMatcher {
    override fun isMatch(
        bytes: BytesRO,
        required: Boolean
    ): Pair<Int, (Appendable, MagicFormatter) -> Unit>? =
        0 to { sb, formatter ->
            formatter.format(sb, "")
        }
}

@Suppress("UNUSED_PARAMETER")
fun UseType(
    typeStr: String,
    testStr: String?,
    andValue: Long?,
    unsignedType: Boolean
): UseType =
    if (testStr == null) {
        throw IllegalArgumentException("No test string for use")
    } else {
        UseType(testStr)
    }

internal fun UseType.write(stream: WritableByteStream) {
    stream.putCompactString(name)
}

internal fun readUseType(stream: MemoryViewReadableStream<HeapViewByteBE>): UseType {
    val name = stream.getCompactString()
    return UseType(
        name
    )
}
