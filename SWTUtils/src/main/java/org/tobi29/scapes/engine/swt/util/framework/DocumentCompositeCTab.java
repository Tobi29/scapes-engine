package org.tobi29.scapes.engine.swt.util.framework;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

class DocumentCompositeCTab extends DocumentComposite {
    public final CTabItem tabItem;

    public DocumentCompositeCTab(Composite parent, int style,
            DocumentShell shell, CTabItem tabItem) {
        super(parent, style, shell);
        this.tabItem = tabItem;
        addDisposeListener(e -> tabItem.dispose());
    }

    @Override
    protected void populate() {
        super.populate();
        tabItem.setText(document.shortTitle());
    }
}
