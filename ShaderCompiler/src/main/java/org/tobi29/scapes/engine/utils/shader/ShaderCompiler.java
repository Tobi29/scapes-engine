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
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.tobi29.scapes.engine.utils.shader.expression.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShaderCompiler {
    private static final Uniform[] EMPTY_UNIFORM = {};
    private final List<Statement> declarations = new ArrayList<>();
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
        TerminalNode idConstant = context.IntegerLiteral();
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
                    new BooleanExpression(true));
        }
        return new ShaderParameter(type, id, name,
                new PropertyExpression(property.Identifier().getText()));
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
        ScapesShaderParser.PrecisionSpecifierContext precisionSpecifier =
                context.precisionSpecifier();
        Precision precision;
        if (precisionSpecifier == null) {
            precision = Precision.mediump;
        } else {
            precision = precision(precisionSpecifier);
        }
        return new Type(type(context.typeSpecifier()), constant, precision);
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
        ScapesShaderParser.PrecisionSpecifierContext precisionSpecifier =
                context.precisionSpecifier();
        Precision precision;
        if (precisionSpecifier == null) {
            precision = Precision.mediump;
        } else {
            precision = precision(precisionSpecifier);
        }
        return new Type(type(context.typeSpecifier()),
                integer(context.integerConstant()), constant, precision);
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
        ScapesShaderParser.PrecisionSpecifierContext precisionSpecifier =
                context.precisionSpecifier();
        Precision precision;
        if (precisionSpecifier == null) {
            precision = Precision.mediump;
        } else {
            precision = precision(precisionSpecifier);
        }
        return new Type(type(context.typeSpecifier()), constant, precision);
    }

    private static Types type(ScapesShaderParser.TypeSpecifierContext context)
            throws ShaderCompileException {
        try {
            return Types.valueOf(context.getText());
        } catch (IllegalArgumentException e) {
            throw new ShaderCompileException(e, context);
        }
    }

    public static Statement statement(
            ScapesShaderParser.StatementContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ExpressionStatementContext expression =
                context.expressionStatement();
        if (expression != null) {
            return new ExpressionStatement(expression(expression));
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
                return new IfStatement(expression(ifStatement),
                        statement(selection.statement()));
            }
            return new IfStatement(expression(ifStatement),
                    statement(selection.statement()),
                    statement(elseStatement.statement()));
        }
        ScapesShaderParser.RangeLoopStatementContext rangeLoop =
                context.rangeLoopStatement();
        if (rangeLoop != null) {
            String name = rangeLoop.Identifier().getText();
            IntegerExpression start = integer(rangeLoop.integerConstant(0));
            IntegerExpression end = integer(rangeLoop.integerConstant(1));
            Statement statement = statement(rangeLoop.statement());
            return new LoopFixedStatement(name, start, end, statement);
        }
        return compound(context.compoundStatement().blockItemList());
    }

    public static Statement declaration(
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

    public static Statement declaration(
            ScapesShaderParser.DeclarationFieldContext context)
            throws ShaderCompileException {
        Type type = type(context.declaratorField());
        return declaration(type, context.initDeclaratorFieldList());
    }

    public static Statement declaration(Type type,
            ScapesShaderParser.InitDeclaratorFieldListContext context)
            throws ShaderCompileException {
        List<Declaration> expressions = new ArrayList<>();
        while (context != null) {
            ScapesShaderParser.InitDeclaratorFieldContext declarator =
                    context.initDeclaratorField();
            ScapesShaderParser.InitializerFieldContext initializer =
                    declarator.initializerField();
            String name = declarator.Identifier().getText();
            if (initializer == null) {
                expressions.add(new Declaration(name));
            } else {
                expressions.add(new Declaration(name,
                        expression(initializer.assignmentExpression())));
            }
            context = context.initDeclaratorFieldList();
        }
        return new DeclarationStatement(type, expressions);
    }

    public static Statement declaration(
            ScapesShaderParser.DeclarationArrayContext context)
            throws ShaderCompileException {
        ScapesShaderParser.InitDeclaratorArrayListContext list =
                context.initDeclaratorArrayList();
        if (list != null) {
            ScapesShaderParser.DeclaratorArrayContext declarator =
                    context.declaratorArray();
            Type type = type(declarator);
            Expression length = integer(declarator.integerConstant());
            return declaration(type, length, list);
        }
        Type type = type(context.declaratorArrayUnsized());
        ScapesShaderParser.InitializerArrayContext initializer =
                context.initializerArray();
        return new ArrayUnsizedDeclarationStatement(type,
                context.Identifier().getText(), initializer(initializer));
    }

    public static Statement declaration(Type type, Expression length,
            ScapesShaderParser.InitDeclaratorArrayListContext context) {
        List<ArrayDeclaration> declarations = new ArrayList<>();
        while (context != null) {
            String name = context.Identifier().getText();
            declarations.add(new ArrayDeclaration(name));
            context = context.initDeclaratorArrayList();
        }
        return new ArrayDeclarationStatement(type, length, declarations);
    }

    public static ArrayExpression initializer(
            ScapesShaderParser.InitializerArrayContext context)
            throws ShaderCompileException {
        ScapesShaderParser.InitializerArrayListContext list =
                context.initializerArrayList();
        if (list != null) {
            return initializer(list);
        }
        return new PropertyArrayExpression(
                context.property().Identifier().getText());
    }

    public static ArrayExpression initializer(
            ScapesShaderParser.InitializerArrayListContext context)
            throws ShaderCompileException {
        List<Expression> expressions = new ArrayList<>();
        while (context != null) {
            expressions.add(expression(context.assignmentExpression()));
            context = context.initializerArrayList();
        }
        return new ArrayLiteralExpression(expressions);
    }

    public static Precision precision(
            ScapesShaderParser.PrecisionSpecifierContext context)
            throws ShaderCompileException {
        try {
            return Precision.valueOf(context.getText());
        } catch (IllegalArgumentException e) {
            throw new ShaderCompileException(e, context);
        }
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
            return new VoidExpression();
        }
        return expression(expression);
    }

    public static CompoundStatement compound(
            ScapesShaderParser.BlockItemListContext context)
            throws ShaderCompileException {
        return new CompoundStatement(block(context));
    }

    public static StatementBlock block(
            ScapesShaderParser.BlockItemListContext context)
            throws ShaderCompileException {
        List<Statement> expressions = new ArrayList<>();
        while (context != null) {
            expressions.add(statement(context.statement()));
            context = context.blockItemList();
        }
        return new StatementBlock(expressions);
    }

    public static Expression expression(
            ScapesShaderParser.AssignmentExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.ConditionalExpressionContext condition =
                context.conditionalExpression();
        if (condition == null) {
            AssignmentType type;
            switch (context.assignmentOperator().getText()) {
                case "=":
                    type = AssignmentType.ASSIGN;
                    break;
                case "*=":
                    type = AssignmentType.ASSIGN_MULTIPLY;
                    break;
                case "/=":
                    type = AssignmentType.ASSIGN_DIVIDE;
                    break;
                case "%=":
                    type = AssignmentType.ASSIGN_MODULUS;
                    break;
                case "+=":
                    type = AssignmentType.ASSIGN_PLUS;
                    break;
                case "-=":
                    type = AssignmentType.ASSIGN_MINUS;
                    break;
                case "<<=":
                    type = AssignmentType.ASSIGN_SHIFT_LEFT;
                    break;
                case ">>=":
                    type = AssignmentType.ASSIGN_SHIFT_RIGHT;
                    break;
                case "&=":
                    type = AssignmentType.ASSIGN_AND;
                    break;
                case "|=":
                    type = AssignmentType.ASSIGN_INCLUSIVE_OR;
                    break;
                case "^=":
                    type = AssignmentType.ASSIGN_EXCLUSIVE_OR;
                    break;
                default:
                    throw new ShaderCompileException(
                            "Invalid assignment operator" +
                                    context.assignmentOperator().getText(),
                            context.assignmentOperator());
            }
            return new AssignmentExpression(type,
                    expression(context.unaryExpression()),
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
        return new TernaryExpression(expression(context.logicalOrExpression()),
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
        return new ConditionExpression(ConditionType.CONDITION_LOGICAL_OR,
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
        return new ConditionExpression(ConditionType.CONDITION_LOGICAL_AND,
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
        return new ConditionExpression(ConditionType.CONDITION_INCLUSIVE_OR,
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
        return new ConditionExpression(ConditionType.CONDITION_EXCLUSIVE_OR,
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
        return new ConditionExpression(ConditionType.CONDITION_AND,
                expression(next), expression(expression));
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
        ConditionType type;
        switch (context.children.get(1).getText()) {
            case "==":
                type = ConditionType.CONDITION_EQUALS;
                break;
            case "!=":
                type = ConditionType.CONDITION_NOT_EQUALS;
                break;
            default:
                throw new ShaderCompileException(
                        "Invalid conditional operator: " +
                                context.children.get(1).getText(), context);
        }
        return new ConditionExpression(type, expression(next),
                expression(expression));
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
        ConditionType type;
        switch (context.children.get(1).getText()) {
            case "<":
                type = ConditionType.CONDITION_LESS;
                break;
            case ">":
                type = ConditionType.CONDITION_GREATER;
                break;
            case "<=":
                type = ConditionType.CONDITION_LESS_EQUAL;
                break;
            case ">=":
                type = ConditionType.CONDITION_GREATER_EQUAL;
                break;
            default:
                throw new ShaderCompileException(
                        "Invalid conditional operator: " +
                                context.children.get(1).getText(), context);
        }
        return new ConditionExpression(type, expression(next),
                expression(expression));
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
        OperationType type;
        switch (context.children.get(1).getText()) {
            case "<<":
                type = OperationType.SHIFT_LEFT;
                break;
            case ">>":
                type = OperationType.SHIFT_RIGHT;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(1).getText(), context);
        }
        return new OperationExpression(type, expression(next),
                expression(expression));
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
        OperationType type;
        switch (context.children.get(1).getText()) {
            case "+":
                type = OperationType.PLUS;
                break;
            case "-":
                type = OperationType.MINUS;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(1).getText(), context);
        }
        return new OperationExpression(type, expression(next),
                expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.MultiplicativeExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.UnaryExpressionContext expression =
                context.unaryExpression();
        ScapesShaderParser.MultiplicativeExpressionContext next =
                context.multiplicativeExpression();
        if (next == null) {
            return expression(expression);
        }
        OperationType type;
        switch (context.children.get(1).getText()) {
            case "*":
                type = OperationType.MULTIPLY;
                break;
            case "/":
                type = OperationType.DIVIDE;
                break;
            case "%":
                type = OperationType.MODULUS;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(1).getText(), context);
        }
        return new OperationExpression(type, expression(next),
                expression(expression));
    }

    public static Expression expression(
            ScapesShaderParser.UnaryExpressionContext context)
            throws ShaderCompileException {
        ScapesShaderParser.UnaryExpressionContext next =
                context.unaryExpression();
        if (next == null) {
            return expression(context.postfixExpression());
        }
        ScapesShaderParser.UnaryOperatorContext unaryOperator =
                context.unaryOperator();
        if (unaryOperator != null) {
            UnaryType type;
            switch (unaryOperator.getText()) {
                case "+":
                    type = UnaryType.POSITIVE;
                    break;
                case "-":
                    type = UnaryType.NEGATIVE;
                    break;
                case "~":
                    type = UnaryType.BIT_NOT;
                    break;
                case "!":
                    type = UnaryType.NOT;
                    break;
                default:
                    throw new ShaderCompileException("Invalid operator: " +
                            context.children.get(0).getText(), context);
            }
            return new UnaryExpression(type, expression(next));
        }
        UnaryType type;
        switch (context.children.get(0).getText()) {
            case "++":
                type = UnaryType.INCREMENT_GET;
                break;
            case "--":
                type = UnaryType.DECREMENT_GET;
                break;
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(0).getText(), context);
        }
        return new UnaryExpression(type, expression(next));
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
        UnaryType type;
        switch (context.children.get(1).getText()) {
            case "++":
                type = UnaryType.GET_INCREMENT;
                break;
            case "--":
                type = UnaryType.GET_DECREMENT;
                break;
            case "(":
                return function(next, Collections.emptyList());
            default:
                throw new ShaderCompileException("Invalid operator: " +
                        context.children.get(1).getText(), context);
        }
        return new UnaryExpression(type, expression(next));
    }

    public static Expression array(
            ScapesShaderParser.PostfixExpressionContext array,
            ScapesShaderParser.ExpressionContext index)
            throws ShaderCompileException {
        return new ArrayAccessExpression(expression(array), expression(index));
    }

    public static Expression function(
            ScapesShaderParser.PostfixExpressionContext function,
            ScapesShaderParser.ArgumentExpressionListContext arguments)
            throws ShaderCompileException {
        List<Expression> expressions = new ArrayList<>();
        while (arguments != null) {
            expressions.add(expression(arguments.assignmentExpression()));
            arguments = arguments.argumentExpressionList();
        }
        return function(function, expressions);
    }

    public static Expression function(
            ScapesShaderParser.PostfixExpressionContext function,
            List<Expression> arguments) throws ShaderCompileException {
        Expression variable = expression(function);
        if (!(variable instanceof VariableExpression)) {
            throw new ShaderCompileException("Function call on member variable",
                    function);
        }
        return new FunctionExpression(((IdentifierExpression) variable).name,
                arguments);
    }

    public static Expression field(
            ScapesShaderParser.PostfixExpressionContext parent,
            TerminalNode name) throws ShaderCompileException {
        return new MemberExpression(name.getText(), expression(parent));
    }

    public static Expression expression(
            ScapesShaderParser.PrimaryExpressionContext context)
            throws ShaderCompileException {
        TerminalNode identifier = context.Identifier();
        if (identifier != null) {
            return new VariableExpression(identifier.getText());
        }
        ScapesShaderParser.ConstantContext constant = context.constant();
        if (constant != null) {
            return constant(constant);
        }
        ScapesShaderParser.PropertyContext property = context.property();
        if (property != null) {
            return new PropertyExpression(property.Identifier().getText());
        }
        return expression(context.expression());
    }

    public static Expression expression(
            ScapesShaderParser.ExpressionContext context)
            throws ShaderCompileException {
        return expression(context.assignmentExpression());
    }

    public static IntegerExpression integer(
            ScapesShaderParser.IntegerConstantContext context)
            throws ShaderCompileException {
        TerminalNode literal = context.IntegerLiteral();
        if (literal != null) {
            return new IntegerLiteralExpression(
                    new BigInteger(literal.getText()));
        }
        ScapesShaderParser.PropertyContext property = context.property();
        if (property != null) {
            return new IntegerPropertyExpression(
                    property.Identifier().getText());
        }
        throw new ShaderCompileException("No constant found", context);
    }

    public static Expression floating(
            ScapesShaderParser.FloatingConstantContext context)
            throws ShaderCompileException {
        TerminalNode literal = context.FloatingLiteral();
        if (literal != null) {
            return new FloatingExpression(new BigDecimal(literal.getText()));
        }
        ScapesShaderParser.PropertyContext property = context.property();
        if (property != null) {
            return new PropertyExpression(property.Identifier().getText());
        }
        throw new ShaderCompileException("No constant found", context);
    }

    public static Expression character(
            ScapesShaderParser.CharacterConstantContext context)
            throws ShaderCompileException {
        // TODO: Implement
        throw new ShaderCompileException("NYI", context);
    }

    public static Expression constant(
            ScapesShaderParser.ConstantContext context)
            throws ShaderCompileException {
        ScapesShaderParser.IntegerConstantContext integer =
                context.integerConstant();
        if (integer != null) {
            return integer(integer);
        }
        ScapesShaderParser.FloatingConstantContext floating =
                context.floatingConstant();
        if (floating != null) {
            return floating(floating);
        }
        ScapesShaderParser.CharacterConstantContext character =
                context.characterConstant();
        if (character != null) {
            return character(character);
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

    public CompiledShader compile(String source) throws ShaderCompileException {
        declarations.clear();
        functions.clear();
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
        List<Statement> declarations =
                new ArrayList<>(this.declarations.size());
        declarations.addAll(this.declarations);
        List<Function> functions = new ArrayList<>(this.functions.size());
        functions.addAll(this.functions);
        return new CompiledShader(declarations, functions, shaderVertex,
                shaderFragment, outputs, uniforms);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
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
                int id = Integer.parseInt(uniform.IntegerLiteral().getText());
                if (uniforms.length <= id) {
                    Uniform[] newUniforms = new Uniform[id + 1];
                    System.arraycopy(uniforms, 0, newUniforms, 0,
                            uniforms.length);
                    uniforms = newUniforms;
                }
                uniforms[id] = new Uniform(type(field), id,
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
            ShaderSignature shaderSignature = new ShaderSignature(name,
                    parameters.toArray(new ShaderParameter[parameters.size()]));
            CompoundStatement compound =
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
            ShaderSignature outputsSignature = new ShaderSignature("outputs",
                    parameters.toArray(new ShaderParameter[parameters.size()]));
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
            ScapesShaderParser.PrecisionSpecifierContext precisionSpecifier =
                    signature.precisionSpecifier();
            Precision returnedPrecision;
            if (precisionSpecifier == null) {
                returnedPrecision = Precision.mediump;
            } else {
                returnedPrecision = precision(precisionSpecifier);
            }
            FunctionSignature functionSignature =
                    new FunctionSignature(name, returned, returnedPrecision,
                            parameters
                                    .toArray(new Parameter[parameters.size()]));
            CompoundStatement compound =
                    compound(function.compoundStatement().blockItemList());
            functions.add(new Function(functionSignature, compound));
            return;
        }
    }
}
