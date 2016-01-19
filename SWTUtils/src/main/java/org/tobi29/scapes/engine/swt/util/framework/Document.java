package org.tobi29.scapes.engine.swt.util.framework;

import org.eclipse.swt.widgets.Composite;
import org.tobi29.scapes.engine.swt.util.widgets.SmartMenuBar;

public abstract class Document {
    private boolean modified;

    public boolean modified() {
        return modified;
    }

    public void modify() {
        modified = true;
    }

    public boolean close() {
        forceClose();
        return true;
    }

    public abstract void forceClose();

    protected abstract String title();

    protected String shortTitle() {
        return title();
    }

    protected abstract boolean empty();

    protected abstract void populate(Composite composite, SmartMenuBar menu,
            MultiDocumentApplication application);

    protected int updateTime() {
        return -1;
    }

    protected void update(Composite composite, SmartMenuBar menu,
            MultiDocumentApplication application) {
    }
}
