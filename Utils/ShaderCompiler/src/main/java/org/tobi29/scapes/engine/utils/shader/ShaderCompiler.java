package org.tobi29.scapes.engine.utils.shader;

import java8.util.Optional;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class ShaderCompiler {
    private static final Uniform[] EMPTY_UNIFORM = {};
    private final List<Expression> declarations = new ArrayList<>();
    private final List<Function> functions = new ArrayList<>();
    private Optional<ShaderFunction> shaderVertex = Optional.empty(),
            shaderFragment = Optional.empty();
    private Optional<ShaderSignature> outputs = Optional.empty();
    private Uniform[] uniforms = EMPTY_UNIFORM;

    private static void parameters(
            ScapesShaderParser.ParameterListContext context,
            List<Parameter> parameters) throws ShaderCompileException {
        while (context != null) {
            Parameter parameter = parameter(context.parameterDeclaration());
            parameters.add(parameter);
            context = context.parameterList();
        }
    }

    private static Parameter parameter(
            ScapesShaderParser.ParameterDeclarationContext context)
            throws ShaderCompileException {
        Type type = type(context.declarator());
        String name = context.Identifier().getText();
        return new Parameter(type, name);
    }

    private static void parameters(
            ScapesShaderParser.ShaderParameterListContext context,
            List<ShaderParameter> parameters) throws ShaderCompileException {
        while (context != null) {
            ShaderParameter parameter =
                    parameter(context.shaderParameterDeclaration());
            parameters.add(parameter);
            context = context.shaderParameterList();
        }
    }

    private static ShaderParameter parameter(
            ScapesShaderParser.ShaderParameterDeclarationContext context)
            throws ShaderCompileException {
        Type type = type(context.declarator());
        TerminalNode idConstant = context.Constant();
        int id;
        if (idConstant == null) {
            id = -1;
        } else {
            id = Integer.parseInt(idConstant.getText());
        }
        String name = context.Identifier().getText();
        ScapesShaderParser.PropertyContext property = context.property();
        if (property == null) {
            return new ShaderParameter(type, id, name,
                    new Expression(ExpressionType.CONSTANT, "true"));
        }
        return new ShaderParameter(type, id, name,
                new Expression(ExpressionType.PROPERTY_CONSTANT,
                        property.Identifier().getText()));
    }

    private static Type type(ScapesShaderParser.DeclaratorContext context)
            throws ShaderCompileException {
        ScapesShaderParser.DeclaratorFieldContext field =
                context.declaratorField();
        if (field != null) {
            return type(field);
        }
        return type(context.declaratorArray());
    }

    private static Type type(ScapesShaderParser.DeclaratorFieldContext context)
            throws ShaderCompileException {
        boolean constant = false;
        for (ParseTree child : context.children) {
            if (child instanceof TerminalNode) {
                switch (child.getText()) {
                    case "const":
                        constant = true;
                        break;
                }
            }
        }
        return new Type(type(context.typeSpecifier()), constant);
    }

    private static Type type(ScapesShaderParser.DeclaratorArrayContext context)
            throws ShaderCompileException {
        boolean constant = false;
        for (ParseTree child : context.children) {
            if (child instanceof TerminalNode) {
                switch (child.getText()) {
                    case "const":
                        constant = true;
                        break;
                }
            }
        }
        return new Type(type(context.typeSpecifier()),
                size(context.integerSize()), constant);
    }

    private static Type type(
            ScapesShaderParser.DeclaratorArrayUnsizedContext context)
            throws ShaderCompileException {
        boolean constant = false;
        for (ParseTree child : context.children) {
            if (child instanceof TerminalNode) {
                switch (child.getText()) {
                    case "const":
                        constant = true;
                        break;
                }
            }
        }
        return new Type(type(context.typeSpecifier()), constant);
    }

    private static Types type(ScapesShaderParser.TypeSpecifierContext context)
            throws ShaderCompileException {
        try {
            return Types.valueOf(context.getText());
        } catch (IllegalArgumentException e) {
            throw new ShaderCompileException(e, context);
        }
    }

    public static Expression statement(
            ScapesShaderParser.StatementContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ExpressionStatementContext expression =
                context.expressionStatement();
        if (expression != null) {
            return expression(expression);
        }
        ScapesShaderParser.DeclarationContext declaration =
                context.declaration();
        if (declaration != null) {
            return declaration(declaration);
        }
        ScapesShaderParser.SelectionStatementContext selection =
                context.selectionStatement();
        if (selection != null) {
            ScapesShaderParser.IfStatementContext ifStatement =
                    selection.ifStatement();
            ScapesShaderParser.ElseStatementContext elseStatement =
                    selection.elseStatement();
            if (elseStatement == null) {
                return new Expression(ExpressionType.IF,
                        expression(ifStatement),
                        statement(selection.statement()));
            }
            return new Expression(ExpressionType.IF_ELSE,
                    expression(ifStatement), statement(selection.statement()),
                    statement(elseStatement.statement()));
        }
        ScapesShaderParser.RangeLoopStatementContext rangeLoop =
                context.rangeLoopStatement();
        if (rangeLoop != null) {
            String name = rangeLoop.Identifier().getText();
            Expression min = size(rangeLoop.integerSize(0));
            Expression max = size(rangeLoop.integerSize(1));
            Expression statement = statement(rangeLoop.statement());
            return new Expression(ExpressionType.LOOP_RANGE, name, min, max,
                    statement);
        }
        return compound(context.compoundStatement().blockItemList());
    }

    public static Expression declaration(
            ScapesShaderParser.DeclarationContext context)
            throws ShaderCompileException {
        ScapesShaderParser.DeclarationFieldContext field =
                context.declarationField();
        if (field != null) {
            return declaration(field);
        }
        ScapesShaderParser.DeclarationArrayContext array =
                context.declarationArray();
        if (array != null) {
            return declaration(array);
        }
        throw new ShaderCompileException("No declaration", context);
    }

    public static Expression declaration(
            ScapesShaderParser.DeclarationFieldContext context)
            throws ShaderCompileException {
        Type type = type(context.declaratorField());
        return declaration(type, context.initDeclaratorFieldList());
    }

    public static Expression declaration(Type type,
            ScapesShaderParser.InitDeclaratorFieldListContext context)
            throws ShaderCompileException {
        List<Expression> expressions = new ArrayList<>();
        while (context != null) {
            ScapesShaderParser.InitDeclaratorFieldContext declarator =
                    context.initDeclaratorField();
            ScapesShaderParser.InitializerFieldContext initializer =
                    declarator.initializerField();
            String name = declarator.Identifier().getText();
            Expression expression;
            if (initializer == null) {
                expression = new Expression(ExpressionType.IDENTIFIER, name);
            } else {
                expression = new Expression(ExpressionType.ASSIGN,
                        new Expression(ExpressionType.IDENTIFIER, name),
                        expression(initializer.assignmentExpression()));
            }
            expressions.add(new Expression(ExpressionType.INITIALIZE, type,
                    expression));
            context = context.initDeclaratorFieldList();
        }
        return new Expression(ExpressionType.BLOCK,
                expressions.toArray(new Expression[expressions.size()]));
    }

    public static Expression declaration(
            ScapesShaderParser.DeclarationArrayContext context)
            throws ShaderCompileException {
        ScapesShaderParser.InitDeclaratorArrayListContext list =
                context.initDeclaratorArrayList();
        if (list != null) {
            ScapesShaderParser.DeclaratorArrayContext declarator =
                    context.declaratorArray();
            Type type = type(declarator);
            return declaration(type, size(declarator.integerSize()), list);
        }
        ScapesShaderParser.DeclaratorArrayUnsizedContext declarator =
                context.declaratorArrayUnsized();
        if (declarator != null) {
            Type type = type(declarator);
            ScapesShaderParser.InitializerArrayListContext initializer =
                    context.initializerArrayList();
            return new Expression(ExpressionType.INITIALIZE_ARRAY, type,
                    new Expression(ExpressionType.IDENTIFIER,
                            context.Identifier().getText()),
                    initializer(initializer));
        }
        throw new ShaderCompileException("No declaration", context);
    }

    public static Expression declaration(Type type, Expression length,
            ScapesShaderParser.InitDeclaratorArrayListContext context)
            throws ShaderCompileException {
        List<Expression> expressions = new ArrayList<>();
        while (context != null) {
            String name = context.Identifier().getText();
            expressions
                    .add(new Expression(ExpressionType.INITIALIZE_ARRAY, type,
                            new Expression(ExpressionType.IDENTIFIER, name),
                            length));
            context = context.initDeclaratorArrayList();
        }
        return new Expression(ExpressionType.BLOCK,
                expressions.toArray(new Expression[expressions.size()]));
    }

    public static Expression initializer(
            ScapesShaderParser.InitializerArrayListContext context)
            throws ShaderCompileException {
        List<Expression> expressions = new ArrayList<>();
        while (context != null) {
            expressions.add(expression(context.assignmentExpression()));
            context = context.initializerArrayList();
        }
        if (expressions.size() == 1 &&
                expressions.get(0).type == ExpressionType.PROPERTY_CONSTANT) {
            return new Expression(ExpressionType.PROPERTY_ARRAY_EXPRESSION,
                    expressions.get(0).value);
        }
        return new Expression(ExpressionType.ARRAY_EXPRESSION,
                expressions.toArray(new Expression[expressions.size()]));
    }

    public static Expression expression(
            ScapesShaderParser.IfStatementContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ExpressionContext expression = context.expression();
        if (expression != null) {
            return expression(expression);
        }
        throw new ShaderCompileException("No expression found", context);
    }

    public static Expression expression(
            ScapesShaderParser.ExpressionStatementContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ExpressionContext expression = context.expression();
        if (expression == null) {
            return new Expression(ExpressionType.VOID);
        }
        return expression(expression);
    }

    public static Expression compound(
            ScapesShaderParser.BlockItemListContext context)
            throws ShaderCompileException {
        List<Expression> expressions = new ArrayList<>();
        while (context != null) {
            expressions.add(statement(context.statement()));
            context = context.blockItemList();
        }
        return new Expression(ExpressionType.COMPOUND,
                expressions.toArray(new Expression[expressions.size()]));
    }

    public static Expression expression(
            ScapesShaderParser.AssignmentExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ConditionalExpressionContext condition =
                context.conditionalExpression();
        if (condition == null) {
            ExpressionType type;
            switch (context.assignmentOperator().getText()) {
                case "=":
                    type = ExpressionType.ASSIGN;
                    break;
                case "*=":
                    type = ExpressionType.ASSIGN_MULTIPLY;
                    break;
                case "/=":
                    type = ExpressionType.ASSIGN_DIVIDE;
                    break;
                case "%=":
                    type = ExpressionType.ASSIGN_MODULUS;
                    break;
                case "+=":
                    type = ExpressionType.ASSIGN_PLUS;
                    break;
                case "-=":
                    type = ExpressionType.ASSIGN_MINUS;
                    break;
                case "<<=":
                    type = ExpressionType.ASSIGN_SHIFT_LEFT;
                    break;
                case ">>=":
                    type = ExpressionType.ASSIGN_SHIFT_RIGHT;
                    break;
                case "&=":
                    type = ExpressionType.ASSIGN_AND;
                    break;
                case "|=":
                    type = ExpressionType.ASSIGN_INCLUSIVE_OR;
                    break;
                case "^=":
                    type = ExpressionType.ASSIGN_EXCLUSIVE_OR;
                    break;
                default:
                    throw new ShaderCompileException(
                            "Invalid assignment operator" +
                                    context.assignmentOperator().getText(),
                            context.assignmentOperator());
            }
            return new Expression(type, expression(context.unaryExpression()),
                    expression(context.assignmentExpression()));
        }
        return expression(condition);
    }

    public static Expression expression(
            ScapesShaderParser.ConditionalExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ConditionalExpressionContext condition =
                context.conditionalExpression();
        if (condition == null) {
            return expression(context.logicalOrExpression());
        }
        return new Expression(ExpressionType.TERNARY,
                expression(context.logicalOrExpression()),
                expression(context.expression()),
                expression(context.conditionalExpression()));
    }

    public static Expression expression(
            ScapesShaderParser.LogicalOrExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.LogicalAndExpressionContext expression =
                context.logicalAndExpression();
        ScapesShaderParser.LogicalOrExpressionContext next =
                context.logicalOrExpression();
        if (next == null) {
            return expression(expression);
        }
        return new Expression(ExpressionType.CONDITION_LOGICAL_OR,
                expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.LogicalAndExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.InclusiveOrExpressionContext expression =
                context.inclusiveOrExpression();
        ScapesShaderParser.LogicalAndExpressionContext next =
                context.logicalAndExpression();
        if (next == null) {
            return expression(expression);
        }
        return new Expression(ExpressionType.CONDITION_LOGICAL_AND,
                expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.InclusiveOrExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ExclusiveOrExpressionContext expression =
                context.exclusiveOrExpression();
        ScapesShaderParser.InclusiveOrExpressionContext next =
                context.inclusiveOrExpression();
        if (next == null) {
            return expression(expression);
        }
        return new Expression(ExpressionType.CONDITION_INCLUSIVE_OR,
                expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.ExclusiveOrExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.AndExpressionContext expression =
                context.andExpression();
        ScapesShaderParser.ExclusiveOrExpressionContext next =
                context.exclusiveOrExpression();
        if (next == null) {
            return expression(expression);
        }
        return new Expression(ExpressionType.CONDITION_EXCLUSIVE_OR,
                expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.AndExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.EqualityExpressionContext expression =
                context.equalityExpression();
        ScapesShaderParser.AndExpressionContext next = context.andExpression();
        if (next == null) {
            return expression(expression);
        }
        return new Expression(ExpressionType.CONDITION_AND, expression(next),
                expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.EqualityExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.RelationalExpressionContext expression =
                context.relationalExpression();
        ScapesShaderParser.EqualityExpressionContext next =
                context.equalityExpression();
        if (next == null) {
            return expression(expression);
        }
        ExpressionType type;
        switch (context.children.get(1).getText()) {
            case "==":
                type = ExpressionType.CONDITION_EQUALS;
                break;
            case "!=":
                type = ExpressionType.CONDITION_NOT_EQUALS;
                break;
            default:
                throw new ShaderCompileException(
                        "Invalid conditional operator: " +
                                context.children.get(1).getText(), context);
        }
        return new Expression(type, expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.RelationalExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ShiftExpressionContext expression =
                context.shiftExpression();
        ScapesShaderParser.RelationalExpressionContext next =
                context.relationalExpression();
        if (next == null) {
            return expression(expression);
        }
        ExpressionType type;
        switch (context.children.get(1).getText()) {
            case "<":
                type = ExpressionType.CONDITION_LESS;
                break;
            case ">":
                type = ExpressionType.CONDITION_GREATER;
                break;
            case "<=":
                type = ExpressionType.CONDITION_LESS_EQUAL;
                break;
            case ">=":
                type = ExpressionType.CONDITION_GREATER_EQUAL;
                break;
            default:
                throw new ShaderCompileException(
                        "Invalid conditional operator: " +
                                context.children.get(1).getText(), context);
        }
        return new Expression(type, expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.ShiftExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.AdditiveExpressionContext expression =
                context.additiveExpression();
        ScapesShaderParser.ShiftExpressionContext next =
                context.shiftExpression();
        if (next == null) {
            return expression(expression);
        }
        ExpressionType type;
        switch (context.children.get(1).getText()) {
            case "<<":
                type = ExpressionType.SHIFT_LEFT;
                break;
            case ">>":
                type = ExpressionType.SHIFT_RIGHT;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(1).getText(), context);
        }
        return new Expression(type, expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.AdditiveExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.MultiplicativeExpressionContext expression =
                context.multiplicativeExpression();
        ScapesShaderParser.AdditiveExpressionContext next =
                context.additiveExpression();
        if (next == null) {
            return expression(expression);
        }
        ExpressionType type;
        switch (context.children.get(1).getText()) {
            case "+":
                type = ExpressionType.PLUS;
                break;
            case "-":
                type = ExpressionType.MINUS;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(1).getText(), context);
        }
        return new Expression(type, expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.MultiplicativeExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.CastExpressionContext expression =
                context.castExpression();
        ScapesShaderParser.MultiplicativeExpressionContext next =
                context.multiplicativeExpression();
        if (next == null) {
            return expression(expression);
        }
        ExpressionType type;
        switch (context.children.get(1).getText()) {
            case "*":
                type = ExpressionType.MULTIPLY;
                break;
            case "/":
                type = ExpressionType.DIVIDE;
                break;
            case "%":
                type = ExpressionType.MODULUS;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(1).getText(), context);
        }
        return new Expression(type, expression(next), expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.CastExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.UnaryExpressionContext expression =
                context.unaryExpression();
        ScapesShaderParser.CastExpressionContext next =
                context.castExpression();
        if (next == null) {
            return expression(expression);
        }
        return new Expression(ExpressionType.CAST,
                type(context.typeSpecifier()), expression(next));
    }

    public static Expression expression(
            ScapesShaderParser.UnaryExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.UnaryExpressionContext next =
                context.unaryExpression();
        if (next == null) {
            ScapesShaderParser.CastExpressionContext expression =
                    context.castExpression();
            if (expression == null) {
                return expression(context.postfixExpression());
            }
            ExpressionType type;
            switch (context.unaryOperator().getText()) {
                case "+":
                    type = ExpressionType.POSITIVE;
                    break;
                case "-":
                    type = ExpressionType.NEGATIVE;
                    break;
                case "~":
                    type = ExpressionType.BIT_NOT;
                    break;
                case "!":
                    type = ExpressionType.NOT;
                    break;
                default:
                    throw new ShaderCompileException("Invalid operator: " +
                            context.children.get(0).getText(), context);
            }
            return new Expression(type, expression(expression));
        }
        ExpressionType type;
        switch (context.children.get(0).getText()) {
            case "++":
                type = ExpressionType.INCREMENT_GET;
                break;
            case "--":
                type = ExpressionType.DECREMENT_GET;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(0).getText(), context);
        }
        return new Expression(type, expression(next));
    }

    public static Expression expression(
            ScapesShaderParser.PostfixExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.PostfixExpressionContext next =
                context.postfixExpression();
        if (next == null) {
            return expression(context.primaryExpression());
        }
        ScapesShaderParser.ExpressionContext array = context.expression();
        if (array != null) {
            return array(next, array);
        }
        ScapesShaderParser.ArgumentExpressionListContext arguments =
                context.argumentExpressionList();
        if (arguments != null) {
            return function(next, arguments);
        }
        TerminalNode field = context.Identifier();
        if (field != null) {
            return field(next, field);
        }
        ExpressionType type;
        switch (context.children.get(1).getText()) {
            case "++":
                type = ExpressionType.GET_INCREMENT;
                break;
            case "--":
                type = ExpressionType.GET_DECREMENT;
                break;
            case "(":
                type = ExpressionType.FUNCTION;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(1).getText(), context);
        }
        return new Expression(type, expression(next));
    }

    public static Expression array(
            ScapesShaderParser.PostfixExpressionContext array,
            ScapesShaderParser.ExpressionContext index)
            throws ShaderCompileException {
        return new Expression(ExpressionType.ARRAY, expression(array),
                expression(index));
    }

    public static Expression function(
            ScapesShaderParser.PostfixExpressionContext function,
            ScapesShaderParser.ArgumentExpressionListContext arguments)
            throws ShaderCompileException {
        List<Expression> expressions = new ArrayList<>();
        expressions.add(expression(function));
        while (arguments != null) {
            expressions.add(expression(arguments.assignmentExpression()));
            arguments = arguments.argumentExpressionList();
        }
        return new Expression(ExpressionType.FUNCTION,
                expressions.toArray(new Expression[expressions.size()]));
    }

    public static Expression field(
            ScapesShaderParser.PostfixExpressionContext parent,
            TerminalNode name) throws ShaderCompileException {
        return new Expression(ExpressionType.IDENTIFIER, name.getText(),
                expression(parent));
    }

    public static Expression expression(
            ScapesShaderParser.PrimaryExpressionContext context)
            throws ShaderCompileException {
        TerminalNode identifier = context.Identifier();
        if (identifier != null) {
            return new Expression(ExpressionType.IDENTIFIER,
                    identifier.getText());
        }
        TerminalNode constant = context.Constant();
        if (constant != null) {
            return new Expression(ExpressionType.CONSTANT, constant.getText());
        }
        ScapesShaderParser.PropertyContext property = context.property();
        if (property != null) {
            return new Expression(ExpressionType.PROPERTY_CONSTANT,
                    property.Identifier().getText());
        }
        return new Expression(ExpressionType.EXPRESSION,
                expression(context.expression()));
    }

    public static Expression expression(
            ScapesShaderParser.ExpressionContext context)
            throws ShaderCompileException {
        return expression(context.assignmentExpression());
    }

    public static Expression size(ScapesShaderParser.IntegerSizeContext context)
            throws ShaderCompileException {
        TerminalNode constant = context.Constant();
        if (constant != null) {
            return new Expression(ExpressionType.CONSTANT, constant.getText());
        }
        ScapesShaderParser.PropertyContext property = context.property();
        if (property != null) {
            return new Expression(ExpressionType.PROPERTY_CONSTANT,
                    property.Identifier().getText());
        }
        throw new ShaderCompileException("No constant found", context);
    }

    public static ScapesShaderParser parser(String source) {
        ANTLRInputStream streamIn = new ANTLRInputStream(source);
        ScapesShaderLexer lexer = new ScapesShaderLexer(streamIn);
        ScapesShaderParser parser =
                new ScapesShaderParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                    Object offendingSymbol, int line, int charPositionInLine,
                    String msg, RecognitionException e) {
                throw new ParseCancellationException(
                        "line " + line + ':' + charPositionInLine + ' ' +
                                msg);
            }
        });
        return parser;
    }

    public void compile(String source) throws ShaderCompileException {
        declarations.clear();
        shaderVertex = Optional.empty();
        shaderFragment = Optional.empty();
        uniforms = EMPTY_UNIFORM;
        try {
            ScapesShaderParser parser = parser(source);
            ScapesShaderParser.TranslationUnitContext program =
                    parser.compilationUnit().translationUnit();
            while (program != null) {
                externalDeclaration(program.externalDeclaration());
                program = program.translationUnit();
            }
        } catch (ParseCancellationException e) {
            throw new ShaderCompileException(e);
        }
    }

    private void externalDeclaration(
            ScapesShaderParser.ExternalDeclarationContext context)
            throws ShaderCompileException {
        ScapesShaderParser.UniformDeclarationContext uniform =
                context.uniformDeclaration();
        if (uniform != null) {
            ScapesShaderParser.DeclaratorContext declarator =
                    uniform.declarator();
            ScapesShaderParser.DeclaratorFieldContext field =
                    declarator.declaratorField();
            if (field != null) {
                int id = Integer.parseInt(uniform.Constant().getText());
                if (uniforms.length <= id) {
                    Uniform[] newUniforms = new Uniform[id + 1];
                    System.arraycopy(uniforms, 0, newUniforms, 0,
                            uniforms.length);
                    uniforms = newUniforms;
                }
                uniforms[id] = new Uniform(type(field.typeSpecifier()), id,
                        uniform.Identifier().getText());
            }
            ScapesShaderParser.DeclaratorArrayContext array =
                    declarator.declaratorArray();
            if (array != null) {
                throw new UnsupportedOperationException("NYI");
            }
            return;
        }
        ScapesShaderParser.DeclarationContext declaration =
                context.declaration();
        if (declaration != null) {
            declarations.add(declaration(declaration));
        }
        ScapesShaderParser.ShaderDefinitionContext shader =
                context.shaderDefinition();
        if (shader != null) {
            ScapesShaderParser.ShaderSignatureContext signature =
                    shader.shaderSignature();
            String name = signature.Identifier().getText();
            List<ShaderParameter> parameters = new ArrayList<>();
            parameters(signature.shaderParameterList(), parameters);
            Types returned = Types.Void;
            ShaderSignature shaderSignature =
                    new ShaderSignature(name, returned, parameters
                            .toArray(new ShaderParameter[parameters.size()]));
            Expression compound =
                    compound(shader.compoundStatement().blockItemList());
            ShaderFunction function =
                    new ShaderFunction(shaderSignature, compound);
            switch (function.signature.name) {
                case "vertex":
                    shaderVertex = Optional.of(function);
                    break;
                case "fragment":
                    shaderFragment = Optional.of(function);
                    break;
                default:
                    throw new ShaderCompileException(
                            "Invalid shader name: " + function.signature.name,
                            shader);
            }
            return;
        }
        ScapesShaderParser.OutputsDefinitionContext outputs =
                context.outputsDefinition();
        if (outputs != null) {
            List<ShaderParameter> parameters = new ArrayList<>();
            parameters(outputs.shaderParameterList(), parameters);
            Types returned = Types.Void;
            ShaderSignature outputsSignature =
                    new ShaderSignature("outputs", returned, parameters
                            .toArray(new ShaderParameter[parameters.size()]));
            this.outputs = Optional.of(outputsSignature);
            return;
        }
        ScapesShaderParser.FunctionDefinitionContext function =
                context.functionDefinition();
        if (function != null) {
            ScapesShaderParser.FunctionSignatureContext signature =
                    function.functionSignature();
            String name = signature.Identifier().getText();
            List<Parameter> parameters = new ArrayList<>();
            parameters(signature.parameterList(), parameters);
            Types returned = type(signature.typeSpecifier());
            FunctionSignature shaderSignature =
                    new FunctionSignature(name, returned, parameters
                            .toArray(new Parameter[parameters.size()]));
            Expression compound =
                    compound(function.compoundStatement().blockItemList());
            functions.add(new Function(shaderSignature, compound));
            return;
        }
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
