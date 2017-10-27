package org.tobi29.scapes.engine.utils

expect fun String.toUUID(): UUID?

expect class UUID(mostSignificantBits: Long,
                  leastSignificantBits: Long) {
    open fun getMostSignificantBits(): Long
    open fun getLeastSignificantBits(): Long
}
