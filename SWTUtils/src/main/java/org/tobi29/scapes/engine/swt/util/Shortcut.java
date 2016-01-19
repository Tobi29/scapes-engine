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
