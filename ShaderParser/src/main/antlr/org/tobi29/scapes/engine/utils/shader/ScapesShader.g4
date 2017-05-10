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

/** Grammar based on C and Java grammars from
 *  https://github.com/antlr/grammars-v4
 */
grammar ScapesShader;

primaryExpression
    :   Identifier
    |   literal
    |   '(' expression ')'
    ;

expressionList
    :   expression
    |   expression ',' expressionList
    ;

expression
    :   primaryExpression
    |   expression '.' Identifier
    |   expression '[' expression ']'
    |   Identifier '(' expressionList? ')'
    |   expression ('++' | '--')
    |   ('+'|'-'|'++'|'--') expression
    |   ('~'|'!') expression
    |   expression ('*'|'/'|'%') expression
    |   expression ('+'|'-') expression
    |   expression ('<<' | '>>') expression
    |   expression ('<=' | '>=' | '>' | '<') expression
    |   expression ('==' | '!=') expression
    |   expression '&' expression
    |   expression '^' expression
    |   expression '|' expression
    |   expression '&&' expression
    |   expression '||' expression
    |   expression '?' expression ':' expression
    |   <assoc=right> expression
            (   '='
            |   '+='
            |   '-='
            |   '*='
            |   '/='
            |   '%='
            |   '&='
            |   '|='
            |   '^='
            |   '<<='
            |   '>>='
            )
            expression
    ;

declaration
    :   declarationField
    |   declarationArray
    ;

declarationField
    :   declaratorField Identifier ('=' expression ';')?
    ;

declarationArray
    :   declaratorArray Identifier '=' initializerArray ';'
    ;

uniformDeclaration
    :   'uniform' IntegerLiteral declarator Identifier ';'
    ;

propertyDeclaration
    :   'property' declarator Identifier ';'
    ;

type
    :   typeSpecifier '[]'?
    ;

typeSpecifier
    :   'Void'
    |   'Boolean'
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
    :   'const'? precisionSpecifier? typeSpecifier '[' expression ']'
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
    :   ('if' '(' expression ')')? IntegerLiteral? declarator Identifier
    ;

initializerArray
    :   '{' expressionList '}'
    |   expression
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
    :   'for' '(' Identifier 'in' expression '...' expression ')' statement
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
    |   propertyDeclaration
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
    :   type precisionSpecifier? Identifier '(' parameterList? ')'
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

literal
    :   IntegerLiteral
    |   FloatingLiteral
    |   CharacterLiteral
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