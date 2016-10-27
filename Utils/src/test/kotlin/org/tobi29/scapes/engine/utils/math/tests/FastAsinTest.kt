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

package org.tobi29.scapes.engine.utils.math.tests

import org.junit.Assert
import org.junit.Test
import org.tobi29.scapes.engine.utils.math.FastAsin

class FastAsinTest {
    @Test
    fun testAsin() {
        var r = -2.0
        while (r <= 2.0) {
            val expected = Math.asin(r)
            Assert.assertEquals("asin($r) => $expected", expected,
                    FastAsin.asin(r), 0.01)
            r += 0.0009765625
        }
    }

    @Test
    fun testAcos() {
        var r = -2.0
        while (r <= 2.0) {
            val expected = Math.acos(r)
            Assert.assertEquals("acos($r) => $expected", expected,
                    FastAsin.acos(r), 0.01)
            r += 0.0009765625
        }
    }
}
