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

package org.tobi29.scapes.engine.utils

/**
 * Chains all given functions into a single function
 * @param functions The functions to use
 * @return A function calling all given functions in order
 */
fun chain(vararg functions: () -> Unit): () -> Unit {
    return {
        functions.forEach { it() }
    }
}

/**
 * Chains all given functions into a single function
 * @param functions The functions to use
 * @param I First argument type of the functions
 * @return A function calling all given functions in order
 */
fun <I> chain(vararg functions: (I) -> Unit): (I) -> Unit {
    return { i ->
        functions.forEach { it(i) }
    }
}

/**
 * Chains all given functions into a single function
 * @param functions The functions to use
 * @param J Second argument type of the functions
 * @return A function calling all given functions in order
 */
fun <I, J> chain(vararg functions: (I, J) -> Unit): (I, J) -> Unit {
    return { i, j ->
        functions.forEach { it(i, j) }
    }
}
