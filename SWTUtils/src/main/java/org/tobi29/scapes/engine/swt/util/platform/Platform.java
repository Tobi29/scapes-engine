package org.tobi29.scapes.engine.swt.util.platform;

import org.tobi29.scapes.engine.swt.util.Shortcut;

public interface Platform {
    default Shortcut shortcut(String id, int key,
            Shortcut.Modifier... modifiers) {
        return shortcut(id, new Shortcut(key, modifiers));
    }

    Shortcut shortcut(String id, Shortcut shortcut);

    int resolve(Shortcut shortcut);
}
