/*
 * Copyright 2012-2017 Tobi29
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
