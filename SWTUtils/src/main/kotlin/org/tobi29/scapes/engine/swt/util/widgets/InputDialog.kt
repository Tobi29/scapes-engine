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

package org.tobi29.scapes.engine.swt.util.widgets

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*

class InputDialog(parent: Shell, title: String, private val button: String = "OK") : Dialog(
        parent) {
    private val shell: Shell

    init {
        shell = Shell(parent,
                SWT.DIALOG_TRIM or SWT.SHEET or SWT.PRIMARY_MODAL)
        shell.setMinimumSize(450, 0)
        shell.text = title
        shell.layout = GridLayout(1, false)
    }

    fun <C : Control> add(label: String,
                          supplier: (Shell) -> C): C {
        val fieldLabel = Label(shell, SWT.NONE)
        fieldLabel.text = label
        val field = supplier(shell)
        field.layoutData = GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1)
        return field
    }

    fun open(runnable: () -> Unit = {}) {
        val ok = Button(shell, SWT.NONE)
        ok.layoutData = GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1)
        ok.text = button
        ok.addListener(SWT.Selection) { event ->
            runnable()
            shell.dispose()
        }
        shell.pack()
        shell.open()
        val display = parent.display
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }
    }

    fun dismiss() {
        if (shell.isDisposed) {
            return
        }
        shell.dispose()
    }
}
