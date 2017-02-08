/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.resource

import org.tobi29.scapes.engine.utils.task.Joiner

class Resource<out T : Any>(
        private var reference: ResourceReference<T>) {
    constructor(resource: T) : this(ResourceReference(resource))

    fun tryGet(): T? = reference.value

    fun get(): T {
        reference.value?.let { return it }
        while (true) {
            reference.joiner.joiner.join()
            reference.value?.let { return it }
        }
    }
}

class ResourceReference<T : Any>(value: T? = null) {
    var value: T? = value
        set(value) {
            field = value
            value?.let { joiner.join() }
        }

    val resource by lazy { Resource(this) }
    internal val joiner = Joiner.BasicJoinable()
}
