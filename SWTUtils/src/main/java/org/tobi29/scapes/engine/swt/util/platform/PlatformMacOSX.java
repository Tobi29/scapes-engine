package org.tobi29.scapes.engine.swt.util.platform;

import org.eclipse.swt.SWT;
import org.tobi29.scapes.engine.swt.util.Shortcut;

public class PlatformMacOSX implements Platform {
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
                    style |= SWT.COMMAND;
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
