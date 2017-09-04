/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.utils.shader

import org.tobi29.scapes.engine.utils.readOnly

object STDLib {
    private val scalar = setOf(
            listOf(Types.Float.exported),
            listOf(Types.Int.exported),
            listOf(Types.Boolean.exported))
    private val vector2 = scalar + setOf(
            listOf(Types.Vector2.exported),
            listOf(Types.Vector2i.exported),
            listOf(Types.Vector2b.exported))
    private val vector3 = vector2 + setOf(
            listOf(Types.Vector3.exported),
            listOf(Types.Vector3i.exported),
            listOf(Types.Vector3b.exported))
    private val vector4 = vector3 + setOf(
            listOf(Types.Vector4.exported),
            listOf(Types.Vector4i.exported),
            listOf(Types.Vector4b.exported))

    val constructScalar = (scalar).toSet().readOnly()
    val constructVector2 = (vector2.asSequence()
            + genT(scalar.asSequence(), 2)
            .filter { it.sumBy { it.type.vectorSize } == 2 })
            .toSet().readOnly()
    val constructVector3 = (vector3.asSequence()
            + genT(vector2.asSequence(), 3)
            .filter { it.sumBy { it.type.vectorSize } == 3 })
            .toSet().readOnly()
    val constructVector4 = (vector4.asSequence()
            + genT(vector3.asSequence(), 4)
            .filter { it.sumBy { it.type.vectorSize } == 4 })
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

    val functions = HashMap<FunctionExportedSignature, (List<Expression>) -> Expression>()
            .also { functions(it) }.readOnly()

    private fun functions(
            functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>) {
        function(functions, FunctionExportedSignature("texture",
                Types.Vector4.exported,
                Types.Texture2.exported,
                Types.Vector2.exported))

        mathFunctions(functions, Types.Float.exported,
                Types.Boolean.exported, Types.Float.exported)
        mathFunctions(functions, Types.Vector2.exported,
                Types.Vector2b.exported, Types.Float.exported)
        mathFunctions(functions, Types.Vector3.exported,
                Types.Vector3b.exported, Types.Float.exported)
        mathFunctions(functions, Types.Vector4.exported,
                Types.Vector4b.exported, Types.Float.exported)

        mathFunctions(functions, Types.Int.exported,
                Types.Boolean.exported, Types.Int.exported)
        mathFunctions(functions, Types.Vector2i.exported,
                Types.Vector2b.exported, Types.Int.exported)
        mathFunctions(functions, Types.Vector3i.exported,
                Types.Vector3b.exported, Types.Int.exported)
        mathFunctions(functions, Types.Vector4i.exported,
                Types.Vector4b.exported, Types.Int.exported)

        logicFunctions(functions, Types.Boolean.exported,
                Types.Boolean.exported)
        logicFunctions(functions, Types.Vector2b.exported,
                Types.Vector2b.exported)
        logicFunctions(functions, Types.Vector3b.exported,
                Types.Vector3b.exported)
        logicFunctions(functions, Types.Vector4b.exported,
                Types.Vector4b.exported)

        vectorFunctions(functions, Types.Vector2.exported,
                Types.Vector2b.exported, Types.Float.exported)
        vectorFunctions(functions, Types.Vector3.exported,
                Types.Vector3b.exported, Types.Float.exported)
        vectorFunctions(functions, Types.Vector4.exported,
                Types.Vector4b.exported, Types.Float.exported)

        matrixFunctions(functions, Types.Matrix2.exported,
                Types.Vector2.exported)
        matrixFunctions(functions, Types.Matrix3.exported,
                Types.Vector3.exported)
        matrixFunctions(functions, Types.Matrix4.exported,
                Types.Vector4.exported)

        constructScalar.forEach {
            function(functions, FunctionExportedSignature("float",
                    Types.Float.exported, it))
            function(functions, FunctionExportedSignature("int",
                    Types.Int.exported, it))
            function(functions, FunctionExportedSignature("boolean",
                    Types.Boolean.exported, it))
        }
        constructVector2.forEach {
            function(functions, FunctionExportedSignature("vector2",
                    Types.Vector2.exported, it))
            function(functions, FunctionExportedSignature("vector2i",
                    Types.Vector2i.exported, it))
            function(functions, FunctionExportedSignature("vector2b",
                    Types.Vector2b.exported, it))
        }
        constructVector3.forEach {
            function(functions, FunctionExportedSignature("vector3",
                    Types.Vector3.exported, it))
            function(functions, FunctionExportedSignature("vector3i",
                    Types.Vector3i.exported, it))
            function(functions, FunctionExportedSignature("vector3b",
                    Types.Vector3b.exported, it))
        }
        constructVector4.forEach {
            function(functions, FunctionExportedSignature("vector4",
                    Types.Vector4.exported, it))
            function(functions, FunctionExportedSignature("vector4i",
                    Types.Vector4i.exported, it))
            function(functions, FunctionExportedSignature("vector4b",
                    Types.Vector4b.exported, it))
        }
        Types.values().forEach { type ->
            function(functions, FunctionExportedSignature("return",
                    Types.Void.exported, type.exported))
            function(functions, FunctionExportedSignature("return",
                    Types.Void.exported, type.exportedArray))
        }
        function(functions,
                FunctionExportedSignature("break", Types.Void.exported))
        function(functions,
                FunctionExportedSignature("continue", Types.Void.exported))
        function(functions,
                FunctionExportedSignature("discard", Types.Void.exported))
    }

