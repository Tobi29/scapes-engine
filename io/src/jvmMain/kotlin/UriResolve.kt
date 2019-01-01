/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.io

import org.tobi29.stdex.PlatformProvidedImplementation

@PublishedApi
@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
internal inline fun Uri.resolveImpl(path: Uri): Uri =
    java.resolve(path.java).toUri()

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun Uri.resolve(path: Uri): Uri =
    resolveImpl(path)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun Uri.resolve(path: UriRelativeNet): Uri =
    resolveImpl(path)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriAbsolute.resolve(path: UriRelativeNet): UriHierarchicalNet =
    resolveImpl(path) as UriHierarchicalNet

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelative.resolve(path: UriRelativeNet): UriRelativeNet =
    resolveImpl(path) as UriRelativeNet

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelativeNet.resolve(path: UriRelativeNet): UriRelativeNet =
    resolveImpl(path) as UriRelativeNet

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelativePath.resolve(path: UriRelativeNet): UriRelativeNet =
    resolveImpl(path) as UriRelativeNet

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun Uri.resolve(path: UriRelativePath): Uri =
    resolveImpl(path)

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriAbsolute.resolve(path: UriRelativePath): UriAbsolute =
    resolveImpl(path) as UriAbsolute

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriHierarchical.resolve(path: UriRelativePath): UriHierarchical =
    resolveImpl(path) as UriHierarchical

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriHierarchicalAbsolute.resolve(path: UriRelativePath): UriHierarchicalAbsolute =
    resolveImpl(path) as UriHierarchicalAbsolute

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriHierarchicalNet.resolve(path: UriRelativePath): UriHierarchicalNet =
    resolveImpl(path) as UriHierarchicalNet

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelative.resolve(path: UriRelativePath): UriRelative =
    resolveImpl(path) as UriRelative

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelativeNet.resolve(path: UriRelativePath): UriRelativeNet =
    resolveImpl(path) as UriRelativeNet

@PlatformProvidedImplementation
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelativePath.resolve(path: UriRelativePath): UriRelativePath =
    resolveImpl(path) as UriRelativePath
