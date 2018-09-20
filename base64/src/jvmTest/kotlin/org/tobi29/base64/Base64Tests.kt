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

package org.tobi29.base64

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.data
import org.tobi29.assertions.shouldEqual
import org.tobi29.assertions.suites.createByteArrays
import java.util.*

object Base64Tests : Spek({
    describe("base64 encoding and decoding") {
        data(
            { a -> "encoding ${a.joinToString()} in base64" },
            *createByteArrays().map {
                data(it, Base64.getEncoder().encodeToString(it))
            }.toList().toTypedArray()
        ) { a, expected ->
            val actual = a.toBase64()
            it("should return $expected") {
                actual shouldEqual expected
            }
        }
        data(
            { a -> "decoding $a from base64" },
            *createByteArrays().map {
                data(Base64.getEncoder().encodeToString(it), it)
            }.toList().toTypedArray()
        ) { a, expected ->
            val actual = a.fromBase64()
            it("should return ${expected.joinToString()}") {
                actual shouldEqual expected
            }
        }
    }
})
