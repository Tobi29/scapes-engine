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

@file:JvmName("MemoryViewsJVMKt")

package org.tobi29.io

import org.tobi29.arrays.BytesRO

actual val BytesRO.ro: BytesRO
    get() = when (this) {
        is ByteViewRORO -> this
        else -> ByteViewRORO(this)
    }

private class ByteViewRORO(
    private val handle: BytesRO
) : BytesRO by handle

actual val ByteViewERO.ro: ByteViewERO
    get() = when (this) {
        is ByteViewERORO -> this
        else -> ByteViewERORO(this)
    }

private class ByteViewERORO(
    private val handle: ByteViewERO
) : ByteViewERO by handle

actual val ByteViewBERO.ro: ByteViewBERO
    get() = when (this) {
        is ByteViewBERORO -> this
        else -> ByteViewBERORO(this)
    }

private class ByteViewBERORO(
    private val handle: ByteViewBERO
) : ByteViewBERO by handle

actual val ByteViewLERO.ro: ByteViewLERO
    get() = when (this) {
        is ByteViewLERORO -> this
        else -> ByteViewLERORO(this)
    }

private class ByteViewLERORO(
    private val handle: ByteViewLERO
) : ByteViewLERO by handle

actual val ShortViewRO.ro: ShortViewRO
    get() = when (this) {
        is ShortViewRORO -> this
        else -> ShortViewRORO(this)
    }

private class ShortViewRORO(
    private val handle: ShortViewRO
) : ShortViewRO by handle

actual val ShortViewERO.ro: ShortViewERO
    get() = when (this) {
        is ShortViewERORO -> this
        else -> ShortViewERORO(this)
    }

private class ShortViewERORO(
    private val handle: ShortViewERO
) : ShortViewERO by handle

actual val ShortViewBERO.ro: ShortViewBERO
    get() = when (this) {
        is ShortViewBERORO -> this
        else -> ShortViewBERORO(this)
    }

private class ShortViewBERORO(
    private val handle: ShortViewBERO
) : ShortViewBERO by handle

actual val ShortViewLERO.ro: ShortViewLERO
    get() = when (this) {
        is ShortViewLERORO -> this
        else -> ShortViewLERORO(this)
    }

private class ShortViewLERORO(
    private val handle: ShortViewLERO
) : ShortViewLERO by handle

actual val IntViewRO.ro: IntViewRO
    get() = when (this) {
        is IntViewRORO -> this
        else -> IntViewRORO(this)
    }

private class IntViewRORO(
    private val handle: IntViewRO
) : IntViewRO by handle

actual val IntViewERO.ro: IntViewERO
    get() = when (this) {
        is IntViewERORO -> this
        else -> IntViewERORO(this)
    }

private class IntViewERORO(
    private val handle: IntViewERO
) : IntViewERO by handle

actual val IntViewBERO.ro: IntViewBERO
    get() = when (this) {
        is IntViewBERORO -> this
        else -> IntViewBERORO(this)
    }

private class IntViewBERORO(
    private val handle: IntViewBERO
) : IntViewBERO by handle

actual val IntViewLERO.ro: IntViewLERO
    get() = when (this) {
        is IntViewLERORO -> this
        else -> IntViewLERORO(this)
    }

private class IntViewLERORO(
    private val handle: IntViewLERO
) : IntViewLERO by handle

actual val LongViewRO.ro: LongViewRO
    get() = when (this) {
        is LongViewRORO -> this
        else -> LongViewRORO(this)
    }

private class LongViewRORO(
    private val handle: LongViewRO
) : LongViewRO by handle

actual val LongViewERO.ro: LongViewERO
    get() = when (this) {
        is LongViewERORO -> this
        else -> LongViewERORO(this)
    }

private class LongViewERORO(
    private val handle: LongViewERO
) : LongViewERO by handle

actual val LongViewBERO.ro: LongViewBERO
    get() = when (this) {
        is LongViewBERORO -> this
        else -> LongViewBERORO(this)
    }

private class LongViewBERORO(
    private val handle: LongViewBERO
) : LongViewBERO by handle

actual val LongViewLERO.ro: LongViewLERO
    get() = when (this) {
        is LongViewLERORO -> this
        else -> LongViewLERORO(this)
    }

private class LongViewLERORO(
    private val handle: LongViewLERO
) : LongViewLERO by handle
