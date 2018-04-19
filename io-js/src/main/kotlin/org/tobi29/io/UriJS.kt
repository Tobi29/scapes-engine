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

import org.tobi29.utils.toString
import org.tobi29.stdex.alphabetLatinLowercase
import org.tobi29.stdex.assert
import org.tobi29.stdex.digitsArabic

actual sealed class Uri(actual val fragment: String?)

actual sealed class UriAbsolute(
        actual val scheme: String,
        fragment: String?
) : Uri(fragment)

actual sealed class UriHierarchical(
        scheme: String,
        path: String?,
        actual val query: String?,
        fragment: String?
) : UriAbsolute(scheme, fragment) {
    init {
        scheme.uriSchemeVerify()
        path?.uriPathAbsoluteVerify()
    }

    @Suppress("CanBePrimaryConstructorProperty") // Keep parameter for verify
    actual open val path: String? = path
}

actual class UriHierarchicalAbsolute actual constructor(
        scheme: String,
        actual override val path: String,
        query: String?,
        fragment: String?
) : UriHierarchical(scheme, path, query, fragment) {
    actual override fun toString(): String =
            "$scheme:${path.uriEscapePath()
            }${query?.uriEscapeQuery()?.let { "?$it" } ?: ""
            }${fragment?.uriEscapeBase()?.let { "#$it" } ?: ""}"

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
        scheme: String,
        actual val userInfo: String?,
        actual val host: String?,
        private val portStr: String?,
        path: String?,
        query: String?,
        fragment: String?
) : UriHierarchical(scheme, path, query, fragment) {
    actual constructor(
            scheme: String,
            userInfo: String?,
            host: String?,
            port: Int?,
            path: String?,
            query: String?,
            fragment: String?
    ) : this(scheme, userInfo, host, port?.toString(), path, query, fragment)

    actual val port: Int? get() = portStr?.toInt()?.takeIf { it in 0x0 until 0xFFFF }

    actual override fun toString(): String =
            "$scheme://${userInfo?.uriEscapeUserInfo()?.let { "$it@" } ?: ""
            }${host?.uriEscapeHost() ?: ""
            }${portStr?.let { ":$it" } ?: ""
            }${path?.uriEscapePath() ?: ""
            }${query?.uriEscapeQuery()?.let { "?$it" } ?: ""
            }${fragment?.uriEscapeBase()?.let { "#$it" } ?: ""}"

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

actual class UriOpaque actual constructor(
        scheme: String,
        actual val opaque: String,
        fragment: String?
) : UriAbsolute(scheme, fragment) {
    actual override fun toString(): String =
            "$scheme:${opaque.uriEscapeOpaque()
            }${fragment?.uriEscapeBase()?.let { "#$it" } ?: ""}"

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

actual class UriRelative actual constructor(
        actual val path: String,
        actual val query: String?,
        fragment: String?
) : Uri(fragment) {
    init {
        path.uriPathRelativeVerify()
    }

    actual override fun toString(): String =
            "${path.uriEscapePath()
            }${query?.uriEscapeQuery()?.let { "?$it" } ?: ""
            }${fragment?.uriEscapeBase()?.let { "#$it" } ?: ""}"

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

actual fun Uri(str: String): Uri {
    uriParseHierarchicalNet.matchEntire(str)?.let { match ->
        return UriHierarchicalNet(match.groups[1]?.value!!,
                match.groups[4]?.value, match.groups[5]?.value,
                match.groups[7]?.value?.toIntOrNull(), match.groups[8]?.value,
                match.groups[10]?.value, match.groups[12]?.value)
    }
    uriParseHierarchicalAbsolute.matchEntire(str)?.let { match ->
        return UriHierarchicalAbsolute(match.groups[1]?.value!!,
                match.groups[2]?.value!!, match.groups[4]?.value,
                match.groups[6]?.value)
    }
    uriParseOpaque.matchEntire(str)?.let { match ->
        return UriOpaque(match.groups[1]?.value!!,
                match.groups[2]?.value!!, match.groups[4]?.value)
    }
    uriParseRelative.matchEntire(str)?.let { match ->
        return UriRelative(match.groups[1]?.value!!,
                match.groups[3]?.value, match.groups[5]?.value)
    }
    throw IllegalArgumentException("Invalid url: $str")
}

