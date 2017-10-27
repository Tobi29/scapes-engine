package org.tobi29.scapes.engine.utils.io.filesystem

/**
 * the path on which the failed operation was performed.
 */
expect val FileSystemException.path: FilePath

/**
 * the second path involved in the operation, if any (for example, the target of
 * a copy or move)
 */
expect val FileSystemException.otherPath: FilePath?

expect internal fun FileSystemExceptionImpl(path: FilePath,
                                            otherPath: FilePath?,
                                            reason: String?): FileSystemException

expect internal fun FileAlreadyExistsExceptionImpl(path: FilePath,
                                                   otherPath: FilePath?,
                                                   reason: String?): FileAlreadyExistsException

expect internal fun AccessDeniedExceptionImpl(path: FilePath,
                                              otherPath: FilePath?,
                                              reason: String?): AccessDeniedException

expect internal fun NoSuchFileExceptionImpl(path: FilePath,
                                            otherPath: FilePath?,
                                            reason: String?): NoSuchFileException
