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
