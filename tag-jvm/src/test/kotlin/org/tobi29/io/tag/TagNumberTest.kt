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

package org.tobi29.io.tag

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.tobi29.assertions.on
import org.tobi29.assertions.shouldEqual
import org.tobi29.assertions.shouldNotEqual
import java.math.BigDecimal
import java.math.BigInteger

object TagNumberTests : Spek({
    describe("a number tag") {
        on(
            { a, b -> "comparing $a with $b" },
            data<Tag, Tag, Boolean>(
                TagByte(4),
                TagInt(4),
                true
            ),
            data<Tag, Tag, Boolean>(
                TagShort(4),
                TagInt(4),
                true
            ),
            data<Tag, Tag, Boolean>(
                TagLong(4),
                TagInt(4),
                true
            ),
            data<Tag, Tag, Boolean>(
                TagFloat(4.0f),
                TagInt(4),
                true
            ),
            data<Tag, Tag, Boolean>(
                TagDouble(4.0),
                TagInt(4),
                true
            ),
            data<Tag, Tag, Boolean>(
                TagBigInteger(BigInteger("4")),
                TagInt(4),
                true
            ),
            data<Tag, Tag, Boolean>(
                TagBigDecimal(BigDecimal("4.0")),
                TagInt(4),
                true
            ),
            data<Tag, Tag, Boolean>(
                TagFloat(4.1f),
                TagInt(4),
                false
            ),
            data<Tag, Tag, Boolean>(
                TagDouble(4.1),
                TagInt(4),
                false
            ),
            data<Tag, Tag, Boolean>(
                TagBigDecimal(BigDecimal("4.1")),
                TagInt(4),
                false
            )
        ) { a, b, expected ->
            if (expected) {
                it("$a should equal $b") {
                    a shouldEqual b
                }
                it("should give the same hash code") {
                    a.hashCode() shouldEqual b.hashCode()
                }
            } else {
                it("should not equal") {
                    a shouldNotEqual b
                }
            }
        }
    }
})
