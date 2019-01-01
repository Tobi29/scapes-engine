/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.scapes.engine.shader

import org.tobi29.stdex.readOnly

object STDLib {
    private val scalar = sequenceOf(
        listOf(Type(Types.Float)),
        listOf(Type(Types.Int)),
        listOf(Type(Types.Boolean))
    )
    private val vector2 = sequenceOf(
        listOf(Type(Types.Vector2)),
        listOf(Type(Types.Vector2i)),
        listOf(Type(Types.Vector2b))
    )
    private val vector3 = sequenceOf(
        listOf(Type(Types.Vector3)),
        listOf(Type(Types.Vector3i)),
        listOf(Type(Types.Vector3b))
    )
    private val vector4 = sequenceOf(
        listOf(Type(Types.Vector4)),
        listOf(Type(Types.Vector4i)),
        listOf(Type(Types.Vector4b))
    )

    val constructScalar = scalar.toSet().readOnly()
    val constructVector2 = (scalar + vector2 + (
            genT(scalar, 2)
            ).filter { it.sumBy { it.type.vectorSize } == 2 })
        .toSet().readOnly()
    val constructVector3 = (scalar + vector3 + (
            genT(scalar, 3) + genT(scalar + vector2, 2)
            ).filter { it.sumBy { it.type.vectorSize } == 3 })
        .toSet().readOnly()
    val constructVector4 = (scalar + vector4 + (
            genT(scalar, 4) + genT(scalar + vector2, 3)
                    + genT(scalar + vector3, 2)
            ).filter { it.sumBy { it.type.vectorSize } == 4 })
        .toSet().readOnly()

    private val swizzleX = (setOf("x")).readOnly()
    private val swizzleXY = (swizzleX + setOf("y")).readOnly()
    private val swizzleXYZ = (swizzleXY + setOf("z")).readOnly()
    private val swizzleXYZW = (swizzleXYZ + setOf("w")).readOnly()

    private val swizzleR = (setOf("r")).readOnly()
    private val swizzleRG = (swizzleR + setOf("g")).readOnly()
    private val swizzleRGB = (swizzleRG + setOf("b")).readOnly()
    private val swizzleRGBA = (swizzleRGB + setOf("a")).readOnly()

    val swizzle2to1 = (gen(swizzleXY.asSequence(), 1)
            + gen(swizzleRG.asSequence(), 1)).toSet().readOnly()
    val swizzle2to2 = (gen(swizzleXY.asSequence(), 2)
            + gen(swizzleRG.asSequence(), 2)).toSet().readOnly()
    val swizzle2to3 = (gen(swizzleXY.asSequence(), 3)
            + gen(swizzleRG.asSequence(), 3)).toSet().readOnly()
    val swizzle2to4 = (gen(swizzleXY.asSequence(), 4)
            + gen(swizzleRG.asSequence(), 4)).toSet().readOnly()
    val swizzle3to1 = (gen(swizzleXYZ.asSequence(), 1)
            + gen(swizzleRGB.asSequence(), 1)).toSet().readOnly()
    val swizzle3to2 = (gen(swizzleXYZ.asSequence(), 2)
            + gen(swizzleRGB.asSequence(), 2)).toSet().readOnly()
    val swizzle3to3 = (gen(swizzleXYZ.asSequence(), 3)
            + gen(swizzleRGB.asSequence(), 3)).toSet().readOnly()
    val swizzle3to4 = (gen(swizzleXYZ.asSequence(), 4)
            + gen(swizzleRGB.asSequence(), 4)).toSet().readOnly()
    val swizzle4to1 = (gen(swizzleXYZW.asSequence(), 1)
            + gen(swizzleRGBA.asSequence(), 1)).toSet().readOnly()
    val swizzle4to2 = (gen(swizzleXYZW.asSequence(), 2)
            + gen(swizzleRGBA.asSequence(), 2)).toSet().readOnly()
    val swizzle4to3 = (gen(swizzleXYZW.asSequence(), 3)
            + gen(swizzleRGBA.asSequence(), 3)).toSet().readOnly()
    val swizzle4to4 = (gen(swizzleXYZW.asSequence(), 4)
            + gen(swizzleRGBA.asSequence(), 4)).toSet().readOnly()

    val functions =
        HashMap<FunctionExportedSignature, (List<Expression>) -> Expression>()
            .also { functions(it) }.readOnly()

