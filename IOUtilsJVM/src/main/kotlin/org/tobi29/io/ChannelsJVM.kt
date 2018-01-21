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
package org.tobi29.io

import java.nio.ByteBuffer

actual typealias Channel = java.nio.channels.Channel

actual typealias InterruptibleChannel = java.nio.channels.InterruptibleChannel

interface JavaChannel : Channel {
    val java: java.nio.channels.Channel

    override fun isOpen(): Boolean = java.isOpen()

    override fun close() = java.close()
}

interface SEChannel : java.nio.channels.Channel {
    val se: Channel

    override fun isOpen(): Boolean = se.isOpen()

    override fun close() = se.close()
}

fun java.nio.channels.ReadableByteChannel.toChannel(): ReadableByteChannel =
        when (this) {
            is SEReadableByteChannel -> se
            else -> object : JavaReadableByteChannel {
                override val java = this@toChannel
            }
        }

fun ReadableByteChannel.toJavaChannel(): java.nio.channels.ReadableByteChannel =
        when (this) {
            is JavaReadableByteChannel -> java
            else -> object : SEReadableByteChannel {
                override val se = this@toJavaChannel
            }
        }

interface JavaReadableByteChannel : JavaChannel, ReadableByteChannel {
    override val java: java.nio.channels.ReadableByteChannel

    override fun isOpen(): Boolean = super.isOpen()

    override fun close() = super.close()

    override fun read(buffer: ByteView): Int =
            buffer.mutateAsByteBuffer { java.read(it) }
}

interface SEReadableByteChannel : SEChannel, java.nio.channels.ReadableByteChannel {
    override val se: ReadableByteChannel

    override fun isOpen(): Boolean = super.isOpen()

    override fun close() = super.close()

    override fun read(dst: java.nio.ByteBuffer): Int =
            se.read(dst.viewSliceE).also {
                if (it > 0) dst._position(dst.position() + it)
            }
}

fun java.nio.channels.WritableByteChannel.toChannel(): WritableByteChannel =
        when (this) {
            is SEWritableByteChannel -> se
            else -> object : JavaWritableByteChannel {
                override val java = this@toChannel
            }
        }

fun WritableByteChannel.toJavaChannel(): java.nio.channels.WritableByteChannel =
        when (this) {
            is JavaWritableByteChannel -> java
            else -> object : SEWritableByteChannel {
                override val se = this@toJavaChannel
            }
        }

interface JavaWritableByteChannel : JavaChannel, WritableByteChannel {
    override val java: java.nio.channels.WritableByteChannel

    override fun isOpen(): Boolean = super.isOpen()

    override fun close() = super.close()

    override fun write(buffer: ByteViewRO): Int =
            buffer.readAsByteBuffer().let { java.write(it) }
}

interface SEWritableByteChannel : SEChannel, java.nio.channels.WritableByteChannel {
    override val se: WritableByteChannel

    override fun isOpen(): Boolean = super.isOpen()

    override fun close() = super.close()

    override fun write(src: java.nio.ByteBuffer): Int =
            se.write(src.viewSliceE).also {
                if (it > 0) src._position(src.position() + it)
            }
}

fun java.nio.channels.ByteChannel.toChannel(): ByteChannel =
        when (this) {
            is SEByteChannel -> se
            else -> object : JavaByteChannel {
                override val java = this@toChannel
            }
        }

fun ByteChannel.toJavaChannel(): java.nio.channels.ByteChannel =
        when (this) {
            is JavaByteChannel -> java
            else -> object : SEByteChannel {
                override val se = this@toJavaChannel
            }
        }

fun ReadableByteChannel.read(buffer: ByteBuffer): Int = when (this) {
    is JavaReadableByteChannel -> java.read(buffer)
    else -> read(buffer.viewSliceE).also {
        if (it > 0) buffer._position(buffer.position() + it)
    }
}

fun WritableByteChannel.write(buffer: ByteBuffer): Int = when (this) {
    is JavaWritableByteChannel -> java.write(buffer)
    else -> write(buffer.viewSliceE).also {
        if (it > 0) buffer._position(buffer.position() + it)
    }
}

interface JavaByteChannel : JavaReadableByteChannel, JavaWritableByteChannel, ByteChannel {
    override val java: java.nio.channels.ByteChannel

    override fun isOpen(): Boolean = super<JavaReadableByteChannel>.isOpen()

    override fun close() = super<JavaReadableByteChannel>.close()
}

interface SEByteChannel : SEReadableByteChannel, SEWritableByteChannel, java.nio.channels.ByteChannel {
    override val se: ByteChannel

    override fun isOpen(): Boolean = super<SEReadableByteChannel>.isOpen()

    override fun close() = super<SEReadableByteChannel>.close()
}
