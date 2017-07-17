package org.tobi29.scapes.engine.resource

internal class ThreadedResource<out T : Any>(
        private var reference: ResourceReference<T>) : Resource<T> {

    override fun tryGet(): T? = reference.value?.get()

    override fun get(): T {
        tryGet()?.let { return it }
        while (true) {
            reference.joiner.joiner.join()
            tryGet()?.let { return it }
        }
    }

    override fun onLoaded(block: () -> Unit) {
        reference.joiner.joiner.onJoin(block)
    }

    override suspend fun getAsync(): T {
        tryGet()?.let { return it }
        reference.joiner.joiner.joinAsync()
        tryGet()?.let { return it }
        throw IllegalStateException("No value after completion")
    }
}
