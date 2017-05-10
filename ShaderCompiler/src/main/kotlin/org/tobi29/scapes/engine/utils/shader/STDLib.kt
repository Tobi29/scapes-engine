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

object STDLib {
    fun functions(
            functionsSignatures: MutableList<FunctionExportedSignature>) {
        functionsSignatures.add(
                FunctionExportedSignature("texture",
                        Types.Vector4.exported,
                        Types.Texture2.exported,
                        Types.Vector2.exported))

        mathFunctions(functionsSignatures, Types.Float.exported,
                Types.Boolean.exported, Types.Float.exported)
        mathFunctions(functionsSignatures, Types.Vector2.exported,
                Types.Vector2b.exported, Types.Float.exported)
        mathFunctions(functionsSignatures, Types.Vector3.exported,
                Types.Vector3b.exported, Types.Float.exported)
        mathFunctions(functionsSignatures, Types.Vector4.exported,
                Types.Vector4b.exported, Types.Float.exported)

        mathFunctions(functionsSignatures, Types.Int.exported,
                Types.Boolean.exported, Types.Int.exported)
        mathFunctions(functionsSignatures, Types.Vector2i.exported,
                Types.Vector2b.exported, Types.Int.exported)
        mathFunctions(functionsSignatures, Types.Vector3i.exported,
                Types.Vector3b.exported, Types.Int.exported)
        mathFunctions(functionsSignatures, Types.Vector4i.exported,
                Types.Vector4b.exported, Types.Int.exported)

        vectorFunctions(functionsSignatures, Types.Vector2.exported,
                Types.Vector2b.exported, Types.Float.exported)
        vectorFunctions(functionsSignatures, Types.Vector3.exported,
                Types.Vector3b.exported, Types.Float.exported)
        vectorFunctions(functionsSignatures, Types.Vector4.exported,
                Types.Vector4b.exported, Types.Float.exported)

        matrixFunctions(functionsSignatures, Types.Matrix2.exported,
                Types.Vector2.exported)
        matrixFunctions(functionsSignatures, Types.Matrix3.exported,
                Types.Vector3.exported)
        matrixFunctions(functionsSignatures, Types.Matrix4.exported,
                Types.Vector4.exported)

        functionsSignatures.add(
                FunctionExportedSignature("float", Types.Float.exported,
                        Types.Float.exported))

        functionsSignatures.add(
                FunctionExportedSignature("vector2",
                        Types.Vector2.exported,
                        Types.Float.exported))
        functionsSignatures.add(
                FunctionExportedSignature("vector2",
                        Types.Vector2.exported,
                        Types.Vector2.exported))
        functionsSignatures.add(
                FunctionExportedSignature("vector2",
                        Types.Vector2.exported,
                        Types.Float.exported,
                        Types.Float.exported))

        functionsSignatures.add(
                FunctionExportedSignature("vector3",
                        Types.Vector3.exported,
                        Types.Float.exported))
        functionsSignatures.add(
                FunctionExportedSignature("vector3",
                        Types.Vector3.exported,
                        Types.Float.exported,
                        Types.Float.exported,
                        Types.Float.exported))
        functionsSignatures.add(
                FunctionExportedSignature("vector3",
                        Types.Vector3.exported,
                        Types.Float.exported,
                        Types.Vector2.exported))
        functionsSignatures.add(
                FunctionExportedSignature("vector3",
                        Types.Vector3.exported,
                        Types.Vector2.exported,
                        Types.Float.exported))
        constructScalar.forEach {
            functionsSignatures.add(FunctionExportedSignature("float",
                    Types.Float.exported, it))
            functionsSignatures.add(FunctionExportedSignature("int",
                    Types.Int.exported, it))
            functionsSignatures.add(FunctionExportedSignature("boolean",
                    Types.Boolean.exported, it))
        }
        constructVector2.forEach {
            functionsSignatures.add(FunctionExportedSignature("vector2",
                    Types.Vector2.exported, it))
            functionsSignatures.add(FunctionExportedSignature("vector2i",
                    Types.Vector2i.exported, it))
            functionsSignatures.add(FunctionExportedSignature("vector2b",
                    Types.Vector2b.exported, it))
        }
        constructVector3.forEach {
            functionsSignatures.add(FunctionExportedSignature("vector3",
                    Types.Vector3.exported, it))
            functionsSignatures.add(FunctionExportedSignature("vector3i",
                    Types.Vector3i.exported, it))
            functionsSignatures.add(FunctionExportedSignature("vector3b",
                    Types.Vector3b.exported, it))
        }
        constructVector4.forEach {
            functionsSignatures.add(FunctionExportedSignature("vector4",
                    Types.Vector4.exported, it))
            functionsSignatures.add(FunctionExportedSignature("vector4i",
                    Types.Vector4i.exported, it))
            functionsSignatures.add(FunctionExportedSignature("vector4b",
                    Types.Vector4b.exported, it))
        }
        Types.values().forEach { type ->
            functionsSignatures.add(FunctionExportedSignature("return",
                    Types.Void.exported, type.exported))
            functionsSignatures.add(FunctionExportedSignature("return",
                    Types.Void.exported, type.exportedArray))
        }
        functionsSignatures.add(
                FunctionExportedSignature("break", Types.Void.exported))
        functionsSignatures.add(
                FunctionExportedSignature("continue", Types.Void.exported))
        functionsSignatures.add(
                FunctionExportedSignature("discard", Types.Void.exported))
    }

