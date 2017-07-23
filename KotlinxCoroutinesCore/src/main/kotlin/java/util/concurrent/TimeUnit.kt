package java.util.concurrent

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header enum class TimeUnit {
    NANOSECONDS,
    MICROSECONDS,
    MILLISECONDS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS;

    open fun convert(sourceDuration: Long,
                     sourceUnit: TimeUnit): Long

    open fun toNanos(duration: Long): Long

    open fun toMicros(duration: Long): Long

    open fun toMillis(duration: Long): Long

    open fun toSeconds(duration: Long): Long

    open fun toMinutes(duration: Long): Long

    open fun toHours(duration: Long): Long

    open fun toDays(duration: Long): Long
}
