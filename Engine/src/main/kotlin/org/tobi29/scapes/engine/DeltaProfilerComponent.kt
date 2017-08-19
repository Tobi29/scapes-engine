package org.tobi29.scapes.engine

import org.tobi29.scapes.engine.gui.debug.GuiWidgetPerformance
import org.tobi29.scapes.engine.utils.ComponentTypeRegisteredUniversal

class DeltaProfilerComponent(
        private val performance: GuiWidgetPerformance
) : ComponentStep {
    override fun step(delta: Double) {
        performance.updateTimestamp(delta)
    }

    companion object {
        val COMPONENT = ComponentTypeRegisteredUniversal<DeltaProfilerComponent>()
    }
}
