package org.tobi29.scapes.engine.utils

interface ComponentHolder<T : Any> {
    val componentStorage: ComponentStorage<T>

    val components: Collection<T> get() = componentStorage.componentsCollection

    @Suppress("UNCHECKED_CAST")
    fun <C : T> registerComponent(type: ComponentTypeRegistered<C>,
                                  component: C): C =
            component.also {
                if (componentStorage.components.putAbsent(type,
                        component) != null) {
                    throw IllegalStateException("Component already registered")
                }
                if (component is ComponentRegistered) {
                    component.init()
                }
            }

    @Suppress("UNCHECKED_CAST")
    fun <C : T> component(type: ComponentType<C>): C =
            componentStorage.components
                    .computeAbsent(type) { type.create(this) } as C

    fun unregisterComponent(type: ComponentType<T>): Boolean {
        val component = componentStorage.components.remove(type) ?: return false
        if (component is ComponentRegistered) {
            component.dispose()
        }
        return true
    }

    fun clearComponents() {
        while (true) {
            val component = componentStorage.components.keys.firstOrNull() ?: break
            unregisterComponent(component)
        }
    }
}

class ComponentStorage<T : Any> {
    internal val components = ConcurrentHashMap<ComponentType<T>, T>()
    internal val componentsCollection = components.values.readOnly()
}

interface ComponentType<out C : Any> {
    fun create(holder: ComponentHolder<*>): C
}

class ComponentTypeRegistered<out C : Any> : ComponentType<C> {
    override fun create(holder: ComponentHolder<*>): C =
            throw IllegalStateException("Component not registered")
}

interface ComponentRegistered {
    fun init() {}
    fun dispose() {}
}
