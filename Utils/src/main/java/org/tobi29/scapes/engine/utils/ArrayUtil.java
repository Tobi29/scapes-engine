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

import com.owtelse.codec.Base64;
import java8.util.function.DoubleSupplier;
import java8.util.function.IntSupplier;
import java8.util.function.LongSupplier;
import java8.util.function.Supplier;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public final class ArrayUtil {
    private static final Pattern REPLACE = Pattern.compile(" ");

    private ArrayUtil() {
    }

    /**
     * Joins all values of a given array into a string separated by ", "
     *
     * @param array Array for values
     * @return A String containing the joined values
     */
    public static String join(byte... array) {
        return join(array, ", ");
    }

    /**
     * Joins all values of a given array into a string
     *
     * @param array     Array for values
     * @param delimiter Separator to put between values
     * @return A String containing the joined values
     */
    public static String join(byte[] array, String delimiter) {
        StringBuilder text = new StringBuilder(array.length << 1);
        int length = array.length - 1;
        for (int i = 0; i < length; i++) {
            text.append(array[i]).append(delimiter);
        }
        if (length >= 0) {
            text.append(array[length]);
        }
        return text.toString();
    }

    /**
     * Joins all values of a given array into a string separated by ", "
     *
     * @param array Array for values
     * @return A String containing the joined values
     */
    public static String join(short... array) {
        return join(array, ", ");
    }

    /**
     * Joins all values of a given array into a string
     *
     * @param array     Array for values
     * @param delimiter Separator to put between values
     * @return A String containing the joined values
     */
    public static String join(short[] array, String delimiter) {
        StringBuilder text = new StringBuilder(array.length << 1);
        int length = array.length - 1;
        for (int i = 0; i < length; i++) {
            text.append(array[i]).append(delimiter);
        }
        if (length >= 0) {
            text.append(array[length]);
        }
        return text.toString();
    }

    /**
     * Joins all values of a given array into a string separated by ", "
     *
     * @param array Array for values
     * @return A String containing the joined values
     */
    public static String join(int... array) {
        return join(array, ", ");
    }

    /**
     * Joins all values of a given array into a string
     *
     * @param array     Array for values
     * @param delimiter Separator to put between values
     * @return A String containing the joined values
     */
    public static String join(int[] array, String delimiter) {
        StringBuilder text = new StringBuilder(array.length << 1);
        int length = array.length - 1;
        for (int i = 0; i < length; i++) {
            text.append(array[i]).append(delimiter);
        }
        if (length >= 0) {
            text.append(array[length]);
        }
        return text.toString();
    }

    /**
     * Joins all values of a given array into a string separated by ", "
     *
     * @param array Array for values
     * @return A String containing the joined values
     */
    public static String join(long... array) {
        return join(array, ", ");
    }

    /**
     * Joins all values of a given array into a string
     *
     * @param array     Array for values
     * @param delimiter Separator to put between values
     * @return A String containing the joined values
     */
    public static String join(long[] array, String delimiter) {
        StringBuilder text = new StringBuilder(array.length << 1);
        int length = array.length - 1;
        for (int i = 0; i < length; i++) {
            text.append(array[i]).append(delimiter);
        }
        if (length >= 0) {
            text.append(array[length]);
        }
        return text.toString();
    }

    /**
     * Joins all values of a given array into a string separated by ", "
     *
     * @param array Array for values
     * @return A String containing the joined values
     */
    public static String join(float... array) {
        return join(array, ", ");
    }

    /**
     * Joins all values of a given array into a string
     *
     * @param array     Array for values
     * @param delimiter Separator to put between values
     * @return A String containing the joined values
     */
    public static String join(float[] array, String delimiter) {
        StringBuilder text = new StringBuilder(array.length << 1);
        int length = array.length - 1;
        for (int i = 0; i < length; i++) {
            text.append(array[i]).append(delimiter);
        }
        if (length >= 0) {
            text.append(array[length]);
        }
        return text.toString();
    }

    /**
     * Joins all values of a given array into a string separated by ", "
     *
     * @param array Array for values
     * @return A String containing the joined values
     */
    public static String join(double... array) {
        return join(array, ", ");
    }

    /**
     * Joins all values of a given array into a string
     *
     * @param array     Array for values
     * @param delimiter Separator to put between values
     * @return A String containing the joined values
     */
    public static String join(double[] array, String delimiter) {
        StringBuilder text = new StringBuilder(array.length << 1);
        int length = array.length - 1;
        for (int i = 0; i < length; i++) {
            text.append(array[i]).append(delimiter);
        }
        if (length >= 0) {
            text.append(array[length]);
        }
        return text.toString();
    }

    /**
     * Joins all values of a given array into a string separated by ", "
     *
     * @param array Array for values
     * @return A String containing the joined values
     */
    public static String join(Object... array) {
        return join(array, ", ");
    }

    /**
     * Joins all values of a given array into a string
     *
     * @param array     Array for values
     * @param delimiter Separator to put between values
     * @return A String containing the joined values
     */
    public static String join(Object[] array, String delimiter) {
        StringBuilder text = new StringBuilder(array.length << 1);
        int length = array.length - 1;
        for (int i = 0; i < length; i++) {
            text.append(array[i]).append(delimiter);
        }
        if (length >= 0) {
            text.append(array[length]);
        }
        return text.toString();
    }

    /**
     * Converts a byte array into a hexadecimal string
     *
     * @param array Array to convert
     * @return String containing the hexadecimal data
     */
    public static String toHexadecimal(byte... array) {
        StringBuilder text = new StringBuilder(array.length << 1);
        for (byte value : array) {
            String append =
                    Integer.toHexString(value < 0 ? value + 256 : value);
            if (append.length() == 1) {
                text.append('0').append(append);
            } else {
                text.append(append);
            }
        }
        return text.toString();
    }

    /**
     * Converts a byte array into a hexadecimal string
     *
     * @param groups How many bytes to group until separated by a space
     * @param array  Array to convert
     * @return String containing the hexadecimal data
     */
    public static String toHexadecimal(int groups, byte... array) {
        StringBuilder text =
                new StringBuilder((array.length << 1) + array.length / groups);
        int group = 0, limit = array.length - 1;
        for (int i = 0; i < array.length; i++) {
            byte value = array[i];
            String append =
                    Integer.toHexString(value < 0 ? value + 256 : value);
            if (append.length() == 1) {
                text.append('0').append(append);
            } else {
                text.append(append);
            }
            group++;
            if (group >= groups && i < limit) {
                text.append(' ');
                group = 0;
            }
        }
        return text.toString();
    }

    /**
     * Converts a hexadecimal string to a byte array Silently discards spaces
     *
     * @param text String to convert
     * @return A byte array containing the data
     * @throws IOException Thrown in case of an invalid string
     */
    public static byte[] fromHexadecimal(String text) throws IOException {
        try {
            text = REPLACE.matcher(text).replaceAll("");
            if ((text.length() & 1) == 1) {
                throw new IOException("String has uneven length");
            }
            byte[] array = new byte[text.length() >> 1];
            for (int i = 0; i < text.length(); i += 2) {
                array[i >> 1] =
                        (byte) Integer.parseInt(text.substring(i, i + 2), 16);
            }
            return array;
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    /**
     * Converts a byte array to a Base64 string
     *
     * @param array Array to convert
     * @return String containing the data
     */
    public static String toBase64(byte... array) {
        try {
            return Base64.encode(array);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedJVMException(e);
        }
    }

    /**
     * Converts a Base64 string to a byte array
     *
     * @param text Base64 String to convert
     * @return Byte array containing the data
     * @throws IOException When an invalid base64 was given
     */
    public static byte[] fromBase64(String text) throws IOException {
        try {
            return Base64.decode(text);
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            throw new IOException(e);
        }
    }

    /**
     * Fills the given array with values
     *
     * @param array    Array to fill
     * @param supplier Supplier called for each value written to the array
     */
    public static void fill(int[] array, IntSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.getAsInt();
        }
    }

    /**
     * Fills the given array with values
     *
     * @param array    Array to fill
     * @param supplier Supplier called for each value written to the array
     */
    public static void fill(long[] array, LongSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.getAsLong();
        }
    }

    /**
     * Fills the given array with values
     *
     * @param array    Array to fill
     * @param supplier Supplier called for each value written to the array
     */
    public static void fill(double[] array, DoubleSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.getAsDouble();
        }
    }

    /**
     * Fills the given array with values
     *
     * @param array    Array to fill
     * @param supplier Supplier called for each value written to the array
     * @param <E> Element type
     */
    public static <E> void fill(E[] array, Supplier<E> supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get();
        }
    }

    /**
     * Fills a buffer with the given value
     *
     * @param buffer Buffer to fill
     * @param value  Value written to the buffer
     * @return The given buffer
     */
    public static ByteBuffer fill(ByteBuffer buffer, byte value) {
        while (buffer.hasRemaining()) {
            buffer.put(value);
        }
        return buffer;
    }
}
