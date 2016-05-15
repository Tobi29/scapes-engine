package org.tobi29.scapes.engine.swt.util.framework;

import java8.util.Optional;
import java8.util.function.Consumer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.tobi29.scapes.engine.swt.util.widgets.Dialogs;
import org.tobi29.scapes.engine.swt.util.widgets.SmartMenuBar;
import org.tobi29.scapes.engine.utils.VersionUtil;
import org.tobi29.scapes.engine.utils.task.TaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MultiDocumentApplication extends Application {
    final Map<Document, DocumentComposite> composites = new HashMap<>();

    protected MultiDocumentApplication(String name, String id,
            VersionUtil.Version version) {
        super(name, id, version);
    }

    protected MultiDocumentApplication(String name, String id,
            VersionUtil.Version version, TaskExecutor taskExecutor) {
        super(name, id, version, taskExecutor);
    }

    protected abstract void populate(Composite composite, SmartMenuBar menu);

    public void openShell(Document document) {
        shell(document);
    }

    public void openTab(Document document) {
        tab(document);
    }

    public Document document(Composite composite) {
        if (!(composite instanceof DocumentComposite)) {
            throw new IllegalArgumentException("Not a document composite");
        }
        return ((DocumentComposite) composite).document;
    }

    public void openNewShell(Composite source, Document document) {
        if (!(source instanceof DocumentComposite)) {
            throw new IllegalArgumentException("Not a document composite");
        }
        shell((DocumentComposite) source, document);
    }

    public void openNewTab(Composite source, Document document) {
        if (!(source instanceof DocumentComposite)) {
            throw new IllegalArgumentException("Not a document composite");
        }
        tab((DocumentComposite) source, document);
    }

    public void replaceTab(Composite source, Document document) {
        if (!(source instanceof DocumentComposite)) {
            throw new IllegalArgumentException("Not a document composite");
        }
        reuse((DocumentComposite) source, document);
    }

    public void closeTab(Composite source) {
        if (!(source instanceof DocumentComposite)) {
            throw new IllegalArgumentException("Not a document composite");
        }
        closeTabItem((DocumentComposite) source);
    }

    public int message(Composite source, int style, String title,
            String message) {
        return Dialogs.openMessage(source.getShell(), style, title, message);
    }

    public boolean access(Document document, Consumer<Composite> consumer) {
        AtomicBoolean output = new AtomicBoolean();
        display.syncExec(() -> output.set(accessDocument(document, consumer)));
        return output.get();
    }

    public void accessAsync(Document document, Consumer<Composite> consumer) {
        display.asyncExec(() -> accessDocument(document, consumer));
    }

    private boolean accessDocument(Document document,
            Consumer<Composite> consumer) {
        DocumentComposite composite = composites.get(document);
        if (composite == null) {
            return false;
        }
        assert composite.document == document;
        consumer.accept(composite);
        return true;
    }

    void closeTabItem(DocumentComposite source) {
        source.document.forceClose();
        source.shell.remove(source);
    }

    private DocumentComposite shell(Document document) {
        // TODO: Make this configurable
        DocumentShell shell =
                new DocumentShellCTab(display, SWT.SHELL_TRIM, this, true);
        return tabFromShell(shell, document);
    }

    private DocumentComposite tab(Document document) {
        Shell activeShell = display.getActiveShell();
        if (activeShell instanceof DocumentShell) {
            return tabFromShell((DocumentShell) activeShell, document);
        }
        for (Shell shell : display.getShells()) {
            if (shell instanceof DocumentShell) {
                return tabFromShell((DocumentShell) shell, document);
            }
        }
        return shell(document);
    }

    private DocumentComposite shell(DocumentComposite source,
            Document document) {
        return tryReuse(source, document).orElseGet(() -> shell(document));
    }

    private DocumentComposite tab(DocumentComposite source, Document document) {
        return tryReuse(source, document)
                .orElseGet(() -> tabFromShell(source.shell, document));
    }

    private DocumentComposite reuse(DocumentComposite source,
            Document document) {
        source.document.forceClose();
        source.setDocument(document);
        source.shell.updateTab();
        return source;
    }

    private Optional<DocumentComposite> tryReuse(DocumentComposite source,
            Document document) {
        if (!source.document.modified() && source.document.empty()) {
            return Optional.of(reuse(source, document));
        }
        return Optional.empty();
    }

    private DocumentComposite tabFromShell(DocumentShell source,
            Document document) {
        DocumentComposite composite = source.item(document);
        if (source.isVisible()) {
            source.forceActive();
        } else {
            source.open();
        }
        return composite;
    }
}
