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

package org.tobi29.scapes.engine.utils.tag.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.test.assertions.shouldNotEqual
import org.tobi29.scapes.engine.utils.tag.*
import java.math.BigDecimal
import java.math.BigInteger

object TagNumberTests : Spek({
    describe("a number tag") {
        val tagsEqual = listOf(
                Pair(TagByte(4), TagInt(4)),
                Pair(TagShort(4), TagInt(4)),
                Pair(TagLong(4), TagInt(4)),
                Pair(TagFloat(4.0f), TagInt(4)),
                Pair(TagDouble(4.0), TagInt(4)),
                Pair(TagBigInteger(BigInteger("4")), TagInt(4)),
                Pair(TagBigDecimal(BigDecimal("4.0")), TagInt(4))
        )
        given("two number tags") {
            for ((first, second) in tagsEqual) {
                it("should equal") {
                    first shouldEqual second
                }
                it("should give the same hash code") {
                    first.hashCode() shouldEqual second.hashCode()
                }
            }
        }
        val tagsNotEqual = listOf(
                Pair(TagFloat(4.1f), TagInt(4)),
                Pair(TagDouble(4.1), TagInt(4)),
                Pair(TagBigDecimal(BigDecimal("4.1")), TagInt(4))
        )
        given("two number tags") {
            for ((first, second) in tagsNotEqual) {
                it("should not equal") {
                    first shouldNotEqual second
                }
                it("should not give the same hash code") {
                    first.hashCode() shouldNotEqual second.hashCode()
                }
            }
        }
    }
})
