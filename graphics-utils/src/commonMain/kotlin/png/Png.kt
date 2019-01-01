/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.graphics.png

import org.tobi29.checksums.tableCrc32

internal val PNG_HEADER = byteArrayOf(
    0x89.toByte(), 0x50.toByte(),
    0x4E.toByte(), 0x47.toByte(), 0x0D.toByte(), 0x0A.toByte(),
    0x1A.toByte(), 0x0A.toByte()
)
internal inline val TYPE_IHDR get() = 0x49484452
internal inline val TYPE_PLTE get() = 0x504c5445
internal inline val TYPE_IDAT get() = 0x49444154
internal inline val TYPE_IEND get() = 0x49454e44

internal val zlibTable = tableCrc32(0xedb88320.toInt())
