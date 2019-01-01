/*
 * Copyright 2012-2019 Tobi29
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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.utils

// TODO: Properly test this

@PublishedApi
internal actual inline fun Double.toStringDecimalImpl(
    precision: Int
): String =
    @Suppress("UnsafeCastFromDynamic")
    asDynamic().toFixed(precision)

@PublishedApi
internal actual fun Double.toStringExponentialImpl(
    precision: Int
): String {
    @Suppress("UnsafeCastFromDynamic")
    val str: String = asDynamic().toExponential(precision)
    SingleDigitExponential.singleDigitExponential.replace(str, "$1e$20$3")
    return str
}

private object SingleDigitExponential {
    val singleDigitExponential = "([0-9]*)e([+-])([0-9])".toRegex()
}
