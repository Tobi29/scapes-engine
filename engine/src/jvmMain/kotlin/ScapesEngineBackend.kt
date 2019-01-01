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

@file:JvmName("ScapesEngineBackendJVMKt")
@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine

import org.tobi29.io.ByteBufferNative
import org.tobi29.io.ByteBufferView
import org.tobi29.io.viewBufferE

actual typealias MemoryBuffer = ByteBufferView
actual typealias MemoryBufferPinned = ByteBufferView

actual inline fun MemoryBufferPinned.close() {}

actual inline fun MemoryBufferPinned.asMemoryBuffer(): MemoryBuffer = this

actual inline fun allocateMemoryBuffer(size: Int): MemoryBuffer =
    ByteBufferNative(size).viewBufferE

actual inline fun allocateMemoryBufferPinned(size: Int): MemoryBuffer =
    allocateMemoryBuffer(size)
