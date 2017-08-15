package org.tobi29.scapes.engine.utils

interface ComponentHolder<T : Any> {
    val componentStorage: ComponentStorage<T>

    val components: Collection<T> get() = componentStorage.componentsCollection

    @Suppress("UNCHECKED_CAST")
    fun <C : T> registerComponent(type: ComponentTypeRegistered<C>,
                                  component: C): C {
        type.permission?.let { checkPermission(it) }
        return component.also {
            if (componentStorage.components.putAbsent(type,
                    component) != null) {
                throw IllegalStateException("Component already registered")
            }
            if (component is ComponentRegistered) {
                component.init()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <C : T> get(type: ComponentType<T, C>): C {
        type.permission?.let { checkPermission(it) }
        return componentStorage.components
                .computeAbsent(type) { type.create(this) } as C
    }

    @Suppress("UNCHECKED_CAST")
    fun <C : T> getOrNull(type: ComponentType<T, C>): C {
        type.permission?.let { checkPermission(it) }
        return componentStorage.components[type] as C
    }

    fun unregisterComponent(type: ComponentType<in T, T>): Boolean {
        type.permission?.let { checkPermission(it) }
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
    internal val components = ConcurrentHashMap<ComponentType<in T, T>, T>()
    internal val componentsCollection = components.values.readOnly()
}

interface ComponentType<T : Any, out C : T> {
    val permission: String? get() = null

    fun create(holder: ComponentHolder<T>): C

    companion object {
        fun <T : Any, C : T> of(supplier: (ComponentHolder<T>) -> C): ComponentType<T, C> =
                object : ComponentType<T, C> {
                    override fun create(holder: ComponentHolder<T>): C =
                            supplier(holder)
                }
    }
}

open class ComponentTypeRegistered<out C : Any> : ComponentType<Any, C> {
    override fun create(holder: ComponentHolder<Any>): C =
            throw IllegalStateException("Component not registered")
}

class ComponentTypeRegisteredPermission<out C : Any>(
        override val permission: String
) : ComponentTypeRegistered<C>()

interface ComponentRegistered {
    fun init() {}
    fun dispose() {}
}
