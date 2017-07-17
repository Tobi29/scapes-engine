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

package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.BitFieldGrid
import org.tobi29.scapes.engine.utils.getAt
import org.tobi29.scapes.engine.utils.setAt
import java.util.*

object BitFieldGridTests : Spek({
    describe("a bit field grid") {
        given("an instance filled with random data") {
            val random = Random(0)
            val chunkData = BitFieldGrid(15, 7)
            for (y in 0..chunkData.height - 1) {
                for (x in 0..chunkData.width - 1) {
                    for (i in 0..7) {
                        chunkData.setAt(x, y, i, random.nextBoolean())
                    }
                }
            }
            it("should contain the same values") {
                val random = Random(0)
                for (y in 0..chunkData.height - 1) {
                    for (x in 0..chunkData.width - 1) {
                        for (i in 0..7) {
                            chunkData.getAt(x, y,
                                    i) shouldEqual random.nextBoolean()
                        }
                    }
                }
            }
        }
    }
})
