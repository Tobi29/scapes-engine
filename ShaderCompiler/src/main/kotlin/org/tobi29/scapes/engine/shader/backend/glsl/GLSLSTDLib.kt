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

package org.tobi29.scapes.engine.shader.backend.glsl

import org.tobi29.scapes.engine.shader.FunctionExportedSignature
import org.tobi29.scapes.engine.shader.STDLib
import org.tobi29.scapes.engine.shader.TypeExported
import org.tobi29.scapes.engine.shader.Types

internal object GLSLSTDLib {
    fun functions(
            functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>) {
        functionsSignatures.add(
                Pair(FunctionExportedSignature("texture",
                        Types.Vector4.exported,
                        Types.Texture2.exported,
                        Types.Vector2.exported),
                        { a -> glslFunction("texture", *a) }))

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

        logicFunctions(functionsSignatures, Types.Boolean.exported,
                Types.Boolean.exported)
        logicFunctions(functionsSignatures, Types.Vector2b.exported,
                Types.Vector2b.exported)
        logicFunctions(functionsSignatures, Types.Vector3b.exported,
                Types.Vector3b.exported)
        logicFunctions(functionsSignatures, Types.Vector4b.exported,
                Types.Vector4b.exported)

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

        STDLib.constructScalar.forEach {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("float",
                            Types.Float.exported, it),
                            { a -> glslFunction("float", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("int",
                            Types.Int.exported, it),
                            { a -> glslFunction("int", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("boolean",
                            Types.Boolean.exported, it),
                            { a -> glslFunction("bool", *a) }))
        }
        STDLib.constructVector2.forEach {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector2",
                            Types.Vector2.exported, it),
                            { a -> glslFunction("vec2", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector2i",
                            Types.Vector2i.exported, it),
                            { a -> glslFunction("vec2i", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector2b",
                            Types.Vector2b.exported, it),
                            { a -> glslFunction("vec2b", *a) }))
        }
        STDLib.constructVector3.forEach {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector3",
                            Types.Vector3.exported, it),
                            { a -> glslFunction("vec3", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector3i",
                            Types.Vector3i.exported, it),
                            { a -> glslFunction("vec3i", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector3b",
                            Types.Vector3b.exported, it),
                            { a -> glslFunction("vec3b", *a) }))
        }
        STDLib.constructVector4.forEach {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector4",
                            Types.Vector4.exported, it),
                            { a -> glslFunction("vec4", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector4i",
                            Types.Vector4i.exported, it),
                            { a -> glslFunction("vec4i", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("vector4b",
                            Types.Vector4b.exported, it),
                            { a -> glslFunction("vec4b", *a) }))
        }
        Types.values().forEach { type ->
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("return",
                            Types.Void.exported, type.exported),
                            { a -> "return ${a.joinToString()}" }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature("return",
                            Types.Void.exported, type.exportedArray),
                            { a -> "return ${a.joinToString()}" }))
        }
        functionsSignatures.add(
                Pair(FunctionExportedSignature("break",
                        Types.Void.exported),
                        { _ -> "break" }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature("continue",
                        Types.Void.exported),
                        { _ -> "continue" }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature("discard",
                        Types.Void.exported),
                        { _ -> "discard" }))
    }

    private fun mathFunctions(
            functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>,
            type: TypeExported,
            typeBoolean: TypeExported,
            typeScalar: TypeExported) {
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "plus", type,
                        type,
                        type),
                        { a -> glslOperation("+", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "minus", type,
                        type,
                        type),
                        { a -> glslOperation("-", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "times", type,
                        type,
                        type),
                        { a -> glslOperation("*", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "div", type,
                        type,
                        type),
                        { a -> glslOperation("/", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "rem", type,
                        type,
                        type),
                        { a -> glslFunction("mod", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "length", typeScalar,
                        type),
                        { a -> glslFunction("length", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "abs", type,
                        type),
                        { a -> glslFunction("abs", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "floor", type,
                        type),
                        { a -> glslFunction("floor", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "sin", type,
                        type),
                        { a -> glslFunction("sin", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "cos", type,
                        type),
                        { a -> glslFunction("cos", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "min", type,
                        type,
                        type),
                        { a -> glslFunction("min", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "max", type,
                        type,
                        type),
                        { a -> glslFunction("max", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "clamp", type,
                        type,
                        type,
                        type),
                        { a -> glslFunction("clamp", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "mix", type,
                        type,
                        type,
                        type),
                        { a -> glslFunction("mix", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "mix", type,
                        type,
                        type,
                        typeScalar),
                        { a -> glslFunction("mix", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "dot", typeScalar,
                        type,
                        type),
                        { a -> glslFunction("dot", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "mod", type,
                        type,
                        type),
                        { a -> glslFunction("mod", *a) }))
        if (type == Types.Int.exported) {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "shl", type,
                            type,
                            type),
                            { a -> glslOperation("<<", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "shr", type,
                            type,
                            type),
                            { a -> glslOperation(">>", *a) }))
        }
        if (type == typeScalar) {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "greaterThan", typeBoolean, type, type),
                            { a -> glslOperation(">", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "greaterThanEqual", typeBoolean, type, type),
                            { a -> glslOperation(">=", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "lessThan", typeBoolean, type, type),
                            { a -> glslOperation("<", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "lessThanEqual", typeBoolean, type, type),
                            { a -> glslOperation("<=", *a) }))
        } else {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "greaterThan", typeBoolean, type, type),
                            { a -> glslFunction("greaterThan", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "greaterThanEqual", typeBoolean, type, type),
                            { a -> glslFunction("greaterThanEqual", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "lessThan", typeBoolean, type, type),
                            { a -> glslFunction("lessThan", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "lessThanEqual", typeBoolean, type, type),
                            { a -> glslFunction("lessThanEqual", *a) }))
        }
        logicFunctions(functionsSignatures, type, typeBoolean)
    }

    private fun logicFunctions(
            functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>,
            type: TypeExported,
            typeBoolean: TypeExported) {
        if (type == Types.Int.exported || type == Types.Boolean.exported) {
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "and", type, type, type),
                            { a -> glslOperation("&", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "or", type, type, type),
                            { a -> glslOperation("|", *a) }))
            functionsSignatures.add(
                    Pair(FunctionExportedSignature(
                            "xor", type, type, type),
                            { a -> glslOperation("^", *a) }))
        }
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "equals", typeBoolean, type, type),
                        { a -> glslOperation("==", *a) }))
    }

    private fun vectorFunctions(
            functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>,
            type: TypeExported,
            typeBoolean: TypeExported,
            typeScalar: TypeExported) {
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "plus", type,
                        type,
                        typeScalar),
                        { a -> glslOperation("+", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "plus", type,
                        typeScalar,
                        type),
                        { a -> glslOperation("+", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "minus", type,
                        type,
                        typeScalar),
                        { a -> glslOperation("-", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "minus", type,
                        typeScalar,
                        type),
                        { a -> glslOperation("-", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "times", type,
                        type,
                        typeScalar),
                        { a -> glslOperation("*", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "times", type,
                        typeScalar,
                        type),
                        { a -> glslOperation("*", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "div", type,
                        type,
                        typeScalar),
                        { a -> glslOperation("/", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "div", type,
                        typeScalar,
                        type),
                        { a -> glslOperation("/", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "rem", type,
                        type,
                        typeScalar),
                        { a -> glslFunction("mod", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "rem", type,
                        typeScalar,
                        type),
                        { a -> glslFunction("mod", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "min", type,
                        type,
                        typeScalar),
                        { a -> glslFunction("min", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "min", type,
                        typeScalar,
                        type),
                        { a -> glslFunction("min", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "max", type,
                        type,
                        typeScalar),
                        { a -> glslFunction("max", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "max", type,
                        typeScalar,
                        type),
                        { a -> glslFunction("max", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "clamp", type,
                        type,
                        type,
                        typeScalar),
                        { a -> glslFunction("clamp", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "clamp", type,
                        type,
                        typeScalar,
                        type),
                        { a -> glslFunction("clamp", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "clamp", type,
                        typeScalar,
                        type,
                        typeScalar),
                        { a -> glslFunction("clamp", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "clamp", type,
                        typeScalar,
                        typeScalar,
                        type),
                        { a -> glslFunction("clamp", *a) }))
    }

    private fun matrixFunctions(
            functionsSignatures: MutableList<Pair<FunctionExportedSignature, (Array<String>) -> String>>,
            type: TypeExported,
            typeVector: TypeExported) {
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "plus", type,
                        type,
                        type),
                        { a -> glslOperation("+", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "minus", type,
                        type,
                        type),
                        { a -> glslOperation("-", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "times", type,
                        type,
                        type),
                        { a -> glslOperation("*", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "div", type,
                        type,
                        type),
                        { a -> glslOperation("/", *a) }))
        functionsSignatures.add(
                Pair(FunctionExportedSignature(
                        "times", typeVector,
                        type,
                        typeVector),
                        { a -> glslOperation("*", *a) }))
    }

    private fun glslFunction(name: String,
                             vararg args: String) =
            "$name(${args.joinToString()})"

    private fun glslOperation(name: String,
                              vararg args: String) =
            args.asSequence().map { "($it)" }.joinToString(
                    separator = " $name ")
}
