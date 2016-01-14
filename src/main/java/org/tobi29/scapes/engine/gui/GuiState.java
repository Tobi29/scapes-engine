package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.GameState;

public class GuiState extends Gui {
    protected final GameState state;

    public GuiState(GameState state, GuiStyle style) {
        super(style);
        this.state = state;
    }

    @Override
    public boolean valid() {
        return state.engine().state() == state;
    }
}
