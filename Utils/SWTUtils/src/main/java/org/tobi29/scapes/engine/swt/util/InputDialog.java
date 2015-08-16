/*
 * Copyright 2012-2015 Tobi29
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.function.Function;

public class InputDialog extends Dialog {
    private final Shell shell;
    private final String button;

    public InputDialog(Shell parent, String title) {
        this(parent, title, "OK");
    }

    public InputDialog(Shell parent, String title, String button) {
        super(parent);
        this.button = button;
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
        shell.setMinimumSize(450, 0);
        shell.setText(title);
        shell.setLayout(new GridLayout(1, false));
    }

    public <C extends Control> C add(String label,
            Function<Shell, ? extends C> supplier) {
        Label fieldLabel = new Label(shell, SWT.NONE);
        fieldLabel.setText(label);
        C field = supplier.apply(shell);
        field.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        return field;
    }

    public void open(Runnable runnable) {
        Button ok = new Button(shell, SWT.NONE);
        ok.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
        ok.setText(button);
        ok.addListener(SWT.Selection, event -> {
            runnable.run();
            shell.dispose();
        });
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
