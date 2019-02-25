/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.scapes.engine

import kotlinx.coroutines.CoroutineScope
import org.tobi29.coroutines.ComponentJobHandle
import org.tobi29.coroutines.Timer
import org.tobi29.coroutines.loopUntilCancel
import org.tobi29.utils.ComponentTypeRegistered
import kotlin.coroutines.CoroutineContext

class CursorCaptureComponent : ComponentJobHandle<ScapesEngine>() {
    private var cursorCaptured = false

    override fun coroutineContextFor(holder: ScapesEngine): CoroutineContext =
        holder.coroutineContext

    override suspend fun CoroutineScope.runJobTask(holder: ScapesEngine) {
        Timer().loopUntilCancel(Timer.toDiff(10.0)) {
            val cursorCapture = holder.isMouseGrabbed
            if (cursorCapture != cursorCaptured) {
                cursorCaptured = cursorCapture
                holder.container.cursorCapture(cursorCapture)
            }
        }
    }

    companion object {
        val COMPONENT =
            ComponentTypeRegistered<ScapesEngine, CursorCaptureComponent, Any>()
    }
}

private inline val ScapesEngine.isMouseGrabbed
    get() = state?.isMouseGrabbed == true || guiController.captureCursor()
