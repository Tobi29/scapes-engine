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

package org.tobi29.scapes.engine.swt.util.platform;

import org.eclipse.swt.SWT;
import org.tobi29.scapes.engine.swt.util.Shortcut;

public class PlatformWindows implements Platform {
    @Override
    public Shortcut shortcut(String id, Shortcut shortcut) {
        switch (id) {
            default:
                return shortcut;
        }
    }

    @Override
    public int resolve(Shortcut shortcut) {
        int style = shortcut.key;
        for (Shortcut.Modifier modifier : shortcut.modifiers) {
            switch (modifier) {
                case CONTROL:
                    style |= SWT.CONTROL;
                    break;
                case ALT:
                    style |= SWT.ALT;
                    break;
                case SHIFT:
                    style |= SWT.SHIFT;
                    break;
            }
        }
        return style;
    }
}
