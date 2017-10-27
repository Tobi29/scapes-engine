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
