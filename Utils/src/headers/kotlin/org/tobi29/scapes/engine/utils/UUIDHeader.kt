package org.tobi29.scapes.engine.utils

header fun String.toUUID(): UUID?

header class UUID(mostSignificantBits: Long,
                  leastSignificantBits: Long) {
    open fun getMostSignificantBits(): Long
    open fun getLeastSignificantBits(): Long
}
