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

package org.tobi29.scapes.engine.backends.lwjgl3

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

inline fun <R> MemoryStack.push(block: () -> R): R {
    push()
    return try {
        block()
    } finally {
        pop()
    }
}

inline fun <R> ByteBuffer.use(block: (ByteBuffer) -> R): R {
    return try {
        block(this)
    } finally {
        MemoryUtil.memFree(this)
    }
}

internal class CurrentFBO {
    var current: Int = 0
}
