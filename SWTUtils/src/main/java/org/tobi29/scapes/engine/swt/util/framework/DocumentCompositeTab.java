package org.tobi29.scapes.engine.swt.util.framework;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabItem;

class DocumentCompositeTab extends DocumentComposite {
    public final TabItem tabItem;

    public DocumentCompositeTab(Composite parent, int style,
            DocumentShell shell, TabItem tabItem) {
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