    private fun mathFunctions(
            functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
            type: TypeExported,
            typeBoolean: TypeExported,
            typeScalar: TypeExported) {
        function(functions,
                FunctionExportedSignature("plus", type, type, type),
                { (a, b) -> a + b })
        function(functions,
                FunctionExportedSignature("minus", type, type, type),
                { (a, b) -> a - b })
        function(functions,
                FunctionExportedSignature("times", type, type, type),
                { (a, b) -> a * b })
        function(functions,
                FunctionExportedSignature("div", type, type, type),
                { (a, b) -> a / b })
        function(functions,
                FunctionExportedSignature("rem", type, type, type),
                { (a, b) -> a % b })
        function(functions, FunctionExportedSignature(
                "length", typeScalar, type))
        function(functions, FunctionExportedSignature(
                "abs", type, type))
        function(functions, FunctionExportedSignature(
                "floor", type, type))
        function(functions, FunctionExportedSignature(
                "sin", type, type))
        function(functions, FunctionExportedSignature(
                "cos", type, type))
        function(functions, FunctionExportedSignature(
                "min", type, type, type))
        function(functions, FunctionExportedSignature(
                "max", type, type, type))
        function(functions, FunctionExportedSignature(
                "clamp", type, type, type, type))
        function(functions, FunctionExportedSignature(
                "mix", type, type, type, type))
        function(functions, FunctionExportedSignature(
                "mix", type, type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "dot", typeScalar, type, type))
        function(functions, FunctionExportedSignature(
                "mod", type, type, type))
        if (type == Types.Int.exported) {
            function(functions, FunctionExportedSignature(
                    "shl", type, type, type))
            function(functions, FunctionExportedSignature(
                    "shr", type, type, type))
        }
        function(functions, FunctionExportedSignature(
                "greaterThan", typeBoolean, type, type))
        function(functions, FunctionExportedSignature(
                "greaterThanEqual", typeBoolean, type, type))
        function(functions, FunctionExportedSignature(
                "lessThan", typeBoolean, type, type))
        function(functions, FunctionExportedSignature(
                "lessThanEqual", typeBoolean, type, type))
        logicFunctions(functions, type, typeBoolean)
    }

    private fun logicFunctions(
            functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
            type: TypeExported,
            typeBoolean: TypeExported) {
        if (type == Types.Int.exported || type == Types.Boolean.exported) {
            function(functions,
                    FunctionExportedSignature("and", type, type, type),
                    { (a, b) -> a and b })
            function(functions,
                    FunctionExportedSignature("or", type, type, type),
                    { (a, b) -> a or b })
            function(functions,
                    FunctionExportedSignature("xor", type, type, type),
                    { (a, b) -> a xor b })
        }
        function(functions,
                FunctionExportedSignature("equals", typeBoolean, type, type),
                { (a, b) -> a equals b })
    }

