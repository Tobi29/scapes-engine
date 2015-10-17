package org.tobi29.scapes.engine.utils;

import java.util.Arrays;

public class Checksum {
    private final byte[] array;

    public Checksum(byte[] array) {
        this.array = array;
    }

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
        return Arrays.equals(array, other.array);
    }

    @Override
    public String toString() {
        return ArrayUtil.toHexadecimal(array);
    }
}
