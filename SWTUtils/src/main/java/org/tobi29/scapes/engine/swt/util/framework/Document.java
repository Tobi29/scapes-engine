/*
 * Copyright 2012-2016 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
