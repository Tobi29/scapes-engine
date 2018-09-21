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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.tobi29.utils.Duration64Nanos
import kotlin.coroutines.CoroutineContext

actual fun CoroutineScope.newResponsiveContext(
    context: CoroutineContext
): Pair<CoroutineContext, () -> Unit> = (context + Dispatchers.Default) to {}

actual class ResponsiveCoroutineScope actual constructor(
    private val delegate: CoroutineScope
) : CoroutineScope by delegate {
    actual suspend inline fun delayResponsiveNanos(time: Duration64Nanos) =
        delayNanos(time)
}
