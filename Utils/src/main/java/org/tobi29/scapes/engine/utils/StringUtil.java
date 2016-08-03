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

import java8.util.function.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for {@link String}s
 */
public final class StringUtil {
    private static final Pattern REPLACE_Q = Pattern.compile("\\?");
    private static final Pattern REPLACE_W = Pattern.compile("\\*");

    private StringUtil() {
    }

    /**
     * Creates a hash from the given {@link String}
     *
     * @param value String to create the hash from
     * @return A 64-bit hash
     */
    public static long hash(String value) {
        return hash(value, 0L);
    }

    /**
     * Creates a hash from the given {@link String}
     *
     * @param value String to create the hash from
     * @param h     Base value for creating the hash
     * @return A 64-bit hash
     */
    public static long hash(String value, long h) {
        int length = value.length();
        for (int i = 0; i < length; i++) {
            h = 31 * h + value.charAt(i);
        }
        return h;
    }

    /**
     * Converts a wildcard expression into a {@link Pattern}
     *
     * @param exp {@link String} containing wildcard expression
     * @return A {@link Pattern} matching like the wildcard expression
     */
    public static Pattern wildcard(String exp) {
        // Replace "?" with ".?" and "*" with ".*"
        String regex =
                REPLACE_W.matcher(REPLACE_Q.matcher(exp).replaceAll(".?"))
                        .replaceAll(".*");
        return Pattern.compile(regex);
    }

    /**
     * Assembles a list of replace operations
     *
     * @param array Matcher and replacement strings Requires 2 arguments per
     *              pattern
     * @return A {@link Function} that runs the replaces on a string
     */
    public static Function<String, String> replace(String... array) {
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Amount of arguments has to be a power of 2");
        }
        List<Function<String, String>> patterns =
                new ArrayList<>(array.length >> 1);
        for (int i = 0; i < array.length; i += 2) {
            Pattern pattern = Pattern.compile(array[i]);
            String replace = array[i + 1];
            patterns.add(str -> pattern.matcher(str).replaceAll(replace));
        }
        return str -> {
            for (Function<String, String> pattern : patterns) {
                str = pattern.apply(str);
            }
            return str;
        };
    }
}
