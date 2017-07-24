package org.tobi29.scapes.engine.utils.profiler

header object Profiler {
    var enabled: Boolean

    fun current(): ProfilerInstance

    fun reset()
}

header class ProfilerInstance internal constructor() {
    fun enterNode(name: String)

    fun exitNode(name: String)
}