    private fun vectorFunctions(
            functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
            type: TypeExported,
            typeBoolean: TypeExported,
            typeScalar: TypeExported) {
        function(functions, FunctionExportedSignature(
                "plus", type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "plus", type, typeScalar, type))
        function(functions, FunctionExportedSignature(
                "minus", type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "minus", type, typeScalar, type))
        function(functions, FunctionExportedSignature(
                "times", type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "times", type, typeScalar, type))
        function(functions, FunctionExportedSignature(
                "div", type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "div", type, typeScalar, type))
        function(functions, FunctionExportedSignature(
                "rem", type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "rem", type, typeScalar, type))
        function(functions, FunctionExportedSignature(
                "min", type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "min", type, typeScalar, type))
        function(functions, FunctionExportedSignature(
                "max", type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "max", type, typeScalar, type))
        function(functions, FunctionExportedSignature(
                "clamp", type, type, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "clamp", type, type, typeScalar, type))
        function(functions, FunctionExportedSignature(
                "clamp", type, typeScalar, type, typeScalar))
        function(functions, FunctionExportedSignature(
                "clamp", type, typeScalar, typeScalar, type))
    }

    private fun matrixFunctions(
            functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
            type: TypeExported,
            typeVector: TypeExported) {
        function(functions, FunctionExportedSignature(
                "plus", type, type, type))
        function(functions, FunctionExportedSignature(
                "minus", type, type, type))
        function(functions, FunctionExportedSignature(
                "times", type, type, type))
        function(functions, FunctionExportedSignature(
                "div", type, type, type))
        function(functions, FunctionExportedSignature(
                "times", typeVector, type, typeVector))
    }

    private fun function(
            functions: MutableMap<FunctionExportedSignature, (List<Expression>) -> Expression>,
            signature: FunctionExportedSignature,
            simplification: (List<Expression>) -> Expression = {
                FunctionExpression(signature, it)
            }) {
        functions[signature] = simplification
    }

    private fun gen(s: Sequence<String>,
                    level: Int): Sequence<String> =
            if (level <= 1) s else gen(s, level - 1) join s

    private infix fun Sequence<String>.join(
            other: Sequence<String>
    ) = flatMap { a -> other.map { b -> "$a$b" } }

    private fun genT(s: Sequence<List<TypeExported>>,
                     level: Int): Sequence<List<TypeExported>> =
            if (level <= 1) s else (genT(s, level - 1) joinT s) + s

    private infix fun Sequence<List<TypeExported>>.joinT(
            other: Sequence<List<TypeExported>>
    ) = flatMap { a -> other.map { b -> a + b } }
}

fun TypeExported.memberType(name: String) =
        if (array) null else type.memberType(name)

fun Types.memberType(name: String) = when (this) {
    Types.Vector2 -> if (name in STDLib.swizzle2to1) {
        Types.Float.exported
    } else if (name in STDLib.swizzle2to2) {
        Types.Vector2.exported
    } else if (name in STDLib.swizzle2to3) {
        Types.Vector3.exported
    } else if (name in STDLib.swizzle2to4) {
        Types.Vector4.exported
    } else {
        null
    }
    Types.Vector3 -> if (name in STDLib.swizzle3to1) {
        Types.Float.exported
    } else if (name in STDLib.swizzle3to2) {
        Types.Vector2.exported
    } else if (name in STDLib.swizzle3to3) {
        Types.Vector3.exported
    } else if (name in STDLib.swizzle3to4) {
        Types.Vector4.exported
    } else {
        null
    }
    Types.Vector4 -> if (name in STDLib.swizzle4to1) {
        Types.Float.exported
    } else if (name in STDLib.swizzle4to2) {
        Types.Vector2.exported
    } else if (name in STDLib.swizzle4to3) {
        Types.Vector3.exported
    } else if (name in STDLib.swizzle4to4) {
        Types.Vector4.exported
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
        Types.Void -> 0
        else -> 1
    }
