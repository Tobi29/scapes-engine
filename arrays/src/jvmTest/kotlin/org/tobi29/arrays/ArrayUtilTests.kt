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
import org.tobi29.assertions.byteArrays
import org.tobi29.assertions.shouldEqual

object ArrayUtilTests : Spek({
    describe("toHexadecimal and fromHexadecimal") {
        describe("encoding, decoding and encoding again") {
            for (array in byteArrays()) {
                val hex = array.toHexadecimal()
                val bytes = hex.fromHexadecimal()
                val hex2 = bytes.toHexadecimal()
                it("should reproduce arrays") {
                    bytes shouldEqual array
                }
                it("should reproduce encoded strings") {
                    hex2 shouldEqual hex
                }
            }
        }
        for (group in 1..15) {
            describe("encoding, decoding and encoding again, grouped by $group") {
                for (array in byteArrays()) {
                    val hex = array.toHexadecimal()
                    val bytes = hex.fromHexadecimal()
                    val hex2 = bytes.toHexadecimal()
                    it("should reproduce arrays") {
                        bytes shouldEqual array
                    }
                    it("should reproduce encoded strings") {
                        hex2 shouldEqual hex
                    }
                }
            }
        }
        describe("decoding and encoding, grouped by 1") {
            val hex = "ff 0f 00 f0 ff"
            val bytes = hex.fromHexadecimal()
            val hex2 = bytes.toHexadecimal(1)
            it("should reproduce encoded strings") {
                hex2 shouldEqual hex
            }
        }
    }
})
