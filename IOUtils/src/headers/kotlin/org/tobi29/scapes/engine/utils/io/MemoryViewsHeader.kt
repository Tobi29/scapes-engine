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

package org.tobi29.scapes.engine.utils.io

@PublishedApi expect internal fun ByteViewRO.roImpl(): ByteViewRO
@PublishedApi expect internal fun ByteViewERO.roImpl(): ByteViewERO
@PublishedApi expect internal fun ByteViewBERO.roImpl(): ByteViewBERO
@PublishedApi expect internal fun ByteViewLERO.roImpl(): ByteViewLERO

@PublishedApi expect internal fun ShortViewRO.roImpl(): ShortViewRO
@PublishedApi expect internal fun ShortViewERO.roImpl(): ShortViewERO
@PublishedApi expect internal fun ShortViewBERO.roImpl(): ShortViewBERO
@PublishedApi expect internal fun ShortViewLERO.roImpl(): ShortViewLERO

@PublishedApi expect internal fun IntViewRO.roImpl(): IntViewRO
@PublishedApi expect internal fun IntViewERO.roImpl(): IntViewERO
@PublishedApi expect internal fun IntViewBERO.roImpl(): IntViewBERO
@PublishedApi expect internal fun IntViewLERO.roImpl(): IntViewLERO

@PublishedApi expect internal fun LongViewRO.roImpl(): LongViewRO
@PublishedApi expect internal fun LongViewERO.roImpl(): LongViewERO
@PublishedApi expect internal fun LongViewBERO.roImpl(): LongViewBERO
@PublishedApi expect internal fun LongViewLERO.roImpl(): LongViewLERO
