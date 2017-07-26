package org.tobi29.scapes.engine.utils.io.filesystem

enum class FileType(val value: Int) {
    TYPE_UNKNOWN(0),
    TYPE_REGULAR_FILE(1),
    TYPE_DIRECTORY(2),
    TYPE_CHARDEV(3),
    TYPE_BLOCKDEV(4),
    TYPE_FIFO(5),
    TYPE_SYMLINK(6),
    TYPE_SOCKET(7)
}

sealed class FileAttribute

data class UnixPermissionMode(val owner: UnixPermissionModeLevel,
                              val group: UnixPermissionModeLevel,
                              val everyone: UnixPermissionModeLevel) : FileAttribute() {
    constructor(value: Int) : this(
            ((value ushr 6) and 7).toUnixPermissionModeLevel(),
            ((value ushr 3) and 7).toUnixPermissionModeLevel(),
            ((value ushr 0) and 7).toUnixPermissionModeLevel())

    val value: Int get() =
    (((owner.value shl 3) or group.value) shl 3) or everyone.value
}

enum class UnixPermissionModeLevel(val value: Int) {
    NONE(0),
    EXECUTE(1),
    WRITE(2),
    WRITE_EXECUTE(3),
    READ(4),
    READ_EXECUTE(5),
    READ_WRITE(6),
    READ_WRITE_EXECUTE(7);

}

fun Int.toUnixPermissionModeLevel(): UnixPermissionModeLevel =
        when (this) {
            0 -> UnixPermissionModeLevel.NONE
            1 -> UnixPermissionModeLevel.EXECUTE
            2 -> UnixPermissionModeLevel.WRITE
            3 -> UnixPermissionModeLevel.WRITE_EXECUTE
            4 -> UnixPermissionModeLevel.READ
            5 -> UnixPermissionModeLevel.READ_EXECUTE
            6 -> UnixPermissionModeLevel.READ_WRITE
            7 -> UnixPermissionModeLevel.READ_WRITE_EXECUTE
            else -> throw IllegalArgumentException(
                    "Invalid unix mode column: $this")
        }
