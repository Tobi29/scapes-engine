package org.tobi29.scapes.engine.utils.io

typealias AutoCloseable = java.lang.AutoCloseable

typealias Closeable = java.io.Closeable

inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Throwable) {
        closed = true
        if (this != null) {
            try {
                close()
            } catch (closeException: Throwable) {
                // e.addSuppressed(closeException)
            }
        }
        throw e
    } finally {
        if (this != null && !closed) {
            close()
        }
    }
}
