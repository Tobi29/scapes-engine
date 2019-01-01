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

package org.tobi29.io.tag.binary

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.shouldEqual
import org.tobi29.assertions.suites.createTagMap
import org.tobi29.io.MemoryViewStreamDefault
import org.tobi29.io.tag.TagMap
import org.tobi29.io.tag.toTag

private fun checkWriteAndRead(
    map: TagMap,
    compression: Byte = -1
): TagMap {
    val channel = MemoryViewStreamDefault()
    map.writeBinary(channel, compression)
    channel.flip()
    return readBinary(channel)
}

object TagStructureBinaryTests : Spek({
    describe("serialization for tag structures") {
        describe("a normal tag structure") {
            val tagMapComplex by memoized { createTagMap() }
            describe("writing and reading, uncompressed") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(
                    tagMap,
                    -1
                )
                it("should return an equal tag structure") {
                    read shouldEqual tagMap
                }
            }
            describe("writing and reading, compressed") {
                val tagMap = tagMapComplex
                val read = checkWriteAndRead(tagMap, 1)
                it("should return an equal tag structure") {
                    read shouldEqual tagMap
                }
            }
        }
        describe("a tag structure with more than 255 different keys") {
            val tagMapManyKeys by memoized {
                TagMap {
                    repeat(512) { this["Entry#$it"] = it.toTag() }
                }
            }
            describe("writing and reading, uncompressed") {
                val tagMap = tagMapManyKeys
                val read = checkWriteAndRead(
                    tagMap,
                    -1
                )
                it("should return an equal tag structure") {
                    read shouldEqual tagMap
                }
            }
        }
    }
})
