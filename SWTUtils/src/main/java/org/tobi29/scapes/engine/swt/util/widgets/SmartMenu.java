package org.tobi29.scapes.engine.swt.util.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.tobi29.scapes.engine.swt.util.Shortcut;
import org.tobi29.scapes.engine.swt.util.framework.Application;

public class SmartMenu extends Menu {
    protected SmartMenu(Shell shell, int style) {
        super(shell, style);
    }

    protected SmartMenu(Menu menu) {
        super(menu);
    }

    public SmartMenu(Control control) {
        super(control);
    }

    public SmartMenu menu(String name) {
        for (MenuItem item : getItems()) {
            Menu menu = item.getMenu();
            if (item.getText().equals(name) && menu instanceof SmartMenu) {
                return (SmartMenu) menu;
            }
        }
        MenuItem item = new MenuItem(this, SWT.CASCADE);
        item.setText(name);
        SmartMenu menu = new SmartMenu(this);
        item.setMenu(menu);
        return menu;
    }

    public MenuItem action(String name, Runnable action) {
        MenuItem item = new MenuItem(this, SWT.PUSH);
        item.setText(name);
        item.addListener(SWT.Selection, e -> action.run());
        return item;
    }

    public MenuItem action(String name, Runnable action, int key,
            Shortcut.Modifier... modifiers) {
        return action(name, action, new Shortcut(key, modifiers));
    }

    public MenuItem action(String name, Runnable action, Shortcut shortcut) {
        MenuItem item = action(name, action);
        item.setAccelerator(Application.platform().resolve(shortcut));
        return item;
    }

    @Override
    protected void checkSubclass() {
    }
}
