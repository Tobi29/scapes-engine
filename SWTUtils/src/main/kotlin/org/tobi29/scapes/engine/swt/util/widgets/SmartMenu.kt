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

package org.tobi29.scapes.engine.swt.util.widgets

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Menu
import org.eclipse.swt.widgets.MenuItem
import org.eclipse.swt.widgets.Shell
import org.tobi29.scapes.engine.swt.util.Shortcut
import org.tobi29.scapes.engine.swt.util.framework.Application

open class SmartMenu : Menu {
    protected constructor(shell: Shell,
                          style: Int) : super(shell, style)

    protected constructor(menu: Menu) : super(menu)

    constructor(control: Control) : super(control)

    fun menu(name: String): SmartMenu {
        for (item in items) {
            val menu = item.menu
            if (item.text == name && menu is SmartMenu) {
                return menu
            }
        }
        val item = MenuItem(this, SWT.CASCADE)
        item.text = name
        val menu = SmartMenu(this)
        item.menu = menu
        return menu
    }

    fun action(name: String,
               action: () -> Unit): MenuItem {
        val item = MenuItem(this, SWT.PUSH)
        item.text = name
        item.addListener(SWT.Selection) { e -> action() }
        return item
    }

    fun action(name: String,
               action: () -> Unit,
               key: Int,
               vararg modifiers: Shortcut.Modifier): MenuItem {
        return action(name, action, Shortcut(key, *modifiers))
    }

    fun action(name: String,
               action: () -> Unit,
               shortcut: Shortcut): MenuItem {
        val item = action(name, action)
        item.accelerator = Application.platform.resolve(shortcut)
        return item
    }

    override fun checkSubclass() {
    }
}
