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

package org.tobi29.scapes.engine.utils.io.filesystem

import org.tobi29.scapes.engine.utils.io.*

// TODO: @Throws(IOException::class)
fun channel(path: FilePath,
            options: Array<out OpenOption> = emptyArray(),
            attributes: Array<out FileAttribute> = emptyArray()): FileChannel =
        channelImpl(path, options, attributes)

inline fun isNotHidden(path: FilePath): Boolean = !isHidden(path)

// TODO: @Throws(IOException::class)
fun <R> read(path: FilePath,
             read: (ReadableByteStream) -> R): R =
        channel(path, options = arrayOf(OPEN_READ)).use {
            read(BufferedReadChannelStream(it))
        }

// TODO: @Throws(IOException::class)
fun <R> write(path: FilePath,
              write: (WritableByteStream) -> R): R =
        channel(path, options = arrayOf(OPEN_WRITE, OPEN_CREATE,
                OPEN_TRUNCATE_EXISTING)).use {
            val stream = BufferedWriteChannelStream(it)
            val result = write(stream)
            stream.flush()
            result
        }

interface FileOption
interface OpenOption : FileOption
interface CopyOption : FileOption
interface LinkOption : OpenOption, CopyOption

val OPEN_READ = object : OpenOption {}
val OPEN_WRITE = object : OpenOption {}
val OPEN_CREATE = object : OpenOption {}
val OPEN_CREATE_NEW = object : OpenOption {}
val OPEN_TRUNCATE_EXISTING = object : OpenOption {}
val LINK_NOFOLLOW = object : LinkOption {}
