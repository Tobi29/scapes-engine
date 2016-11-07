/*
 * Copyright 2012-2016 Tobi29
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

import java.io.IOException

interface TagStructureWriter {
    @Throws(IOException::class)
    fun begin(root: TagStructure)

    @Throws(IOException::class)
    fun end()

    @Throws(IOException::class)
    fun beginStructure()

    @Throws(IOException::class)
    fun beginStructure(key: String)

    @Throws(IOException::class)
    fun endStructure()

    @Throws(IOException::class)
    fun structureEmpty()

    @Throws(IOException::class)
    fun structureEmpty(key: String)

    @Throws(IOException::class)
    fun beginList(key: String)

    @Throws(IOException::class)
    fun beginList()

    @Throws(IOException::class)
    fun beginListStructure()

    @Throws(IOException::class)
    fun endListWithTerminate()

    @Throws(IOException::class)
    fun endListWithEmpty()

    @Throws(IOException::class)
    fun endList()

    @Throws(IOException::class)
    fun listEmpty(key: String)

    @Throws(IOException::class)
    fun listEmpty()

    @Throws(IOException::class)
    fun writePrimitiveTag(key: String,
                          tag: Any)

    @Throws(IOException::class)
    fun writePrimitiveTag(tag: Any)
}
