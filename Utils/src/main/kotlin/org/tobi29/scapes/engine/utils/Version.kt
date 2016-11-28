/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils

import java.util.regex.Pattern

/**
 * Class representing a semantic version
 */
data class Version(
        val major: Int = 0,
        val minor: Int = 0,
        val revision: Int = 0) {
    override fun toString(): kotlin.String {
        return "$major.$minor.$revision"
    }
}

private val DOT = Pattern.compile("\\.")

/**
 * Compares two [Version] instances and returns a [Comparison] as
 * result
 *
 * If `check` is higher than `current` it will return as "higher"
 *
 * @param current Primary version used as baseline
 * @param check Secondary version
 */
fun compare(current: Version,
            check: Version): Comparison {
    if (check.major > current.major) {
        return Comparison.HIGHER_MAJOR
    } else if (check.major < current.major) {
        return Comparison.LOWER_MAJOR
    } else if (check.minor > current.minor) {
        return Comparison.HIGHER_MINOR
    } else if (check.minor < current.minor) {
        return Comparison.LOWER_MINOR
    } else if (check.revision > current.revision) {
        return Comparison.HIGHER_REVISION
    } else if (check.revision < current.revision) {
        return Comparison.LOWER_REVISION
    }
    return Comparison.EQUAL
}

/**
 * Constructs a [Version] instance from the given [String]
 *
 * **Note**: if `str` is from an unreliable source, consider using [versionParse]
 * @param str Version in x.x.x_x format
 * @throws IllegalArgumentException When the parsing failed
 */
fun version(str: kotlin.String): Version {
    try {
        return versionParse(str)
    } catch (e: VersionException) {
        throw IllegalArgumentException(e)
    }
}

/**
 * Constructs a [Version] instance from the given [String]
 * @param str Version in x.x.x_x format
 * @throws VersionException When the parsing failed
 */
@Throws(VersionException::class)
fun versionParse(str: kotlin.String): Version {
    val split = DOT.split(str)
    if (split.size > 3) {
        throw VersionException("Too many delimiters: $str")
    }
    if (split.isEmpty()) {
        throw VersionException("Weird string: $str")
    }
    val major: Int
    var minor = 0
    var revision = 0
    try {
        major = split[0].toInt()
    } catch (e: NumberFormatException) {
        throw VersionException("Invalid major: ${split[0]}", e)
    }
    if (split.size >= 2) {
        try {
            minor = split[1].toInt()
        } catch (e: NumberFormatException) {
            throw VersionException("Invalid minor: ${split[1]}", e)
        }
        if (split.size == 3) {
            try {
                revision = split[2].toInt()
            } catch (e: NumberFormatException) {
                throw VersionException("Invalid revision: ${split[2]}", e)
            }
        }
    }
    return Version(major, minor, revision)
}

enum class Comparison constructor(val level: Int) {
    LOWER_MAJOR(-4),
    LOWER_MINOR(-3),
    LOWER_REVISION(-2),
    LOWER_BUILD(-1),
    EQUAL(0),
    HIGHER_BUILD(1),
    HIGHER_REVISION(2),
    HIGHER_MINOR(3),
    HIGHER_MAJOR(4);

    fun atLeast(other: Comparison): Boolean {
        return level >= other.level
    }

    fun atMost(other: Comparison): Boolean {
        return level <= other.level
    }

    fun `in`(lower: Comparison,
             upper: Comparison): Boolean {
        return level >= lower.level && level <= upper.level
    }
}

class VersionException : Exception {
    constructor(message: kotlin.String) : super(message) {
    }

    constructor(message: kotlin.String, cause: Throwable) : super(message,
            cause) {
    }
}
