package org.tobi29.scapes.engine.utils.profiler

header class Profiler {
    val root: Node

    fun current(): ProfilerHandle
}

header class ProfilerHandle internal constructor(node: Node) {
    fun enterNode(name: String)

    fun exitNode(name: String)
}

header internal val dispatchers: List<ProfilerDispatcher>
