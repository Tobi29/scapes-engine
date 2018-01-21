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

package org.tobi29.io.tag

interface TagStructureWriter {
    // TODO: @Throws(IOException::class)
    fun begin(root: TagMap)

    // TODO: @Throws(IOException::class)
    fun end()

    // TODO: @Throws(IOException::class)
    fun beginStructure()

    // TODO: @Throws(IOException::class)
    fun beginStructure(key: String)

    // TODO: @Throws(IOException::class)
    fun endStructure()

    // TODO: @Throws(IOException::class)
    fun structureEmpty()

    // TODO: @Throws(IOException::class)
    fun structureEmpty(key: String)

    // TODO: @Throws(IOException::class)
    fun beginList(key: String)

    // TODO: @Throws(IOException::class)
    fun beginList()

    // TODO: @Throws(IOException::class)
    fun beginListStructure() {
        beginStructure()
    }

    // TODO: @Throws(IOException::class)
    fun endListWithTerminate() {
        endStructure()
        endList()
    }

    // TODO: @Throws(IOException::class)
    fun endListWithEmpty() {
        structureEmpty()
        endList()
    }

    // TODO: @Throws(IOException::class)
    fun endList()

    // TODO: @Throws(IOException::class)
    fun listEmpty(key: String)

    // TODO: @Throws(IOException::class)
    fun listEmpty()

    // TODO: @Throws(IOException::class)
    fun writePrimitiveTag(key: String,
                          tag: TagPrimitive)

    // TODO: @Throws(IOException::class)
    fun writePrimitiveTag(tag: TagPrimitive)
}
