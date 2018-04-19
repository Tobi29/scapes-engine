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

package org.tobi29.scapes.engine.resource

import kotlinx.coroutines.experimental.Deferred
import org.tobi29.arrays.readAsByteArray
import org.tobi29.coroutines.map
import org.tobi29.io.ReadSource
import org.tobi29.stdex.utf8ToString

fun ResourceLoader.loadString(source: ReadSource) =
        load {
            source.data().readAsByteArray { array, offset, size ->
                array.utf8ToString(offset, size)
            }
        }

/**
 * Transforms a resource by calling [transform] on completion
 *
 * **Note:** Just like with [Deferred.map] [transform] should be
 * lock-free and fast, exceptions however are caught and passed to the given
 * deferred value
 * @param transform Synchronous mapping to desired value
 * @receiver Resource to map
 * @return A resource available as soon as the given one is available
 */
inline fun <T : Any, R : Any> Resource<T>.map(
        crossinline transform: (T) -> R
): Resource<R> = DeferredResource(get().map(transform))
