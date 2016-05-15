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
