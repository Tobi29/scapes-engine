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

package org.tobi29.tilemaps.tiled

import org.tobi29.io.ByteStreamInputStream
import org.tobi29.io.IOException
import org.tobi29.io.Path
import org.tobi29.io.ReadableByteStream
import org.tobi29.stdex.printerrln
import org.tobi29.tilemaps.tiled.internal.readTMXMap
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

suspend fun ReadableByteStream.readTMXMap(
    assetProvider: Path,
    warn: (String) -> Unit = { printerrln(it) }
): TMXMap = ByteStreamInputStream(this).readTMXMap(assetProvider, warn)

suspend fun InputStream.readTMXMap(
    assetProvider: Path,
    warn: (String) -> Unit = { printerrln(it) }
): TMXMap {
    val doc = try {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isIgnoringComments = true
        factory.isIgnoringElementContentWhitespace = true
        factory.isExpandEntityReferences = false
        val builder = factory.newDocumentBuilder()
        builder.parse(InputSource(this).apply {
            encoding = "UTF-8"
        })
    } catch (e: SAXException) {
        throw IOException(e)
    }
    return doc.readTMXMap(assetProvider, warn)
}