    private fun mathFunctions(
            functionsSignatures: MutableList<FunctionExportedSignature>,
            type: TypeExported,
            typeBoolean: TypeExported,
            typeScalar: TypeExported) {
        functionsSignatures.add(FunctionExportedSignature(
                "plus", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "minus", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "times", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "div", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "rem", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "length", typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "abs", type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "floor", type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "sin", type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "cos", type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "min", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "max", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "clamp", type, type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "mix", type, type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "mix", type, type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "dot", typeScalar, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "mod", type, type, type))
        if (type == Types.Int.exported) {
            functionsSignatures.add(FunctionExportedSignature(
                    "shl", type, type, type))
            functionsSignatures.add(FunctionExportedSignature(
                    "shr", type, type, type))
            functionsSignatures.add(FunctionExportedSignature(
                    "and", type, type, type))
            functionsSignatures.add(FunctionExportedSignature(
                    "or", type, type, type))
            functionsSignatures.add(FunctionExportedSignature(
                    "xor", type, type, type))
        }
    }

    private fun vectorFunctions(
            functionsSignatures: MutableList<FunctionExportedSignature>,
            type: TypeExported,
            typeBoolean: TypeExported,
            typeScalar: TypeExported) {
        functionsSignatures.add(FunctionExportedSignature(
                "greaterThan", typeBoolean, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "greaterThanEqual", typeBoolean, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "lessThan", typeBoolean, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "lessThanEqual", typeBoolean, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "plus", type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "plus", type, typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "minus", type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "minus", type, typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "times", type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "times", type, typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "div", type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "div", type, typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "rem", type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "rem", type, typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "min", type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "min", type, typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "max", type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "max", type, typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "clamp", type, type, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "clamp", type, type, typeScalar, type))
        functionsSignatures.add(FunctionExportedSignature(
                "clamp", type, typeScalar, type, typeScalar))
        functionsSignatures.add(FunctionExportedSignature(
                "clamp", type, typeScalar, typeScalar, type))
    }

    private fun matrixFunctions(
            functionsSignatures: MutableList<FunctionExportedSignature>,
            type: TypeExported,
            typeVector: TypeExported) {
        functionsSignatures.add(FunctionExportedSignature(
                "plus", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "minus", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "times", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "div", type, type, type))
        functionsSignatures.add(FunctionExportedSignature(
                "times", typeVector, type, typeVector))
    }

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

    private fun genT(s: Set<List<TypeExported>>,
                     level: Int): Set<List<TypeExported>> =
            if (level <= 1) s else (genT(s, level - 1) joinT s) + s

    private infix fun Set<List<TypeExported>>.joinT(other: Set<List<TypeExported>>): Set<List<TypeExported>> {
        val set = HashSet<List<TypeExported>>()
        forEach { a ->
            other.forEach { b ->
                set.add(a + b)
            }
        }
        return set
    }

    val constructScalar = scalar
    val constructVector2 = scalar +
            genT(vector2, 2).filter { it.sumBy { it.type.vectorSize } == 2 }
    val constructVector3 = scalar +
            genT(vector3, 3).filter { it.sumBy { it.type.vectorSize } == 3 }
    val constructVector4 = scalar +
            genT(vector4, 4).filter { it.sumBy { it.type.vectorSize } == 4 }

    private val swizzleX = setOf("x")
    private val swizzleXY = swizzleX + setOf("y")
    private val swizzleXYZ = swizzleXY + setOf("z")
    private val swizzleXYZW = swizzleXYZ + setOf("w")

    private val swizzleR = setOf("r")
    private val swizzleRG = swizzleR + setOf("g")
    private val swizzleRGB = swizzleRG + setOf("b")
    private val swizzleRGBA = swizzleRGB + setOf("a")

    private fun gen(s: Set<String>,
                    level: Int): Set<String> =
            if (level <= 1) s else gen(s, level - 1) join s

    private infix fun Set<String>.join(other: Set<String>): Set<String> {
        val set = HashSet<String>()
        forEach { a ->
            other.forEach { b ->
                set.add("$a$b")
            }
        }
        return set
    }

    val swizzle2to1 = gen(swizzleXY, 1) + gen(swizzleRG, 1)
    val swizzle2to2 = gen(swizzleXY, 2) + gen(swizzleRG, 2)
    val swizzle2to3 = gen(swizzleXY, 3) + gen(swizzleRG, 3)
    val swizzle2to4 = gen(swizzleXY, 4) + gen(swizzleRG, 4)
    val swizzle3to1 = gen(swizzleXYZ, 1) + gen(swizzleRGB, 1)
    val swizzle3to2 = gen(swizzleXYZ, 2) + gen(swizzleRGB, 2)
    val swizzle3to3 = gen(swizzleXYZ, 3) + gen(swizzleRGB, 3)
    val swizzle3to4 = gen(swizzleXYZ, 4) + gen(swizzleRGB, 4)
    val swizzle4to1 = gen(swizzleXYZW, 1) + gen(swizzleRGBA, 1)
    val swizzle4to2 = gen(swizzleXYZW, 2) + gen(swizzleRGBA, 2)
    val swizzle4to3 = gen(swizzleXYZW, 3) + gen(swizzleRGBA, 3)
    val swizzle4to4 = gen(swizzleXYZW, 4) + gen(swizzleRGBA, 4)
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

val Types.vectorSize get() = when (this) {
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
