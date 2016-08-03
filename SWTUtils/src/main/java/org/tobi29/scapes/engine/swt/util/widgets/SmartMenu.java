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
