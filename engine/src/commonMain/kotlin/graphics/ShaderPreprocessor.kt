/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.scapes.engine.graphics

import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.scapes.engine.shader.Expression

class ShaderPreprocessor {
    private val properties = ConcurrentHashMap<String, Expression>()

    fun supplyProperty(key: String,
                       value: Expression) {
        properties.put(key, value)
    }

    fun properties(): Map<String, Expression> {
        return properties
    }
}