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

@file:JvmName("NumberFormatJVMKt")

package org.tobi29.utils

import org.tobi29.stdex.ThreadLocal
import java.util.*

@PublishedApi
internal actual fun Double.toStringDecimalImpl(
    precision: Int
): String {
    val (builder, formatter) = formatter
    formatter.format("%.${precision}f", this)
    val output = builder.toString()
    builder.setLength(0)
    return output
}

@PublishedApi
internal actual fun Double.toStringExponentialImpl(
    precision: Int
): String {
    val (builder, formatter) = formatter
    formatter.format("%.${precision}e", this)
    val output = builder.toString()
    builder.setLength(0)
    return output
}

private val formatter by ThreadLocal {
    StringBuilder().let {
        it to Formatter(it, Locale.US)
    }
}
