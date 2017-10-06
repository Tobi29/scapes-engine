package java.lang

@Suppress("HEADER_WITHOUT_IMPLEMENTATION", "NO_ACTUAL_FOR_EXPECT")
header interface AutoCloseable {
    /**
     * @throws Exception
     */
    fun close()
}
