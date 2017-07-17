package java.lang

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header interface AutoCloseable {
    /**
     * @throws Exception
     */
    fun close()
}
