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

package org.tobi29.scapes.engine.graphics

class MatrixStack(matrices: Int) {
    private val stack = Array(matrices, { Matrix() })
    private var i = 0

    init {
        stack[0].identity()
    }

    fun push(): Matrix {
        val bottom = stack[i++]
        if (i >= stack.size) {
            throw IllegalStateException("Stack overflow.")
        }
        val top = stack[i]
        top.copy(bottom)
        return top
    }

    fun pop(): Matrix {
        i--
        if (i < 0) {
            throw IllegalStateException("Stack underflow.")
        }
        return stack[i]
    }

    fun current(): Matrix {
        return stack[i]
    }
}

inline fun <R> MatrixStack.push(block: (Matrix) -> R): R {
    val matrix = push()
    try {
        return block(matrix)
    } finally {
        pop()
    }
}
