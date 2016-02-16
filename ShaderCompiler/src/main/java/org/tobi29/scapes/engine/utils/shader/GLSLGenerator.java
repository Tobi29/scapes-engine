package org.tobi29.scapes.engine.utils.shader;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.tobi29.scapes.engine.utils.ArrayUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GLSLGenerator {
    private final String header;
    private final StringBuilder output = new StringBuilder(1024);
    private final Map<String, Expression> fields = new ConcurrentHashMap<>();
    private Map<String, String> properties;
    private List<Function> functions;

    public GLSLGenerator(String header) {
        this.header = header;
        fields.put("out_Position",
                new Expression(ExpressionType.IDENTIFIER, "gl_Position"));
        fields.put("varying_Fragment",
                new Expression(ExpressionType.IDENTIFIER, "gl_FragCoord"));
    }

    private String function(Expression[] expressions)
            throws ShaderGenerateException {
        Expression path = expressions[0];
        String[] arguments = new String[expressions.length - 1];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = expression(expressions[i + 1]);
        }
        return staticFunction(String.valueOf(path.value), arguments);
    }

    private String staticFunction(String name, String[] arguments)
            throws ShaderGenerateException {
        for (Function function : functions) {
            if (!function.signature.name.equals(name)) {
                continue;
            }
            if (function.signature.parameters.length != arguments.length) {
                continue;
            }
            return function.signature.name + '(' +
                    ArrayUtil.join((Object[]) arguments) +
                    ')';
        }
        switch (arguments.length) {
            case 0:
                return staticFunction(name);
            case 1:
                return staticFunction(name, arguments[0]);
            case 2:
                return staticFunction(name, arguments[0], arguments[1]);
            case 3:
                return staticFunction(name, arguments[0], arguments[1],
                        arguments[2]);
            case 4:
                return staticFunction(name, arguments[0], arguments[1],
                        arguments[2], arguments[3]);
        }
        throw new ShaderGenerateException(
                "No functions for given arguments: " + name);
    }

    private String staticFunction(String name) throws ShaderGenerateException {
        switch (name) {
            case "discard":
                return "discard";
        }
        throw new ShaderGenerateException("Unknown function: " + name + "()");
    }

    private String staticFunction(String name, String argument0)
            throws ShaderGenerateException {
        switch (name) {
            case "return":
                return "return " + argument0;
            case "length":
                return "length(" + argument0 + ')';
            case "floor":
                return "floor(" + argument0 + ')';
            case "abs":
                return "abs(" + argument0 + ')';
            case "sin":
                return "sin(" + argument0 + ')';
            case "cos":
                return "cos(" + argument0 + ')';
            case "float":
                return "float(" + argument0 + ')';
            case "vector2":
                return "vec2(" + argument0 + ')';
            case "vector3":
                return "vec3(" + argument0 + ')';
            case "vector4":
                return "vec4(" + argument0 + ')';
        }
        throw new ShaderGenerateException("Unknown function: " + name + "(x)");
    }

    private String staticFunction(String name, String argument0,
            String argument1) throws ShaderGenerateException {
        switch (name) {
            case "texture":
                return "texture(" + argument0 + ", " +
                        argument1 + ')';
            case "min":
                return "min(" + argument0 + ", " +
                        argument1 + ')';
            case "max":
                return "max(" + argument0 + ", " +
                        argument1 + ')';
            case "dot":
                return "dot(" + argument0 + ", " +
                        argument1 + ')';
            case "mod":
                return "mod(" + argument0 + ", " +
                        argument1 + ')';
            case "greaterThan":
                return "greaterThan(" + argument0 + ", " +
                        argument1 + ')';
            case "greaterThanEqual":
                return "greaterThanEqual(" + argument0 + ", " +
                        argument1 + ')';
            case "lessThan":
                return "lessThan(" + argument0 + ", " +
                        argument1 + ')';
            case "lessThanEqual":
                return "lessThanEqual(" + argument0 + ", " +
                        argument1 + ')';
            case "vector2":
                return "vec2(" + argument0 + ", " +
                        argument1 + ')';
            case "vector3":
                return "vec3(" + argument0 + ", " +
                        argument1 + ')';
            case "vector4":
                return "vec4(" + argument0 + ", " +
                        argument1 + ')';
        }
        throw new ShaderGenerateException(
                "Unknown function: " + name + "(x, y)");
    }

    private String staticFunction(String name, String argument0,
            String argument1, String argument2) throws ShaderGenerateException {
        switch (name) {
            case "mix":
                return "mix(" + argument0 + ", " +
                        argument1 + ", " + argument2 + ')';
            case "clamp":
                return "clamp(" + argument0 + ", " +
                        argument1 + ", " + argument2 + ')';
            case "vector3":
                return "vec3(" + argument0 + ", " +
                        argument1 + ", " + argument2 + ')';
            case "vector4":
                return "vec4(" + argument0 + ", " +
                        argument1 + ", " + argument2 + ')';
        }
        throw new ShaderGenerateException(
                "Unknown function: " + name + "(x, y, z)");
    }

    private String staticFunction(String name, String argument0,
            String argument1, String argument2, String argument3)
            throws ShaderGenerateException {
        switch (name) {
            case "vector4":
                return "vec4(" + argument0 + ", " +
                        argument1 + ", " + argument2 + ", " + argument3 + ')';
        }
        throw new ShaderGenerateException(
                "Unknown function: " + name + "(x, y, z, w)");
    }

    private String field(String field) throws ShaderGenerateException {
        Expression expression = fields.get(field);
        if (expression == null) {
            return field;
        }
        return pack(expression(expression));
    }

    @SuppressWarnings("TailRecursion")
    private String expression(Expression expression)
            throws ShaderGenerateException {
        switch (expression.type) {
            case VOID:
                return "";
            case EXPRESSION:
                return expression(expression.expressions[0]);
            case IDENTIFIER:
                if (expression.expressions.length > 0) {
                    Expression field = expression.expressions[0];
                    return pack(expression(field)) + '.' +
                            field(String.valueOf(expression.value));
                }
                return field(String.valueOf(expression.value));
            case FUNCTION:
                return function(expression.expressions);
            case ARRAY:
                return pack(expression(expression.expressions[0])) + '[' +
                        expression(expression.expressions[1]) + ']';
            case ARRAY_EXPRESSION:
                StringBuilder builder = new StringBuilder(32);
                builder.append('(');
                if (expression.expressions.length > 0) {
                    builder.append(expression(expression.expressions[0]));
                    for (int i = 1; i < expression.expressions.length; i++) {
                        builder.append(", ")
                                .append(expression(expression.expressions[i]));
                    }
                }
                builder.append(')');
                return builder.toString();
            case CONDITION_LOGICAL_OR:
                return combine(expression, "||");
            case CONDITION_LOGICAL_AND:
                return combine(expression, "&&");
            case CONDITION_INCLUSIVE_OR:
                return combine(expression, "|");
            case CONDITION_EXCLUSIVE_OR:
                return combine(expression, "^");
            case CONDITION_AND:
                return combine(expression, "&");
            case CONDITION_EQUALS:
                return combine(expression, "==");
            case CONDITION_NOT_EQUALS:
                return combine(expression, "!=");
            case CONDITION_LESS:
                return combine(expression, "<");
            case CONDITION_GREATER:
                return combine(expression, ">");
            case CONDITION_LESS_EQUAL:
                return combine(expression, "<=");
            case CONDITION_GREATER_EQUAL:
                return combine(expression, ">=");
            case SHIFT_LEFT:
                return combine(expression, "<<");
            case SHIFT_RIGHT:
                return combine(expression, ">>");
            case PLUS:
                return combine(expression, "+");
            case MINUS:
                return combine(expression, "-");
            case MULTIPLY:
                return combine(expression, "*");
            case DIVIDE:
                return combine(expression, "/");
            case MODULUS:
                return combine(expression, "%");
            case ASSIGN:
                return combineNotPacked(expression, "=");
            case ASSIGN_SHIFT_LEFT:
                return combineNotPacked(expression, "<<=");
            case ASSIGN_SHIFT_RIGHT:
                return combineNotPacked(expression, ">>=");
            case ASSIGN_PLUS:
                return combineNotPacked(expression, "+=");
            case ASSIGN_MINUS:
                return combineNotPacked(expression, "-=");
            case ASSIGN_MULTIPLY:
                return combineNotPacked(expression, "*=");
            case ASSIGN_DIVIDE:
                return combineNotPacked(expression, "/=");
            case ASSIGN_MODULUS:
                return combineNotPacked(expression, "%=");
            case ASSIGN_AND:
                return combineNotPacked(expression, "&=");
            case ASSIGN_INCLUSIVE_OR:
                return combineNotPacked(expression, "|=");
            case ASSIGN_EXCLUSIVE_OR:
                return combineNotPacked(expression, "^=");
            case CAST:
                return pack(type(types(expression.value))) + ' ' +
                        pack(expression(expression.expressions[0]));
            case INCREMENT_GET:
                return "++" + pack(expression(expression.expressions[0]));
            case DECREMENT_GET:
                return "--" + pack(expression(expression.expressions[0]));
            case GET_INCREMENT:
                return pack(expression(expression.expressions[0])) + "++";
            case GET_DECREMENT:
                return pack(expression(expression.expressions[0])) + "--";
            case POSITIVE:
                return '+' + pack(expression(expression.expressions[0]));
            case NEGATIVE:
                return '-' + pack(expression(expression.expressions[0]));
            case BIT_NOT:
                return '~' + pack(expression(expression.expressions[0]));
            case NOT:
                return '!' + pack(expression(expression.expressions[0]));
            case CONSTANT:
                return String.valueOf(expression.value);
            case PROPERTY_CONSTANT:
                return property(String.valueOf(expression.value));
            case PROPERTY_ARRAY_EXPRESSION:
                try {
                    String source = property(String.valueOf(expression.value));
                    ScapesShaderParser parser = ShaderCompiler.parser(source);
                    Expression propertyExpression = ShaderCompiler
                            .initializer(parser.initializerArrayList());
                    return expression(propertyExpression);
                } catch (ShaderCompileException | ParseCancellationException e) {
                    throw new ShaderGenerateException(e);
                }
            default:
                throw new ShaderGenerateException(
                        "Unexpected expression type: " + expression.type);
        }
    }

    private String combine(Expression expression, String operator)
            throws ShaderGenerateException {
        return pack(expression(expression.expressions[0])) + ' ' + operator +
                ' ' +
                pack(expression(expression.expressions[1]));
    }

    private String combineNotPacked(Expression expression, String operator)
            throws ShaderGenerateException {
        return expression(expression.expressions[0]) + ' ' + operator +
                ' ' +
                pack(expression(expression.expressions[1]));
    }

    private String pack(String expression) throws ShaderGenerateException {
        return '(' + expression + ')';
    }

    private String type(Type type, String identifier)
            throws ShaderGenerateException {
        StringBuilder qualifiers = new StringBuilder(24);
        if (type.constant) {
            qualifiers.append("const ");
        }
        String declarator = qualifiers + type(type.type) + ' ' + identifier;
        if (type.array.isPresent()) {
            return declarator + '[' + expression(type.array.get()) + ']';
        }
        return declarator;
    }

    private String type(Types type) throws ShaderGenerateException {
        switch (type) {
            case Void:
                return "void";
            case Float:
                return "float";
            case Int:
                return "int";
            case Vector2:
                return "vec2";
            case Vector2i:
                return "ivec2";
            case Matrix2:
                return "mat2";
            case Vector3:
                return "vec3";
            case Vector3i:
                return "ivec3";
            case Matrix3:
                return "mat3";
            case Vector4:
                return "vec4";
            case Vector4i:
                return "ivec4";
            case Matrix4:
                return "mat4";
            case Texture2:
                return "sampler2D";
            default:
                throw new ShaderGenerateException("Unexpected type: " + type);
        }
    }

    private Type type(Object type) throws ShaderGenerateException {
        if (type instanceof Type) {
            return (Type) type;
        }
        throw new ShaderGenerateException(
                "Value is no type: " + type.getClass());
    }

    private Types types(Object type) throws ShaderGenerateException {
        if (type instanceof Types) {
            return (Types) type;
        }
        throw new ShaderGenerateException(
                "Value is no type: " + type.getClass());
    }

    private String property(String name) throws ShaderGenerateException {
        String value = properties.get(name);
        if (value == null) {
            throw new ShaderGenerateException("Unknown property: " + name);
        }
        return value;
    }

    private int integer(String value) throws ShaderGenerateException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ShaderGenerateException(e);
        }
    }

    public String generateVertex(CompiledShader shader,
            Map<String, String> properties) throws ShaderGenerateException {
        if (output.length() > 0) {
            output.delete(0, output.length() - 1);
        }
        this.properties = properties;
        functions = shader.functions();
        ShaderFunction shaderVertex = shader.shaderVertex().get();
        ShaderFunction shaderFragment = shader.shaderFragment().get();
        Uniform[] uniforms = shader.uniforms();
        header(uniforms, shaderVertex.signature);
        println();
        header(shaderFragment.signature);
        println();
        header(shader.declarations());
        println();
        functions(functions);
        println();
        shader(shaderVertex);
        return output.toString();
    }

    public String generateFragment(CompiledShader shader,
            Map<String, String> properties) throws ShaderGenerateException {
        if (output.length() > 0) {
            output.delete(0, output.length() - 1);
        }
        this.properties = properties;
        functions = shader.functions();
        ShaderFunction shaderFragment = shader.shaderFragment().get();
        ShaderSignature outputs = shader.outputs().get();
        Uniform[] uniforms = shader.uniforms();
        header(uniforms, shaderFragment.signature);
        println();
        header(outputs);
        println();
        header(shader.declarations());
        println();
        functions(functions);
        println();
        shader(shaderFragment);
        return output.toString();
    }

    private void header(Uniform[] uniforms, ShaderSignature input)
            throws ShaderGenerateException {
        println(0, header);
        println();
        for (Uniform uniform : uniforms) {
            if (uniform != null) {
                println(0,
                        "uniform " + type(uniform.type) + ' ' + uniform.name +
                                ';');
            }
        }
        println();
        for (ShaderParameter parameter : input.parameters) {
            if (!Boolean.parseBoolean(expression(parameter.available))) {
                continue;
            }
            if (parameter.id == -1) {
                println(0, "in " + type(parameter.type, parameter.name) +
                        ';');
            } else {
                println(0, "layout(location = " + parameter.id + ") in " +
                        type(parameter.type, parameter.name) + ';');
            }
        }
    }

    private void header(ShaderSignature output) throws ShaderGenerateException {
        for (ShaderParameter parameter : output.parameters) {
            if (!Boolean.parseBoolean(expression(parameter.available))) {
                continue;
            }
            if (parameter.id == -1) {
                println(0, "out " + type(parameter.type, parameter.name) +
                        ';');
            } else {
                println(0, "layout(location = " + parameter.id + ") out " +
                        type(parameter.type, parameter.name) + ';');
            }
        }
    }

    private void header(List<Expression> expressions)
            throws ShaderGenerateException {
        for (Expression expression : expressions) {
            statement(expression, 0);
        }
    }

    private void functions(List<Function> functions)
            throws ShaderGenerateException {
        for (Function function : functions) {
            FunctionSignature signature = function.signature;
            println(0, signature(signature));
            block(function.compound, 1);
            println(0, "}");
        }
    }

    private String signature(FunctionSignature signature)
            throws ShaderGenerateException {
        StringBuilder output = new StringBuilder(24);
        output.append(type(signature.returned)).append(' ');
        output.append(signature.name).append('(');
        if (signature.parameters.length > 0) {
            output.append(type(signature.parameters[0].type,
                    signature.parameters[0].name));
            for (int i = 1; i < signature.parameters.length; i++) {
                output.append(", ").append(type(signature.parameters[i].type,
                        signature.parameters[i].name));
            }
        }
        output.append(") {");
        return output.toString();
    }

    private void shader(ShaderFunction function)
            throws ShaderGenerateException {
        println(0, "void main(void) {");
        block(function.compound, 1);
        println(0, "}");
    }

    private void block(Expression expression, int level)
            throws ShaderGenerateException {
        switch (expression.type) {
            case COMPOUND:
            case BLOCK:
                for (Expression statement : expression.expressions) {
                    statement(statement, level);
                }
                break;
            default:
                statement(expression, level);
                break;
        }
    }

    private void statement(Expression expression, int level)
            throws ShaderGenerateException {
        switch (expression.type) {
            case COMPOUND:
                println(level, "{");
                block(expression, level + 1);
                println(level, "}");
                break;
            case BLOCK:
                block(expression, level);
                break;
            case INITIALIZE:
                println(level, type(type(expression.value),
                        expression(expression.expressions[0])) + ';');
                break;
            case INITIALIZE_ARRAY:
                Type type = type(expression.value);
                String name = String.valueOf(expression.expressions[0].value);
                switch (expression.expressions[1].type) {
                    case CONSTANT:
                        println(level, type(type, name) + '[' +
                                expression.expressions[1].value + "];");
                        break;
                    case ARRAY_EXPRESSION:
                        println(level, type(type, name) + '[' +
                                expression.expressions[1].expressions.length +
                                "] = " + type(type.type) + "[]" +
                                expression(expression.expressions[1]) +
                                ';');
                        break;
                    case PROPERTY_ARRAY_EXPRESSION:
                        String source = property(String.valueOf(
                                expression.expressions[1].value));
                        try {
                            ScapesShaderParser parser =
                                    ShaderCompiler.parser(source);
                            Expression propertyExpression = ShaderCompiler
                                    .initializer(parser.initializerArrayList());
                            println(level, type(type, name) + '[' +
                                    propertyExpression.expressions.length +
                                    "] = " + type(type.type) + "[]" +
                                    expression(propertyExpression) +
                                    ';');
                        } catch (ParseCancellationException | ShaderCompileException e) {
                            throw new ShaderGenerateException(e);
                        }
                        break;
                }
                break;
            case IF:
                if (expression.expressions[0].type ==
                        ExpressionType.PROPERTY_CONSTANT) {
                    if (Boolean.parseBoolean(property(
                            String.valueOf(expression.expressions[0].value)))) {
                        println(level, "{");
                        block(expression.expressions[1], level + 1);
                        println(level, "}");
                    }
                } else {
                    println(level,
                            "if(" + expression(expression.expressions[0]) +
                                    ") {");
                    block(expression.expressions[1], level + 1);
                    println(level, "}");
                }
                break;
            case IF_ELSE:
                if (expression.expressions[0].type ==
                        ExpressionType.PROPERTY_CONSTANT) {
                    println(level, "{");
                    if (Boolean.parseBoolean(property(
                            String.valueOf(expression.expressions[0].value)))) {
                        block(expression.expressions[1], level + 1);
                    } else {
                        block(expression.expressions[2], level + 1);
                    }
                    println(level, "}");
                } else {
                    println(level,
                            "if(" + expression(expression.expressions[0]) +
                                    ") {");
                    block(expression.expressions[1], level + 1);
                    println(level, "} else {");
                    block(expression.expressions[2], level + 1);
                    println(level, "}");
                }
                break;
            case LOOP_RANGE:
                name = String.valueOf(expression.value);
                int min = integer(expression(expression.expressions[0]));
                int max = integer(expression(expression.expressions[1]));
                if (fields.containsKey(name)) {
                    throw new ShaderGenerateException(
                            "Duplicate field: " + name);
                }
                for (int i = min; i < max; i++) {
                    fields.put(name, new Expression(ExpressionType.CONSTANT,
                            String.valueOf(i)));
                    println(level, "{");
                    block(expression.expressions[2], level + 1);
                    println(level, "}");
                }
                fields.remove(name);
                break;
            default:
                println(level, expression(expression) + ';');
                break;
        }
    }

    private void println() {
        output.append('\n');
    }

    private void println(int level, String str) {
        while (level > 0) {
            output.append("    ");
            level--;
        }
        output.append(str).append('\n');
    }
}
