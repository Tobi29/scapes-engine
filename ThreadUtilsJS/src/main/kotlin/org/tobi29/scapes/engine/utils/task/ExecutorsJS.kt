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

package org.tobi29.scapes.engine.utils.task

import kotlinx.coroutines.experimental.*
import org.tobi29.scapes.engine.utils.toIntClamped
import kotlin.browser.window
import kotlin.coroutines.experimental.CoroutineContext

object WindowDispatcher : CoroutineDispatcher(), Delay {
    override fun dispatch(context: CoroutineContext,
                          block: Runnable) {
        window.setTimeout({ block.run() }, 0)
    }

    override fun scheduleResumeAfterDelay(time: Long,
                                          unit: TimeUnit,
                                          continuation: CancellableContinuation<Unit>) {
        window.setTimeout({
            with(continuation) { resumeUndispatched(Unit) }
        }, unit.toMillis(time).toIntClamped())
    }

    override fun invokeOnTimeout(time: Long,
                                 unit: TimeUnit,
                                 block: Runnable): DisposableHandle =
            DelayedRunnableTask(block).also { task ->
                window.setTimeout({ task.run() },
                        unit.toMillis(time).toIntClamped())
            }
}

private class DelayedRunnableTask(
        private val block: Runnable
) : Runnable, DisposableHandle {
    private var disposed = false

    override fun run() {
        if (!disposed) {
            block.run()
        }
    }

    override fun dispose() {
        disposed = true
    }
}
