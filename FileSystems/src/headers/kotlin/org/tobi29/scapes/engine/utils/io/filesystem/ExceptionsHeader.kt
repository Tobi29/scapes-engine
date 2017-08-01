package org.tobi29.scapes.engine.utils.io.filesystem

/**
 * the path on which the failed operation was performed.
 */
header val FileSystemException.path: FilePath

/**
 * the second path involved in the operation, if any (for example, the target of
 * a copy or move)
 */
header val FileSystemException.otherPath: FilePath?

header internal fun FileSystemExceptionImpl(path: FilePath,
                                            otherPath: FilePath?,
                                            reason: String?): FileSystemException

header internal fun FileAlreadyExistsExceptionImpl(path: FilePath,
                                                   otherPath: FilePath?,
                                                   reason: String?): FileAlreadyExistsException

header internal fun AccessDeniedExceptionImpl(path: FilePath,
                                              otherPath: FilePath?,
                                              reason: String?): AccessDeniedException

header internal fun NoSuchFileExceptionImpl(path: FilePath,
                                            otherPath: FilePath?,
                                            reason: String?): NoSuchFileException
