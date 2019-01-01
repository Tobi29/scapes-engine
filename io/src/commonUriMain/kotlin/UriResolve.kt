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

import org.tobi29.stdex.InlineUtility

actual fun Uri.resolve(path: Uri): Uri =
    when (path) {
        is UriRelativeNet -> resolve(path)
        is UriRelativePath -> resolve(path)
        else -> path
    }

@Suppress("REDUNDANT_ELSE_IN_WHEN")
actual fun Uri.resolve(path: UriRelativeNet): Uri =
    when (this) {
        is UriAbsolute -> resolve(path)
        is UriRelative -> resolve(path)
        else -> error("Impossible")
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriAbsolute.resolve(path: UriRelativeNet): UriHierarchicalNet =
    UriHierarchicalNet(
        scheme,
        path.userInfo,
        path.host,
        path.portStr,
        path.path,
        path.query,
        path.fragment
    )

@Suppress("REDUNDANT_ELSE_IN_WHEN")
actual fun UriRelative.resolve(path: UriRelativeNet): UriRelativeNet =
    when (this) {
        is UriRelativeNet -> resolve(path)
        is UriRelativePath -> resolve(path)
        else -> error("Impossible")
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelativeNet.resolve(path: UriRelativeNet): UriRelativeNet =
    path

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelativePath.resolve(path: UriRelativeNet): UriRelativeNet =
    path

@Suppress("REDUNDANT_ELSE_IN_WHEN")
actual fun Uri.resolve(path: UriRelativePath): Uri =
    when (this) {
        is UriAbsolute -> resolve(path)
        is UriRelative -> resolve(path)
        else -> error("Impossible")
    }

@Suppress("REDUNDANT_ELSE_IN_WHEN")
actual fun UriAbsolute.resolve(path: UriRelativePath): UriAbsolute =
    when (this) {
        is UriHierarchical -> resolve(path)
        is UriOpaque -> throw IllegalArgumentException("Cannot resolve from opaque URI")
        else -> error("Impossible")
    }

@Suppress("REDUNDANT_ELSE_IN_WHEN")
actual fun UriHierarchical.resolve(path: UriRelativePath): UriHierarchical =
    when (this) {
        is UriHierarchicalAbsolute -> resolve(path)
        is UriHierarchicalNet -> resolve(path)
        else -> error("Impossible")
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriHierarchicalAbsolute.resolve(path: UriRelativePath): UriHierarchicalAbsolute =
    copy(
        path = UriPathEnvironment.run { this@resolve.path.resolve(path.path) },
        query = path.query,
        fragment = path.fragment
    )

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriHierarchicalNet.resolve(path: UriRelativePath): UriHierarchicalNet =
    copy(
        path = if (this.path == null) {
            path.path
        } else {
            UriPathEnvironment.run { this@resolve.path.resolve(path.path) }
        },
        query = path.query,
        fragment = path.fragment
    )

@Suppress("REDUNDANT_ELSE_IN_WHEN")
actual fun UriRelative.resolve(path: UriRelativePath): UriRelative =
    when (this) {
        is UriRelativeNet -> resolve(path)
        is UriRelativePath -> resolve(path)
        else -> error("Impossible")
    }

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelativeNet.resolve(path: UriRelativePath): UriRelativeNet =
    copy(
        path = if (this.path == null) {
            path.path
        } else {
            UriPathEnvironment.run { this@resolve.path.resolve(path.path) }
        },
        query = path.query,
        fragment = path.fragment
    )

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
actual inline fun UriRelativePath.resolve(path: UriRelativePath): UriRelativePath =
    copy(
        path = UriPathEnvironment.run { this@resolve.path.resolve(path.path) },
        query = path.query,
        fragment = path.fragment
    )
