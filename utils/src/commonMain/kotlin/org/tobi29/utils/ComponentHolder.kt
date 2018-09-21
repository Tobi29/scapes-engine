/*
 * Copyright 2012-2018 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.utils

import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.checkPermission
import org.tobi29.stdex.readOnly

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

    @Suppress("UNCHECKED_CAST")
    fun <H : ComponentHolder<out T>, C : T> unregisterComponent(
        type: ComponentType<H, C, T>
    ): Boolean = componentStorage.unregisterComponent(this as H, type)

    @Suppress("UNCHECKED_CAST")
    fun clearComponents() = componentStorage.clearComponents(this)
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

        if (components.putIfAbsent(type, component) != null) {
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
            if (components.putIfAbsent(type, component) == null) {
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
        holder: H,
        type: ComponentType<H, C, T>
    ): Boolean {
        type.permission?.let { checkPermission(it) }
        verifyRemove(type)

        components as ConcurrentHashMap<ComponentType<H, C, T>, C>

        val component = components.remove(type) ?: return false
        if (component is ComponentRegisteredHolder<*>) {
            component as ComponentRegisteredHolder<H>
            component.dispose(holder)
        }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    fun <H : ComponentHolder<out T>> clearComponents(
        holder: H
    ) {
        while (true) {
            val component = components.keys.firstOrNull() ?: break
            component as ComponentType<H, T, T>
            unregisterComponent(holder, component)
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

open class ComponentTypeRegistered<in H : ComponentHolder<out T>, out C : T, out T : Any> :
    ComponentType<H, C, T> {
    override fun create(holder: H): C =
        throw IllegalStateException("Component not registered")
}

class ComponentTypeRegisteredPermission<in H : ComponentHolder<out T>, out C : T, out T : Any>(
    override val permission: String
) : ComponentTypeRegistered<H, C, T>()

interface ComponentRegisteredHolder<in H : ComponentHolder<out Any>> {
    fun init(holder: H) {}
    fun dispose(holder: H) {}
}

interface ComponentRegistered :
    ComponentRegisteredHolder<ComponentHolder<out Any>> {
}

typealias ComponentTypeUniversal<C> =
        ComponentType<ComponentHolder<out C>, C, C>

typealias ComponentTypeRegisteredUniversal<C> =
        ComponentTypeRegistered<ComponentHolder<out C>, C, C>

typealias ComponentTypeRegisteredPermissionUniversal<C> =
        ComponentTypeRegisteredPermission<ComponentHolder<out C>, C, C>
