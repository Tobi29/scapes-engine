package org.tobi29.scapes.engine.swt.util.framework;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.tobi29.scapes.engine.swt.util.widgets.OptionalWidget;
import org.tobi29.scapes.engine.swt.util.widgets.SmartMenuBar;

import java.util.Arrays;

class DocumentComposite extends Composite {
    public final DocumentShell shell;
    public final SmartMenuBar menu;
    public Document document;
    private long updateStamp;

    public DocumentComposite(Composite parent, int style, DocumentShell shell) {
        super(parent, style);
        this.shell = shell;
        menu = new SmartMenuBar(parent.getShell());
        addDisposeListener(e -> {
            if (document != null) {
                document.forceClose();
                shell.application.composites.remove(document);
            }
            document = null;
            // Changing menu bar now causes GTK2 and Win32 port to crash
            getDisplay().timerExec(0, () -> OptionalWidget
                    .ifPresent(menu, SmartMenuBar::dispose));
        });
    }

    void setDocument(Document document) {
        if (document == null) {
            throw new IllegalStateException("Document removed");
        }
        removeDocument();
        this.document = document;
        populate();
        shell.application.composites.put(document, this);
    }

    void removeDocument() {
        if (document == null) {
            return;
        }
        document.destroy();
        shell.application.composites.remove(document);
        document = null;
    }

    protected void populate() {
        if (document == null) {
            throw new IllegalStateException("Document removed");
        }
        Arrays.stream(getChildren()).forEach(Control::dispose);
        Arrays.stream(menu.getItems()).forEach(Widget::dispose);
        setLayout(null);
        long nextStamp = ++updateStamp;
        shell.application.populate(this, menu);
        document.populate(this, menu, shell.application);
        layout();
        int updateTime = document.updateTime();
        if (updateTime >= 0) {
            getDisplay().timerExec(updateTime, () -> loop(nextStamp));
        }
    }

    private void loop(long updateStamp) {
        if (updateStamp < this.updateStamp || isDisposed()) {
            return;
        }
        document.update(this, menu, shell.application);
        int updateTime = document.updateTime();
        if (updateTime >= 0 && !isDisposed()) {
            long nextStamp = ++this.updateStamp;
            getDisplay().timerExec(updateTime, () -> loop(nextStamp));
        }
    }

    @Override
    protected void checkSubclass() {
    }
}
