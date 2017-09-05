package org.tobi29.scapes.engine.utils

class MutableString(initial: Int = 0) : CharSequence, Appendable {
    private var array = CharArray(initial)
    override var length = 0
        private set

    override fun get(index: Int): Char {
        if (index < 0 || index >= length) {
            throw IndexOutOfBoundsException("$index")
        }
        return array[index]
    }

    override fun subSequence(startIndex: Int,
                             endIndex: Int) =
            substring(startIndex, endIndex)

    constructor(str: String) : this(str.length) {
        str.copyToArray(array)
        length = str.length
    }

    override fun append(c: Char): MutableString {
        ensure(1)
        array[length++] = c
        return this
    }

    override fun append(csq: CharSequence?) = append(csq, 0, csq?.length ?: 4)

    override fun append(csq: CharSequence?,
                        start: Int,
                        end: Int): MutableString {
        val rcsq = csq ?: "null"
        ensure(rcsq.length)
        for (i in start until end) {
            array[length++] = rcsq[i]
        }
        return this
    }

    fun insert(position: Int,
               char: Char): MutableString {
        if (position < 0 || position > length) {
            throw IndexOutOfBoundsException("$position")
        }
        ensure(1)
        copy(array, array, this.length - position, position, position + 1)
        array[position] = char
        length++
        return this
    }

    fun insert(position: Int,
               str: String): MutableString =
            insert(position, str.copyToArray())

    fun insert(position: Int,
               array: CharArray,
               offset: Int = 0,
               length: Int = array.size): MutableString {
        if (position < 0 || position > this.length) {
            throw IndexOutOfBoundsException("$position")
        }
        ensure(length)
        copy(this.array, this.array, this.length - position, position,
                position + length)
        copy(array, this.array, length, offset, position)
        this.length += length
        return this
    }

    fun delete(range: IntRange): MutableString =
            delete(range.start, range.endInclusive + 1)

    fun delete(startIndex: Int,
               endIndex: Int = startIndex + 1): MutableString {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                    "End index less than start index: $endIndex < $startIndex")
        }
        if (startIndex < 0 || endIndex > length) {
            throw IndexOutOfBoundsException("$startIndex..$endIndex")
        }
        copy(array, array, length - endIndex, endIndex, startIndex)
        length -= endIndex - startIndex
        return this
    }

    fun clear(): MutableString {
        length = 0
        return this
    }

    fun substring(startIndex: Int,
                  endIndex: Int = length): String {
        if (endIndex < startIndex) {
            throw IllegalArgumentException(
                    "End index less than start index: $endIndex < $startIndex")
        }
        if (startIndex < 0 || endIndex > length) {
            throw IndexOutOfBoundsException("$startIndex..$endIndex")
        }
        return array.copyToString(startIndex, endIndex - startIndex)
    }

    override fun toString() = array.copyToString(0, length)

    private fun ensure(length: Int,
                       position: Int = this.length) {
        val end = length + position
        if (array.size < end) {
            val newArray = CharArray(end)
            copy(array, newArray)
            array = newArray
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.toMutableString(): MutableString = MutableString(this)
