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

package org.tobi29.scapes.engine.utils.io

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
expect class UriHierarchicalAbsolute(scheme: String,
                                     path: String,
                                     query: String?,
                                     fragment: String?) : UriHierarchical {
    override val path: String

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

/**
 * Class representing a hierarchical network URI
 */
expect class UriHierarchicalNet(scheme: String,
                                userInfo: String?,
                                host: String?,
                                port: Int?,
                                path: String?,
                                query: String?,
                                fragment: String?) : UriHierarchical {
    val userInfo: String?
    val host: String?
    val port: Int?

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

/**
 * Class representing an opaque URI
 */
expect class UriOpaque(scheme: String,
                       opaque: String,
                       fragment: String?) : UriAbsolute {
    val opaque: String

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

/**
 * Class representing a relative URI
 */
expect class UriRelative(path: String,
                         query: String?,
                         fragment: String?) : Uri {
    val path: String
    val query: String?

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

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
