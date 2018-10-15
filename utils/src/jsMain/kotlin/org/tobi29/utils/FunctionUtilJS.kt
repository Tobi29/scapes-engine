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

package org.tobi29.utils

actual infix fun (() -> Unit).chain(other: () -> Unit): () -> Unit =
    chainFunctions(arrayOf(*unchain(), *other.unchain()))

actual fun chain(vararg functions: () -> Unit): () -> Unit =
    chainFunctions(
        functions.flatMap { it.unchain().asIterable() }.toTypedArray()
    )

actual infix fun <P1> ((P1) -> Unit).chain(other: (P1) -> Unit): (P1) -> Unit =
    chainFunctions(arrayOf(*unchain(), *other.unchain()))

actual fun <P1> chain(vararg functions: (P1) -> Unit): (P1) -> Unit =
    chainFunctions(
        functions.flatMap { it.unchain().asIterable() }.toTypedArray()
    )

actual infix fun <P1, P2> ((P1, P2) -> Unit).chain(other: (P1, P2) -> Unit): (P1, P2) -> Unit =
    chainFunctions(arrayOf(*unchain(), *other.unchain()))

actual fun <P1, P2> chain(vararg functions: (P1, P2) -> Unit): (P1, P2) -> Unit =
    chainFunctions(
        functions.flatMap { it.unchain().asIterable() }.toTypedArray()
    )

private fun <T : Function<Unit>> T.unchain(): Array<T> =
    if (isChained(this)) {
        @Suppress("UnsafeCastFromDynamic")
        asDynamic()._ScapesEngine_ChainedFunctions
    } else {
        arrayOf(this)
    }

private fun <T : Function<Unit>> chainFunctions(functions: Array<T>): T {
    val function = {
        for (element in functions) {
            element.asDynamic().apply(null, js("(arguments)"))
        }
    }
    Object.defineProperty(
        function, "_ScapesEngine_ChainedFunctions",
        DataDescriptor(functions)
    )
    @Suppress("UnsafeCastFromDynamic")
    return function.asDynamic()
}

private fun isChained(function: Function<Unit>) =
    function.asDynamic()._ScapesEngine_ChainedFunctions !== undefined

external object Object {
    fun defineProperty(
        obj: Any?,
        name: String,
        descriptor: dynamic
    )
}

fun DataDescriptor(
    value: dynamic,
    writeable: Boolean = false
): dynamic {
    val descriptor = object {}.asDynamic()
    descriptor.value = value
    descriptor.writeable = writeable
    return descriptor
}
