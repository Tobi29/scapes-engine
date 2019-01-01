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

package org.tobi29.io.tag

interface TagStructureWriter {
    fun begin(root: TagMap)

    fun end()

    fun beginStructure(key: String)

    fun beginStructure()

    fun endStructure()

    fun structureEmpty(key: String) {
        beginStructure(key)
        endStructure()
    }

    fun structureEmpty() {
        beginStructure()
        endStructure()
    }

    fun beginList(key: String)

    fun beginList()

    fun beginListStructure() {
        beginStructure()
    }

    fun endListWithTerminate() {
        endStructure()
        endList()
    }

    fun endListWithEmpty() {
        structureEmpty()
        endList()
    }

    fun endList()

    fun listEmpty(key: String) {
        beginList(key)
        endList()
    }

    fun listEmpty() {
        beginList()
        endList()
    }

    fun writePrimitiveTag(key: String, tag: TagPrimitive)

    fun writePrimitiveTag(tag: TagPrimitive)
}
