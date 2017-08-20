package org.tobi29.scapes.engine.utils

interface ComponentHolder<T : Any> {
    val componentStorage: ComponentStorage<T>

    val components: Collection<T> get() = componentStorage.componentsCollection

    @Suppress("UNCHECKED_CAST")
    fun <H : ComponentHolder<out T>, C : T> registerComponent(
            type: ComponentTypeRegistered<H, C, T>,
            component: C
    ): C = componentStorage.registerComponent(this as H, type, component)

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

class ComponentStorage<T : Any>(
        private val verifyAdd: (ComponentType<*, T, T>) -> Unit = {},
        private val verifyRemove: (ComponentType<*, T, T>) -> Unit = verifyAdd
) {
    private val components = ConcurrentHashMap<ComponentType<*, T, T>, T>()
    internal val componentsCollection = components.values.readOnly()

    @Suppress("UNCHECKED_CAST")
    fun <H : ComponentHolder<out T>, C : T> registerComponent(
            holder: H,
            type: ComponentTypeRegistered<H, C, T>,
            component: C
    ): C {
        type.permission?.let { checkPermission(it) }
        verifyAdd(type as ComponentType<H, T, T>)

        components as ConcurrentHashMap<ComponentType<H, C, T>, C>

        if (components.putAbsent(type, component) != null) {
            throw IllegalStateException("Component already registered")
        }
        if (component is ComponentRegisteredHolder<*>) {
            component as ComponentRegisteredHolder<H>
            component.init(holder)
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

        components[type]?.let { return it }

        return type.create(holder).also { verifyAdd(type) }.also { component ->
            if (components.putAbsent(type, component) == null) {
                if (component is ComponentRegisteredHolder<*>) {
                    component as ComponentRegisteredHolder<H>
                    component.init(holder)
                }
            }
        }
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
        verifyRemove(type)

        components as ConcurrentHashMap<ComponentType<H, C, T>, C>

        val component = components.remove(type) ?: return false
        if (component is ComponentRegisteredHolder<*>) {
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

interface ComponentRegisteredHolder<in H : ComponentHolder<out Any>> {
    fun init(holder: H) {}
    fun dispose() {}
}

interface ComponentRegistered : ComponentRegisteredHolder<ComponentHolder<out Any>> {
    fun init() {}

    override fun init(holder: ComponentHolder<out Any>) = init()
}

typealias ComponentTypeUniversal<C> =
ComponentType<ComponentHolder<out C>, C, C>

typealias ComponentTypeRegisteredUniversal<C> =
ComponentTypeRegistered<ComponentHolder<out C>, C, C>

typealias ComponentTypeRegisteredPermissionUniversal<C> =
ComponentTypeRegisteredPermission<ComponentHolder<out C>, C, C>
