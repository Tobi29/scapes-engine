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

import org.junit.Assert
import org.junit.Test
import org.tobi29.scapes.engine.utils.fromHexadecimal
import org.tobi29.scapes.engine.utils.join
import org.tobi29.scapes.engine.utils.tests.util.RandomInput
import org.tobi29.scapes.engine.utils.toHexadecimal
import java.util.*

class ArrayUtilTest {
    @Test
    fun testJoin() {
        for (array in RandomInput.createRandomArrays(64, 4)) {
            val joined = "[${join(*array)}]"
            val check = Arrays.toString(array)
            Assert.assertEquals("Joined array different to Arrays.toString()",
                    check, joined)
        }
    }

    @Test
    fun testHexadecimal() {
        for (array in RandomInput.createRandomArrays(64, 4)) {
            val hex = array.toHexadecimal()
            val bytes = hex.fromHexadecimal()
            Assert.assertArrayEquals(
                    "Decoded array not equal to original array", array, bytes)
            val hex2 = bytes.toHexadecimal()
            Assert.assertEquals(
                    "Encoded string not equal to original encoded string", hex,
                    hex2)
        }
    }

    @Test
    fun testHexadecimalGrouped() {
        for (array in RandomInput.createRandomArrays(64, 4)) {
            for (group in 1..15) {
                val hex = array.toHexadecimal(group)
                val bytes = hex.fromHexadecimal()
                Assert.assertArrayEquals(
                        "Decoded array not equal to original array", array,
                        bytes)
                val hex2 = bytes.toHexadecimal(group)
                Assert.assertEquals(
                        "Encoded string not equal to original encoded string",
                        hex, hex2)
            }
        }
    }

    @Test
    fun testHexadecimalGroup() {
        val hex = "ff 0f 00 f0 ff"
        val bytes = hex.fromHexadecimal()
        val hex2 = bytes.toHexadecimal(1)
        Assert.assertEquals(
                "Encoded string not equal to original encoded string", hex,
                hex2)
    }
}
