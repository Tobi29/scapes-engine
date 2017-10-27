package org.tobi29.scapes.engine.platform

import org.tobi29.scapes.engine.utils.IS_ANDROID

actual val PLATFORM: Platform = run {
    if (IS_ANDROID) return@run Platform.ANDROID

    val osName = System.getProperty("os.name").toLowerCase()

    if (osName.contains("nux")) return@run Platform.LINUX
    if (osName.contains("mac")) return@run Platform.MACOS
    if (osName.contains("nix")
            || osName.contains("aix")
            || osName.contains("sun")) return@run Platform.UNIX
    if (osName.contains("win")) return@run Platform.WINDOWS

    Platform.UNKNOWN
}
