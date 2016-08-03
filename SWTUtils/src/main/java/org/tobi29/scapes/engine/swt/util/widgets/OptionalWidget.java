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

package org.tobi29.scapes.engine.swt.util.widgets;

import java8.util.Optional;
import java8.util.function.Consumer;
import org.eclipse.swt.widgets.Widget;

public class OptionalWidget {
    public static <T extends Widget> void ifPresent(Optional<T> optional,
            Consumer<T> consumer) {
        if (!optional.isPresent()) {
            return;
        }
        ifPresent(optional.get(), consumer);
    }

    public static <T extends Widget> void ifPresent(T widget,
            Consumer<T> consumer) {
        if (widget.isDisposed()) {
            return;
        }
        consumer.accept(widget);
    }
}
