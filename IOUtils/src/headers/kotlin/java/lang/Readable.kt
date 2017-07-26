package java.lang

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface Readable {
    /**
     * @throws IOException
     */
    fun read(cb: java.nio.CharBuffer): Int
}