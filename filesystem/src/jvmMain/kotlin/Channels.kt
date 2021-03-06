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

package org.tobi29.io.filesystem

import org.tobi29.io.JavaByteChannel

fun java.nio.channels.FileChannel.toChannel(): FileChannel =
    object : JavaFileChannel {
        override val java = this@toChannel
    }

interface JavaFileChannel : JavaByteChannel, FileChannel {
    override val java: java.nio.channels.FileChannel

    override var position: Long
        get() = java.position()
        set(value) {
            java.position(value)
        }
    override val size: Long get() = java.size()

    override fun truncate(size: Long) {
        java.truncate(size)
    }

    override fun isOpen(): Boolean = super<JavaByteChannel>.isOpen()

    override fun close() = super<JavaByteChannel>.close()
}