    private fun functions(
        functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>
    ) {
        function(
            functions, FunctionExportedSignature(
                "texture",
                Type(Types.Vector4),
                Type(Types.Texture2),
                Type(Types.Vector2)
            )
        )

        mathFunctions(
            functions, Type(Types.Float),
            Type(Types.Boolean), Type(Types.Float)
        )
        mathFunctions(
            functions, Type(Types.Vector2),
            Type(Types.Vector2b), Type(Types.Float)
        )
        mathFunctions(
            functions, Type(Types.Vector3),
            Type(Types.Vector3b), Type(Types.Float)
        )
        mathFunctions(
            functions, Type(Types.Vector4),
            Type(Types.Vector4b), Type(Types.Float)
        )

        mathFunctions(
            functions, Type(Types.Int),
            Type(Types.Boolean), Type(Types.Int)
        )
        mathFunctions(
            functions, Type(Types.Vector2i),
            Type(Types.Vector2b), Type(Types.Int)
        )
        mathFunctions(
            functions, Type(Types.Vector3i),
            Type(Types.Vector3b), Type(Types.Int)
        )
        mathFunctions(
            functions, Type(Types.Vector4i),
            Type(Types.Vector4b), Type(Types.Int)
        )

        logicFunctions(
            functions, Type(Types.Boolean),
            Type(Types.Boolean)
        )
        logicFunctions(
            functions, Type(Types.Vector2b),
            Type(Types.Vector2b)
        )
        logicFunctions(
            functions, Type(Types.Vector3b),
            Type(Types.Vector3b)
        )
        logicFunctions(
            functions, Type(Types.Vector4b),
            Type(Types.Vector4b)
        )

        vectorFunctions(
            functions, Type(Types.Vector2),
            Type(Types.Vector2b), Type(Types.Float)
        )
        vectorFunctions(
            functions, Type(Types.Vector3),
            Type(Types.Vector3b), Type(Types.Float)
        )
        vectorFunctions(
            functions, Type(Types.Vector4),
            Type(Types.Vector4b), Type(Types.Float)
        )

        matrixFunctions(
            functions, Type(Types.Matrix2),
            Type(Types.Vector2)
        )
        matrixFunctions(
            functions, Type(Types.Matrix3),
            Type(Types.Vector3)
        )
        matrixFunctions(
            functions, Type(Types.Matrix4),
            Type(Types.Vector4)
        )

        constructScalar.forEach {
            function(
                functions, FunctionExportedSignature(
                    "float",
                    Type(Types.Float), it
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "int",
                    Type(Types.Int), it
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "boolean",
                    Type(Types.Boolean), it
                )
            )
        }
        constructVector2.forEach {
            function(
                functions, FunctionExportedSignature(
                    "vector2",
                    Type(Types.Vector2), it
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "vector2i",
                    Type(Types.Vector2i), it
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "vector2b",
                    Type(Types.Vector2b), it
                )
            )
        }
        constructVector3.forEach {
            function(
                functions, FunctionExportedSignature(
                    "vector3",
                    Type(Types.Vector3), it
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "vector3i",
                    Type(Types.Vector3i), it
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "vector3b",
                    Type(Types.Vector3b), it
                )
            )
        }
        constructVector4.forEach {
            function(
                functions, FunctionExportedSignature(
                    "vector4",
                    Type(Types.Vector4), it
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "vector4i",
                    Type(Types.Vector4i), it
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "vector4b",
                    Type(Types.Vector4b), it
                )
            )
        }
        function(
            functions,
            FunctionExportedSignature("discard", Type(Types.Unit))
        )
    }

    private fun mathFunctions(
        functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
        type: Type,
        typeBoolean: Type,
        typeScalar: Type
    ) {
        function(
            functions, FunctionExportedSignature(
                "plus", type, type, type
            )
        ) { (a, b) -> a + b }
        function(
            functions, FunctionExportedSignature(
                "minus", type, type, type
            )
        ) { (a, b) -> a - b }
        function(
            functions, FunctionExportedSignature(
                "times", type, type, type
            )
        ) { (a, b) -> a * b }
        function(
            functions, FunctionExportedSignature(
                "div", type, type, type
            )
        ) { (a, b) -> a / b }
        function(
            functions, FunctionExportedSignature(
                "rem", type, type, type
            )
        ) { (a, b) -> a % b }
        function(
            functions, FunctionExportedSignature(
                "length", typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "abs", type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "floor", type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "sin", type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "cos", type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "min", type, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "max", type, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "clamp", type, type, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "mix", type, type, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "mix", type, type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "dot", typeScalar, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "mod", type, type, type
            )
        )
        if (type == Type(Types.Int)) {
            function(
                functions, FunctionExportedSignature(
                    "shl", type, type, type
                )
            )
            function(
                functions, FunctionExportedSignature(
                    "shr", type, type, type
                )
            )
        }
        function(
            functions, FunctionExportedSignature(
                "greaterThan", typeBoolean, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "greaterThanEqual", typeBoolean, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "lessThan", typeBoolean, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "lessThanEqual", typeBoolean, type, type
            )
        )
        logicFunctions(functions, type, typeBoolean)
    }

