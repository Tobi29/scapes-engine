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

package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.byteArrays
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.fromHexadecimal
import org.tobi29.scapes.engine.utils.join
import org.tobi29.scapes.engine.utils.toHexadecimal
import java.util.*

object ArrayUtilTests : Spek({
    describe("join") {
        given("any byte array") {
            val arrays by memoized { byteArrays() }
            on("an array with default arguments and wrapped with []") {
                for (array in arrays) {
                    val joined = "[${join(*array)}]"
                    val arrayStr = Arrays.toString(array)
                    it("should equal Arrays.toString") {
                        joined shouldEqual arrayStr
                    }
                }
            }
        }
    }
    describe("toHexadecimal and fromHexadecimal") {
        given("any byte array") {
            val arrays by memoized { byteArrays() }
            on("encoding, decoding and encoding again") {
                for (array in arrays) {
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
                on("encoding, decoding and encoding again, grouped by $group") {
                    for (array in arrays) {
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
        }
        on("decoding and encoding, grouped by 1") {
            val hex = "ff 0f 00 f0 ff"
            val bytes = hex.fromHexadecimal()
            val hex2 = bytes.toHexadecimal(1)
            it("should reproduce encoded strings") {
                hex2 shouldEqual hex
            }
        }
    }
})
