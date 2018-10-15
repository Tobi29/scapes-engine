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

package org.tobi29.scapes.engine.shader

import org.tobi29.stdex.assert

class Scope(private vararg val parents: Scope) {
    private val map = HashMap<String, Identifier>()

    fun add(name: String,
            type: TypeExported): Identifier? {
        if (map.containsKey(name)) {
            return null
        }
        val variable = Identifier(name, type)
        val old = map.put(name, variable)
        assert { old == null }
        return variable
    }

    fun add(identifier: Identifier) {
        map[identifier.name] = identifier
    }

    operator fun get(name: String): Identifier? {
        map[name]?.let { return it }
        parents.forEach { parent ->
            parent[name]?.let { return it }
        }
        return null
    }

    fun check(other: Scope): Boolean {
        return map == other.map
    }

    override fun toString(): String = map.toString()
}
