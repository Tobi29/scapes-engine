package org.tobi29.scapes.engine.platform

/**
 * Enum containing all platforms known that may be relevant to a running
 * application using this api
 */
enum class Platform {
    /**
     * GNU/Linux system
     */
    LINUX,

    /**
     * macOS system
     */
    MACOS,

    /**
     * Non-Linux, non-macOS Unix system, e.g. BSDs, Solaris, etc.
     */
    UNIX,

    /**
     * Something that needs to go.
     */
    WINDOWS,

    /**
     * Android system
     */
    ANDROID,

    /**
     * iOS system (either through Kotlin/Native or Multi OS Engine)
     */
    IOS,

    /**
     * Something that luckily never made it anywhere so far. Also needs to go.
     */
    WINDOWS_PHONE,

    /**
     * An unsupported system, consider reporting info on how to detect it if any
     * device returns this
     */
    UNKNOWN
}
