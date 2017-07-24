package org.tobi29.scapes.engine.utils

header class EventDispatcher internal constructor(parent: EventDispatcher? = null) {
    fun enable()

    fun disable()

    fun <E : Any> fire(event: E)
}

header class ListenerRegistrar internal constructor(events: EventDispatcher) {
    val events: EventDispatcher

    inline fun <reified E : Any> listen(
            noinline listener: (E) -> Unit)

    inline fun <reified E : Any> listen(
            priority: Int,
            noinline listener: (E) -> Unit)

    inline fun <reified E : Any> listen(
            noinline accepts: (E) -> Boolean,
            noinline listener: (E) -> Unit)

    inline fun <reified E : Any> listen(
            priority: Int,
            noinline accepts: (E) -> Boolean,
            noinline listener: (E) -> Unit)
}