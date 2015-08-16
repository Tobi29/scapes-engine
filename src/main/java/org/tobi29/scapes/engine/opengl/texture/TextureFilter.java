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

package org.tobi29.scapes.engine.opengl.texture;

import java.util.HashMap;

public enum TextureFilter {
    NEAREST("Nearest"),
    LINEAR("Linear");
    private static final HashMap<String, TextureFilter> BY_NAME =
            new HashMap<>();

    static {
        for (TextureFilter value : values()) {
            BY_NAME.put(value.name, value);
        }
    }

    private final String name;

    TextureFilter(String name) {
        this.name = name;
    }

    public static TextureFilter get(String name) {
        return BY_NAME.get(name);
    }
}
