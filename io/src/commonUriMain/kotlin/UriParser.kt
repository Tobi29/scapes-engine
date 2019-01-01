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

import org.tobi29.stdex.Constant

actual fun Uri(str: String): Uri {
    uriParseHierarchicalNet.matchEntire(str)?.let { match ->
        return UriHierarchicalNet(
            match.groups[1]?.value!!,
            match.groups[4]?.value, match.groups[5]?.value,
            match.groups[7]?.value?.toIntOrNull(), match.groups[8]?.value,
            match.groups[10]?.value, match.groups[12]?.value
        )
    }
    uriParseHierarchicalAbsolute.matchEntire(str)?.let { match ->
        return UriHierarchicalAbsolute(
            match.groups[1]?.value!!,
            match.groups[2]?.value!!, match.groups[4]?.value,
            match.groups[6]?.value
        )
    }
    uriParseOpaque.matchEntire(str)?.let { match ->
        return UriOpaque(
            match.groups[1]?.value!!,
            match.groups[2]?.value!!, match.groups[4]?.value
        )
    }
    uriParseRelativeNet.matchEntire(str)?.let { match ->
        return UriRelativeNet(
            match.groups[3]?.value, match.groups[4]?.value,
            match.groups[6]?.value?.toIntOrNull(), match.groups[7]?.value,
            match.groups[9]?.value, match.groups[11]?.value
        )
    }
    uriParseRelativePath.matchEntire(str)?.let { match ->
        return UriRelativePath(
            match.groups[1]?.value!!,
            match.groups[3]?.value, match.groups[5]?.value
        )
    }
    throw IllegalArgumentException("Invalid url: $str")
}

@Constant
private inline val uriScheme
    get() = """([^:/?#]*)"""
@Constant
private inline val uriUserInfo
    get() = """([^@/?#]*)@"""
@Constant
private inline val uriHost
    get() = """([^:/?#]+)"""
@Constant
private inline val uriPort
    get() = """:([^/?#]*)"""
@Constant
private inline val uriPath
    get() = """(/[^?#]*)"""
@Constant
private inline val uriQuery
    get() = """\?([^#]*)"""
@Constant
private inline val uriOpaque
    get() = """([^/#][^#]*)"""
@Constant
private inline val uriRelativePath
    get() = """([^?#]*)"""
@Constant
private inline val uriFragment
    get() = """#(.*)"""

private val uriParseHierarchicalAbsolute =
    "$uriScheme:$uriPath($uriQuery)?($uriFragment)?".toRegex()
// Scheme : Path : ?Query : Query : #Fragment : Fragment

private val uriParseHierarchicalNet =
    "$uriScheme://(($uriUserInfo)?$uriHost($uriPort)?)?$uriPath?($uriQuery)?($uriFragment)?".toRegex()
// Scheme : UserInfo@Host:Port : UserInfo@ : UserInfo : Host : :Port : Port : Path : ?Query : Query : #Fragment : Fragment

private val uriParseOpaque =
    "$uriScheme:$uriOpaque($uriFragment)?".toRegex()
// Scheme : Opaque : #Fragment : Fragment

private val uriParseRelativeNet =
    "//(($uriUserInfo)?$uriHost($uriPort)?)?$uriPath?($uriQuery)?($uriFragment)?".toRegex()
// UserInfo@Host:Port : UserInfo@ : UserInfo : Host : :Port : Port : Path : ?Query : Query : #Fragment : Fragment

private val uriParseRelativePath =
    "$uriRelativePath($uriQuery)?($uriFragment)?".toRegex()
// Path : ?Query : Query : #Fragment : Fragment
