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

package org.tobi29.coroutines

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.tobi29.stdex.InlineUtility
import org.tobi29.utils.Duration64Nanos
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CoroutineScope.launchResponsive(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend ResponsiveCoroutineScope.() -> Unit
): Job {
    val (responsiveContext, closeExecutor) = newResponsiveContext()
    return launch(context + responsiveContext, start) {
        try {
            ResponsiveCoroutineScope(this).block()
        } finally {
            closeExecutor()
        }
    }
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun JobHandle.launchResponsive(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend ResponsiveCoroutineScope.() -> Unit
): Job? {
    val (responsiveContext, closeExecutor) = newResponsiveContext()
    return launch(context + responsiveContext, start) {
        try {
            ResponsiveCoroutineScope(this).block()
        } finally {
            closeExecutor()
        }
    }
}

expect fun CoroutineScope.newResponsiveContext(
    context: CoroutineContext = EmptyCoroutineContext
): Pair<CoroutineContext, () -> Unit>

expect class ResponsiveCoroutineScope(
    delegate: CoroutineScope
) : CoroutineScope {
    suspend inline fun delayResponsiveNanos(time: Duration64Nanos)
}
