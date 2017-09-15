package org.tobi29.scapes.engine.utils.io.filesystem

import org.tobi29.scapes.engine.utils.InstantMillis

interface FileMetadata

interface FileAttribute : FileMetadata

data class FileBasicMetadata(val type: FileType,
                             val size: Long,
                             val uid: Any?) : FileMetadata

data class FileUID(val high: Long,
                   val low: Long)

data class FileModificationTime(val time: InstantMillis) : FileAttribute

data class FileVisibility(val hidden: Boolean) : FileAttribute

data class UnixPermissionMode(val owner: UnixPermissionModeLevel,
                              val group: UnixPermissionModeLevel,
                              val others: UnixPermissionModeLevel) : FileAttribute {
    constructor(value: Int) : this(
            ((value ushr 6) and 7).toUnixPermissionModeLevel(),
            ((value ushr 3) and 7).toUnixPermissionModeLevel(),
            ((value ushr 0) and 7).toUnixPermissionModeLevel())

    val value: Int
        get() =
            (((owner.value shl 3) or group.value) shl 3) or others.value
}

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

fun Int.toFileType(): FileType =
        when (this) {
            0 -> FileType.TYPE_UNKNOWN
            1 -> FileType.TYPE_REGULAR_FILE
            2 -> FileType.TYPE_DIRECTORY
            3 -> FileType.TYPE_CHARDEV
            4 -> FileType.TYPE_BLOCKDEV
            5 -> FileType.TYPE_FIFO
            6 -> FileType.TYPE_SYMLINK
            7 -> FileType.TYPE_SOCKET
            else -> throw IllegalArgumentException(
                    "Invalid file type: $this")
        }

enum class UnixPermissionModeLevel(val value: Int,
                                   val isExecute: Boolean,
                                   val isWrite: Boolean,
                                   val isRead: Boolean) {
    NONE(0, false, false, false),
    EXECUTE(1, true, false, false),
    WRITE(2, false, true, false),
    WRITE_EXECUTE(3, true, true, false),
    READ(4, false, false, true),
    READ_EXECUTE(5, true, false, true),
    READ_WRITE(6, false, true, true),
    READ_WRITE_EXECUTE(7, true, true, true);

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
