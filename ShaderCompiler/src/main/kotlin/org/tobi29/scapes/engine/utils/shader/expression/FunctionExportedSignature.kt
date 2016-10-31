/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils.shader.expression

import org.tobi29.scapes.engine.utils.stream
import org.tobi29.scapes.engine.utils.toTypedArray

class FunctionExportedSignature(val name: String,
                                val returned: Types,
                                vararg val parameters: TypeExported) : Expression() {
    constructor(signature: FunctionSignature) : this(signature.name,
            signature.returned, *convertParameters(signature.parameters))

    companion object {
        private fun convertParameters(parameters: Array<out Parameter>) = stream(
                *parameters).map {
            TypeExported(it.type.type, it.type.array != null)
        }.toTypedArray()
    }
}