    private fun logicFunctions(
        functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
        type: Type,
        typeBoolean: Type
    ) {
        if (type == Type(Types.Int) || type == Type(Types.Boolean)) {
            function(
                functions, FunctionExportedSignature(
                    "and", type, type, type
                )
            ) { (a, b) -> a and b }
            function(
                functions, FunctionExportedSignature(
                    "or", type, type, type
                )
            ) { (a, b) -> a or b }
            function(
                functions, FunctionExportedSignature(
                    "xor", type, type, type
                )
            ) { (a, b) -> a xor b }
        }
        function(
            functions, FunctionExportedSignature(
                "equals", typeBoolean, type, type
            )
        ) { (a, b) -> a equals b }
    }

    private fun vectorFunctions(
        functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
        type: Type,
        typeBoolean: Type,
        typeScalar: Type
    ) {
        function(
            functions, FunctionExportedSignature(
                "plus", type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "plus", type, typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "minus", type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "minus", type, typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "times", type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "times", type, typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "div", type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "div", type, typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "rem", type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "rem", type, typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "min", type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "min", type, typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "max", type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "max", type, typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "clamp", type, type, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "clamp", type, type, typeScalar, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "clamp", type, typeScalar, type, typeScalar
            )
        )
        function(
            functions, FunctionExportedSignature(
                "clamp", type, typeScalar, typeScalar, type
            )
        )
    }

    private fun matrixFunctions(
        functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
        type: Type,
        typeVector: Type
    ) {
        function(
            functions, FunctionExportedSignature(
                "plus", type, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "minus", type, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "times", type, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "div", type, type, type
            )
        )
        function(
            functions, FunctionExportedSignature(
                "times", typeVector, type, typeVector
            )
        )
    }

    private fun function(
        functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
        signature: FunctionExportedSignature,
        simplification: (List<Expression>) -> Expression = {
            FunctionStatement(signature, it)
        }
    ) {
        functions[signature] = simplification
    }

    private fun gen(s: Sequence<String>, level: Int): Sequence<String> =
        if (level <= 1) s else gen(s, level - 1) join s

    private infix fun Sequence<String>.join(
        other: Sequence<String>
    ) = flatMap { a -> other.map { b -> "$a$b" } }

    private fun genT(
        s: Sequence<List<Type>>,
        level: Int
    ): Sequence<List<Type>> =
        if (level <= 1) s else (genT(s, level - 1) joinT s) + s

    private infix fun Sequence<List<Type>>.joinT(
        other: Sequence<List<Type>>
    ) = flatMap { a -> other.map { b -> a + b } }
}

fun TypeExported.memberType(name: String) =
    if (array != null) null else type.memberType(name)

fun Type.memberType(name: String) =
    exported.memberType(name)

fun Types.memberType(name: String) = when (this) {
    Types.Vector2 -> if (name in STDLib.swizzle2to1) {
        Type(Types.Float)
    } else if (name in STDLib.swizzle2to2) {
        Type(Types.Vector2)
    } else if (name in STDLib.swizzle2to3) {
        Type(Types.Vector3)
    } else if (name in STDLib.swizzle2to4) {
        Type(Types.Vector4)
    } else {
        null
    }
    Types.Vector3 -> if (name in STDLib.swizzle3to1) {
        Type(Types.Float)
    } else if (name in STDLib.swizzle3to2) {
        Type(Types.Vector2)
    } else if (name in STDLib.swizzle3to3) {
        Type(Types.Vector3)
    } else if (name in STDLib.swizzle3to4) {
        Type(Types.Vector4)
    } else {
        null
    }
    Types.Vector4 -> if (name in STDLib.swizzle4to1) {
        Type(Types.Float)
    } else if (name in STDLib.swizzle4to2) {
        Type(Types.Vector2)
    } else if (name in STDLib.swizzle4to3) {
        Type(Types.Vector3)
    } else if (name in STDLib.swizzle4to4) {
        Type(Types.Vector4)
    } else {
        null
    }
    else -> null
}

val Types.vectorSize
    get() = when (this) {
        Types.Vector2 -> 2
        Types.Vector3 -> 3
        Types.Vector4 -> 4
        Types.Vector2i -> 2
        Types.Vector3i -> 3
        Types.Vector4i -> 4
        Types.Vector2b -> 2
        Types.Vector3b -> 3
        Types.Vector4b -> 4
        Types.Matrix2 -> 2
        Types.Matrix3 -> 3
        Types.Matrix4 -> 4
        Types.Unit -> 0
        else -> 1
    }
