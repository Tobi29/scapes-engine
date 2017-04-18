/*
 [The "BSD licence"]
 Copyright (c) 2013 Sam Harwell
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/** C 2011 grammar built from the C11 Spec */
grammar ScapesShader;

primaryExpression
    :   Identifier
    |   constant
    |   property
    |   '(' expression ')'
    ;

postfixExpression
    :   primaryExpression
    |   functionExpression
    |   postfixExpression '[' expression ']'
    |   postfixExpression '.' Identifier
    |   postfixExpression '++'
    |   postfixExpression '--'
    ;

functionExpression
    :   Identifier '(' argumentExpressionList? ')'
    ;

argumentExpressionList
    :   assignmentExpression
    |   assignmentExpression ',' argumentExpressionList
    ;

unaryExpression
    :   postfixExpression
    |   '++' unaryExpression
    |   '--' unaryExpression
    |   unaryOperator unaryExpression
    ;

unaryOperator
    :   '+' | '-' | '~' | '!'
    ;

multiplicativeExpression
    :   unaryExpression
    |   multiplicativeExpression '*' unaryExpression
    |   multiplicativeExpression '/' unaryExpression
    |   multiplicativeExpression '%' unaryExpression
    ;

additiveExpression
    :   multiplicativeExpression
    |   additiveExpression '+' multiplicativeExpression
    |   additiveExpression '-' multiplicativeExpression
    ;

shiftExpression
    :   additiveExpression
    |   shiftExpression '<<' additiveExpression
    |   shiftExpression '>>' additiveExpression
    ;

relationalExpression
    :   shiftExpression
    |   relationalExpression '<' shiftExpression
    |   relationalExpression '>' shiftExpression
    |   relationalExpression '<=' shiftExpression
    |   relationalExpression '>=' shiftExpression
    ;

equalityExpression
    :   relationalExpression
    |   equalityExpression '==' relationalExpression
    |   equalityExpression '!=' relationalExpression
    ;

andExpression
    :   equalityExpression
    |   andExpression '&' equalityExpression
    ;

exclusiveOrExpression
    :   andExpression
    |   exclusiveOrExpression '^' andExpression
    ;

inclusiveOrExpression
    :   exclusiveOrExpression
    |   inclusiveOrExpression '|' exclusiveOrExpression
    ;

logicalAndExpression
    :   inclusiveOrExpression
    |   logicalAndExpression '&&' inclusiveOrExpression
    ;

logicalOrExpression
    :   logicalAndExpression
    |   logicalOrExpression '||' logicalAndExpression
    ;

conditionalExpression
    :   logicalOrExpression ('?' expression ':' conditionalExpression)?
    ;

assignmentExpression
    :   conditionalExpression
    |   unaryExpression assignmentOperator assignmentExpression
    ;

assignmentOperator
    :   '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '|=' | '^='
    ;

expression
    :   assignmentExpression
    ;

declaration
    :   declarationField
    |   declarationArray
    ;

declarationField
    :   declaratorField initDeclaratorFieldList ';'
    ;

declarationArray
    :   declaratorArray initDeclaratorArrayList ';'
    |   declaratorArrayUnsized Identifier '=' initializerArray
    ;

uniformDeclaration
    :   'uniform' IntegerLiteral declarator Identifier ';'
    ;

initDeclaratorFieldList
    :   initDeclaratorField ',' initDeclaratorFieldList
    |   initDeclaratorField
    ;

initDeclaratorArrayList
    :   Identifier
    |   Identifier ',' initDeclaratorArrayList
    ;

initDeclaratorField
    :   Identifier
    |   Identifier '=' initializerField
    ;

type
    :   typeSpecifier '[]'
    ;

typeSpecifier
    :   'Void'
    |   'Float'
    |   'Int'
    |   'Vector2'
    |   'Vector2i'
    |   'Matrix2'
    |   'Vector3'
    |   'Vector3i'
    |   'Matrix3'
    |   'Vector4'
    |   'Vector4i'
    |   'Matrix4'
    |   'Texture2'
    ;

precisionSpecifier
    :   'lowp'
    |   'mediump'
    |   'highp'
    ;

declarator
    :   declaratorField
    |   declaratorArray
    ;

declaratorField
    :   'const'? precisionSpecifier? typeSpecifier
    ;