private const val uriScheme = """([^:/?#]*)"""
private const val uriUserInfo = """([^@/?#]*)@"""
private const val uriHost = """([^:/?#]+)"""
private const val uriPort = """:([^/?#]*)"""
private const val uriPath = """(/[^?#]*)"""
private const val uriQuery = """\?([^#]*)"""
private const val uriOpaque = """([^/#][^#]*)"""
private const val uriRelativePath = """([^/?#][^?#]*)"""
private const val uriFragment = """#(.*)"""

private val uriParseHierarchicalAbsolute =
        "$uriScheme:$uriPath($uriQuery)?($uriFragment)?".toRegex()
// Scheme : Path : ?Query : Query : #Fragment : Fragment
private val uriParseHierarchicalNet =
        "$uriScheme://(($uriUserInfo)?$uriHost($uriPort)?)?$uriPath?($uriQuery)?($uriFragment)?".toRegex()
// Scheme : UserInfo@Host:Port : UserInfo@ : UserInfo : Host : :Port : Port : Path : ?Query : Query : #Fragment : Fragment
private val uriParseOpaque =
        "$uriScheme:$uriOpaque($uriFragment)?".toRegex()
// Scheme : Opaque : #Fragment : Fragment
private val uriParseRelative =
        "$uriRelativePath($uriQuery)?($uriFragment)?".toRegex()
// Path : ?Query : Query : #Fragment : Fragment

private fun String.uriSchemeVerify() {
    toLowerCase().find { it !in "$alphabetLatinLowercase$digitsArabic+.-" }?.let {
        throw IllegalArgumentException("Invalid character in scheme: $it")
    }
}

private fun String.uriPathAbsoluteVerify() {
    if (this[0] != '/') throw IllegalArgumentException("Path is relative")
}

private fun String.uriPathRelativeVerify() {
    if (this[0] == '/') throw IllegalArgumentException("Path is absolute")
}

private fun String.uriEscapeUserInfo() = uriEscape(
        ::uriRequireEscapeUserInfo)

private fun uriRequireEscapeUserInfo(char: Char): Boolean =
        uriRequireEscapeBase(char) || char in ";,:@?/"

private fun String.uriEscapeHost() = uriEscape(
        ::uriRequireEscapeHost)

private fun uriRequireEscapeHost(char: Char): Boolean =
        uriRequireEscapeBase(char) || char in ";,:@?/"

private fun String.uriEscapePath() = uriEscape(
        ::uriRequireEscapePath)

private fun uriRequireEscapePath(char: Char): Boolean =
        uriRequireEscapeBase(char) || char in "?"

private fun String.uriEscapeQuery() = uriEscape(
        ::uriRequireEscapeQuery)

private fun uriRequireEscapeQuery(char: Char): Boolean =
        uriRequireEscapeBase(char) || char in ";/?:@&=+,\$"

private fun String.uriEscapeOpaque() = uriEscape(
        ::uriRequireEscapeOpaque)

private fun uriRequireEscapeOpaque(char: Char): Boolean =
        uriRequireEscapeBase(char) || char in "?"

private fun String.uriEscapeBase() = uriEscape(::uriRequireEscapeBase)

private fun uriRequireEscapeBase(char: Char): Boolean =
        char.toInt() < 0x1F || char in " <>#%\"{}|\\^[]`"

private inline fun String.uriEscape(require: (Char) -> Boolean): String {
    val output = StringBuilder(length)
    for (char in this) {
        if (require(char)) output.append(char.uriEscape())
        else output.append(char)
    }
    return output.toString()
}

private fun Char.uriEscape(): String {
    val i = toInt()
    assert { i < 256 }
    return "%${i.toString(16, 2).toUpperCase()}"
}
