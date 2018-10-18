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

package org.tobi29.scapes.engine.backends.openal.openal

import kotlinx.coroutines.experimental.CoroutineName
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import org.tobi29.coroutines.ResponsiveCoroutineScope
import org.tobi29.coroutines.launchResponsive
import org.tobi29.stdex.atomic.AtomicReference
import org.tobi29.utils.unpark

internal actual fun CoroutineScope.launchAudioCoroutine(
    block: suspend ResponsiveCoroutineScope.(
        suspend ResponsiveCoroutineScope.(Long) -> Unit,
        suspend ResponsiveCoroutineScope.() -> Unit
    ) -> Unit
): Pair<Job, () -> Unit> {
    val thread = AtomicReference<Thread?>(null)
    return launchResponsive(CoroutineName("Engine-Sounds")) {
        block({ delayResponsiveNanos(it) }, { parkResponsive(thread) })
    } to { thread.unpark() }
}
