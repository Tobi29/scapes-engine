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

package org.tobi29.uuid

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.data
import org.tobi29.assertions.shouldEqual

object UuidTests : Spek({
    describe("converting a binary uuid to a string") {
        data(
            { a -> "converting ${a.first}, ${a.second} to a string" },
            data(
                Pair(0L, 0L),
                "00000000-0000-0000-0000-000000000000"
            ),
            data(
                Pair(-1L, -1L),
                "ffffffff-ffff-ffff-ffff-ffffffffffff"
            ),
            data(
                Pair(5650701559700802061L, -5249906936225336612L),
                "4e6b5321-3db6-4a0d-b724-9574ea6df2dc"
            ),
            data(
                Pair(5440586950734991076L, -5145713377773407038L),
                "4b80d901-982f-4ee4-b896-c0f231d734c2"
            ),
            data(
                Pair(7953353969410133574L, -5890048876739488405L),
                "6e5ffd4b-e7a2-4e46-ae42-57c0e041156b"
            ),
            data(
                Pair(-4214925361858917762L, -7600080116318795460L),
                "c58193b7-0b9d-4a7e-9687-1766b24f093c"
            ),
            data(
                Pair(-5922784680015019990L, -8862217414733433550L),
                "adce0ab6-ba94-402a-8503-143720887532"
            ),
            data(
                Pair(5995180038047548198L, -4771318480610000415L),
                "5333287b-1456-4326-bdc8-df1f14fc01e1"
            ),
            data(
                Pair(-7071325407853719683L, -8433848961981573533L),
                "9ddd9b01-cdaf-4f7d-8af4-f30db3ca9663"
            ),
            data(
                Pair(5610600356221961524L, -5121457888204384947L),
                "4ddcdb4c-394a-4534-b8ec-ed2f7174954d"
            )
        ) { a, expected ->
            val actual = Uuid(a.first, a.second).toString()
            it("should return $expected") {
                actual shouldEqual expected
            }
        }
    }
    describe("converting a string uuid to binary") {
        data(
            { a -> "converting $a to binary" },
            data(
                "00000000-0000-0000-0000-000000000000",
                Pair(0L, 0L)
            ),
            data(
                "ffffffff-ffff-ffff-ffff-ffffffffffff",
                Pair(-1L, -1L)
            ),
            data(
                "0-0-0-0-0",
                Pair(0L, 0L)
            ),
            data(
                "f-f-f-f-f",
                Pair(0x0000000F000F000FL, 0x000F00000000000FL)
            ),
            // FIXME: This is bugged on OpenJDK (and presumable Oracle as well) before 1.9
            // data(
            //     "0-fffff-0-0-0",
            //     Pair(0x00000000FFFF0000L, 0L)
            // ),
            // data(
            //     "0-0-0-fffff-0",
            //     Pair(0L, -0x000000000000L)
            // ),
            data(
                "4e6b5321-3db6-4a0d-b724-9574ea6df2dc",
                Pair(5650701559700802061L, -5249906936225336612L)
            ),
            data(
                "4b80d901-982f-4ee4-b896-c0f231d734c2",
                Pair(5440586950734991076L, -5145713377773407038L)
            ),
            data(
                "6e5ffd4b-e7a2-4e46-ae42-57c0e041156b",
                Pair(7953353969410133574L, -5890048876739488405L)
            ),
            data(
                "c58193b7-0b9d-4a7e-9687-1766b24f093c",
                Pair(-4214925361858917762L, -7600080116318795460L)
            ),
            data(
                "adce0ab6-ba94-402a-8503-143720887532",
                Pair(-5922784680015019990L, -8862217414733433550L)
            ),
            data(
                "5333287b-1456-4326-bdc8-df1f14fc01e1",
                Pair(5995180038047548198L, -4771318480610000415L)
            ),
            data(
                "9ddd9b01-cdaf-4f7d-8af4-f30db3ca9663",
                Pair(-7071325407853719683L, -8433848961981573533L)
            ),
            data(
                "4ddcdb4c-394a-4534-b8ec-ed2f7174954d",
                Pair(5610600356221961524L, -5121457888204384947L)
            )
        ) { a, expected ->
            val actual = a.toUuid()
                    ?: throw IllegalStateException("Parsing failed")
            it("should return ${expected.first}, ${expected.second}") {
                actual.getMostSignificantBits() shouldEqual expected.first
                actual.getLeastSignificantBits() shouldEqual expected.second
            }
        }
    }
})