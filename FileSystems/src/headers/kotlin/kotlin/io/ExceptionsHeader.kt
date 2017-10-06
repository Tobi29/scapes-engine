package kotlin.io

import java.io.IOException

@Suppress("HEADER_WITHOUT_IMPLEMENTATION", "NO_ACTUAL_FOR_EXPECT")
header abstract class FileSystemException(deny: Nothing) : IOException

@Suppress("HEADER_WITHOUT_IMPLEMENTATION", "NO_ACTUAL_FOR_EXPECT")
header abstract class FileAlreadyExistsException(deny: Nothing) : FileSystemException

@Suppress("HEADER_WITHOUT_IMPLEMENTATION", "NO_ACTUAL_FOR_EXPECT")
header abstract class AccessDeniedException(deny: Nothing) : FileSystemException

@Suppress("HEADER_WITHOUT_IMPLEMENTATION", "NO_ACTUAL_FOR_EXPECT")
header abstract class NoSuchFileException(deny: Nothing) : FileSystemException
