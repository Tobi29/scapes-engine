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
package org.tobi29.scapes.engine.swt.util

import org.tobi29.scapes.engine.swt.util.framework.Application

class Shortcut(val key: Int,
               vararg modifiers: Shortcut.Modifier) {
    val modifiers: Array<out Modifier>

    init {
        this.modifiers = modifiers
    }

    enum class Modifier {
        CONTROL,
        ALT,
        SHIFT
    }

    companion object {
        operator fun get(id: String,
                         key: Char,
                         vararg modifiers: Modifier): Shortcut {
            return get(id, key.toInt(), *modifiers)
        }

        operator fun get(id: String,
                         key: Int,
                         vararg modifiers: Modifier): Shortcut {
            return Application.platform.shortcut(id, Shortcut(key, *modifiers))
        }
    }
}
