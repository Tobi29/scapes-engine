/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.utils.io

import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.tobi29.scapes.engine.utils.ThreadLocal
import java.io.InputStream

actual internal fun detectMimeImpl(stream: ReadableByteStream?,
                                 name: String?) =
        detectMimeIO(stream?.let { ByteStreamInputStream(it) }, name)

/**
 * Detect the mime type of the given resource
 *
 * **Note:** The exact result may vary from platform to platform
 * @param streamIn The input stream to read from or `null`
 * @param name The name of the resource or `null`
 * @return a mime-type string
 */
fun detectMimeIO(streamIn: InputStream? = null,
                 name: String? = null): String {
    val metadata = Metadata()
    name?.let { metadata.set(Metadata.RESOURCE_NAME_KEY, it) }
    return tika.get().detect(streamIn, name)
}

private val tika = ThreadLocal { Tika() }
