/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.utils.tests;

import org.junit.Assert;
import org.junit.Test;
import org.tobi29.scapes.engine.utils.ArrayUtil;
import org.tobi29.scapes.engine.utils.tests.util.RandomInput;

import java.util.Arrays;

public class ArrayUtilTest {
    @Test
    public void testJoin() {
        for (byte[] array : RandomInput.createRandomArrays(64, 4)) {
            String joined = '[' + ArrayUtil.join(array) + ']';
            String check = Arrays.toString(array);
            Assert.assertEquals("Decoded array not equal to original array",
                    check, joined);
        }
    }

    @Test
    public void testHexadecimal() {
        for (byte[] array : RandomInput.createRandomArrays(64, 4)) {
            String hex = ArrayUtil.toHexadecimal(array);
            byte[] bytes = ArrayUtil.fromHexadecimal(hex);
            Assert.assertArrayEquals(
                    "Decoded array not equal to original array", array, bytes);
            String hex2 = ArrayUtil.toHexadecimal(bytes);
            Assert.assertEquals(
                    "Encoded string not equal to original encoded string", hex,
                    hex2);
        }
    }

    @Test
    public void testHexadecimalGrouped() {
        for (byte[] array : RandomInput.createRandomArrays(64, 4)) {
            for (int group = 1; group < 16; group++) {
                String hex = ArrayUtil.toHexadecimal(group, array);
                byte[] bytes = ArrayUtil.fromHexadecimal(hex);
                Assert.assertArrayEquals(
                        "Decoded array not equal to original array", array,
                        bytes);
                String hex2 = ArrayUtil.toHexadecimal(group, bytes);
                Assert.assertEquals(
                        "Encoded string not equal to original encoded string",
                        hex, hex2);
            }
        }
    }

    @Test
    public void testHexadecimalGroup() {
        String hex = "ff 0f 00 f0 ff";
        byte[] bytes = ArrayUtil.fromHexadecimal(hex);
        String hex2 = ArrayUtil.toHexadecimal(1, bytes);
        Assert.assertEquals(
                "Encoded string not equal to original encoded string", hex,
                hex2);
    }
}
