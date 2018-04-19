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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.coroutines

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

/**
 * Transforms a deferred value by calling [transform] on completion
 *
 * **Note:** Just like with [Deferred.invokeOnCompletion] [transform] should be
 * lock-free and fast, exceptions however are caught and passed to the given
 * deferred value
 * @param transform Synchronous mapping to desired value
 * @receiver Deferred value to map
 * @return A deferred value available as soon as the given one is available
 */
inline fun <T, R> Deferred<T>.map(
    crossinline transform: (T) -> R
): Deferred<R> = CompletableDeferred<R>().also { deferred ->
    invokeOnCompletion(true) { cause ->
        if (cause == null) {
            try {
                deferred.complete(transform(getCompleted()))
            } catch (e: Throwable) {
                deferred.completeExceptionally(e)
            }
        } else deferred.completeExceptionally(cause)
    }
}

inline fun <T> Deferred<T>.tryGet(): T? =
    if (isCompleted) getCompleted() else null
