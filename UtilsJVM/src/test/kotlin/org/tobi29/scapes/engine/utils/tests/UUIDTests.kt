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
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.UUID
import org.tobi29.scapes.engine.utils.toUUID

object UUIDTests : Spek({
    describe("converting a binary uuid to a string") {
        given("a binary uuid") {
            val tests = listOf(
                    Pair(0L, 0L) to
                            "00000000-0000-0000-0000-000000000000",
                    Pair(-1L, -1L) to
                            "ffffffff-ffff-ffff-ffff-ffffffffffff",
                    Pair(5650701559700802061L, -5249906936225336612L) to
                            "4e6b5321-3db6-4a0d-b724-9574ea6df2dc",
                    Pair(5440586950734991076L, -5145713377773407038L) to
                            "4b80d901-982f-4ee4-b896-c0f231d734c2",
                    Pair(7953353969410133574L, -5890048876739488405L) to
                            "6e5ffd4b-e7a2-4e46-ae42-57c0e041156b",
                    Pair(-4214925361858917762L, -7600080116318795460L) to
                            "c58193b7-0b9d-4a7e-9687-1766b24f093c",
                    Pair(-5922784680015019990L, -8862217414733433550L) to
                            "adce0ab6-ba94-402a-8503-143720887532",
                    Pair(5995180038047548198L, -4771318480610000415L) to
                            "5333287b-1456-4326-bdc8-df1f14fc01e1",
                    Pair(-7071325407853719683L, -8433848961981573533L) to
                            "9ddd9b01-cdaf-4f7d-8af4-f30db3ca9663",
                    Pair(5610600356221961524L, -5121457888204384947L) to
                            "4ddcdb4c-394a-4534-b8ec-ed2f7174954d")
            for ((bits, expected) in tests) {
                on("converting ${bits.first}, ${bits.second} to a string") {
                    val actual = UUID(bits.first, bits.second).toString()
                    it("should return $expected") {
                        actual shouldEqual expected
                    }
                }
            }
        }
    }
    describe("converting a string uuid to binary") {
        given("a binary uuid") {
            val tests = listOf(
                    "00000000-0000-0000-0000-000000000000" to
                            Pair(0L, 0L),
                    "ffffffff-ffff-ffff-ffff-ffffffffffff" to
                            Pair(-1L, -1L),
                    "0-0-0-0-0" to
                            Pair(0L, 0L),
                    "f-f-f-f-f" to
                            Pair(0x0000000F000F000FL, 0x000F00000000000FL),
                    // FIXME: This is bugged on OpenJDK (and presumable Oracle as well) before 1.9
                    // "0-fffff-0-0-0" to
                    //         Pair(0x00000000FFFF0000L, 0L),
                    "0-0-0-fffff-0" to
                            Pair(0L, -0x000000000000L),
                    "4e6b5321-3db6-4a0d-b724-9574ea6df2dc" to
                            Pair(5650701559700802061L, -5249906936225336612L),
                    "4b80d901-982f-4ee4-b896-c0f231d734c2" to
                            Pair(5440586950734991076L, -5145713377773407038L),
                    "6e5ffd4b-e7a2-4e46-ae42-57c0e041156b" to
                            Pair(7953353969410133574L, -5890048876739488405L),
                    "c58193b7-0b9d-4a7e-9687-1766b24f093c" to
                            Pair(-4214925361858917762L, -7600080116318795460L),
                    "adce0ab6-ba94-402a-8503-143720887532" to
                            Pair(-5922784680015019990L, -8862217414733433550L),
                    "5333287b-1456-4326-bdc8-df1f14fc01e1" to
                            Pair(5995180038047548198L, -4771318480610000415L),
                    "9ddd9b01-cdaf-4f7d-8af4-f30db3ca9663" to
                            Pair(-7071325407853719683L, -8433848961981573533L),
                    "4ddcdb4c-394a-4534-b8ec-ed2f7174954d" to
                            Pair(5610600356221961524L, -5121457888204384947L))
            for ((str, expected) in tests) {
                on("converting $str to binary") {
                    val actual = str.toUUID()
                            ?: throw IllegalStateException("Parsing failed")
                    it("should return ${expected.first}, ${expected.second}") {
                        actual.getMostSignificantBits() shouldEqual expected.first
                        actual.getLeastSignificantBits() shouldEqual expected.second
                    }
                }
            }
        }
    }
})