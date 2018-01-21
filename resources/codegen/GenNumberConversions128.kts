#!/usr/bin/kotlinc -script
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

import java.math.BigInteger

print("""// GENERATED FILE, DO NOT EDIT DIRECTLY!!!
// Generation script can be found in `resources/codegen/GenNumberConversions.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.utils
""")

val types = setOf(
        (BigInteger(Byte.MIN_VALUE.toString()) to
                BigInteger(Byte.MAX_VALUE.toString())) to
                "Byte",
        (BigInteger(Short.MIN_VALUE.toString()) to
                BigInteger(Short.MAX_VALUE.toString())) to
                "Short",
        (BigInteger(Int.MIN_VALUE.toString()) to
                BigInteger(Int.MAX_VALUE.toString())) to
                "Int",
        (BigInteger(Long.MIN_VALUE.toString()) to
                BigInteger(Long.MAX_VALUE.toString())) to
                "Long",
        (BigInteger("-170141183460469231731687303715884105728") to
                BigInteger("170141183460469231731687303715884105727")) to
                "Int128",
        (BigInteger("0") to
                BigInteger("340282366920938463463374607431768211455")) to
                "UInt128")
val whitelist = setOf("Int128", "UInt128")

for ((fromRange, from) in types) {
    val (fromMin, fromMax) = fromRange
    for ((toRange, to) in types) {
        if (from !in whitelist && to !in whitelist) continue
        val (toMin, toMax) = toRange
        print("""
/**
 * Converts the given number to another type, clamping the value into the
 * maximum range of the destination type instead of overflowing
 * @receiver Number to convert
 * @return Number as new type
 */
""")
        print("inline fun $from.to${to}Clamped(): $to = ")
        if (from == to) {
            println("this")
        } else {
            val checkMin = fromMin < toMin
            val checkMax = fromMax > toMax
            if (!checkMin && !checkMax) {
                println("to$to()")
            } else {
                println("when {")
                if (checkMin) {
                    println("    this <= $to.MIN_VALUE.to$from() -> $to.MIN_VALUE")
                }
                if (checkMax) {
                    println("    this >= $to.MAX_VALUE.to$from() -> $to.MAX_VALUE")
                }
                println("    else -> to$to()")
                println("}")
            }
        }
    }
}
