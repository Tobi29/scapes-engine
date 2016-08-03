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

package org.tobi29.scapes.engine.utils.shader;

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.shader.expression.*;

import java.util.Collections;
import java.util.List;

public class CompiledShader {
    public final List<Statement> declarations;
    public final List<Function> functions;
    public final Optional<ShaderFunction> shaderVertex, shaderFragment;
    public final Optional<ShaderSignature> outputs;
    private final Uniform[] uniforms;

    public CompiledShader(List<Statement> declarations,
            List<Function> functions, Optional<ShaderFunction> shaderVertex,
            Optional<ShaderFunction> shaderFragment,
            Optional<ShaderSignature> outputs, Uniform[] uniforms) {
        this.declarations = Collections.unmodifiableList(declarations);
        this.functions = Collections.unmodifiableList(functions);
        this.shaderVertex = shaderVertex;
        this.shaderFragment = shaderFragment;
        this.outputs = outputs;
        this.uniforms = uniforms;
    }

    public Uniform[] uniforms() {
        return uniforms.clone();
    }
}
