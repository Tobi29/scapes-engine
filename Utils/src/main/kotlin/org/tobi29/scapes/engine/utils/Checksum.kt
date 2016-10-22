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

import org.tobi29.scapes.engine.utils.io.Algorithm
import org.tobi29.scapes.engine.utils.io.tag.MultiTag
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import java.util.*

/**
 * Class representing a checksum hash
 */
class Checksum : MultiTag.Writeable {
    private val algorithm: Algorithm
    private val array: ByteArray

    /**
     * Construct new checksum from the given array
     * @param array Byte array containing hash
     */
    constructor(algorithm: Algorithm, array: ByteArray) {
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
     * Return algorithm used to create this checksum
     * @return Algorithm of this checksum, might be [Algorithm.UNKNOWN]
     */
    fun algorithm(): Algorithm {
        return algorithm
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
        val tagStructure = TagStructure()
        tagStructure.setString("Algorithm", algorithm.toString())
        tagStructure.setByteArray("Array", *array)
        return tagStructure
    }
}
