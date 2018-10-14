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

import org.tobi29.stdex.ArrayDeque

/**
 * Environment for operating on string-based paths
 */
interface PathEnvironment {
    /**
     * Resolve a path based on an existing path
     * @receiver The path to resolve with
     * @param path The path to resolve
     * @return An encoded path by combining the two given paths
     */
    fun String.resolve(path: String): String

    /**
     * Parent directory of given path, not taking links into account or `null`
     * in case no parent is available
     */
    val String.parent: String?

    /**
     * Returns the components of the given encoded path
     *
     * **Note:** Absolute and relative paths look identical in this
     * representation
     * @receiver The path to get the components from
     */
    val String.components: List<String>
}

/**
 * Environment for operating on string-based file paths
 */
interface FilePathEnvironment : PathEnvironment {
    /**
     * Append a relative path to an encoded path
     * @receiver The path to normalize
     * @return An encoded path by combining the two given paths
     */
    fun String.normalize(): String

    /**
     * File name of the given encoded path
     */
    val String.fileName: String?

    /**
     * `true` if the given encoded path is absolute
     */
    val String.isAbsolute: Boolean
}

/**
 * Sane default path environment
 */
abstract class StandardPathEnvironment : PathEnvironment {
    /**
     * Separator between path components
     */
    abstract val separator: String

    /**
     * Whether or not trailing separators should be removed on sanitize
     */
    open val stripTrailingSeparator: Boolean get() = true

    /**
     * Whether or not the parent of root is the root again
     */
    open val recursiveRoot: Boolean get() = true

    protected open tailrec fun String.sanitize(): String {
        val sanitized = replace("$separator$separator", separator)
        return when {
            this == separator -> this
            this == sanitized ->
                if (stripTrailingSeparator) removeSuffix(separator) else this
            else -> sanitized.sanitize()
        }
    }

    override fun String.resolve(path: String): String {
        val left = sanitize()
        val right = path.sanitize()
        return if (left.isEmpty() || right.startsWith(separator)) {
            right
        } else if (left.endsWith(separator)) {
            "$left$right"
        } else {
            "$left$separator$right"
        }
    }

    override val String.parent
        get() = sanitize().let {
            val index = it.lastIndexOf(separator)
            if (index < 0 || it == separator) {
                null
            } else if (index == 0) {
                separator
            } else {
                it.substring(0, index)
            }
        }

    override val String.components
        get() = sanitize().split(separator)
}

/**
 * Sane default path environment supporting absolute paths as well as `.` and `..` as used in unix paths
 */
abstract class StandardFilePathEnvironment : StandardPathEnvironment(),
    FilePathEnvironment {
    override fun String.normalize(): String {
        val path = sanitize()
        val root = path.startsWith(UriPathEnvironment.separator)
        val directory = path.endsWith(UriPathEnvironment.separator)
        val components = path.components
        val stack = ArrayDeque<String>(components.size)
        var forceDirectory = false
        for (component in components) {
            when (component) {
                "." -> {
                    forceDirectory = true
                }
                ".." -> {
                    val last = stack.pollLast()
                    if (last == null) {
                        if (!root || !recursiveRoot) {
                            stack.add(component)
                        }
                    } else if (last == component) {
                        stack.add(component)
                        stack.add(component)
                    }
                    forceDirectory = true
                }
                else -> {
                    stack.add(component)
                    forceDirectory = false
                }
            }
        }
        val normalized = stack.joinToString(UriPathEnvironment.separator)
        forceDirectory = (directory || forceDirectory) && stack.isNotEmpty()
        forceDirectory = forceDirectory && !stripTrailingSeparator
        return "${if (root) UriPathEnvironment.separator else ""
        }$normalized${if (forceDirectory) UriPathEnvironment.separator else ""}"
    }

    fun String.relativize(other: String): String? {
        val base = sanitize()
        val destination = other.sanitize()
        if (base == destination) {
            return ""
        }
        val rootBase = base.isAbsolute
        val rootDestination = destination.isAbsolute
        if (rootBase != rootDestination) {
            return null
        }
        val common = findCommonRoot(base, destination)
        val componentsBase = base.components
        val componentsDestination = destination.components
        val backtrack =
            (common.size until componentsBase.size).asSequence().map { ".." }
        val destinationRelative = componentsDestination.subList(
            common.size,
            componentsDestination.size
        ).asSequence()
        return (backtrack + destinationRelative).joinToString(separator)
    }

    override val String.fileName
        get() = lastIndexOf(separator).let {
            if (it < 0) {
                separator
            } else if (it == 0 && this == separator) {
                null
            } else {
                substring(it + 1)
            }
        }

    override val String.isAbsolute get() = startsWith(separator)

    override val String.components
        get() = sanitize().removePrefix(separator).removeSuffix(separator).let {
            if (isEmpty()) {
                emptyList()
            } else {
                it.split(separator)
            }
        }

    private fun findCommonRoot(
        first: String,
        second: String
    ): List<String> {
        val rootFirst = first.isAbsolute
        val rootSecond = second.isAbsolute
        if (rootFirst != rootSecond) {
            throw IllegalArgumentException("'other' is different type of Path")
        }
        val componentsFirst = if (rootFirst) {
            first.substring(separator.length)
        } else {
            first
        }.split(separator)
        val componentsSecond = if (rootSecond) {
            second.substring(separator.length)
        } else {
            second
        }.split(separator)
        val length = componentsFirst.size.coerceAtMost(componentsSecond.size)
        var i = 0
        while (i < length && componentsFirst[i] == componentsSecond[i]) {
            i++
        }
        return componentsFirst.subList(0, i)
    }
}

/**
 * Path environment that emulates unix paths
 */
object UnixPathEnvironment : StandardFilePathEnvironment() {
    override val separator get() = "/"
}

/**
 * Path environment that emulates uri paths
 */
object UriPathEnvironment : StandardFilePathEnvironment() {
    override val separator get() = "/"
    override val stripTrailingSeparator get() = false
    override val recursiveRoot get() = false

    override tailrec fun String.sanitize(): String {
        val sanitized = replace("$separator$separator", separator)
        return when {
            this == separator || this == sanitized -> this
            else -> sanitized.sanitize()
        }
    }

    override fun String.resolve(path: String): String {
        val base = if (endsWith(separator)) {
            this
        } else {
            val index = lastIndexOf(separator)
            if (index == -1) "" else substring(0, index + separator.length)
        }
        val left = base.sanitize()
        val right = path.sanitize()
        return if (right == ".") {
            left
        } else if (left.isEmpty() || right.startsWith(separator)) {
            right
        } else if (left.endsWith(separator)) {
            "$left$right"
        } else {
            "$left$separator$right"
        }.normalize()
    }
}
