package org.tobi29.scapes.engine.utils.shader;

import java8.util.Optional;

import java.util.ArrayList;
import java.util.List;

public class CompiledShader {
    private static final Uniform[] EMPTY_UNIFORM = {};
    private final List<Expression> declarations;
    private final List<Function> functions;
    private final Optional<ShaderFunction> shaderVertex, shaderFragment;
    private final Optional<ShaderSignature> outputs;
    private final Uniform[] uniforms;

    public CompiledShader(List<Expression> declarations,
            List<Function> functions, Optional<ShaderFunction> shaderVertex,
            Optional<ShaderFunction> shaderFragment,
            Optional<ShaderSignature> outputs, Uniform[] uniforms) {
        this.declarations = declarations;
        this.functions = functions;
        this.shaderVertex = shaderVertex;
        this.shaderFragment = shaderFragment;
        this.outputs = outputs;
        this.uniforms = uniforms;
    }

    public List<Expression> declarations() {
        List<Expression> declarations = new ArrayList<>();
        declarations.addAll(this.declarations);
        return declarations;
    }

    public List<Function> functions() {
        List<Function> functions = new ArrayList<>();
        functions.addAll(this.functions);
        return functions;
    }

    public Uniform[] uniforms() {
        return uniforms.clone();
    }

    public Optional<ShaderFunction> shaderVertex() {
        return shaderVertex;
    }

    public Optional<ShaderFunction> shaderFragment() {
        return shaderFragment;
    }

    public Optional<ShaderSignature> outputs() {
        return outputs;
    }
}
