package org.tobi29.scapes.engine.input

inline fun keyPressesForScroll(
        delta: ScrollDelta,
        addPressEvent: (ControllerKey, ControllerButtons.Action) -> Unit
) {
    val x: Double
    val y: Double
    when (delta) {
        is ScrollDelta.Pixel -> {
            x = delta.delta.x
            y = delta.delta.y
        }
        is ScrollDelta.Line -> {
            x = delta.delta.x
            y = delta.delta.y
        }
        is ScrollDelta.Page -> {
            x = delta.delta.x
            y = delta.delta.y
        }
    }
    keyPressesForScroll(x, y, addPressEvent)
}

inline fun keyPressesForScroll(
        x: Double,
        y: Double,
        addPressEvent: (ControllerKey, ControllerButtons.Action) -> Unit
) {
    if (x < 0.0) {
        addPressEvent(ControllerKey.SCROLL_LEFT,
                ControllerButtons.Action.PRESS)
        addPressEvent(ControllerKey.SCROLL_LEFT,
                ControllerButtons.Action.RELEASE)
    } else if (x > 0.0) {
        addPressEvent(ControllerKey.SCROLL_RIGHT,
                ControllerButtons.Action.PRESS)
        addPressEvent(ControllerKey.SCROLL_RIGHT,
                ControllerButtons.Action.RELEASE)
    }
    if (y < 0.0) {
        addPressEvent(ControllerKey.SCROLL_DOWN,
                ControllerButtons.Action.PRESS)
        addPressEvent(ControllerKey.SCROLL_DOWN,
                ControllerButtons.Action.RELEASE)
    } else if (y > 0.0) {
        addPressEvent(ControllerKey.SCROLL_UP,
                ControllerButtons.Action.PRESS)
        addPressEvent(ControllerKey.SCROLL_UP,
                ControllerButtons.Action.RELEASE)
    }
}
