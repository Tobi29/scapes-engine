package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.io.*

@PublishedApi header internal fun ByteViewRO.roImpl(): ByteViewRO
@PublishedApi header internal fun ByteViewERO.roImpl(): ByteViewERO
@PublishedApi header internal fun ByteViewBERO.roImpl(): ByteViewBERO
@PublishedApi header internal fun ByteViewLERO.roImpl(): ByteViewLERO

@PublishedApi header internal fun ShortViewRO.roImpl(): ShortViewRO
@PublishedApi header internal fun ShortViewERO.roImpl(): ShortViewERO
@PublishedApi header internal fun ShortViewBERO.roImpl(): ShortViewBERO
@PublishedApi header internal fun ShortViewLERO.roImpl(): ShortViewLERO

@PublishedApi header internal fun IntViewRO.roImpl(): IntViewRO
@PublishedApi header internal fun IntViewERO.roImpl(): IntViewERO
@PublishedApi header internal fun IntViewBERO.roImpl(): IntViewBERO
@PublishedApi header internal fun IntViewLERO.roImpl(): IntViewLERO

@PublishedApi header internal fun LongViewRO.roImpl(): LongViewRO
@PublishedApi header internal fun LongViewERO.roImpl(): LongViewERO
@PublishedApi header internal fun LongViewBERO.roImpl(): LongViewBERO
@PublishedApi header internal fun LongViewLERO.roImpl(): LongViewLERO
