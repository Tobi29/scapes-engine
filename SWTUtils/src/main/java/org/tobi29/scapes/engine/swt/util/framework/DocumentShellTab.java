package org.tobi29.scapes.engine.swt.util.framework;

import java8.util.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class DocumentShellTab extends DocumentShell {
    public Optional<TabFolder> tabFolder = Optional.empty();

    public DocumentShellTab(Display display, int style,
            MultiDocumentApplication application, boolean hideSingleTab) {
        super(display, style, application, hideSingleTab);
    }

    private static Optional<DocumentCompositeTab> tab(TabItem tabItem) {
        Control control = tabItem.getControl();
        if (!(control instanceof DocumentCompositeTab)) {
            throw new IllegalStateException(
                    "Non document composite in tab folder");
        }
        return Optional.of((DocumentCompositeTab) control);
    }

    @Override
    void updateTab() {
        Optional<DocumentComposite> currentComposite = Optional.empty();
        if (tabFolder.isPresent()) {
            TabItem[] tabItems = tabFolder.get().getSelection();
            if (tabItems.length > 0) {
                Control control = tabItems[0].getControl();
                if (!(control instanceof DocumentCompositeTab)) {
                    throw new IllegalStateException(
                            "Non document composite in tab folder");
                }
                currentComposite = Optional.of((DocumentComposite) control);
            }
        } else if (directComposite.isPresent()) {
            currentComposite = Optional.of(directComposite.get());
        }
        if (!currentComposite.isPresent()) {
            return;
        }
        DocumentComposite composite = currentComposite.get();
        setText(composite.document.title());
        if (composite.menu.getItemCount() == 0) {
            setMenuBar(null);
        } else {
            setMenuBar(composite.menu);
        }
    }

    @Override
    void remove(DocumentComposite composite) {
        composite.document.destroy();
        if (tabFolder.isPresent()) {
            composite.dispose();
            TabFolder tabFolder = this.tabFolder.get();
            List<DocumentCompositeTab> composites =
                    Arrays.stream(tabFolder.getItems())
                            .map(DocumentShellTab::tab)
                            .filter(Optional::isPresent).map(Optional::get)
                            .collect(Collectors.toList());
            if (composites.isEmpty()) {
                dispose();
            } else if (composites.size() == 1 && hideSingleTab) {
                Document document = composites.get(0).removeDocument();
                tabFolder.dispose();
                this.tabFolder = Optional.empty();
                item(document);
                layout();
                updateTab();
            }
        } else if (directComposite.isPresent() &&
                directComposite.get() == composite) {
            dispose();
        }
    }

    @Override
    protected boolean empty() {
        return !directComposite.isPresent() && !tabFolder.isPresent();
    }

    @Override
    protected DocumentCompositeTab tabItem(Document document) {
        tabFolder();
        TabFolder tabFolder = this.tabFolder.get();
        TabItem tabItem = new TabItem(tabFolder, SWT.CLOSE);
        DocumentCompositeTab composite =
                new DocumentCompositeTab(tabFolder, SWT.NONE, this, tabItem);
        tabItem.setControl(composite);
        composite.setDocument(document);
        tabFolder.setSelection(tabItem);
        return composite;
    }

    private void tabFolder() {
        if (directComposite.isPresent()) {
            DocumentComposite directComposite = this.directComposite.get();
            Document otherDocument = directComposite.document;
            directComposite.removeDocument();
            directComposite.dispose();
            this.directComposite = Optional.empty();
            tabItem(otherDocument);
        } else if (!tabFolder.isPresent()) {
            TabFolder tabFolder = new TabFolder(this, SWT.NONE);
            tabFolder.addListener(SWT.Selection, e -> updateTab());
            this.tabFolder = Optional.of(tabFolder);
        }
    }
}
