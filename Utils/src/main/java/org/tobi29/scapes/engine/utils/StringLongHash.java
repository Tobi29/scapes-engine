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

/**
 * Utility class for creating 64-bit hashes from {@code String}s
 */
public final class StringLongHash {
    private StringLongHash() {
    }

    /**
     * Creates a hash from the given {@code String}
     *
     * @param value String to create the hash from
     * @return A 64-bit hash
     */
    public static long hash(String value) {
        return hash(value, 0L);
    }

    /**
     * Creates a hash from the given {@code String}
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
}
