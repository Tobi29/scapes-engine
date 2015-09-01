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
package org.tobi29.scapes.engine.qt.util;

import com.trolltech.qt.gui.*;

public class InputDialog extends QDialog {
    private final QFormLayout forms;
    private final QPushButton ok;
    private Runnable runnable;

    public InputDialog(QWidget parent, String title) {
        this(parent, title, "OK");
    }

    public InputDialog(QWidget parent, String title, String textOK) {
        this(parent, title, textOK, "Cancel");
    }

    public InputDialog(QWidget parent, String title, String textOK,
            String textCancel) {
        super(parent);
        setWindowTitle(title);
        setModal(true);
        QVBoxLayout layout = new QVBoxLayout();
        forms = new QFormLayout();
        layout.addLayout(forms);
        setLayout(layout);
        QDialogButtonBox buttons = new QDialogButtonBox();
        layout.addWidget(buttons);
        ok = new QPushButton(textOK);
        QPushButton cancel = new QPushButton(textCancel);
        buttons.addButton(ok, QDialogButtonBox.ButtonRole.AcceptRole);
        buttons.addButton(cancel, QDialogButtonBox.ButtonRole.RejectRole);
        cancel.pressed.connect(this, "close()");
    }

    public <W extends QWidget> W add(String label, W widget) {
        forms.addRow(label, widget);
        return widget;
    }

    public void show(Runnable runnable) {
        this.runnable = runnable;
        ok.pressed.connect(this, "ok()");
        show();
    }

    public void ok() {
        runnable.run();
        close();
    }
}