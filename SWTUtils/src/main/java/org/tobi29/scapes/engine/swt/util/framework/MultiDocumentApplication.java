package org.tobi29.scapes.engine.swt.util.framework;

import java8.util.Optional;
import java8.util.function.Consumer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.tobi29.scapes.engine.swt.util.widgets.Dialogs;
import org.tobi29.scapes.engine.swt.util.widgets.OptionalWidget;
import org.tobi29.scapes.engine.swt.util.widgets.SmartMenuBar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MultiDocumentApplication extends Application {
    private final Map<Document, DocumentShell.DocumentComposite> composites =
            new HashMap<>();

    protected MultiDocumentApplication(String name, String id, String version) {
        super(name, id, version);
    }

    protected abstract void populate(Composite composite, SmartMenuBar menu);

    public void openShell(Document document) {
        open(shell(document));
    }

    public void openTab(Document document) {
        open(tab(document));
    }

    public void openNewShell(Composite source, Document document) {
        if (source instanceof DocumentShell.DocumentComposite) {
            open(shell((DocumentShell.DocumentComposite) source, document));
        } else {
            openShell(document);
        }
    }

    public void openNewTab(Composite source, Document document) {
        if (source instanceof DocumentShell.DocumentComposite) {
            open(tab((DocumentShell.DocumentComposite) source, document));
        } else {
            openTab(document);
        }
    }

    public void replaceTab(Composite source, Document document) {
        if (source instanceof DocumentShell.DocumentComposite) {
            open(reuse((DocumentShell.DocumentComposite) source, document));
        } else {
            throw new IllegalArgumentException("Not a document composite");
        }
    }

    public void closeTab(Composite source) {
        if (source instanceof DocumentShell.DocumentComposite) {
            closeTabItem((DocumentShell.DocumentComposite) source);
        }
    }

    public int message(Composite source, int style, String title,
            String message) {
        return Dialogs.openMessage(source.getShell(), style, title, message);
    }

    public void access(Document document,
            Consumer<Optional<Composite>> consumer) {
        display.syncExec(() -> accessDocument(document, consumer));
    }

    public void accessAsync(Document document,
            Consumer<Optional<Composite>> consumer) {
        display.asyncExec(() -> accessDocument(document, consumer));
    }

    private void accessDocument(Document document,
            Consumer<Optional<Composite>> consumer) {
        DocumentShell.DocumentComposite composite = composites.get(document);
        Optional<Composite> optional;
        if (composite == null) {
            optional = Optional.empty();
        } else {
            assert composite.document == document;
            optional = Optional.of(composite);
        }
        consumer.accept(optional);
    }

    private void open(DocumentShell.DocumentComposite tab) {
        tab.shell.open();
    }

    private void closeTabItem(DocumentShell.DocumentComposite source) {
        source.document.forceClose();
        source.shell.remove(source);
    }

    private DocumentShell.DocumentComposite shell(Document document) {
        DocumentShell shell = new DocumentShell(display, SWT.SHELL_TRIM);
        return tabFromShell(shell, document);
    }

    private DocumentShell.DocumentComposite tab(Document document) {
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

    private DocumentShell.DocumentComposite shell(
            DocumentShell.DocumentComposite source, Document document) {
        return tryReuse(source, document).orElseGet(() -> shell(document));
    }

    private DocumentShell.DocumentComposite tab(
            DocumentShell.DocumentComposite source, Document document) {
        return tryReuse(source, document)
                .orElseGet(() -> tabFromShell(source.shell, document));
    }

    private DocumentShell.DocumentComposite reuse(
            DocumentShell.DocumentComposite source, Document document) {
        source.document.forceClose();
        source.setDocument(document);
        source.populate();
        source.shell.updateTab();
        return source;
    }

    private Optional<DocumentShell.DocumentComposite> tryReuse(
            DocumentShell.DocumentComposite source, Document document) {
        if (!source.document.modified() && source.document.empty()) {
            return Optional.of(reuse(source, document));
        }
        return Optional.empty();
    }

    private DocumentShell.DocumentComposite tabFromShell(DocumentShell source,
            Document document) {
        return source.item(document);
    }

    private Optional<DocumentShell.DocumentComposite> tab(TabItem tabItem) {
        Control control = tabItem.getControl();
        if (control instanceof DocumentShell.DocumentComposite) {
            return Optional.of((DocumentShell.DocumentComposite) control);
        }
        return Optional.empty();
    }

    private class DocumentShell extends Shell {
        public Optional<DocumentComposite> directComposite = Optional.empty();
        public Optional<TabFolder> tabFolder = Optional.empty();

        public DocumentShell(Display display, int style) {
            super(display, style);
            setLayout(new FillLayout());
            addDisposeListener(e -> {
                if (tabFolder.isPresent()) {
                    Arrays.stream(tabFolder.get().getItems())
                            .map(MultiDocumentApplication.this::tab)
                            .filter(Optional::isPresent).map(Optional::get)
                            .forEach(tabItem -> tabItem.document.forceClose());
                }
                directComposite.ifPresent(
                        composite -> composite.document.forceClose());
            });
        }

        private void updateTab() {
            Optional<DocumentComposite> currentComposite = Optional.empty();
            if (tabFolder.isPresent()) {
                TabItem[] tabItems = tabFolder.get().getSelection();
                for (TabItem tabItem : tabItems) {
                    Control control = tabItem.getControl();
                    if (control instanceof DocumentComposite) {
                        currentComposite =
                                Optional.of((DocumentComposite) control);
                    }
                }
            } else if (directComposite.isPresent()) {
                currentComposite = Optional.of(directComposite.get());
            }
            if (!currentComposite.isPresent()) {
                return;
            }
            DocumentComposite composite = currentComposite.get();
            setText(composite.document.title());
            composite.tabItem.ifPresent(tabItem -> tabItem
                    .setText(composite.document.shortTitle()));
            setMenuBar(composite.menu);
        }

        private DocumentComposite item(Document document) {
            if (directComposite.isPresent()) {
                DocumentComposite directComposite = this.directComposite.get();
                this.directComposite = Optional.empty();
                directComposite.dispose();
                TabFolder tabFolder = new TabFolder(this, SWT.NONE);
                tabFolder.addListener(SWT.Selection, e -> updateTab());
                this.tabFolder = Optional.of(tabFolder);
                tabItem(directComposite.document);
            } else if (!tabFolder.isPresent()) {
                DocumentComposite composite =
                        new DocumentComposite(this, SWT.NONE, document);
                directComposite = Optional.of(composite);
                updateTab();
                return populate(composite);
            }
            DocumentComposite composite = tabItem(document);
            layout();
            updateTab();
            return composite;
        }

        private void remove(DocumentComposite composite) {
            if (tabFolder.isPresent()) {
                composite.dispose();
                TabFolder tabFolder = this.tabFolder.get();
                List<DocumentComposite> composites =
                        Arrays.stream(tabFolder.getItems())
                                .map(MultiDocumentApplication.this::tab)
                                .filter(Optional::isPresent).map(Optional::get)
                                .collect(Collectors.toList());
                assert !composites.isEmpty();
                if (composites.size() == 1) {
                    directComposite = Optional.of(populate(
                            new DocumentComposite(this, SWT.NONE,
                                    composites.get(0).document)));
                    tabFolder.dispose();
                    this.tabFolder = Optional.empty();
                    layout();
                }
                updateTab();
            } else if (directComposite.isPresent() &&
                    directComposite.get() == composite) {
                dispose();
            }
        }

        private DocumentComposite populate(DocumentComposite composite) {
            composite.populate();
            return composite;
        }

        private DocumentComposite tabItem(Document document) {
            TabFolder tabFolder = this.tabFolder.get();
            TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
            DocumentComposite composite =
                    new DocumentComposite(tabFolder, SWT.NONE, document);
            tabItem.setControl(composite);
            tabItem.setText(composite.document.shortTitle());
            composite.tabItem = Optional.of(tabItem);
            return populate(composite);
        }

        @Override
        protected void checkSubclass() {
        }

        private class DocumentComposite extends Composite {
            public final DocumentShell shell;
            public final SmartMenuBar menu;
            public Optional<TabItem> tabItem = Optional.empty();
            public Document document;
            private long updateStamp;

            public DocumentComposite(Composite parent, int style,
                    Document document) {
                super(parent, style);
                shell = DocumentShell.this;
                this.document = document;
                menu = new SmartMenuBar(parent.getShell());
                addDisposeListener(e -> {
                    composites.remove(this.document);
                    tabItem.ifPresent(Widget::dispose);
                    // Changing menu bar now causes Win32 port to crash
                    display.timerExec(0, () -> OptionalWidget
                            .ifPresent(menu, SmartMenuBar::dispose));
                });
                int updateTime = document.updateTime();
                if (updateTime >= 0) {
                    long nextStamp = ++updateStamp;
                    shell.getDisplay()
                            .timerExec(updateTime, () -> loop(nextStamp));
                }
            }

            public void setDocument(Document document) {
                composites.remove(this.document);
                this.document = document;
                composites.put(document, this);
                int updateTime = document.updateTime();
                if (updateTime >= 0) {
                    long nextStamp = ++updateStamp;
                    shell.getDisplay()
                            .timerExec(updateTime, () -> loop(nextStamp));
                }
            }

            private void populate() {
                Arrays.stream(getChildren()).forEach(Control::dispose);
                Arrays.stream(menu.getItems()).forEach(Widget::dispose);
                setLayout(null);
                MultiDocumentApplication.this.populate(this, menu);
                document.populate(this, menu, MultiDocumentApplication.this);
                layout();
            }

            private void loop(long updateStamp) {
                if (updateStamp < this.updateStamp || isDisposed()) {
                    return;
                }
                document.update(this, menu, MultiDocumentApplication.this);
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
    }
}
