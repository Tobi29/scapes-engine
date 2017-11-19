@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils.io

@PublishedApi actual inline internal fun ByteViewRO.roImpl(): ByteViewRO = this
@PublishedApi actual inline internal fun ByteViewERO.roImpl(): ByteViewERO = this
@PublishedApi actual inline internal fun ByteViewBERO.roImpl(): ByteViewBERO = this
@PublishedApi actual inline internal fun ByteViewLERO.roImpl(): ByteViewLERO = this

@PublishedApi actual inline internal fun ShortViewRO.roImpl(): ShortViewRO = this
@PublishedApi actual inline internal fun ShortViewERO.roImpl(): ShortViewERO = this
@PublishedApi actual inline internal fun ShortViewBERO.roImpl(): ShortViewBERO = this
@PublishedApi actual inline internal fun ShortViewLERO.roImpl(): ShortViewLERO = this

@PublishedApi actual inline internal fun IntViewRO.roImpl(): IntViewRO = this
@PublishedApi actual inline internal fun IntViewERO.roImpl(): IntViewERO = this
@PublishedApi actual inline internal fun IntViewBERO.roImpl(): IntViewBERO = this
@PublishedApi actual inline internal fun IntViewLERO.roImpl(): IntViewLERO = this

@PublishedApi actual inline internal fun LongViewRO.roImpl(): LongViewRO = this
@PublishedApi actual inline internal fun LongViewERO.roImpl(): LongViewERO = this
@PublishedApi actual inline internal fun LongViewBERO.roImpl(): LongViewBERO = this
@PublishedApi actual inline internal fun LongViewLERO.roImpl(): LongViewLERO = this
