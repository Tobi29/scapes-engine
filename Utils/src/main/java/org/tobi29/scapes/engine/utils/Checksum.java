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

package org.tobi29.scapes.engine.utils;

import org.tobi29.scapes.engine.utils.io.ChecksumUtil;
import org.tobi29.scapes.engine.utils.io.tag.MultiTag;
import org.tobi29.scapes.engine.utils.io.tag.TagStructure;

import java.util.Arrays;

/**
 * Class representing a checksum hash
 */
public class Checksum implements MultiTag.Writeable {
    private final ChecksumUtil.Algorithm algorithm;
    private final byte[] array;

    /**
     * Construct new checksum from the given array
     *
     * @param array Byte array containing hash
     */
    public Checksum(ChecksumUtil.Algorithm algorithm, byte[] array) {
        if (array.length != algorithm.bytes()) {
            throw new IllegalArgumentException(
                    "Byte array size different from algorithm: " +
                            array.length + " (Should be: " + algorithm.bytes() +
                            ')');
        }
        this.algorithm = algorithm;
        this.array = array;
    }

    public Checksum(TagStructure tagStructure) {
        ChecksumUtil.Algorithm algorithm = ChecksumUtil.Algorithm.UNKNOWN;
        try {
            algorithm = ChecksumUtil.Algorithm
                    .valueOf(tagStructure.getString("Algorithm"));
        } catch (IllegalArgumentException e) {
        }
        this.algorithm = algorithm;
        array = tagStructure.getByteArray("Array");
    }

    /**
     * Return algorithm used to create this checksum
     *
     * @return Algorithm of this checksum, might be {@link ChecksumUtil.Algorithm#UNKNOWN}
     */
    public ChecksumUtil.Algorithm algorithm() {
        return algorithm;
    }

    /**
     * Returns byte array containing checksum hash
     *
     * @return A clone of the byte array held by the checksum instance
     */
    public byte[] array() {
        return array.clone();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Checksum)) {
            return false;
        }
        Checksum other = (Checksum) obj;
        return algorithm == other.algorithm &&
                Arrays.equals(array, other.array);
    }

    @Override
    public String toString() {
        return ArrayUtil.toHexadecimal(array);
    }

    @Override
    public TagStructure write() {
        return null;
    }
}
