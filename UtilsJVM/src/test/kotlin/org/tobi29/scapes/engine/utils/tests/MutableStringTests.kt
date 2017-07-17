package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.toMutableString

object MutableStringTests : Spek({
    describe("appending to a mutable string") {
        given("initial strings and strings to append") {
            val cases = listOf(
                    Triple("A", "B", "AB"),
                    Triple("ABC", "DEF", "ABCDEF"))

            for ((initial, append, expected) in cases) {
                on("appending \"$append\" to \"$initial\"") {
                    val result = initial.toMutableString()
                            .append(append).toString()
                    it("should return \"$expected\"") {
                        result shouldEqual expected
                    }
                }
            }
        }
    }
    describe("inserting into a mutable string") {
        given("initial strings and strings to insert") {
            val cases = listOf(
                    Triple("A", Pair("B", 0), "BA"),
                    Triple("A", Pair("B", 1), "AB"),
                    Triple("ABC", Pair("DEF", 1), "ADEFBC"))

            for ((initial, insertPair, expected) in cases) {
                val (insert, position) = insertPair
                on("inserting \"$insert\" into \"$initial\" at $position") {
                    val result = initial.toMutableString()
                            .insert(position, insert).toString()
                    it("should return \"$expected\"") {
                        result shouldEqual expected
                    }
                }
            }
        }
    }
    describe("deleting from a mutable string") {
        given("initial strings and range to delete") {
            val cases = listOf(
                    Triple("A", 0..0, ""),
                    Triple("ABC", 0..0, "BC"),
                    Triple("ABC", 1..1, "AC"),
                    Triple("ABC", 2..2, "AB"),
                    Triple("ABCDEF", 0..2, "DEF"),
                    Triple("ABCDEF", 1..4, "AF"),
                    Triple("ABCDEF", 3..5, "ABC"))

            for ((initial, range, expected) in cases) {
                on("delete \"$range\" into \"$initial\"") {
                    val result = initial.toMutableString()
                            .delete(range).toString()
                    it("should return \"$expected\"") {
                        result shouldEqual expected
                    }
                }
            }
        }
    }
})