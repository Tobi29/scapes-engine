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

package org.tobi29.scapes.engine.swt.util.framework;

import java8.util.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

abstract class DocumentShell extends Shell {
    public final MultiDocumentApplication application;
    protected final boolean hideSingleTab;
    protected Optional<DocumentComposite> directComposite = Optional.empty();

    protected DocumentShell(Display display, int style,
            MultiDocumentApplication application, boolean hideSingleTab) {
        super(display, style);
        this.application = application;
        this.hideSingleTab = hideSingleTab;
        setLayout(new FillLayout());
    }

    abstract void updateTab();

    DocumentComposite item(Document document) {
        if (empty() && hideSingleTab) {
            DocumentComposite composite =
                    new DocumentComposite(this, SWT.NONE, this);
            composite.setDocument(document);
            directComposite = Optional.of(composite);
            updateTab();
            return composite;
        }
        DocumentComposite composite = tabItem(document);
        layout();
        updateTab();
        return composite;
    }

    abstract void remove(DocumentComposite composite);

    protected abstract boolean empty();

    protected abstract DocumentComposite tabItem(Document document);

    @Override
    protected void checkSubclass() {
    }
}
