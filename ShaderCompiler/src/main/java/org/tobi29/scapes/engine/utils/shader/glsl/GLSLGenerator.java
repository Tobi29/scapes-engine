package org.tobi29.scapes.engine.utils.shader.glsl;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.tobi29.scapes.engine.utils.ArrayUtil;
import org.tobi29.scapes.engine.utils.shader.*;
import org.tobi29.scapes.engine.utils.shader.expression.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GLSLGenerator {
    private final Version version;
    private final StringBuilder output = new StringBuilder(1024);
    private final Map<String, Expression> variables = new ConcurrentHashMap<>();
    private Map<String, String> properties;
    private List<Function> functions;

    public GLSLGenerator(Version version) {
        this.version = version;
        variables.put("out_Position", new GLSLExpression("gl_Position"));
        variables.put("varying_Fragment", new GLSLExpression("gl_FragCoord"));
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

    private String variable(String field) throws ShaderGenerateException {
        Expression expression = variables.get(field);
        if (expression == null) {
            return field;
        }
        return pack(expression);
    }

    private String expression(Expression expression)
            throws ShaderGenerateException {
        if (expression instanceof AssignmentExpression) {
            return assignmentExpression((AssignmentExpression) expression);
        } else if (expression instanceof ConditionExpression) {
            return conditionExpression((ConditionExpression) expression);
        } else if (expression instanceof OperationExpression) {
            return operationExpression((OperationExpression) expression);
        } else if (expression instanceof UnaryExpression) {
            return unaryExpression((UnaryExpression) expression);
        } else if (expression instanceof TernaryExpression) {
            return ternaryExpression((TernaryExpression) expression);
        } else if (expression instanceof FunctionExpression) {
            return functionExpression((FunctionExpression) expression);
        } else if (expression instanceof ArrayAccessExpression) {
            return arrayAccessExpression((ArrayAccessExpression) expression);
        } else if (expression instanceof BooleanExpression) {
            return booleanExpression((BooleanExpression) expression);
        } else if (expression instanceof IntegerExpression) {
            return integerExpression((IntegerExpression) expression);
        } else if (expression instanceof FloatingExpression) {
            return floatingExpression((FloatingExpression) expression);
        } else if (expression instanceof VariableExpression) {
            return variableExpression((VariableExpression) expression);
        } else if (expression instanceof MemberExpression) {
            return memberExpression((MemberExpression) expression);
        } else if (expression instanceof PropertyExpression) {
            return propertyExpression((PropertyExpression) expression);
        } else if (expression instanceof GLSLExpression) {
            return glslExpression((GLSLExpression) expression);
        } else if (expression instanceof VoidExpression) {
            return "";
        }
        throw new ShaderGenerateException(
                "Unknown expression: " + expression.getClass());
    }

    private String assignmentExpression(AssignmentExpression expression)
            throws ShaderGenerateException {
        return combineNotPacked(expression.left, expression.right,
                assignmentOperator(expression.type));
    }

    private String assignmentOperator(AssignmentType type)
            throws ShaderGenerateException {
        switch (type) {
            case ASSIGN:
                return "=";
            case ASSIGN_SHIFT_LEFT:
                return "<<=";
            case ASSIGN_SHIFT_RIGHT:
                return ">>=";
            case ASSIGN_PLUS:
                return "+=";
            case ASSIGN_MINUS:
                return "-=";
            case ASSIGN_MULTIPLY:
                return "*=";
            case ASSIGN_DIVIDE:
                return "/=";
            case ASSIGN_MODULUS:
                return "%=";
            case ASSIGN_AND:
                return "&=";
            case ASSIGN_INCLUSIVE_OR:
                return "|=";
            case ASSIGN_EXCLUSIVE_OR:
                return "^=";
            default:
                throw new ShaderGenerateException(
                        "Unexpected expression type: " + type);
        }
    }

    private String conditionExpression(ConditionExpression expression)
            throws ShaderGenerateException {
        return combine(expression.left, expression.right,
                conditionOperator(expression.type));
    }

    private String conditionOperator(ConditionType type)
            throws ShaderGenerateException {
        switch (type) {
            case CONDITION_LOGICAL_OR:
                return "||";
            case CONDITION_LOGICAL_AND:
                return "&&";
            case CONDITION_INCLUSIVE_OR:
                return "|";
            case CONDITION_EXCLUSIVE_OR:
                return "^";
            case CONDITION_AND:
                return "&";
            case CONDITION_EQUALS:
                return "==";
            case CONDITION_NOT_EQUALS:
                return "!=";
            case CONDITION_LESS:
                return "<";
            case CONDITION_GREATER:
                return ">";
            case CONDITION_LESS_EQUAL:
                return "<=";
            case CONDITION_GREATER_EQUAL:
                return ">=";
            default:
                throw new ShaderGenerateException(
                        "Unexpected expression type: " + type);
        }
    }

    private String operationExpression(OperationExpression expression)
            throws ShaderGenerateException {
        return combine(expression.left, expression.right,
                operationOperator(expression.type));
    }

    private String operationOperator(OperationType type)
            throws ShaderGenerateException {
        switch (type) {
            case SHIFT_LEFT:
                return "<<";
            case SHIFT_RIGHT:
                return ">>";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case MULTIPLY:
                return "*";
            case DIVIDE:
                return "/";
            case MODULUS:
                return "%";
            default:
                throw new ShaderGenerateException(
                        "Unexpected expression type: " + type);
        }
    }

    private String unaryExpression(UnaryExpression expression)
            throws ShaderGenerateException {
        String str = pack(expression.value);
        switch (expression.type) {
            case INCREMENT_GET:
                return "++" + str;
            case DECREMENT_GET:
                return "--" + str;
            case GET_INCREMENT:
                return str + "++";
            case GET_DECREMENT:
                return str + "--";
            case POSITIVE:
                return '+' + str;
            case NEGATIVE:
                return '-' + str;
            case BIT_NOT:
                return '~' + str;
            case NOT:
                return '!' + str;
            default:
                throw new ShaderGenerateException(
                        "Unexpected expression type: " + expression.type);
        }
    }

    private String ternaryExpression(TernaryExpression expression)
            throws ShaderGenerateException {
        return pack(expression.condition) + " ? " +
                pack(expression.expression) + " : " +
                pack(expression.expressionElse);
    }

    private String functionExpression(FunctionExpression expression)
            throws ShaderGenerateException {
        String[] args = new String[expression.args.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = expression(expression.args.get(i));
        }
        return staticFunction(expression.name, args);
    }

    private String arrayAccessExpression(ArrayAccessExpression expression)
            throws ShaderGenerateException {
        return pack(expression.name) + '[' +
                expression(expression.index) + ']';
    }

    private String booleanExpression(BooleanExpression expression) {
        return Boolean.toString(expression.value);
    }

    private String integerExpression(IntegerExpression expression)
            throws ShaderGenerateException {
        if (expression instanceof IntegerLiteralExpression) {
            return ((IntegerLiteralExpression) expression).value.toString();
        } else if (expression instanceof IntegerPropertyExpression) {
            return property(((IntegerPropertyExpression) expression).key);
        }
        throw new ShaderGenerateException(
                "Unknown integer: " + expression.getClass());
    }

    private String floatingExpression(FloatingExpression expression) {
        String str = expression.value.toString();
        if (str.indexOf('.') == -1) {
            return str + ".0";
        }
        return str;
    }

    private String variableExpression(VariableExpression expression)
            throws ShaderGenerateException {
        return variable(expression.name);
    }

    private String memberExpression(MemberExpression expression)
            throws ShaderGenerateException {
        return pack(expression.member) + '.' + variable(expression.name);
    }

    private String propertyExpression(PropertyExpression expression)
            throws ShaderGenerateException {
        return property(expression.key);
    }

    private String glslExpression(GLSLExpression expression) {
        return expression.code;
    }

    private void ifStatement(IfStatement expression, int level)
            throws ShaderGenerateException {
        String condition = expression(expression.condition);
        if ("true".equals(condition)) {
            statement(expression.statement, level);
        } else if ("false".equals(condition)) {
            if (expression.statementElse.isPresent()) {
                statement(expression.statementElse.get(), level);
            }
        } else {
            println(level, "if(" + condition +
                    ')');
            statement(expression.statement, level);
            if (expression.statementElse.isPresent()) {
                println(level, "else");
                statement(expression.statementElse.get(), level);
            }
        }
    }

    private void loopFixedStatement(LoopFixedStatement expression, int level)
            throws ShaderGenerateException {
        String name = String.valueOf(expression.name);
        int start = integer(expression(expression.start));
        int end = integer(expression(expression.end));
        if (variables.containsKey(name)) {
            throw new ShaderGenerateException("Duplicate field: " + name);
        }
        for (int i = start; i < end; i++) {
            variables.put(name, new GLSLExpression(String.valueOf(i)));
            statement(expression.statement, level);
        }
        variables.remove(name);
    }

    private void declarationStatement(DeclarationStatement expression,
            int level) throws ShaderGenerateException {
        println(level, declarationStatement(expression) + ';');
    }

    private String declarationStatement(DeclarationStatement statement)
            throws ShaderGenerateException {
        StringBuilder str = new StringBuilder(32);
        str.append(type(statement.type));
        str.append(' ');
        boolean first = true;
        for (Declaration declaration : statement.declarations) {
            if (first) {
                first = false;
            } else {
                str.append(", ");
            }
            str.append(declaration(statement.type, declaration));
        }
        return str.toString();
    }

    private String declaration(Type type, Declaration declaration)
            throws ShaderGenerateException {
        String str = identifier(type, declaration.name);
        if (declaration.initializer.isPresent()) {
            return str + " = " + expression(declaration.initializer.get());
        }
        return str;
    }

    private void arrayDeclarationStatement(ArrayDeclarationStatement statement,
            int level) throws ShaderGenerateException {
        println(level, arrayDeclarationStatement(statement) + ';');
    }

    private String arrayDeclarationStatement(
            ArrayDeclarationStatement statement)
            throws ShaderGenerateException {
        StringBuilder str = new StringBuilder(32);
        str.append(type(statement.type));
        str.append(' ');
        boolean first = true;
        for (ArrayDeclaration declaration : statement.declarations) {
            if (first) {
                first = false;
            } else {
                str.append(", ");
            }
            str.append(arrayDeclaration(statement.type, declaration));
        }
        return str.toString();
    }

    private String arrayDeclaration(Type type, ArrayDeclaration declaration)
            throws ShaderGenerateException {
        return identifier(type, declaration.name);
    }

    private void arrayUnsizedDeclarationStatement(
            ArrayUnsizedDeclarationStatement statement, int level)
            throws ShaderGenerateException {
        ArrayLiteralExpression initializer;
        if (statement.initializer instanceof PropertyArrayExpression) {
            initializer =
                    property((PropertyArrayExpression) statement.initializer);
        } else if (statement.initializer instanceof ArrayLiteralExpression) {
            initializer = (ArrayLiteralExpression) statement.initializer;
        } else {
            throw new ShaderGenerateException(
                    "Unknown array initializer: " + statement.getClass());
        }
        println(level, type(statement.type, statement.name) + '[' +
                initializer.content.size() + "] = " +
                type(statement.type.type) + "[]" +
                arrayExpression(initializer) + ';');
    }

    private String arrayExpression(ArrayLiteralExpression initializer)
            throws ShaderGenerateException {
        StringBuilder str = new StringBuilder(initializer.content.size() * 7);
        str.append('(');
        boolean first = true;
        for (Expression expression : initializer.content) {
            if (first) {
                first = false;
            } else {
                str.append(", ");
            }
            str.append(expression(expression));
        }
        str.append(')');
        return str.toString();
    }

    private String combine(Expression a, Expression b, String operator)
            throws ShaderGenerateException {
        return pack(a) + ' ' + operator + ' ' + pack(b);
    }

    private String combineNotPacked(Expression a, Expression b, String operator)
            throws ShaderGenerateException {
        return expression(a) + ' ' + operator + ' ' + pack(b);
    }

    private String pack(Expression expression) throws ShaderGenerateException {
        return '(' + expression(expression) + ')';
    }

    private String type(Type type, String identifier)
            throws ShaderGenerateException {
        StringBuilder str = new StringBuilder(24);
        if (type.constant) {
            str.append("const ");
        }
        switch (version) {
            case GLES_300:
                str.append(precision(type.precision)).append(' ');
                break;
        }
        str.append(type(type.type));
        str.append(' ');
        str.append(identifier);
        if (type.array.isPresent()) {
            str.append('[');
            str.append(expression(type.array.get()));
            str.append(']');
        }
        return str.toString();
    }

    private String type(Type type) throws ShaderGenerateException {
        StringBuilder qualifiers = new StringBuilder(24);
        if (type.constant) {
            qualifiers.append("const ");
        }
        switch (version) {
            case GLES_300:
                qualifiers.append(precision(type.precision)).append(' ');
                break;
        }
        return qualifiers + type(type.type);
    }

    private String identifier(Type type, String identifier)
            throws ShaderGenerateException {
        if (type.array.isPresent()) {
            return identifier + '[' + expression(type.array.get()) + ']';
        }
        return identifier;
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

    private String precision(Precision precision)
            throws ShaderGenerateException {
        switch (precision) {
            case lowp:
                return "lowp";
            case mediump:
                return "mediump";
            case highp:
                return "highp";
            default:
                throw new ShaderGenerateException(
                        "Unexpected precision: " + precision);
        }
    }

    private ArrayLiteralExpression property(PropertyArrayExpression expression)
            throws ShaderGenerateException {
        String source = property(expression.key);
        try {
            ScapesShaderParser parser = ShaderCompiler.parser(source);
            ArrayExpression expression2 =
                    ShaderCompiler.initializer(parser.initializerArrayList());
            if (expression2 instanceof ArrayLiteralExpression) {
                return (ArrayLiteralExpression) expression2;
            } else {
                throw new ShaderGenerateException(
                        "Property has to be a static array expression");
            }
        } catch (ParseCancellationException | ShaderCompileException e) {
            throw new ShaderGenerateException(e);
        }
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
        functions = shader.functions;
        ShaderFunction shaderVertex = shader.shaderVertex.get();
        ShaderFunction shaderFragment = shader.shaderFragment.get();
        Uniform[] uniforms = shader.uniforms();
        header(uniforms, shaderVertex.signature);
        println();
        header(shaderFragment.signature);
        println();
        header(shader.declarations);
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
        functions = shader.functions;
        ShaderFunction shaderFragment = shader.shaderFragment.get();
        ShaderSignature outputs = shader.outputs.get();
        Uniform[] uniforms = shader.uniforms();
        header(uniforms, shaderFragment.signature);
        println();
        header(outputs);
        println();
        header(shader.declarations);
        println();
        functions(functions);
        println();
        shader(shaderFragment);
        return output.toString();
    }

    private void header(Uniform[] uniforms, ShaderSignature input)
            throws ShaderGenerateException {
        switch (version) {
            case GL_330:
                println(0, "#version 330");
                break;
            case GLES_300:
                println(0, "#version 300 es");
                break;
        }
        println();
        for (Uniform uniform : uniforms) {
            if (uniform != null) {
                println(0, "uniform " + type(uniform.type) + ' ' +
                        identifier(uniform.type, uniform.name) +
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

    private void header(List<Statement> statements)
            throws ShaderGenerateException {
        for (Statement statement : statements) {
            statement(statement, 0);
        }
    }

    private void functions(List<Function> functions)
            throws ShaderGenerateException {
        for (Function function : functions) {
            FunctionSignature signature = function.signature;
            println(0, signature(signature));
            statement(function.compound, 0);
        }
    }

    private String signature(FunctionSignature signature)
            throws ShaderGenerateException {
        StringBuilder str = new StringBuilder(24);
        str.append(precision(signature.returnedPrecision)).append(' ');
        str.append(type(signature.returned)).append(' ');
        str.append(signature.name).append('(');
        if (signature.parameters.length > 0) {
            str.append(type(signature.parameters[0].type,
                    signature.parameters[0].name));
            for (int i = 1; i < signature.parameters.length; i++) {
                str.append(", ").append(type(signature.parameters[i].type,
                        signature.parameters[i].name));
            }
        }
        str.append(')');
        return str.toString();
    }

    private void shader(ShaderFunction function)
            throws ShaderGenerateException {
        println(0, "void main(void)");
        statement(function.compound, 0);
    }

    private void block(StatementBlock expression, int level)
            throws ShaderGenerateException {
        for (Statement statement : expression.statements) {
            statement(statement, level);
        }
    }

    private void statement(Statement statement, int level)
            throws ShaderGenerateException {
        if (statement instanceof CompoundStatement) {
            println(level, "{");
            block(((CompoundStatement) statement).block, level + 1);
            println(level, "}");
        } else if (statement instanceof IfStatement) {
            ifStatement((IfStatement) statement, level);
        } else if (statement instanceof LoopFixedStatement) {
            loopFixedStatement((LoopFixedStatement) statement, level);
        } else if (statement instanceof DeclarationStatement) {
            declarationStatement((DeclarationStatement) statement, level);
        } else if (statement instanceof ArrayDeclarationStatement) {
            arrayDeclarationStatement((ArrayDeclarationStatement) statement,
                    level);
        } else if (statement instanceof ArrayUnsizedDeclarationStatement) {
            arrayUnsizedDeclarationStatement(
                    (ArrayUnsizedDeclarationStatement) statement, level);
        } else if (statement instanceof ExpressionStatement) {
            println(level,
                    expression(((ExpressionStatement) statement).expression) +
                            ';');
        } else {
            throw new ShaderGenerateException(
                    "Unknown statement: " + statement.getClass());
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

    public enum Version {
        GL_330,
        GLES_300
    }
}
