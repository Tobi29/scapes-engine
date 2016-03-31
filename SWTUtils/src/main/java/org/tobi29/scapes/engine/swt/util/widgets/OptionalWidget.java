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
