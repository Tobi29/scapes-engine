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

package org.tobi29.scapes.engine.utils.io.tag

import org.tobi29.scapes.engine.utils.io.*
import org.tobi29.scapes.engine.utils.tag.TagMap
import org.tobi29.scapes.engine.utils.tag.toByteArray
import org.tobi29.scapes.engine.utils.tag.toMap

class TagBundle(private val map: TagMap) {
    fun resolve(path: String): ByteViewRO? {
        val segments = UnixPathEnvironment.run { path.components }
        if (segments.isEmpty()) {
            return null
        }
        var directory = map
        for (i in 0 until segments.lastIndex) {
            directory = directory[segments[i]]?.toMap()
                    ?.toDirectory() ?: return null
        }
        return directory[segments.last()]?.toMap()?.toFile()?.ro
    }

    private fun TagMap.toDirectory(): TagMap? {
        if (this["Type"].toString() != "Directory") {
            return null
        }
        return this["Contents"]?.toMap()
    }

    private fun TagMap.toFile(): ByteView? {
        if (this["Type"].toString() != "File") {
            return null
        }
        return this["Contents"]?.toByteArray()?.view
    }
}
