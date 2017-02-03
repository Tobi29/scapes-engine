/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.graphics

import java.util.*

enum class TextureFilter(private val name2: String) {
    NEAREST("Nearest"),
    LINEAR("Linear");


    companion object {
        private val BY_NAME = HashMap<String, TextureFilter>()

        init {
            for (value in values()) {
                BY_NAME.put(value.name2, value)
            }
        }

        operator fun get(name: String): TextureFilter {
            return BY_NAME[name] ?: throw IllegalArgumentException(
                    "Invalid texture filter: $name")
        }
    }
}
