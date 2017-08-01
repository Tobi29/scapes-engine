package org.tobi29.scapes.engine.utils.io.filesystem

/**
 * A base exception class for file system exceptions.
 */
fun FileSystemException(path: FilePath,
                        otherPath: FilePath? = null,
                        reason: String? = null): FileSystemException =
        FileSystemExceptionImpl(path, otherPath, reason)

/**
 * An exception class which is used when some file to create or copy to already exists.
 */
fun FileAlreadyExistsException(path: FilePath,
                               otherPath: FilePath? = null,
                               reason: String? = null): FileSystemException =
        FileAlreadyExistsExceptionImpl(path, otherPath, reason)

/**
 * An exception class which is used when we have not enough access for some operation.
 */
fun AccessDeniedException(path: FilePath,
                          otherPath: FilePath? = null,
                          reason: String? = null): FileSystemException =
        AccessDeniedExceptionImpl(path, otherPath, reason)

/**
 * An exception class which is used when file to copy does not exist.
 */
fun NoSuchFileException(path: FilePath,
                        otherPath: FilePath? = null,
                        reason: String? = null): FileSystemException =
        NoSuchFileExceptionImpl(path, otherPath, reason)
