package org.tobi29.scapes.engine.swt.util.framework;

import java8.util.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class DocumentShellCTab extends DocumentShell {
    public Optional<CTabFolder> tabFolder = Optional.empty();

    public DocumentShellCTab(Display display, int style,
            MultiDocumentApplication application, boolean hideSingleTab) {
        super(display, style, application, hideSingleTab);
    }

    private static DocumentCompositeCTab tab(CTabItem tabItem) {
        Control control = tabItem.getControl();
        if (!(control instanceof DocumentCompositeCTab)) {
            throw new IllegalStateException(
                    "Non document composite in tab folder");
        }
        return (DocumentCompositeCTab) control;
    }

    @Override
    void updateTab() {
        Optional<DocumentComposite> currentComposite = Optional.empty();
        if (tabFolder.isPresent()) {
            CTabItem tabItem = tabFolder.get().getSelection();
            Control control = tabItem.getControl();
            if (!(control instanceof DocumentCompositeCTab)) {
                throw new IllegalStateException(
                        "Non document composite in tab folder");
            }
            currentComposite = Optional.of((DocumentComposite) control);
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
            CTabFolder tabFolder = this.tabFolder.get();
            List<DocumentCompositeCTab> composites =
                    Arrays.stream(tabFolder.getItems())
                            .map(DocumentShellCTab::tab)
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
    protected DocumentCompositeCTab tabItem(Document document) {
        tabFolder();
        CTabFolder tabFolder = this.tabFolder.get();
        CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
        DocumentCompositeCTab composite =
                new DocumentCompositeCTab(tabFolder, SWT.NONE, this, tabItem);
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
            CTabFolder tabFolder = new CTabFolder(this, SWT.NONE);
            tabFolder.setRenderer(new CTabFolderRendererModern(tabFolder));
            tabFolder.addListener(SWT.Selection, e -> updateTab());
            tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
                @Override
                public void close(CTabFolderEvent event) {
                    CTabItem tabItem = (CTabItem) event.item;
                    Control control = tabItem.getControl();
                    if (!(control instanceof DocumentCompositeCTab)) {
                        throw new IllegalStateException(
                                "Non document composite in tab folder");
                    }
                    application.closeTabItem((DocumentComposite) control);
                }
            });
            this.tabFolder = Optional.of(tabFolder);
        }
    }
}
