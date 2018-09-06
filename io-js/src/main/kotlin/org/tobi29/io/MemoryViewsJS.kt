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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.io

import org.tobi29.arrays.BytesRO
import org.tobi29.stdex.InlineUtility

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val BytesRO.ro: BytesRO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val ByteViewERO.ro: ByteViewERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val ByteViewBERO.ro: ByteViewBERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val ByteViewLERO.ro: ByteViewLERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val ShortViewRO.ro: ShortViewRO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val ShortViewERO.ro: ShortViewERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val ShortViewBERO.ro: ShortViewBERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val ShortViewLERO.ro: ShortViewLERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val IntViewRO.ro: IntViewRO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val IntViewERO.ro: IntViewERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val IntViewBERO.ro: IntViewBERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val IntViewLERO.ro: IntViewLERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val LongViewRO.ro: LongViewRO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val LongViewERO.ro: LongViewERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val LongViewBERO.ro: LongViewBERO
    get() = this

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline val LongViewLERO.ro: LongViewLERO
    get() = this
