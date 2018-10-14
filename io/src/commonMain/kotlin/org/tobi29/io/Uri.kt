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

package org.tobi29.io

import org.tobi29.stdex.InlineUtility

/**
 * Class representing an URI, as specified by
 * http://www.ietf.org/rfc/rfc2396.txt
 *
 * **Note:** Due to implementation details, handling of "invalid" URIs may
 * differ slightly across platforms
 */
expect sealed class Uri {
    val fragment: String?
}

/**
 * Class representing an absolute URI
 */
expect sealed class UriAbsolute : Uri {
    val scheme: String
}

/**
 * Class representing a hierarchical URI
 */
expect sealed class UriHierarchical : UriAbsolute {
    open val path: String?
    val query: String?
}

/**
 * Class representing a hierarchical non-network URI
 */
expect class UriHierarchicalAbsolute(
    scheme: String,
    path: String,
    query: String? = null,
    fragment: String? = null
) : UriHierarchical {
    override val path: String

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalAbsolute.component1(): String = scheme

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalAbsolute.component2(): String = path

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalAbsolute.component3(): String? = query

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalAbsolute.component4(): String? = fragment

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UriHierarchicalAbsolute.copy(
    scheme: String = this.scheme,
    path: String = this.path,
    query: String? = this.query,
    fragment: String? = this.fragment
): UriHierarchicalAbsolute = UriHierarchicalAbsolute(
    scheme, path, query, fragment
)

/**
 * Class representing a hierarchical network URI
 */
expect class UriHierarchicalNet(
    scheme: String,
    userInfo: String? = null,
    host: String? = null,
    port: Int? = null,
    path: String? = null,
    query: String? = null,
    fragment: String? = null
) : UriHierarchical {
    val userInfo: String?
    val host: String?
    val port: Int?

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalNet.component1(): String = scheme

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalNet.component2(): String? = userInfo

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalNet.component3(): String? = host

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalNet.component4(): Int? = port

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalNet.component5(): String? = path

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalNet.component6(): String? = query

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriHierarchicalNet.component7(): String? = fragment

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UriHierarchicalNet.copy(
    scheme: String = this.scheme,
    userInfo: String? = this.userInfo,
    host: String? = this.host,
    port: Int? = this.port,
    path: String? = this.path,
    query: String? = this.query,
    fragment: String? = this.fragment
): UriHierarchicalNet = UriHierarchicalNet(
    scheme, userInfo, host, port, path, query, fragment
)

/**
 * Class representing an opaque URI
 */
expect class UriOpaque(
    scheme: String,
    opaque: String,
    fragment: String? = null
) : UriAbsolute {
    val opaque: String

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriOpaque.component1(): String = scheme

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriOpaque.component2(): String = opaque

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriOpaque.component3(): String? = fragment

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UriOpaque.copy(
    scheme: String = this.scheme,
    opaque: String = this.opaque,
    fragment: String? = this.fragment
): UriOpaque = UriOpaque(
    scheme, opaque, fragment
)

/**
 * Class representing a relative URI
 */
expect sealed class UriRelative : Uri {
    open val path: String?
    val query: String?
}

/**
 * Class representing a relative URI
 */
expect class UriRelativeNet(
    userInfo: String? = null,
    host: String? = null,
    port: Int? = null,
    path: String? = null,
    query: String? = null,
    fragment: String? = null
) : UriRelative {
    val userInfo: String?
    val host: String?
    val port: Int?

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriRelativeNet.component1(): String? = userInfo

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriRelativeNet.component2(): String? = host

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriRelativeNet.component3(): Int? = port

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriRelativeNet.component4(): String? = query

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriRelativeNet.component5(): String? = fragment

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UriRelativeNet.copy(
    userInfo: String? = this.userInfo,
    host: String? = this.host,
    port: Int? = this.port,
    path: String? = this.path,
    query: String? = this.query,
    fragment: String? = this.fragment
): UriRelativeNet = UriRelativeNet(
    userInfo, host, port, path, query, fragment
)

/**
 * Class representing a relative URI
 */
expect class UriRelativePath(
    path: String,
    query: String? = null,
    fragment: String? = null
) : UriRelative {
    override val path: String

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriRelativePath.component1(): String? = query

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline operator fun UriRelativePath.component2(): String? = fragment

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun UriRelativePath.copy(
    path: String = this.path,
    query: String? = this.query,
    fragment: String? = this.fragment
): UriRelativePath = UriRelativePath(
    path, query, fragment
)

/**
 * Parses the given URI
 *
 * **Note:** Due to implementation details, handling of "invalid" URIs may
 * differ slightly across platforms
 * @param str The URI to parse
 * @throws IllegalArgumentException When an "invalid" URI is given
 * @return Some kind of [Uri], depending on the format of the URI
 */
expect fun Uri(str: String): Uri

expect fun Uri.resolve(path: Uri): Uri

expect fun Uri.resolve(path: UriRelativeNet): Uri

expect fun UriAbsolute.resolve(path: UriRelativeNet): UriHierarchicalNet

expect fun UriRelative.resolve(path: UriRelativeNet): UriRelativeNet

expect fun UriRelativeNet.resolve(path: UriRelativeNet): UriRelativeNet

expect fun UriRelativePath.resolve(path: UriRelativeNet): UriRelativeNet

expect fun Uri.resolve(path: UriRelativePath): Uri

expect fun UriAbsolute.resolve(path: UriRelativePath): UriAbsolute

expect fun UriHierarchical.resolve(path: UriRelativePath): UriHierarchical

expect fun UriHierarchicalAbsolute.resolve(path: UriRelativePath): UriHierarchicalAbsolute

expect fun UriHierarchicalNet.resolve(path: UriRelativePath): UriHierarchicalNet

expect fun UriRelative.resolve(path: UriRelativePath): UriRelative

expect fun UriRelativeNet.resolve(path: UriRelativePath): UriRelativeNet

expect fun UriRelativePath.resolve(path: UriRelativePath): UriRelativePath
