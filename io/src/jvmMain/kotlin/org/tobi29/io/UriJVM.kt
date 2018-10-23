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
import org.tobi29.stdex.substringAfterLastOrNull

typealias JavaUri = java.net.URI

actual sealed class Uri(val java: JavaUri) {
    actual val fragment: String? get() = java.fragment
}

actual sealed class UriAbsolute(
    java: JavaUri,
    unsafe: Boolean = false
) : Uri(java) {
    init {
        if (!unsafe && !java.isAbsolute)
            throw IllegalArgumentException("URI is not absolute")
    }

    actual val scheme: String get() = java.scheme ?: "" // Crash safety
}

actual sealed class UriHierarchical(
    java: JavaUri,
    unsafe: Boolean = false
) : UriAbsolute(java) {
    init {
        if (!unsafe && java.isOpaque)
            throw IllegalArgumentException("URI is not hierarchical")
    }

    actual open val path: String? get() = java.path
    actual val query: String? get() = java.query
}

actual class UriHierarchicalAbsolute(
    java: JavaUri,
    unsafe: Boolean = false
) : UriHierarchical(java) {
    init {
        if (!unsafe && java.host != null)
            throw IllegalArgumentException("URI has a host")
    }

    actual constructor(
        scheme: String,
        path: String,
        query: String?,
        fragment: String?
    ) : this(JavaUri(scheme, null, path, query, fragment), true)

    actual override val path: String get() = java.path ?: "" // Crash safety

    actual override fun toString(): String = java.toString()

    actual override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is UriHierarchicalAbsolute) return false
        return scheme.equals(other.scheme, false)
                && path == other.path
                && query == other.query
                && fragment == other.fragment
    }

    actual override fun hashCode(): Int {
        var result = scheme.toLowerCase().hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + (query?.hashCode() ?: 0)
        result = 31 * result + (fragment?.hashCode() ?: 0)
        return result
    }
}

actual class UriHierarchicalNet(
    java: JavaUri,
    unsafe: Boolean = false
) : UriHierarchical(java) {
    init {
        if (!unsafe && java.host == null)
            throw IllegalArgumentException("URI has no host")
    }

    actual constructor(
        scheme: String,
        userInfo: String?,
        host: String?,
        port: Int?,
        path: String?,
        query: String?,
        fragment: String?
    ) : this(
        JavaUri(scheme, userInfo, host, port ?: -1, path, query, fragment),
        true
    )

    actual val userInfo: String? get() = java.userInfo
    actual val host: String? get() = java.host
    actual val port: Int? get() = java.port.takeIf { it >= 0 }

    actual override fun toString(): String = java.toString()

    actual override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is UriHierarchicalNet) return false
        return scheme.equals(other.scheme, false)
                && host == other.host
                && port == other.port
                && path == other.path
                && query == other.query
                && fragment == other.fragment
    }

    actual override fun hashCode(): Int {
        var result = scheme.toLowerCase().hashCode()
        result = 31 * result + (host?.hashCode() ?: 0)
        result = 31 * result + (port ?: 0)
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (query?.hashCode() ?: 0)
        result = 31 * result + (fragment?.hashCode() ?: 0)
        return result
    }
}

actual class UriOpaque(
    java: JavaUri,
    unsafe: Boolean = false
) : UriAbsolute(java) {
    init {
        if (!unsafe && !java.isOpaque)
            throw IllegalArgumentException("URI is not opaque")
    }

    actual constructor(
        scheme: String,
        opaque: String,
        fragment: String?
    ) : this(JavaUri(scheme, opaque, fragment), true)

    actual val opaque: String
        get() = java.schemeSpecificPart ?: "" // Crash safety

    actual override fun toString(): String = java.toString()

    actual override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is UriOpaque) return false
        return scheme.equals(other.scheme, false)
                && fragment == other.fragment
    }

    actual override fun hashCode(): Int {
        var result = scheme.toLowerCase().hashCode()
        result = 31 * result + (fragment?.hashCode() ?: 0)
        return result
    }
}

actual sealed class UriRelative(
    java: JavaUri,
    unsafe: Boolean = false
) : Uri(java) {
    init {
        if (!unsafe && java.isAbsolute)
            throw IllegalArgumentException("URI is not relative")
    }

    actual open val path: String? get() = java.path
    actual val query: String? get() = java.query
}

actual class UriRelativeNet(
    java: JavaUri,
    unsafe: Boolean = false
) : UriRelative(java, unsafe) {
    init {
        if (!unsafe && java.host == null)
            throw IllegalArgumentException("URI has no host")
    }

    actual constructor(
        userInfo: String?,
        host: String?,
        port: Int?,
        path: String?,
        query: String?,
        fragment: String?
    ) : this(
        JavaUri(null, userInfo, host, port ?: -1, path, query, fragment),
        true
    )

    actual val userInfo: String? get() = java.userInfo
    actual val host: String? get() = java.host
    actual val port: Int? get() = java.port.takeIf { it >= 0 }

    actual override fun toString(): String = java.toString()

    actual override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is UriRelative) return false
        return path == other.path
                && query == other.query
                && fragment == other.fragment
    }

    actual override fun hashCode(): Int {
        var result = host?.hashCode() ?: 0
        result = 31 * result + (port ?: 0)
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (query?.hashCode() ?: 0)
        result = 31 * result + (fragment?.hashCode() ?: 0)
        return result
    }
}

actual class UriRelativePath(
    java: JavaUri,
    unsafe: Boolean = false
) : UriRelative(java, unsafe) {
    init {
        if (!unsafe && java.host != null)
            throw IllegalArgumentException("URI has a host")
    }

    actual constructor(
        path: String,
        query: String?,
        fragment: String?
    ) : this(JavaUri(null, null, path, query, fragment), true)

    actual override val path: String get() = java.path ?: "" // Crash safety

    actual override fun toString(): String = java.toString()

    actual override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is UriRelative) return false
        return path == other.path
                && query == other.query
                && fragment == other.fragment
    }

    actual override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + (query?.hashCode() ?: 0)
        result = 31 * result + (fragment?.hashCode() ?: 0)
        return result
    }
}

actual fun Uri(str: String): Uri = JavaUri(str).toUri()

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun UriHierarchicalNet.copyImpl(
    scheme: String,
    userInfo: String?,
    host: String?,
    path: String?,
    query: String?,
    fragment: String?
): UriHierarchicalNet = UriHierarchicalNet(
    JavaUri(scheme, java.authority(userInfo, host), path, query, fragment)
)

@PublishedApi
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun UriRelativeNet.copyImpl(
    userInfo: String?,
    host: String?,
    path: String?,
    query: String?,
    fragment: String?
): UriRelativeNet = UriRelativeNet(
    JavaUri(null, java.authority(userInfo, host), path, query, fragment)
)

fun JavaUri.toUri(): Uri =
    if (isAbsolute) {
        if (!isOpaque) {
            if (host == null) UriHierarchicalAbsolute(this)
            else UriHierarchicalNet(this)
        } else UriOpaque(this)
    } else {
        if (host == null) UriRelativePath(this)
        else UriRelativeNet(this)
    }

@PublishedApi
internal fun JavaUri.authority(
    userInfo: String?,
    host: String?
): String {
    val port = portStr?.let { ":$it" } ?: ""
    val convertUri = JavaUri(null, userInfo, host, -1, null, null, null)
    return "${convertUri.authority}$port"
}

@PublishedApi
internal val JavaUri.portStr: String?
    get() = when {
        port >= 0 -> port.toString()
        else -> authority?.substringAfterLastOrNull(':')
    }
