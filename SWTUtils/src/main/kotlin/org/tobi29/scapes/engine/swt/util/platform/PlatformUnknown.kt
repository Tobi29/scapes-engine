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

package org.tobi29.scapes.engine.swt.util.platform

import org.eclipse.swt.SWT
import org.tobi29.scapes.engine.swt.util.Shortcut

class PlatformUnknown : Platform {
    override fun shortcut(id: String,
                          shortcut: Shortcut): Shortcut {
        when (id) {
            else -> return shortcut
        }
    }

    override fun resolve(shortcut: Shortcut): Int {
        var style = shortcut.key
        for (modifier in shortcut.modifiers) {
            when (modifier) {
                Shortcut.Modifier.CONTROL -> style = style or SWT.CONTROL
                Shortcut.Modifier.ALT -> style = style or SWT.ALT
                Shortcut.Modifier.SHIFT -> style = style or SWT.SHIFT
            }
        }
        return style
    }
}
