package org.tobi29.scapes.engine.utils.io

@PublishedApi impl internal fun ByteViewRO.roImpl(): ByteViewRO = when (this) {
    is ByteViewRORO -> this
    else -> ByteViewRORO(this)
}

private class ByteViewRORO(private val handle: ByteViewRO) : ByteViewRO by handle

@PublishedApi impl internal fun ByteViewERO.roImpl(): ByteViewERO = when (this) {
    is ByteViewERORO -> this
    else -> ByteViewERORO(this)
}

private class ByteViewERORO(private val handle: ByteViewERO) : ByteViewERO by handle

@PublishedApi impl internal fun ByteViewBERO.roImpl(): ByteViewBERO = when (this) {
    is ByteViewBERORO -> this
    else -> ByteViewBERORO(this)
}

private class ByteViewBERORO(private val handle: ByteViewBERO) : ByteViewBERO by handle

@PublishedApi impl internal fun ByteViewLERO.roImpl(): ByteViewLERO = when (this) {
    is ByteViewLERORO -> this
    else -> ByteViewLERORO(this)
}

private class ByteViewLERORO(private val handle: ByteViewLERO) : ByteViewLERO by handle

@PublishedApi impl internal fun ShortViewRO.roImpl(): ShortViewRO = when (this) {
    is ShortViewRORO -> this
    else -> ShortViewRORO(this)
}

private class ShortViewRORO(private val handle: ShortViewRO) : ShortViewRO by handle

@PublishedApi impl internal fun ShortViewERO.roImpl(): ShortViewERO = when (this) {
    is ShortViewERORO -> this
    else -> ShortViewERORO(this)
}

private class ShortViewERORO(private val handle: ShortViewERO) : ShortViewERO by handle

@PublishedApi impl internal fun ShortViewBERO.roImpl(): ShortViewBERO = when (this) {
    is ShortViewBERORO -> this
    else -> ShortViewBERORO(this)
}

private class ShortViewBERORO(private val handle: ShortViewBERO) : ShortViewBERO by handle

@PublishedApi impl internal fun ShortViewLERO.roImpl(): ShortViewLERO = when (this) {
    is ShortViewLERORO -> this
    else -> ShortViewLERORO(this)
}

private class ShortViewLERORO(private val handle: ShortViewLERO) : ShortViewLERO by handle

@PublishedApi impl internal fun IntViewRO.roImpl(): IntViewRO = when (this) {
    is IntViewRORO -> this
    else -> IntViewRORO(this)
}

private class IntViewRORO(private val handle: IntViewRO) : IntViewRO by handle

@PublishedApi impl internal fun IntViewERO.roImpl(): IntViewERO = when (this) {
    is IntViewERORO -> this
    else -> IntViewERORO(this)
}

private class IntViewERORO(private val handle: IntViewERO) : IntViewERO by handle

@PublishedApi impl internal fun IntViewBERO.roImpl(): IntViewBERO = when (this) {
    is IntViewBERORO -> this
    else -> IntViewBERORO(this)
}

private class IntViewBERORO(private val handle: IntViewBERO) : IntViewBERO by handle

@PublishedApi impl internal fun IntViewLERO.roImpl(): IntViewLERO = when (this) {
    is IntViewLERORO -> this
    else -> IntViewLERORO(this)
}

private class IntViewLERORO(private val handle: IntViewLERO) : IntViewLERO by handle

@PublishedApi impl internal fun LongViewRO.roImpl(): LongViewRO = when (this) {
    is LongViewRORO -> this
    else -> LongViewRORO(this)
}

private class LongViewRORO(private val handle: LongViewRO) : LongViewRO by handle

@PublishedApi impl internal fun LongViewERO.roImpl(): LongViewERO = when (this) {
    is LongViewERORO -> this
    else -> LongViewERORO(this)
}

private class LongViewERORO(private val handle: LongViewERO) : LongViewERO by handle

@PublishedApi impl internal fun LongViewBERO.roImpl(): LongViewBERO = when (this) {
    is LongViewBERORO -> this
    else -> LongViewBERORO(this)
}

private class LongViewBERORO(private val handle: LongViewBERO) : LongViewBERO by handle

@PublishedApi impl internal fun LongViewLERO.roImpl(): LongViewLERO = when (this) {
    is LongViewLERORO -> this
    else -> LongViewLERORO(this)
}

private class LongViewLERORO(private val handle: LongViewLERO) : LongViewLERO by handle
