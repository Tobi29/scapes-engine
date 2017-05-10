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

package org.tobi29.scapes.engine.utils.shader.frontend.clike

import org.tobi29.scapes.engine.utils.BigDecimal
import org.tobi29.scapes.engine.utils.BigInteger
import org.tobi29.scapes.engine.utils.shader.DecimalExpression
import org.tobi29.scapes.engine.utils.shader.IntegerExpression
import org.tobi29.scapes.engine.utils.shader.ScapesShaderParser
import org.tobi29.scapes.engine.utils.shader.attach

internal fun ScapesShaderParser.LiteralContext.ast() = run {
    val integer = IntegerLiteral()
    if (integer != null) {
        return@run IntegerExpression(BigInteger(integer.text))
    }
    val floating = FloatingLiteral()
    if (floating != null) {
        return@run DecimalExpression(BigDecimal(floating.text))
    }
    throw IllegalStateException("Invalid parse tree node")
}.also { it.attach(this) }
