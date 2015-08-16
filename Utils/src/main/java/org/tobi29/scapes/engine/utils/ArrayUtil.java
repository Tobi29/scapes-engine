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

package org.tobi29.scapes.engine.utils;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.*;
import java.util.regex.Pattern;

public final class ArrayUtil {
    private static final Pattern REPLACE = Pattern.compile(" ");

    private ArrayUtil() {
    }

    public static String join(byte... array) {
        return join(array, ", ");
    }

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

    public static String join(short... array) {
        return join(array, ", ");
    }

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

    public static String join(int... array) {
        return join(array, ", ");
    }

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

    public static String join(long... array) {
        return join(array, ", ");
    }

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

    public static String join(float... array) {
        return join(array, ", ");
    }

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

    public static String join(double... array) {
        return join(array, ", ");
    }

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

    public static String join(Object... array) {
        return join(array, ", ");
    }

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

    public static byte[] fromHexadecimal(String text) {
        text = REPLACE.matcher(text).replaceAll("");
        if ((text.length() & 1) == 1) {
            throw new IllegalArgumentException("String has uneven length");
        }
        byte[] array = new byte[text.length() >> 1];
        for (int i = 0; i < text.length(); i += 2) {
            array[i >> 1] = (byte) Integer
                    .parseUnsignedInt(text.substring(i, i + 2), 16);
        }
        return array;
    }

    public static String toBase64(byte... array) {
        return new String(Base64.getEncoder().encode(array),
                StandardCharsets.UTF_8);
    }

    public static byte[] fromBase64(String text) {
        return Base64.getDecoder().decode(text);
    }

    public static void fill(int[] array, IntSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.getAsInt();
        }
    }

    public static void fill(long[] array, LongSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.getAsLong();
        }
    }

    public static void fill(double[] array, DoubleSupplier supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.getAsDouble();
        }
    }

    public static <E> void fill(E[] array, Supplier<E> supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get();
        }
    }

    public static <T, R> R[] map(T[] values,
            Function<? super T, ? extends R> function, Class<R> type) {
        @SuppressWarnings("unchecked") R[] array =
                (R[]) Array.newInstance(type, values.length);
        for (int i = 0; i < array.length; i++) {
            array[i] = function.apply(values[i]);
        }
        return array;
    }

    public static ByteBuffer fill(ByteBuffer buffer, byte value) {
        while (buffer.hasRemaining()) {
            buffer.put(value);
        }
        return buffer;
    }
}
