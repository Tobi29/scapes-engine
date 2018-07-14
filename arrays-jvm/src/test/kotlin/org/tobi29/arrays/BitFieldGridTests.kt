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

package org.tobi29.arrays

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.shouldEqual
import java.util.*

object BitFieldGridTests : Spek({
    describe("a bit field grid") {
        describe("filling an instance filled with random data") {
            val random = Random(0)
            val chunkData = BitFieldGrid(15, 7)
            for (y in 0 until chunkData.height) {
                for (x in 0 until chunkData.width) {
                    for (i in 0..7) {
                        chunkData.setAt(x, y, i, random.nextBoolean())
                    }
                }
            }
            it("should contain the same values afterwards") {
                val random = Random(0)
                for (y in 0 until chunkData.height) {
                    for (x in 0 until chunkData.width) {
                        for (i in 0..7) {
                            chunkData.getAt(x, y, i) shouldEqual
                                    random.nextBoolean()
                        }
                    }
                }
            }
        }
    }
})
