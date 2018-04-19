/*
 * Copyright 2012-2018 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.contentinfo

import com.j256.simplemagik.ContentInfoUtil
import com.j256.simplemagik.ContentType
import org.tobi29.io.ReadableByteStream
import org.tobi29.io.view

internal actual fun detectMimeImpl(
    stream: ReadableByteStream?,
    name: String?
): String {
    val array = if (stream != null) {
        val array = ByteArray(ContentInfoUtil.DEFAULT_READ_SIZE)
        val read = stream.getSome(array.view).coerceAtLeast(0)
        array.sliceArray(0 until read)
    } else ByteArray(0)
    val contentInfoUtil = SimpleMagik.contentInfoUtil
    val contentInfo = contentInfoUtil.findMatch(array)
    return (contentInfo
            ?: name?.let { ContentInfoUtil.findExtensionMatch(name) })?.mimeType
            ?: ContentType.OTHER.mimeType
}

private object SimpleMagik {
    val contentInfoUtil = ContentInfoUtil()
}
