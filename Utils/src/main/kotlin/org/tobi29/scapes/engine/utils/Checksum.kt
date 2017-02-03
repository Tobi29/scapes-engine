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

import org.tobi29.scapes.engine.utils.io.Algorithm
import org.tobi29.scapes.engine.utils.io.tag.MultiTag
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.structure
import java.util.*

/**
 * Class representing a checksum hash
 */
class Checksum : MultiTag.Writeable {
    /**
     * Algorithm of this checksum, might be [Algorithm.UNKNOWN]
     */
    val algorithm: Algorithm
    private val array: ByteArray

    /**
     * Construct new checksum from the given array
     * @param array Byte array containing hash
     */
    constructor(algorithm: Algorithm,
                array: ByteArray) {
        if (array.size != algorithm.bytes) {
            throw IllegalArgumentException(
                    "Byte array size different from algorithm: ${array.size} (Should be: ${algorithm.bytes})")
        }
        this.algorithm = algorithm
        this.array = array
    }

    constructor(tagStructure: TagStructure) {
        var algorithm = Algorithm.UNKNOWN
        try {
            algorithm = Algorithm.valueOf(
                    tagStructure.getString("Algorithm") ?: "")
        } catch (e: IllegalArgumentException) {
        }
        this.algorithm = algorithm
        array = tagStructure.getByteArray("Array") ?: ByteArray(algorithm.bytes)
    }

    /**
     * Returns byte array containing checksum hash
     * @return A clone of the byte array held by the checksum instance
     */
    fun array(): ByteArray {
        return array.clone()
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(array)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Checksum) {
            return false
        }
        return algorithm === other.algorithm && Arrays.equals(array,
                other.array)
    }

    override fun toString(): String {
        return array.toHexadecimal()
    }

    override fun write(): TagStructure {
        return structure {
            setString("Algorithm", algorithm.toString())
            setByteArray("Array", *array())
        }
    }
}

fun TagStructure.setChecksum(key: String,
                             checksum: Checksum) {
    setStructure(key, checksum.write())
}
