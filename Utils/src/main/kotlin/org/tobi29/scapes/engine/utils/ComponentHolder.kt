package org.tobi29.scapes.engine.utils

interface ComponentHolder<T : Any> {
    val componentStorage: ComponentStorage<ComponentHolder<T>, T>

    val components: Collection<T> get() = componentStorage.componentsCollection

    fun <H : ComponentHolder<out T>, C : T> registerComponent(
            type: ComponentTypeRegistered<H, C, T>,
            component: C
    ): C = componentStorage.registerComponent(type, component)

    @Suppress("UNCHECKED_CAST")
    operator fun <H : ComponentHolder<out T>, C : T> get(
            type: ComponentType<H, C, T>
    ): C = componentStorage.get(this as H, type)

    fun <H : ComponentHolder<out T>, C : T> getOrNull(
            type: ComponentType<H, C, T>
    ): C? = componentStorage.getOrNull(type)

    fun <H : ComponentHolder<out T>, C : T> unregisterComponent(
            type: ComponentType<H, C, T>
    ): Boolean = componentStorage.unregisterComponent(type)

    fun clearComponents() = componentStorage.clearComponents()
}

class ComponentStorage<out H : ComponentHolder<out T>, T : Any> {
    private val components = ConcurrentHashMap<ComponentType<H, T, T>, T>()
    internal val componentsCollection = components.values.readOnly()

    @Suppress("UNCHECKED_CAST")
    fun <H : ComponentHolder<out T>, C : T> registerComponent(
            type: ComponentTypeRegistered<H, C, T>,
            component: C
    ): C {
        type.permission?.let { checkPermission(it) }

        components as ConcurrentHashMap<ComponentType<H, C, T>, C>

        if (components.putAbsent(type, component) != null) {
            throw IllegalStateException("Component already registered")
        }
        if (component is ComponentRegistered) {
            component.init()
        }
        return component
    }

    @Suppress("UNCHECKED_CAST")
    fun <H : ComponentHolder<out T>, C : T> get(
            holder: H,
            type: ComponentType<H, C, T>
    ): C {
        type.permission?.let { checkPermission(it) }

        components as ConcurrentHashMap<ComponentType<H, C, T>, C>

        return components.computeAbsent(type) { type.create(holder) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <H : ComponentHolder<out T>, C : T> getOrNull(
            type: ComponentType<H, C, T>
    ): C? {
        type.permission?.let { checkPermission(it) }

        components as ConcurrentHashMap<ComponentType<H, C, T>, C>

        return components[type]
    }

    @Suppress("UNCHECKED_CAST")
    fun <H : ComponentHolder<out T>, C : T> unregisterComponent(
            type: ComponentType<H, C, T>
    ): Boolean {
        type.permission?.let { checkPermission(it) }

        components as ConcurrentHashMap<ComponentType<H, C, T>, C>

        val component = components.remove(type) ?: return false
        if (component is ComponentRegistered) {
            component.dispose()
        }
        return true
    }

    fun clearComponents() {
        while (true) {
            val component = components.keys.firstOrNull() ?: break
            unregisterComponent(component)
        }
    }
}

interface ComponentType<in H : ComponentHolder<out T>, out C : T, out T : Any> {
    val permission: String? get() = null

    fun create(holder: H): C

    companion object {
        fun <H : ComponentHolder<out T>, C : T, T : Any> of(supplier: (H) -> C): ComponentType<H, C, T> =
                object : ComponentType<H, C, T> {
                    override fun create(holder: H): C = supplier(holder)
                }
    }
}

open class ComponentTypeRegistered<in H : ComponentHolder<out T>, out C : T, out T : Any> : ComponentType<H, C, T> {
    override fun create(holder: H): C =
            throw IllegalStateException("Component not registered")
}

class ComponentTypeRegisteredPermission<in H : ComponentHolder<out T>, out C : T, out T : Any>(
        override val permission: String
) : ComponentTypeRegistered<H, C, T>()

interface ComponentRegistered {
    fun init() {}
    fun dispose() {}
}

typealias ComponentTypeUniversal<C> =
ComponentType<ComponentHolder<out C>, C, C>

typealias ComponentTypeRegisteredUniversal<C> =
ComponentTypeRegistered<ComponentHolder<out C>, C, C>

typealias ComponentTypeRegisteredPermissionUniversal<C> =
ComponentTypeRegisteredPermission<ComponentHolder<out C>, C, C>
