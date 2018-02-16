/*
 * Copyright 2012-2018 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.io.filesystem

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

/**
 * the path on which the failed operation was performed.
 */
expect val FileSystemException.path: FilePath

/**
 * the second path involved in the operation, if any (for example, the target of
 * a copy or move)
 */
expect val FileSystemException.otherPath: FilePath?

internal expect fun FileSystemExceptionImpl(
    path: FilePath,
    otherPath: FilePath?,
    reason: String?
): FileSystemException

internal expect fun FileAlreadyExistsExceptionImpl(
    path: FilePath,
    otherPath: FilePath?,
    reason: String?
): FileAlreadyExistsException

internal expect fun AccessDeniedExceptionImpl(
    path: FilePath,
    otherPath: FilePath?,
    reason: String?
): AccessDeniedException

internal expect fun NoSuchFileExceptionImpl(
    path: FilePath,
    otherPath: FilePath?,
    reason: String?
): NoSuchFileException
