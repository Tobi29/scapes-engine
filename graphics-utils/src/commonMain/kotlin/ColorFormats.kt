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

package org.tobi29.graphics

import org.tobi29.arrays.IntsRO2
import org.tobi29.arrays.Vars2
import kotlin.reflect.KClass

sealed class ColorFormat<in D : Vars2>

@Suppress("UNCHECKED_CAST")
inline val <F : ColorFormat<*>> KClass<F>.instance: F
    get() = when (this) {
        RGBA::class -> RGBA as F
        else -> throw IllegalArgumentException("Unknown format class: $this")
    }

sealed class ColorFormatInt : ColorFormat<IntsRO2>()

object RGBA : ColorFormatInt()
