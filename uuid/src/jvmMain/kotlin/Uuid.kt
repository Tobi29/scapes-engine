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

@file:JvmName("UuidJVMKt")

package org.tobi29.uuid

import org.tobi29.stdex.PlatformProvidedImplementation

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun String.toUuid(): Uuid? = try {
    Uuid.fromString(this)
} catch (e: IllegalArgumentException) {
    null
}

actual typealias Uuid = java.util.UUID

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun randomUuid(): Uuid = java.util.UUID.randomUUID()
