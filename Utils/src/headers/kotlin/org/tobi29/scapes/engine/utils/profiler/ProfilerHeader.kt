package org.tobi29.scapes.engine.utils.profiler

expect class Profiler() {
    val root: Node

    fun current(): ProfilerHandle
}

expect class ProfilerHandle internal constructor(node: Node) {
    fun enterNode(name: String)

    fun exitNode(name: String)
}

expect internal val dispatchers: List<ProfilerDispatcher>
