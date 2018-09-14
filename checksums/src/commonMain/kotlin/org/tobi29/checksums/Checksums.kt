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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.checksums

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.sliceOver
import org.tobi29.arrays.toHexadecimal
import org.tobi29.io.tag.*

/**
 * Class representing a checksum hash
 */
class Checksum(
    /**
     * Algorithm of this checksum
     */
    val algorithm: ChecksumAlgorithm,
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

interface ChecksumContext {
    fun update(data: ByteArray) = update(data.sliceOver())
    fun update(data: BytesRO)
    fun finish(): ByteArray
}

fun MutableTag.toChecksum(): Checksum? {
    val map = toMap() ?: return null
    val algorithm = map["Algorithm"]?.toString()
        ?.let { checksumAlgorithmOrNull(it) } ?: return null
    val array = map["Array"]?.toByteArray() ?: return null
    if (array.size != algorithm.bytes) {
        return null
    }
    return Checksum(algorithm, array)
}

expect sealed class ChecksumAlgorithm {
    val bytes: Int
    val name: String

    abstract fun createContext(): ChecksumContext

    // TODO: object Sha512 : ChecksumAlgorithm
    object Sha256 : ChecksumAlgorithm {
        override fun createContext(): ChecksumContext
    }
    // TODO: object Md5 : ChecksumAlgorithm
}

fun checksumAlgorithm(name: String): ChecksumAlgorithm =
    checksumAlgorithmOrNull(name)
            ?: throw IllegalArgumentException("Unknown checksum algorithm: $name")

fun checksumAlgorithmOrNull(name: String): ChecksumAlgorithm? = when (name) {
    "SHA256" -> ChecksumAlgorithm.Sha256
    else -> null
}

/**
 * Creates a checksum from the given array
 * @param array     Byte array that will be used to create the checksum
 * @param algorithm The algorithm that will be used to create the checksum
 * @return A [Checksum] containing the checksum
 */
inline fun checksum(
    array: ByteArray,
    algorithm: ChecksumAlgorithm = ChecksumAlgorithm.Sha256
): Checksum {
    val ctx = algorithm.createContext()
    ctx.update(array)
    return Checksum(algorithm, ctx.finish())
}

/**
 * Creates a checksum from the given array
 * @param array     Byte array that will be used to create the checksum
 * @param algorithm The algorithm that will be used to create the checksum
 * @return A [Checksum] containing the checksum
 */
inline fun checksum(
    array: BytesRO,
    algorithm: ChecksumAlgorithm = ChecksumAlgorithm.Sha256
): Checksum {
    val ctx = algorithm.createContext()
    ctx.update(array)
    return Checksum(algorithm, ctx.finish())
}
