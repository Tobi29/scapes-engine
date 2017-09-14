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

package org.tobi29.scapes.engine.utils

/**
 * Class representing a semantic version
 */
data class Version(
        /**
         * Major version component
         */
        val major: Int = 0,
        /**
         * Minor version component
         */
        val minor: Int = 0,
        /**
         * Revision version component
         */
        val revision: Int = 0) {
    override fun toString(): String = "$major.$minor.$revision"

    /**
     * Comparison results when comparing versions
     */
    enum class Comparison(
            /**
             * Level of comparison, absolute value describes magnitude and sign
             * describes direction
             */
            val level: Int) {
        /**
         * Major version component is lower
         */
        LOWER_MAJOR(-3),
        /**
         * Minor version component is lower
         */
        LOWER_MINOR(-2),
        /**
         * Revision version component is lower
         */
        LOWER_REVISION(-1),
        /**
         * Versions are equal
         */
        EQUAL(0),
        /**
         * Revision version component is higher
         */
        HIGHER_REVISION(1),
        /**
         * Minor version component is higher
         */
        HIGHER_MINOR(2),
        /**
         * Major version component is higher
         */
        HIGHER_MAJOR(3);

        /**
         * Returns `true` if the given comparison has at least the [level] of
         * [other]
         *
         * @param other The minimum level to check for
         * @return `true` if `this.level >= other.level`
         */
        infix fun atLeast(other: Comparison): Boolean = level >= other.level

        /**
         * Returns `true` if the given comparison has at most the [level] of
         * [other]
         *
         * @param other The maximum level to check for
         * @return `true` if `this.level <= other.level`
         */
        infix fun atMost(other: Comparison): Boolean = level <= other.level

        /**
         * Returns `true` if the given comparison has a [level] between [lower]
         * and [upper]
         *
         * @param lower The minimum level to check for
         * @param upper The maximum level to check for
         * @return `true` if `lower.level <= this.level <= upper.level`
         */
        fun inside(lower: Comparison,
                   upper: Comparison): Boolean =
                atLeast(lower) && atMost(upper)
    }
}

/**
 * Compares two [Version] instances and returns a [Version.Comparison] as
 * result
 *
 * If `check` is higher than `current` it will return as "higher"
 * @param current Primary version used as baseline
 * @param check Secondary version
 */
fun compare(current: Version,
            check: Version): Version.Comparison = when {
    check.major > current.major -> Version.Comparison.HIGHER_MAJOR
    check.major < current.major -> Version.Comparison.LOWER_MAJOR
    check.minor > current.minor -> Version.Comparison.HIGHER_MINOR
    check.minor < current.minor -> Version.Comparison.LOWER_MINOR
    check.revision > current.revision -> Version.Comparison.HIGHER_REVISION
    check.revision < current.revision -> Version.Comparison.LOWER_REVISION
    else -> Version.Comparison.EQUAL
}

/**
 * Constructs a [Version] instance from the given [String]
 *
 * **Note**: if `str` is from an unreliable source, consider using [versionParse]
 * @param str Version in x.x.x format
 * @throws IllegalArgumentException When the parsing failed
 */
fun version(str: String): Version {
    try {
        return versionParse(str)
    } catch (e: VersionException) {
        throw IllegalArgumentException(e.message ?: "")
    }
}

/**
 * Constructs a [Version] instance from the given [String]
 * @param str Version in x.x.x format
 * @throws VersionException When the parsing failed
 */
fun versionParse(str: String): Version {
    val split = str.split('.')
    if (split.size > 3) {
        throw VersionException("Too many delimiters: $str")
    }
    if (split.isEmpty()) {
        throw VersionException("Weird string: $str")
    }
    val major: Int
    var minor = 0
    var revision = 0
    major = split[0].toIntOrNull() ?: throw VersionException(
            "Invalid major: ${split[0]}")
    if (split.size >= 2) {
        minor = split[1].toIntOrNull() ?: throw VersionException(
                "Invalid minor: ${split[1]}")
        if (split.size == 3) {
            revision = split[2].toIntOrNull() ?: throw VersionException(
                    "Invalid revision: ${split[2]}")
        }
    }
    return Version(major, minor, revision)
}

/**
 * Exception thrown when parsing an badly formatted version string
 */
class VersionException(message: String) : Exception(message)
