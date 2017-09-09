package org.tobi29.scapes.engine.utils

header class MutableString : CharSequence, Appendable {
    override val length: Int

    override fun get(index: Int): Char

    override fun subSequence(startIndex: Int,
                             endIndex: Int): String

    constructor()

    constructor(initial: Int)

    constructor(str: String)

    override fun append(c: Char): MutableString

    override fun append(csq: CharSequence?): MutableString

    override fun append(csq: CharSequence?,
                        start: Int,
                        end: Int): MutableString

    fun insert(position: Int,
               char: Char): MutableString

    fun insert(position: Int,
               str: String): MutableString

    fun insert(position: Int,
               array: CharArray): MutableString

    fun insert(position: Int,
               array: CharArray,
               offset: Int): MutableString

    fun insert(position: Int,
               array: CharArray,
               offset: Int,
               length: Int): MutableString

    fun delete(range: IntRange): MutableString

    fun delete(startIndex: Int): MutableString

    fun delete(startIndex: Int,
               endIndex: Int): MutableString

    fun clear(): MutableString

    fun substring(startIndex: Int): String

    fun substring(startIndex: Int,
                  endIndex: Int): String

    override fun toString(): String
}
