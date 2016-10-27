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
import org.tobi29.scapes.engine.utils.math.ceil
import org.tobi29.scapes.engine.utils.math.floor
import org.tobi29.scapes.engine.utils.math.round

class MathUtilTest {
    @Test
    fun testFloorFloat() {
        Assert.assertEquals("floor(-1.5f) => -2", -2, floor(-1.5f))
        Assert.assertEquals("floor(-1.0f) => -1", -1, floor(-1.0f))
        Assert.assertEquals("floor(-0.5f) => -1", -1, floor(-0.5f))
        Assert.assertEquals("floor(0.0f) => 0", 0, floor(0.0f))
        Assert.assertEquals("floor(0.5f) => 0", 0, floor(0.5f))
        Assert.assertEquals("floor(1.0f) => 1", 1, floor(1.0f))
        Assert.assertEquals("floor(1.5f) => 1", 1, floor(1.5f))
    }

    @Test
    fun testFloorDouble() {
        Assert.assertEquals("floor(-1.5) => -2", -2, floor(-1.5))
        Assert.assertEquals("floor(-1.0) => -1", -1, floor(-1.0))
        Assert.assertEquals("floor(-0.5) => -1", -1, floor(-0.5))
        Assert.assertEquals("floor(0.0) => 0", 0, floor(0.0))
        Assert.assertEquals("floor(0.5) => 0", 0, floor(0.5))
        Assert.assertEquals("floor(1.0) => 1", 1, floor(1.0))
        Assert.assertEquals("floor(1.5) => 1", 1, floor(1.5))
    }

    @Test
    fun testRoundFloat() {
        Assert.assertEquals("round(-0.6f) => -1", -1, round(-0.6f))
        Assert.assertEquals("round(-0.5f) => 0", 0, round(-0.5f))
        Assert.assertEquals("round(-0.4f) => 0", 0, round(-0.4f))
        Assert.assertEquals("round(0.4f) => 0", 0, round(0.4f))
        Assert.assertEquals("round(0.5f) => 1", 1, round(0.5f))
        Assert.assertEquals("round(0.6f) => 1", 1, round(0.6f))
    }

    @Test
    fun testRoundDouble() {
        Assert.assertEquals("round(-0.6) => -1", -1, round(-0.6))
        Assert.assertEquals("round(-0.5) => 0", 0, round(-0.5))
        Assert.assertEquals("round(-0.4) => 0", 0, round(-0.4))
        Assert.assertEquals("round(0.4) => 0", 0, round(0.4))
        Assert.assertEquals("round(0.5) => 1", 1, round(0.5))
        Assert.assertEquals("round(0.6) => 1", 1, round(0.6))
    }

    @Test
    fun testCeilFloat() {
        Assert.assertEquals("ceil(-1.5f) => -1", -1, ceil(-1.5f))
        Assert.assertEquals("ceil(-1.0f) => -1", -1, ceil(-1.0f))
        Assert.assertEquals("ceil(-0.5f) => 0", 0, ceil(-0.5f))
        Assert.assertEquals("ceil(0.0f) => 0", 0, ceil(0.0f))
        Assert.assertEquals("ceil(0.5f) => 1", 1, ceil(0.5f))
        Assert.assertEquals("ceil(1.0f) => 1", 1, ceil(1.0f))
        Assert.assertEquals("ceil(1.5f) => 2", 2, ceil(1.5f))
    }

    @Test
    fun testCeilDouble() {
        Assert.assertEquals("ceil(-1.5) => -1", -1, ceil(-1.5))
        Assert.assertEquals("ceil(-1.0) => -1", -1, ceil(-1.0))
        Assert.assertEquals("ceil(-0.5) => 0", 0, ceil(-0.5))
        Assert.assertEquals("ceil(0.0) => 0", 0, ceil(0.0))
        Assert.assertEquals("ceil(0.5) => 1", 1, ceil(0.5))
        Assert.assertEquals("ceil(1.0) => 1", 1, ceil(1.0))
        Assert.assertEquals("ceil(1.5) => 2", 2, ceil(1.5))
    }
}
