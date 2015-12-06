package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.GameState;

public class GuiState extends Gui {
    protected final GameState state;

    public GuiState(GameState state, GuiStyle style, GuiAlignment alignment) {
        super(style, alignment);
        this.state = state;
    }

    @Override
    public boolean valid() {
        return state.engine().state() == state;
    }
}
