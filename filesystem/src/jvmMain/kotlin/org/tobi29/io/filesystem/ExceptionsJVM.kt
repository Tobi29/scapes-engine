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

actual val FileSystemException.path get() = path(file)

actual val FileSystemException.otherPath get() = other?.let { path(it) }

internal actual fun FileSystemExceptionImpl(
    path: FilePath,
    otherPath: FilePath?,
    reason: String?
): FileSystemException =
    FileSystemException(path.toFile(), otherPath?.toFile(), reason)

internal actual fun FileAlreadyExistsExceptionImpl(
    path: FilePath,
    otherPath: FilePath?,
    reason: String?
): FileAlreadyExistsException =
    FileAlreadyExistsException(path.toFile(), otherPath?.toFile(), reason)

internal actual fun AccessDeniedExceptionImpl(
    path: FilePath,
    otherPath: FilePath?,
    reason: String?
): AccessDeniedException =
    AccessDeniedException(path.toFile(), otherPath?.toFile(), reason)

internal actual fun NoSuchFileExceptionImpl(
    path: FilePath,
    otherPath: FilePath?,
    reason: String?
): NoSuchFileException =
    NoSuchFileException(path.toFile(), otherPath?.toFile(), reason)
