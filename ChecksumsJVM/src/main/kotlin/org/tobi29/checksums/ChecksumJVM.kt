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

package org.tobi29.checksums

import org.tobi29.arrays.toHexadecimal
import org.tobi29.io.tag.*
import org.tobi29.stdex.UnsupportedJVMException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Class representing a checksum hash
 */
class Checksum(
    /**
     * Algorithm of this checksum, might be [Algorithm.UNKNOWN]
     */
    val algorithm: Algorithm,
    private val array: ByteArray
) : TagMapWrite {

    /**
     * Returns byte array containing checksum hash
     * @return A clone of the byte array held by the checksum instance
     */
    fun array(): ByteArray {
        return array.copyOf()
    }

    override fun write(map: ReadWriteTagMap) {
        map["Algorithm"] = algorithm.toString().toTag()
        map["Array"] = array.toTag()
    }

    override fun hashCode(): Int {
        return array.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Checksum) {
            return false
        }
        return algorithm == other.algorithm && array contentEquals other.array
    }

    override fun toString(): String {
        return array.toHexadecimal()
    }

    init {
        if (array.size != algorithm.bytes) {
            throw IllegalArgumentException(
                "Byte array size different from algorithm: ${array.size} (Should be: ${algorithm.bytes})"
            )
        }
    }
}

fun MutableTag.toChecksum(): Checksum? {
    val map = toMap() ?: return null
    val algorithm =
        try {
            Algorithm.valueOf(map["Algorithm"].toString())
        } catch (e: IllegalArgumentException) {
            return null
        }
    val array = map["Array"]?.toByteArray() ?: return null
    if (array.size != algorithm.bytes) {
        return null
    }
    return Checksum(algorithm, array)
}

/**
 * Enum containing available checksum algorithms
 */
enum class Algorithm(
    private val digestName: String,
    val bytes: Int
) {
    UNKNOWN("UNKNOWN", 0),
    SHA256("SHA-256", 32),
    SHA1("SHA1", 20),
    @Deprecated("")
    MD5("MD5", 16);

    /**
     * Creates a new [MessageDigest]
     * @return [MessageDigest] using the specified algorithm
     */
    fun digest(): MessageDigest {
        return try {
            MessageDigest.getInstance(digestName)
        } catch (e: NoSuchAlgorithmException) {
            throw UnsupportedJVMException(e)
        }
    }
}
