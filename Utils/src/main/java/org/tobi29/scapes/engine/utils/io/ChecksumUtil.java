/*
 * Copyright 2012-2015 Tobi29
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
package org.tobi29.scapes.engine.utils.io;

import org.tobi29.scapes.engine.utils.Checksum;
import org.tobi29.scapes.engine.utils.UnsupportedJVMException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for creating checksums
 */
public final class ChecksumUtil {
    private ChecksumUtil() {
    }

    /**
     * Creates a checksum from the given array
     *
     * @param array Byte array that will be used to create the checksum
     * @return A {@link Checksum} containing the checksum
     */
    public static Checksum checksum(byte[] array) {
        return checksum(array, Algorithm.SHA256);
    }

    /**
     * Creates a checksum from the given array
     *
     * @param array     Byte array that will be used to create the checksum
     * @param algorithm The algorithm that will be used to create the checksum
     * @return A {@link Checksum} containing the checksum
     */
    public static Checksum checksum(byte[] array, Algorithm algorithm) {
        MessageDigest digest = algorithm.digest();
        digest.update(array);
        return new Checksum(digest.digest());
    }

    /**
     * Creates a checksum from the given {@link ByteBuffer}
     *
     * @param buffer {@link ByteBuffer} that will be used to create the checksum
     * @return A {@link Checksum} containing the checksum
     */
    public static Checksum checksum(ByteBuffer buffer) {
        return checksum(buffer, Algorithm.SHA256);
    }

    /**
     * Creates a checksum from the given {@link ByteBuffer}
     *
     * @param buffer    {@link ByteBuffer} that will be used to create the checksum
     * @param algorithm The algorithm that will be used to create the checksum
     * @return A {@link Checksum} containing the checksum
     */
    public static Checksum checksum(ByteBuffer buffer, Algorithm algorithm) {
        MessageDigest digest = algorithm.digest();
        digest.update(buffer);
        return new Checksum(digest.digest());
    }

    /**
     * Creates a checksum from the given {@link ReadableByteStream}
     *
     * @param input {@link ReadableByteStream} that will be used to create the checksum
     * @return A {@link Checksum} containing the checksum
     */
    public static Checksum checksum(ReadableByteStream input)
            throws IOException {
        return checksum(input, Algorithm.SHA256);
    }

    /**
     * Creates a checksum from the given {@link ReadableByteStream}
     *
     * @param input     {@link ReadableByteStream} that will be used to create the checksum
     * @param algorithm The algorithm that will be used to create the checksum
     * @return A {@link Checksum} containing the checksum
     */
    public static Checksum checksum(ReadableByteStream input,
            Algorithm algorithm) throws IOException {
        MessageDigest digest = algorithm.digest();
        ProcessStream.process(input, digest::update);
        return new Checksum(digest.digest());
    }

    /**
     * Enum containing available checksum algorithms
     */
    public enum Algorithm {
        SHA256("SHA-256", 32),
        SHA1("SHA1", 20),
        @Deprecated MD5("MD5", 16);
        private final String name;
        private final int bytes;

        Algorithm(String name, int bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        /**
         * Creates a new {@code MessageDigest}
         *
         * @return {@code MessageDigest} using the specified algorithm
         */
        public MessageDigest digest() {
            try {
                return MessageDigest.getInstance(name);
            } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedJVMException(e);
            }
        }

        /**
         * Gives the length of the returned digest
         *
         * @return Length of digest in bytes
         */
        public int bytes() {
            return bytes;
        }
    }
}
