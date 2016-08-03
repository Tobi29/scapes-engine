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

package org.tobi29.scapes.engine.input;

import org.tobi29.scapes.engine.utils.Pair;

public class FileType {
    public static final FileType IMAGE = new FileType("*.png", "PNG File");
    public static final FileType MUSIC =
            new FileType("*.ogg", "ogg-Vorbis File", "*.mp3", "MP3 File",
                    "*.wav", "Wave File");
    private final Pair<String, String>[] extensions;

    public FileType(String... extensions) {
        this(extensions(extensions));
    }

    @SafeVarargs
    public FileType(Pair<String, String>... extensions) {
        this.extensions = extensions;
    }

    @SuppressWarnings("unchecked")
    private static Pair<String, String>[] extensions(String[] array) {
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException("Array has to have even length");
        }
        Pair<String, String>[] extensions = new Pair[array.length >> 1];
        for (int i = 0; i < extensions.length; i++) {
            int j = i << 1;
            extensions[i] = new Pair<>(array[j], array[j + 1]);
        }
        return extensions;
    }

    public Pair<String, String>[] extensions() {
        return extensions;
    }
}
