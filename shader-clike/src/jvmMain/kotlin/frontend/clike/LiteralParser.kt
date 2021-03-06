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

package org.tobi29.scapes.engine.shader.frontend.clike

import org.tobi29.scapes.engine.shader.DecimalExpression
import org.tobi29.scapes.engine.shader.IntegerExpression
import org.tobi29.scapes.engine.shader.ScapesShaderParser
import org.tobi29.scapes.engine.shader.attach

internal fun ScapesShaderParser.LiteralContext.ast() = run {
    val integer = IntegerLiteral()
    if (integer != null) {
        return@run IntegerExpression(integer.text.toInt())
    }
    val floating = FloatingLiteral()
    if (floating != null) {
        return@run DecimalExpression(floating.text.toDouble())
    }
    throw IllegalStateException("Invalid parse tree node")
}.also { it.attach(this) }
