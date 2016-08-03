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

package org.tobi29.scapes.engine.swt.util;

import org.tobi29.scapes.engine.swt.util.framework.Application;

public class Shortcut {
    public final int key;
    public final Modifier[] modifiers;

    public Shortcut(int key, Modifier... modifiers) {
        this.key = key;
        this.modifiers = modifiers;
    }

    public static Shortcut get(String id, int key, Modifier... modifiers) {
        return Application.platform().shortcut(id, key, modifiers);
    }

    public enum Modifier {
        CONTROL,
        ALT,
        SHIFT
    }
}