declaratorArray
    :   'const'? precisionSpecifier? typeSpecifier '[' integerConstant ']'
    ;

declaratorArrayUnsized
    :   'const'? precisionSpecifier? typeSpecifier '[]'
    ;

parameterList
    :   parameterDeclaration
    |   parameterDeclaration ',' parameterList
    ;

parameterDeclaration
    :   declarator Identifier
    ;

shaderParameterList
    :   shaderParameterDeclaration
    |   shaderParameterDeclaration ',' shaderParameterList
    ;

shaderParameterDeclaration
    :   ('if' '(' property ')')? IntegerLiteral? declarator Identifier
    ;

initializerField
    :   assignmentExpression
    ;

initializerArray
    :   '{' initializerArrayList '}'
    |   property
    ;

initializerArrayList
    :   assignmentExpression
    |   assignmentExpression ',' initializerArrayList
    ;

property
    :   '$' Identifier
    ;

statement
    :   declaration
    |   expressionStatement
    |   compoundStatement
    |   selectionStatement
    |   rangeLoopStatement
    ;

selectionStatement
    :   ifStatement statement elseStatement?
    ;

ifStatement
    :   'if' '(' expression ')'
    ;

elseStatement
    :   'else' statement
    ;

rangeLoopStatement
    :   'for' '(' Identifier 'in' integerConstant '...' integerConstant ')' statement
    ;

compoundStatement
    :   '{' blockItemList? '}'
    ;

blockItemList
    :   statement
    |   statement blockItemList
    ;

expressionStatement
    :   expression? ';'
    ;

compilationUnit
    :   translationUnit? EOF
    ;

translationUnit
    :   externalDeclaration
    |   externalDeclaration translationUnit
    ;

externalDeclaration
    :   shaderDefinition
    |   outputsDefinition
    |   functionDefinition
    |   declaration
    |   uniformDeclaration
    |   ';'
    ;

shaderDefinition
    :   'shader' shaderSignature compoundStatement
    ;

shaderSignature
    :   Identifier '(' shaderParameterList? ')'
    ;

outputsDefinition
    :   'outputs' '(' shaderParameterList? ')'
    ;

functionDefinition
    :   'fun' functionSignature compoundStatement
    ;

functionSignature
    :   typeSpecifier precisionSpecifier? Identifier '(' parameterList? ')'
    ;

Identifier
    :   IdentifierNondigit
        (   IdentifierNondigit
        |   Digit
        )*
    ;

fragment
IdentifierNondigit
    :   Nondigit
    ;

fragment
Nondigit
    :   [a-zA-Z_]
    ;

fragment
Digit
    :   [0-9]
    ;

constant
    :   integerConstant
    |   floatingConstant
    |   characterConstant
    ;

integerConstant
    :   IntegerLiteral
    |   property
    ;

floatingConstant
    :   FloatingLiteral
    |   property
    ;

characterConstant
    :   CharacterLiteral
    |   property
    ;

IntegerLiteral
    :   Digit+
    ;

FloatingLiteral
    :   FractionalConstant ExponentPart?
    ;

CharacterLiteral
    :   '\'' CCharSequence '\''
    |   'L\'' CCharSequence '\''
    |   'u\'' CCharSequence '\''
    |   'U\'' CCharSequence '\''
    ;

fragment
FractionalConstant
    :   DigitSequence? '.' DigitSequence
    |   DigitSequence '.'
    ;

fragment
ExponentPart
    :   'e' Sign? DigitSequence
    |   'E' Sign? DigitSequence
    ;

fragment
Sign
    :   '+' | '-'
    ;

fragment
DigitSequence
    :   Digit+
    ;

fragment
CCharSequence
    :   CChar+
    ;

fragment
CChar
    :   ~['\\\r\n]
    |   EscapeSequence
    ;

fragment
EscapeSequence
    :   '\\' ['"?abfnrtv\\]
    ;

Whitespace
    :   [ \t]+
        -> skip
    ;

Newline
    :   (   '\r' '\n'?
        |   '\n'
        )
        -> skip
    ;

BlockComment
    :   '/*' .*? '*/'
        -> skip
    ;

LineComment
    :   '//' ~[\r\n]*
        -> skip
    ;