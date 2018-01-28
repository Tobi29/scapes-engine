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

package org.tobi29.utils

import java.util.*
import kotlin.collections.ArrayList

inline fun <S> spiLoadFirst(
    iterator: Iterator<S>,
    error: (ServiceConfigurationError) -> Unit
): S? = spiLoadFirst(iterator, error, { true })

inline fun <S> spiLoadFirst(
    iterator: Iterator<S>,
    error: (ServiceConfigurationError) -> Unit,
    predicate: (S) -> Boolean
): S? {
    while (true) {
        try {
            if (!iterator.hasNext()) break
            iterator.next().takeIf(predicate)?.let { return it }
        } catch (e: ServiceConfigurationError) {
            error(e)
        }
    }
    return null
}

inline fun <S> spiLoad(
    iterator: Iterator<S>,
    error: (ServiceConfigurationError) -> Unit
): List<S> {
    val providers = ArrayList<S>()
    while (true) {
        try {
            if (!iterator.hasNext()) break
            providers.add(iterator.next())
        } catch (e: ServiceConfigurationError) {
            error(e)
        }
    }
    return providers
}

inline fun <reified S> spiLoad(classLoader: ClassLoader? = null): Iterator<S> {
    var iterator = spiLoadFromDefault<S>()
    classLoader?.let { iterator += spiLoadFromClassLoader(it) }
    return iterator
}

inline fun <reified S> spiLoadFromDefault(): Iterator<S> =
    ServiceLoader.load(S::class.java).iterator()

inline fun <reified S> spiLoadFromClassLoader(classLoader: ClassLoader): Iterator<S> =
    ServiceLoader.load(S::class.java, classLoader).iterator()
