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

package org.tobi29.stdex

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline infix fun Int.lrot(other: Int): Int =
    (this shl other) or (this ushr -other)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline infix fun Int.rrot(other: Int): Int =
    (this ushr other) or (this shl -other)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline infix fun Long.lrot(other: Int): Long =
    (this shl other) or (this ushr -other)

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline infix fun Long.rrot(other: Int): Long =
    (this ushr other) or (this shl -other)
