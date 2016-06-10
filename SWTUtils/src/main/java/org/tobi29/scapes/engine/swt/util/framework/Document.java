package org.tobi29.scapes.engine.swt.util.framework;

import org.eclipse.swt.widgets.Composite;
import org.tobi29.scapes.engine.swt.util.widgets.SmartMenuBar;

public interface Document {
    default boolean modified() {
        return false;
    }

    default boolean close() {
        forceClose();
        return true;
    }

    void forceClose();

    void destroy();

    String title();

    default String shortTitle() {
        return title();
    }

    default boolean empty() {
        return false;
    }

    void populate(Composite composite, SmartMenuBar menu,
            MultiDocumentApplication application);

    default int updateTime() {
        return -1;
    }

    default void update(Composite composite, SmartMenuBar menu,
            MultiDocumentApplication application) {
    }
}
