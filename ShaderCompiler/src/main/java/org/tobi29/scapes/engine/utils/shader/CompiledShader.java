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
