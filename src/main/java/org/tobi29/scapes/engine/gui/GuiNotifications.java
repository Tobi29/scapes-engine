package org.tobi29.scapes.engine.gui;

import java8.util.function.Function;

import java.util.concurrent.atomic.AtomicLong;

public class GuiNotifications extends Gui {
    private final GuiComponentGroup pane;
    private final AtomicLong id = new AtomicLong(Long.MIN_VALUE);

    public GuiNotifications(GuiStyle style) {
        super(style);
        spacer();
        pane = addHori(0, 0, 310, -1, GuiComponentGroup::new);
    }

    public <T extends GuiComponent> T add(
            Function<GuiLayoutDataVertical, T> child) {
        return pane
                .addVert(10, 10, 10, 10, -1, 60, id.getAndIncrement(), child);
    }

    @Override
    public boolean valid() {
        return true;
    }
}
