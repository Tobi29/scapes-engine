package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.byteArrays
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.fromBase64
import org.tobi29.scapes.engine.utils.toBase64
import java.util.*

object Base64Tests : Spek({
    describe("encoding an array in base64") {
        for (array in byteArrays()) {
            given("[${array.joinToString()}]") {
                on("encoding in base64") {
                    val actual = array.toBase64()
                    val expected = Base64.getEncoder().encodeToString(array)
                    it("should return $expected") {
                        actual shouldEqual expected
                    }
                }
            }
        }
    }
    describe("decoding a base64 encoded string") {
        for (expected in byteArrays()) {
            val str = Base64.getEncoder().encodeToString(expected)
            given(str) {
                on("decoding from base64") {
                    val actual = str.fromBase64()
                    it("should return [${expected.joinToString()}]") {
                        actual shouldEqual expected
                    }
                }
            }
        }
    }
})
